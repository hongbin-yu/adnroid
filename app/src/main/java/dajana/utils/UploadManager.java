package dajana.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wave.fileuploadservice.FileUploadService;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import dajana.data.database.realm.FileRealm;
import dajana.service.DownloadService;
import dajana.service.FileManageService;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.subjects.PublishSubject;
import wang.switchy.hin2n.Application;

public class UploadManager {
    private static final String LOG_TAG = "UploadManager";
    private static UploadManager instance;
    public static Queue<FileRealm> pathQueue = new LinkedList<FileRealm>();
    private PublishSubject<UploadManager.ProgressData> progressSubscribe = PublishSubject.create();
    private boolean isUploading = false;
    private int uid;
    private String username;
    private Context context;
    private FileRealm lastRealm = null;

    public static UploadManager getInstance() {
        if (instance == null) instance = new UploadManager();
        return instance;
    }

    public void reset() {
        isUploading = false;
        pathQueue.clear();
    }

    public PublishSubject<UploadManager.ProgressData> subscribeForProgress() {
        return progressSubscribe;
    }

    public void sync(Context context, String username,int maxId,boolean isWifi) {
        if (isUploading) {
            progressSubscribe.onNext(new UploadManager.ProgressData(0, "Sync already started", false, uid));
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(FileManageService.KEY_FILE_UID,new Long(uid));
        intent.putExtra(FileManageService.KEY_FILE_ID,maxId);
        intent.putExtra(FileManageService.KEY_USERNAME,username);
        intent.putExtra(FileManageService.KEY_ACTION,"sync");
        FileManageService.enqueueWork(context,intent);

        Realm realm = Realm.getDefaultInstance();
        RealmResults results = realm.where(FileRealm.class).isNull(FileRealm.Fields.URL).findAll();
        this.context = context;
        this.username = username;
        Log.d("Sync","size="+results.size());
        pathQueue.clear();
        pathQueue.addAll(realm.copyFromRealm(results));

        onNext();

    }

    public void uploadFile(FileRealm attachment, int myId, Context context) {

        if (isUploading) {
            progressSubscribe.onNext(new UploadManager.ProgressData(0, "Downloading already started", false, uid));
            return;
        }

        isUploading = true;
        this.context = context;
        // check space
        /*
        if (attachment.getSize() >= getAvailableSpace()) {
            Log.d(LOG_TAG, "Not enough space for downloading");
            progressSubscribe.onNext(new UploadManager.ProgressData(0, "Not enough space for downloading", false, attachmentId));
            isUploading = false;
            return;
        }
        */
        uid = attachment.getUid();
        //Intent intent = new Intent(context, DownloadService.class);
        Intent intent = new Intent();
        intent.putExtra(FileUploadService.KEY_RECEIVER, new UploadManager.UploadReceiver(new Handler()));
        intent.putExtra(FileUploadService.KEY_ATTACHMENT_ID, attachment.getUid());
        intent.putExtra(FileUploadService.KEY_MYCLOUD_ID, myId);
        intent.putExtra(FileUploadService.KEY_USERNAME,username);
        intent.putExtra(FileUploadService.KEY_FILE_PATH, attachment.getOriginalUrl());
        intent.putExtra(FileUploadService.KEY_FILE_NAME, attachment.getFilename());
        intent.putExtra(FileUploadService.KEY_URL, attachment.getUrl());
        intent.putExtra(FileUploadService.KEY_URI, attachment.getUrl());
        intent.putExtra(FileUploadService.KEY_FILE_SIZE, attachment.getSize());
        FileUploadService.enqueueWork(context,intent);
        //context.startService(intent);
        return;
    }

    public void cancelUpload(Context context) {
        pathQueue.clear();
        Intent intent = new Intent(context, FileUploadService.class);
        context.stopService(intent);
        String error = "stop";
        progressSubscribe.onNext(new UploadManager.ProgressData(0, error, false, uid));
        isUploading = false;
    }

    private long getAvailableSpace() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
    }
    public void start(String username, Context context) {
        this.username = username;
        this.context = context;
        Realm realm = Realm.getDefaultInstance();
        RealmResults results = realm.where(FileRealm.class).isNull(FileRealm.Fields.URL).findAll();
        Log.d("upload start","size="+results.size());
        pathQueue.clear();
        pathQueue.addAll(realm.copyFromRealm(results));
        onNext();
    }

    public void onNext() {
        if(pathQueue.isEmpty()) return;
        FileRealm fileRealm = pathQueue.poll();
        if(fileRealm.getUrl() == null && (lastRealm == null || fileRealm.getUid() != lastRealm.getUid())) {
            uid = fileRealm.getUid();
            Intent intent = new Intent();
            intent.putExtra(FileUploadService.KEY_USERNAME,username);
            intent.putExtra(FileUploadService.KEY_RECEIVER, new UploadManager.UploadReceiver(new Handler()));
            intent.putExtra(FileUploadService.KEY_FILE_PATH,fileRealm.getOriginalUrl());
            intent.putExtra(FileUploadService.KEY_ATTACHMENT_ID, fileRealm.getUid());
            if(context == null) {
                context = Application.getInstance().getApplicationContext();
            }
            lastRealm = fileRealm;
            FileUploadService.enqueueWork(context,intent);
        }


    }
    private class UploadReceiver extends ResultReceiver {

        public UploadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            switch (resultCode) {
                case DownloadService.UPDATE_PROGRESS_CODE:
                    int currentProgress = resultData.getInt(DownloadService.KEY_PROGRESS);
                    progressSubscribe.onNext(new UploadManager.ProgressData(currentProgress, null, false, uid));
                    break;
                case DownloadService.ERROR_CODE:
                    String error = resultData.getString(DownloadService.KEY_ERROR);
                    progressSubscribe.onNext(new UploadManager.ProgressData(0, error, false, uid));
                    isUploading = false;
                    if(!pathQueue.isEmpty()) {
                        onNext();
                    }
                    break;
                case DownloadService.COMPLETE_CODE:
                    progressSubscribe.onNext(new UploadManager.ProgressData(100, null, true, uid));
                    isUploading = false;
                    if(!pathQueue.isEmpty()) {
                       onNext();
                    }
                    break;
            }
        }
    }

    public class ProgressData {
        final int progress;
        final String error;
        final boolean completed;
        final int uid;


        public ProgressData(int progress, String error, boolean completed, int uid) {
            this.progress = progress;
            this.error = error;
            this.completed = completed;
            this.uid = uid;
        }

        public int getProgress() {
            return progress;
        }

        @Nullable
        public String getError() {
            return error;
        }

        public boolean isCompleted() {
            return completed;
        }

        public int getUid() {
            return uid;
        }


    }

    public class UploadData {
        final int uid;
        final String username;
        final String filePath;

        public UploadData(int attachmentId, String username, String filePath) {
            this.uid = attachmentId;
            this.username = username;
            this.filePath = filePath;
        }

        public int getUid() {
            return uid;
        }

        public String getUsername() {
            return username;
        }

        public String getFilePath() {
            return filePath;
        }
    }


 }
