package dajana.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import dajana.adapter.FileRealmRecylerViewAdapter;
import dajana.adapter.UserShareAdapter;
import dajana.data.database.realm.FileRealm;
import dajana.fragment.UserShareFragment;
import dajana.model.DataHelper;
import dajana.service.FileManageService;
import dajana.utils.UploadManager;
import io.realm.Realm;
import wang.switchy.hin2n.R;

public class FileEditActivity extends AppCompatActivity {

    private Realm realm;
    private EditText edit_filename;
    private EditText edit_description;
    private EditText edit_tags;
    private TextView copy_count;
    private ImageView image_copy;
    private UserShareFragment userShareFragment;
    private RecyclerView recyclerView;
    private UserShareAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String username;
    private FileRealm fileEdit;
    private ArrayList<Integer> countersToCopy = new ArrayList<>();
    private int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "CLose action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                finish();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValus()) {
                    saveFileReam();
                    Snackbar.make(view, "Saved", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });
        uid = getIntent().getIntExtra("UID", -1);
        ArrayList<Integer> uids = getIntent().getIntegerArrayListExtra("SELECTED");
        realm = Realm.getDefaultInstance();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if(savedInstanceState == null) {
            userShareFragment = (UserShareFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_user_share);
        }
        recyclerView = findViewById(R.id.user_share_recycler_view);
        if(userShareFragment!=null) {
            userShareFragment.recyclerView = recyclerView;
        }
        this.username = getIntent().getStringExtra(FileManageService.KEY_USERNAME);
        countersToCopy = getIntent().getIntegerArrayListExtra("SELECTED");
        edit_filename = findViewById(R.id.edit_filename);
        edit_description = findViewById(R.id.edit_description);
        image_copy = findViewById(R.id.image_copy);
        edit_tags = findViewById(R.id.edit_tags);
        copy_count = findViewById(R.id.text_selected);
        if (uids != null && ((ArrayList) uids).size() > 0) {
            image_copy.setVisibility(View.VISIBLE);
            copy_count.setVisibility(View.VISIBLE);
            copy_count.setText("" + uids.size());
        } else {
            image_copy.setVisibility(View.GONE);
            copy_count.setVisibility(View.GONE);
        }

        Intent intent = new Intent();
        intent.putExtra(FileManageService.KEY_ACTION,"user-table");
        intent.putExtra(FileManageService.KEY_USERNAME,username);
        FileManageService.enqueueWork(getApplicationContext(),intent);
        fileEdit = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID, uid).findFirst();
        if (fileEdit != null && fileEdit.getId() != null) {
            edit_filename.setText(fileEdit.getFilename());
            edit_description.setText(fileEdit.getDescription()) ;
            edit_tags.setText(fileEdit.getTags());
            intent.putExtra(FileManageService.KEY_ACTION,"user-file");
            intent.putExtra(FileManageService.KEY_USERNAME,username);
            intent.putExtra(FileManageService.KEY_FILE_ID,fileEdit.getId());
            FileManageService.enqueueWork(getApplicationContext(),intent);
        }
        if(fileEdit.getId() != null)
         setUpRecyclerView();
    }


    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        TouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return 0;
            //return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }
    }

    private boolean checkValus() {

        if(TextUtils.isEmpty(edit_filename.getText())) {
            edit_filename.setError(edit_filename.getHint()+ " "+getString(R.string.is_required));
            return false;
        }

        return true;
    }

    private void saveFileReam() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileRealm fileRealm = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID,uid).findFirst();
                if(fileRealm!=null) {
                    String name = edit_filename.getText().toString();
                    fileRealm.setFilename(name);
                    String description = edit_description.getText() == null?"":edit_description.getText().toString();
                    fileRealm.setDescription(description);
                    String tags = edit_tags.getText() == null? "":edit_tags.getText().toString();
                    if(countersToCopy.size()>0) {
                        for(int uid:countersToCopy) {
                            FileRealm fileCopy = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID,uid).findFirst();
                            if(fileCopy!=null) {
                                fileCopy.setTags(tags);
                            }
                        }

                    }else {
                        fileRealm.setTags(tags);
                    }
                    if(fileRealm.getId() != null) {
                        Intent intent = new Intent();
                        intent.putExtra(FileManageService.KEY_NAME,name);
                        intent.putExtra(FileManageService.KEY_DESCRIPTION,description);
                        intent.putExtra(FileManageService.KEY_FILE_ID,fileRealm.getId());
                        intent.putExtra(FileManageService.KEY_FILE_NAME,fileRealm.getUrl());
                        intent.putExtra(FileManageService.KEY_ACTION,"update");
                        intent.putExtra(FileManageService.KEY_USERNAME,username);
                        FileManageService.enqueueWork(getApplicationContext(),intent);
                    }

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        realm.close();
    }

    private void setUpRecyclerView() {
        Log.d("FileEditActivity","SetUpRecylerView");
        adapter = new UserShareAdapter(userShareFragment, username, fileEdit.getId());
            //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.loadSharedUsers();
            }
        },1000);

        FileEditActivity.TouchHelperCallback touchHelperCallback = new FileEditActivity.TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerView);
        Log.d("FileEditActivity","SetAdapter");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //IntentFilter intentFilter = new IntentFilter("manage.own.broadcast");
        //LocalBroadcastManager.getInstance(this).registerReceiver(myLocalBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //IntentFilter intentFilter = new IntentFilter("manage.own.broadcast");
        //LocalBroadcastManager.getInstance(this).registerReceiver(myLocalBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver myLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String ok = intent.getStringExtra("ok");
            String error = intent.getStringExtra("error");
            String url = intent.getStringExtra("url");
            long uid = intent.getLongExtra("uid",-1l);
            Log.i("FileEditctivity","Ok="+ok+",uid="+uid+",url="+url+",error="+error);
            if(error != null) {
                Toast.makeText(getApplicationContext(),error, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), ok, Toast.LENGTH_SHORT).show();
                /*if(uid > 0) {
                    DataHelper.deleteItemAsync(realm, uid);
                }*/
            }


        }
    };


}
