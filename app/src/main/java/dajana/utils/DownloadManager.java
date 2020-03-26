package dajana.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wave.fileuploadservice.FileDownloadService;
import com.wave.fileuploadservice.FileUploadService;

import java.io.File;

import dajana.data.database.realm.FileRealm;
import dajana.model.DataHelper;
import dajana.service.DownloadService;
import io.realm.Realm;
import rx.subjects.PublishSubject;

public class DownloadManager {

    private static final String LOG_TAG = "DownloadManager";
    private static DownloadManager instance;

    private PublishSubject<ProgressData> progressSubscribe = PublishSubject.create();
    private boolean isDownloading;
    private int uid;

    public static DownloadManager getInstance() {
        if (instance == null) instance = new DownloadManager();
        return instance;
    }

    public void reset() {
        isDownloading = false;
    }

    public PublishSubject<ProgressData> subscribeForProgress() {
        return progressSubscribe;
    }

    public void downloadFile(FileRealm attachment, String username, String server,int myId, Context context) {

        if (isDownloading) {
            progressSubscribe.onNext(new ProgressData(0, "Downloading already started", false, uid));
            return;
        }

        isDownloading = true;

        // check space
        if (attachment.getSize() != null&& attachment.getSize() >= getAvailableSpace()) {
            Log.d(LOG_TAG, "Not enough space for downloading");
            progressSubscribe.onNext(new ProgressData(0, "Not enough space for downloading", false, uid));
            isDownloading = false;
            return;
        }

        //check file exists

        String filePath = DownloadService.getDownloadDirPath()+ File.separator+attachment.getOriginalUrl();
        if(new File(filePath).exists()) {
            Realm realm = Realm.getDefaultInstance();
            DataHelper.updateItemAsync(realm,context,attachment);
            //isDownloading = false;
            //return;
        }
        uid = attachment.getUid();
        //Intent intent = new Intent(context, DownloadService.class);
        Intent intent = new Intent();
        intent.putExtra(FileDownloadService.KEY_RECEIVER, new DownloadReceiver(new Handler()));
        intent.putExtra(FileDownloadService.KEY_ATTACHMENT_ID, attachment.getUid());
        intent.putExtra(FileDownloadService.KEY_USERNAME, username);
        intent.putExtra(FileDownloadService.KEY_SERVER, server);
        intent.putExtra(FileDownloadService.KEY_MYCLOUD_ID, myId);
        intent.putExtra(FileDownloadService.KEY_FILE_NAME, attachment.getOriginalUrl());
        intent.putExtra(FileDownloadService.KEY_URL, attachment.getUrl());
        intent.putExtra(FileDownloadService.KEY_FILE_PATH, attachment.getFilePath());
        intent.putExtra(FileDownloadService.KEY_URI, attachment.getUrl());
        intent.putExtra(FileDownloadService.KEY_FILE_SIZE, attachment.getSize());
        FileDownloadService.enqueueWork(context,intent);
        //context.startService(intent);
        return;
    }

    public void cancelDownload(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        context.stopService(intent);
    }

    private long getAvailableSpace() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
    }

    private class DownloadReceiver extends ResultReceiver {

        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            switch (resultCode) {
                case DownloadService.UPDATE_PROGRESS_CODE:
                    int currentProgress = resultData.getInt(DownloadService.KEY_PROGRESS);
                    progressSubscribe.onNext(new ProgressData(currentProgress, null, false, uid));
                    break;
                case DownloadService.ERROR_CODE:
                    String error = resultData.getString(DownloadService.KEY_ERROR);
                    progressSubscribe.onNext(new ProgressData(0, error, false, uid));
                    isDownloading = false;
                    break;
                case DownloadService.COMPLETE_CODE:
                    progressSubscribe.onNext(new ProgressData(100, null, true, uid));
                    isDownloading = false;
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

}
