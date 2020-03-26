package dajana.adapter;

import android.content.Intent;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import dajana.activity.DocumentActivity;
import dajana.model.DataHelper;
import dajana.utils.DownloadManager;
import dajana.utils.FileCategory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import dajana.activity.ImageViewActivity;
import dajana.data.database.realm.FileRealm;
import dajana.utils.FileManager;
import dajana.utils.UploadManager;
import io.realm.Case;
import io.realm.OrderedRealmCollection;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import wang.switchy.hin2n.Application;
import wang.switchy.hin2n.R;


public class FileRealmRecylerViewAdapter extends RealmRecyclerViewAdapter<FileRealm, FileRealmRecylerViewAdapter.FileViewHolder>  implements Filterable {
    public boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<>();
    private View itemView;
    private String username;
    private String server;
    private Realm realm;
    private OrderedRealmCollection<FileRealm> fileList;
    private FileListListener listener;

    @Override
    public Filter getFilter() {
        return null;
    }


    public interface FileListListener {
        void onFileClick(FileRealm attachment,int position);
        void onFileLongClick(FileRealm attachment, View caller);
        void onIconLongClick(FileRealm attachment, View caller);
        void onEditButtonClick(FileRealm attachment, Set<Integer> selected);
        void onDownloadCancel();
        void onDownloadError(String error);
        void onDeleteClick(FileRealm attachment, View caller);
        void onDownloadClick(FileRealm attachment, int position);
        void onUploadClick(FileRealm attachment, View caller);
    }
    public FileRealmRecylerViewAdapter(@Nullable OrderedRealmCollection data, FileListListener listener) {
        super(data, true);
        fileList = data;
        this.listener = listener;
        this.realm = Realm.getDefaultInstance();
        setHasStableIds(true);
    }

    public FileRealmRecylerViewAdapter(@Nullable OrderedRealmCollection data, boolean autoUpdate) {
        super(data, autoUpdate);
        fileList = data;
        setHasStableIds(true);
    }
    public FileRealmRecylerViewAdapter(@Nullable OrderedRealmCollection data) {
        super(data, true);
        fileList = data;
        setHasStableIds(true);
    }

    public FileRealmRecylerViewAdapter(@Nullable OrderedRealmCollection data,String username, String server) {
        super(data, true);
        fileList = data;
        this.username = username;
        this.server = server;
        this.realm = Realm.getDefaultInstance();
        setHasStableIds(true);
    }

