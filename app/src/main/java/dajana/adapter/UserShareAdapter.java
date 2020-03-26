package dajana.adapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dajana.data.database.realm.FileRealm;
import dajana.data.database.realm.UserRealm;
import dajana.model.DataHelper;
import dajana.model.User;
import dajana.service.FileManageService;
import dajana.utils.PermissionsRequester;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import wang.switchy.hin2n.Application;
import wang.switchy.hin2n.R;

public class UserShareAdapter extends RecyclerView.Adapter<UserShareAdapter.UserShareViewHolder>  {

    @NonNull
    private List<UserRealm> userPaths = new ArrayList<UserRealm>();
    @NonNull
    private List<UserRealm> selectedUserPaths = new ArrayList();
    @NonNull
    private final Listener listener;
    @NonNull
    private String username;
    @NonNull
    private int file_id;
    private View itemView;
    private String uploader;
    //private Realm realm;

    public UserShareAdapter(@NonNull Listener listener, String username, int file_id) {
        this.userPaths = null;
        this.listener = listener;
        this.username = username;
        this.file_id = file_id;
        //this.selectedUserPaths = selectedUserPaths;//realm.copyFromRealm(fileRealm.getUserList());
    }


    public interface Listener {
        void fileManage(String action,String username,int file_id,int client_id);
        void onUsersSelected();
    }


    @NonNull
    @Override
    public UserShareAdapter.UserShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_image, parent, false);
        return new UserShareAdapter.UserShareViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserShareAdapter.UserShareViewHolder holder, int position) {
        final UserRealm user = userPaths.get(position);
        /*
        Glide.with(recentImageViewHolder.image.getContext())
                .load(new File(path)).apply(new RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.ic_image))
                .into(recentImageViewHolder.image);
        */

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("UserShareAdapter","image clicked");
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });
        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getName());
        holder.checkBox.setOnCheckedChangeListener(null);

        holder.checkBox.setChecked(selectedUserPaths.contains(user));

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        FileRealm fileDb = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ID,file_id).findFirst();
                        UserRealm userDb = realm.where(UserRealm.class).equalTo(UserRealm.Fields.ID,user.getId()).findFirst();
                        if (isChecked) {
                            if(userDb != null) {
                                selectedUserPaths.add(realm.copyFromRealm(userDb));
                                fileDb.getUserList().add(userDb);
                                listener.fileManage("assign",username,file_id,userDb.getId());
                                Log.d("CheckBox changed","Add "+userDb.getUsername());
                            }
                        } else {

                            listener.fileManage("unassign",username,file_id,userDb.getId());
                            fileDb.getUserList().remove(userDb);
                            selectedUserPaths.remove(userDb);
                        }
                    }
                });


                listener.onUsersSelected();
            }
        });
        if(user.getUsername().equals(uploader)) {
            holder.checkBox.setVisibility(View.GONE);
            holder.image.setImageResource(R.drawable.ic_muc_owner);
        }
    }

    @Override
    public int getItemCount() {
        if(userPaths == null) {
            return 0;
        }
        return userPaths.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    public List<UserRealm> getSelectedUserPaths() {
        return selectedUserPaths;
    }

    static class UserShareViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        CheckBox checkBox;
        TextView username;
        TextView fullname;

        UserShareViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.user_item_image);
            checkBox = itemView.findViewById(R.id.user_item_checkbox);
            username = itemView.findViewById(R.id.text_username);
            fullname = itemView.findViewById(R.id.text_fullname);
        }
    }

    public void loadSharedUsers() {
        Application.getInstance().runInBackgroundUserRequest(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getDefaultInstance();
                RealmResults   users =  realm.where(UserRealm.class).findAllSorted(UserRealm.Fields.ID);
                List<UserRealm> userList = realm.copyFromRealm(users);
                FileRealm fileRealm = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ID,file_id).findFirst();
                if(fileRealm != null && fileRealm.getUserList()!=null) {
                    uploader = fileRealm.getUploader();
                    selectedUserPaths = realm.copyFromRealm(fileRealm.getUserList());

                    Log.d("SelectedUser","size:"+selectedUserPaths.size()+",uploader="+uploader);
                }
                /*
                try {
                    users = realm.where(UserRealm.class).findAllSorted(UserRealm.Fields.ID);

                } catch (Exception e) {
                    Log.e("RecentImages", e.getMessage());
                } finally {

                }*/

                Application.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        update(userList);
                    }
                });
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    void update(List<UserRealm> imagePaths) {
        this.userPaths = imagePaths;
        //this.selectedUserPaths.clear();

        notifyDataSetChanged();
    }

    public void clearSelection() {
        //this.selectedUserPaths.clear();
        notifyDataSetChanged();
    }
}