    public void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }
    public void setListener(FileListListener listener) {
        this.listener = listener;
    }

    public Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_message, parent, false);
        return new FileViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {

        final FileViewHolder fileViewHolder = (FileRealmRecylerViewAdapter.FileViewHolder) holder;
        final FileRealm obj = getItem(position);

        holder.uid = obj.getUid();
        holder.data = obj;
        final int itemId = obj==null?0:obj.getUid();
        //noinspection ConstantConditions
        String path = obj.getOriginalUrl();
        holder.title.setText(obj.getUid()+"."+obj.getFilename());
        holder.deletedCheckBox.setChecked(countersToDelete.contains(itemId));
        holder.progressBar.setVisibility(View.INVISIBLE);
        holder.ivCancelDownload.setVisibility(View.GONE);
        if(obj.getSize()!=null) {
            holder.fileSize.setText(obj.getTimestamp()+" "+FileCategory.determineFileCategory(obj.getContentType()) + "  " + obj.getSize() / 1000 + "kb" +(username.equals(obj.getUploader())?"":" "+obj.getUploader()));
        }else {
            holder.fileSize.setText(obj.getTimestamp()+" "+FileCategory.determineFileCategory(obj.getContentType()) + "  "  + "?kb" +(username.equals(obj.getUploader())?"":" "+obj.getUploader()));
        }
        String description = (obj.getTags()==null || "".equals(obj.getTags())?"":obj.getTags()+": ")+(obj.getDescription()==null?"":obj.getDescription());
        if(description != null) {
            holder.fileDescription.setText(description);
        }else {
            holder.fileDescription.setText("");
        }

        String source = getSource(obj);
        String url = "http://"+server+"/cloud/includes/timthumb/timthumb.php?src=upload/files/"+obj.getUrl()+"&w=64&h=64&q=90";
        String contentType = obj.getContentType();
        if(contentType != null &&  contentType.startsWith("image")) {
            if (source != null) {
                Glide.with(fileViewHolder.icon.getContext())
                        .load(new File(source)).apply(new RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.ic_sync_upload))
                        .into(fileViewHolder.icon);
            } else {
                Glide.with(fileViewHolder.icon.getContext())
                        .load(url).apply(new RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.ic_download_black))
                        .into(fileViewHolder.icon);

            }
        }else if(contentType != null &&  contentType.startsWith("video") && source !=null && new File(source).exists()) {
            Bitmap bmThumbnail;
            bmThumbnail = ThumbnailUtils.createVideoThumbnail(source, MediaStore.Video.Thumbnails.MINI_KIND);
            holder.icon.setImageBitmap(bmThumbnail);
        }else {
            if(source != null || obj.getContentType() != null) {
                holder.icon.setImageResource(getFileIconByCategory(FileCategory.determineFileCategory(obj.getContentType())));
            }else if(obj.getUrl() == null){
                holder.icon.setImageResource(R.drawable.ic_sync_upload);
            }else {
                String mimeType = FileManager.getMimeType(obj.getUrl());
                if(mimeType != null)
                    holder.icon.setImageResource(getFileIconByCategory(FileCategory.determineFileCategory(mimeType)));
                else
                    holder.icon.setImageResource(R.drawable.ic_download_black);
            }

        }




        if (inDeletionMode) {
            holder.deletedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        countersToDelete.add(itemId);
                    } else {
                        countersToDelete.remove(itemId);
                    }
                }
            });

            if(obj.getUrl() == null) {
                holder.edit_upload.setVisibility(View.VISIBLE);
                holder.edit_delete.setVisibility(View.GONE);
                holder.edit_download.setVisibility(View.GONE);
            }else if(obj.getFilePath()!=null || obj.getOriginalUrl()!=null) {
                holder.edit_upload.setVisibility(View.GONE);
                holder.edit_delete.setVisibility(View.VISIBLE);
                holder.edit_download.setVisibility(View.GONE);
            }else if(obj.getOriginalUrl()==null){
                holder.edit_upload.setVisibility(View.GONE);
                holder.edit_delete.setVisibility(View.GONE);
                holder.edit_download.setVisibility(View.VISIBLE);
            }
        } else {
            holder.edit_upload.setVisibility(View.GONE);
            holder.edit_delete.setVisibility(View.GONE);
            holder.edit_download.setVisibility(View.GONE);
            holder.deletedCheckBox.setOnCheckedChangeListener(null);
        }
        holder.deletedCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
        holder.edit_imageButton.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
        //if(holder.data.getContentType() != null && holder.data.getContentType().startsWith("image/")) {
        holder.icon.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View view) {
                listener.onIconLongClick(holder.data,view);
               return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.ivFileIcon:
                        iconOnClick(holder.data,position);
                        break;
                    case R.id.edit_delete:
                        onDeleteClick(holder.data,v);
                        break;
                    case R.id.edit_download:
                        onDownloadClick(holder.data,position);
                        break;
                    case R.id.edit_upload:
                        onUploadClick(holder.data,v);
                        break;
                    case R.id.edit_imageButton:
                        listener.onEditButtonClick(holder.data,countersToDelete);
                        break;
                    case R.id.ivCancelDownload:
                        listener.onDownloadCancel();
                        break;
                    default:
                        listener.onFileClick(holder.data,position);
                }

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (fileList.size() > position)
                    listener.onFileLongClick(fileList.get(position), v);
                return true;
            }
        });

        holder.itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                if(holder.data.getUrl() != null) {
                    holder.subscribeForDownloadProgress();
                } else {
                    holder.subscribeForUploadProgress();
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                holder.unsubscribeAll();
            }
        });


        holder.icon.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           iconOnClick(holder.data,position);
                       }
                   }
        );

        holder.ivCancelDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDownloadCancel();
            }
        });


        holder.edit_imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                listener.onEditButtonClick(holder.data,countersToDelete);
            }
        });

        holder.edit_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeleteClick(holder.data,view);
            }
        });

        holder.edit_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDownloadClick(holder.data,position);
            }
        });

        holder.edit_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUploadClick(holder.data,view);
            }
        });

    }

    private void iconOnClick(FileRealm item, int position) {
        //Log.i("FileAdapter","Icon click:"+item.getFilename());
        if(item.getContentType() != null && item.getContentType().startsWith("image")) {
            Intent intent = ImageViewActivity.createIntent(Application.getInstance().getApplicationContext(),username,server,item.getUid());
            Application.getInstance().getApplicationContext().startActivity(intent);
        }else {
            listener.onFileClick(item,position);
        }

    }

    private void onDeleteClick(FileRealm data, View v) {
        listener.onDeleteClick(data,v);
    }
    private void onDownloadClick(FileRealm data, int position) {
        listener.onDownloadClick(data,position);
    }
    private void onUploadClick(FileRealm data, View v) {
        listener.onUploadClick(data,v);
    }
    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getUid();
    }

    public void filterResults(String text) {
        text = text == null ? null : text.toLowerCase().trim();
        RealmQuery<FileRealm> query = realm.where(FileRealm.class);
        if(!(text == null || "".equals(text))) {
            query.contains(FileRealm.Fields.FILENAME, text, Case.INSENSITIVE)
            .or().contains(FileRealm.Fields.DESCRIPTION, text, Case.INSENSITIVE)
            .or().contains(FileRealm.Fields.TIMESTAMP, text, Case.INSENSITIVE)
            .or().contains(FileRealm.Fields.CONTENTTYPE, text, Case.INSENSITIVE);
        }else {
            updateData(query.findAllSortedAsync(FileRealm.Fields.UID, Sort.DESCENDING));
            return;
        }
        updateData(query.findAllSortedAsync(FileRealm.Fields.TIMESTAMP, Sort.DESCENDING));
    }

    public void filterFileResults() {
        RealmQuery<FileRealm> query = realm.where(FileRealm.class);
            query.not().contains(FileRealm.Fields.CONTENTTYPE, "image/", Case.INSENSITIVE)
            .not().contains(FileRealm.Fields.CONTENTTYPE, "audio/", Case.INSENSITIVE)
            .not().contains(FileRealm.Fields.CONTENTTYPE, "video/", Case.INSENSITIVE);
        updateData(query.findAllSortedAsync(FileRealm.Fields.TIMESTAMP, Sort.DESCENDING));
    }

    public OrderedRealmCollection<FileRealm> getFileList() {
        return fileList;
    }

    public void setFileList(OrderedRealmCollection<FileRealm> fileList) {
        this.fileList = fileList;
    }

    private String getSource(FileRealm fileRealm) {
        String filePath = fileRealm.getFilePath();
        String originalUrl = fileRealm.getOriginalUrl();
        if(filePath != null && new File(filePath).exists()) {
            return filePath;
        }else if(originalUrl != null && new File(originalUrl).exists()) {
            return originalUrl;
        }else
            return null;
    }

    private int getFileIconByCategory(FileCategory category) {
        switch (category) {
            case image:
                return R.drawable.ic_image;
            case audio:
                return R.drawable.ic_audio;
            case video:
                return R.drawable.ic_video;
            case document:
                return R.drawable.ic_document;
            case pdf:
                return R.drawable.ic_pdf;
            case table:
                return R.drawable.ic_table;
            case presentation:
                return R.drawable.ic_presentation;
            case archive:
                return R.drawable.ic_archive;
            default:
                return R.drawable.ic_file;
        }
    }

    class FileViewHolder  extends RecyclerView.ViewHolder {
        private CompositeSubscription subscriptions = new CompositeSubscription();
        int uid;

        public FileRealm data;
        ImageView icon;
        TextView title;
        TextView fileSize;
        TextView fileDescription;
        CheckBox deletedCheckBox;
        ProgressBar progressBar;
        final ImageButton ivCancelDownload;
        ImageView edit_imageButton;
        ImageView edit_delete;
        ImageView edit_upload;
        ImageView edit_download;

        public FileViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivFileIcon);
            title = itemView.findViewById(R.id.tvFileName);
            fileSize = itemView.findViewById(R.id.tvFileSize);
            fileDescription = itemView.findViewById(R.id.file_description);
            progressBar = itemView.findViewById(R.id.progressBar);
            deletedCheckBox = itemView.findViewById(R.id.file_item_checkbox);
            ivCancelDownload = itemView.findViewById(R.id.ivCancelDownload);
            edit_imageButton = itemView.findViewById(R.id.edit_imageButton);
            edit_delete = itemView.findViewById(R.id.edit_delete);
            edit_upload = itemView.findViewById(R.id.edit_upload);
            edit_download = itemView.findViewById(R.id.edit_download);
        }

        public void unsubscribeAll() {
            subscriptions.clear();
        }

        public void subscribeForDownloadProgress() {
            subscriptions.add(DownloadManager.getInstance().subscribeForProgress()
                    .doOnNext(new Action1<DownloadManager.ProgressData>() {
                        @Override
                        public void call(DownloadManager.ProgressData progressData) {
                            setUpDownloadProgress(progressData);
                        }
                    }).subscribe());
        }

        public void subscribeForUploadProgress() {
            subscriptions.add(UploadManager.getInstance().subscribeForProgress()
                    .doOnNext(new Action1<UploadManager.ProgressData>() {
                        @Override
                        public void call(UploadManager.ProgressData progressData) {
                            setUpUploadProgress(progressData);
                        }
                    }).subscribe());
        }


        private void setUpUploadProgress(UploadManager.ProgressData progressData) {
            if (progressData != null && progressData.getUid() == uid) {
                if (progressData.isCompleted()) {
                    showProgress(false);
                } else if (progressData.getError() != null) {
                    showProgress(false);
                    listener.onDownloadError(progressData.getError());
                } else {
                    progressBar.setProgress(progressData.getProgress());
                    showProgress(true);
                }
            } else showProgress(false);
        }

        private void setUpDownloadProgress(DownloadManager.ProgressData progressData) {
            if (progressData != null && progressData.getUid()== uid) {
                if (progressData.isCompleted()) {
                    showProgress(false);
                } else if (progressData.getError() != null) {
                    showProgress(false);
                    listener.onDownloadError(progressData.getError());
                } else {
                    progressBar.setProgress(progressData.getProgress());
                    if(progressData.getProgress() == 100)
                        showProgress(false);
                    else
                        showProgress(true);
                }
            } else showProgress(false);
        }

        private void showProgress(boolean show) {
            if (show) {
                progressBar.setVisibility(View.VISIBLE);
                ivCancelDownload.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                ivCancelDownload.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
            }
        }
    }

    private class FileFilter extends Filter {
        final FileRealmRecylerViewAdapter adapter;

        public FileFilter(FileRealmRecylerViewAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            adapter.filterResults(charSequence.toString());
        }
    }
}
