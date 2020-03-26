package com.wave.fileuploadservice;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.wave.fileuploadservice.service.CountingRequestBody;
import com.wave.fileuploadservice.service.RestApiService;
import com.wave.fileuploadservice.service.RetrofitInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import dajana.data.database.realm.FileRealm;
import dajana.model.Result;
import dajana.utils.FileManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import wang.switchy.hin2n.R;

public class FileDownloadService extends JobIntentService {
    private static final String DAJANA_DIR = "Dajana";
    public static final int UPDATE_PROGRESS_CODE = 3132;
    public static final int ERROR_CODE = 3133;
    public static final int COMPLETE_CODE = 3134;
    public final static String KEY_ERROR = "error";
    public final static String KEY_PROGRESS = "progress";
    public final static String KEY_ATTACHMENT_ID = "attachment_id";
    public final static String KEY_RECEIVER = "receiver";
    public final static String KEY_MYCLOUD_ID = "mycloud_id";
    public final static String KEY_USERNAME = "username";
    public final static String KEY_SERVER = "server";
    public final static String KEY_FILE_NAME = "file_name";
    public final static String KEY_FILE_PATH = "file_path";
    public final static String KEY_FILE_SIZE = "file_size";
    public final static String KEY_URL = "url";
    public final static String KEY_URI = "uri";
    private static final String TAG = "FileDownloadService";
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private static FileDownloadService instance;
    String syntax;
    String username, password;
    private String fileName;
    private String filePath;
    private int attachmentId;
    private ResultReceiver receiver;
    private boolean needStop = false;

    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 103;

    public static void enqueueWork(Context context, Intent intent) {
        FileUploadService.context = context;
        enqueueWork(context, FileDownloadService.class, JOB_ID, intent);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i(TAG,"Service started");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        /**
         * Download/Upload of file
         * The system or framework is already holding a wake lock for us at this point
         */

        // get file file here
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        syntax = intent.getStringExtra("syntax");
        this.receiver = intent.getParcelableExtra(KEY_RECEIVER);
        this.attachmentId = intent.getIntExtra(KEY_ATTACHMENT_ID,-1);
        this.filePath = intent.getStringExtra(KEY_FILE_PATH);
        long fileSize = intent.getLongExtra(KEY_FILE_SIZE, 0);
        String uri = intent.getStringExtra(KEY_URI);
        String url = intent.getStringExtra(KEY_URL);
        if (url == null) {
            sendBroadcastMessage(syntax,"onHandleWork: Invalid file URI");
            Log.e(TAG, "onHandleWork: Invalid file URI");
            return;
        }
        /*
        if (username == null) {
            sendBroadcastMessage(syntax,"onHandleWork: "+getString(R.string.Invalid_username));
            Log.e(TAG, "onHandleWork: Invalid username");
            return;
        }*/

        ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //if(mWifi.isConnected()) {
        onDownload(filePath, fileSize, url);
        /*
        } else {
            //sendBroadcastMessage(syntax,getString(R.string.File_uploading));
            RestApiService apiService = RetrofitInstance.getApiService();

            apiService.onFileDownload(uri)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Result>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(Result result) {
                            success(result);
                        }

                        @Override
                        public void onError(Throwable e) {
                            errors(e);
                        }



                    });
        }*/

    }

    private void onDownload(String filePath, final long fileSize, String url) {
        Log.d(TAG,"Filename="+filePath+",url="+url+",size="+fileSize);
        url = "http://10.7.0.1/cloud/upload/files/"+url;
        Request request = new Request.Builder().url(url).build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.d(TAG, "download onFailure " + response.toString());
                publishError(response.toString());
                return;
            }

            // create dir
            File directory = new File(getDownloadDirPath());
            if (!directory.exists())
                if (!directory.mkdir()) {
                    publishError("Directory not created");
                    return;
                }

            // create file
            Calendar cal = Calendar.getInstance();
            long timeInMillis = cal.getTimeInMillis();
            String  fileName = String.valueOf(timeInMillis) + ".jpeg";
            filePath = filePath==null?directory.getPath() + File.separator + fileName:filePath;

            File file = new File(filePath);

            if (file.exists()) {
                file = new File(directory.getPath() + File.separator +
                        FileManager.generateUniqueNameForFile(directory.getPath()
                                + File.separator, fileName));
            }
            Log.d(TAG, "download file " + file.getAbsolutePath());
            if (file.createNewFile()) {

                // download
                FileOutputStream fos = new FileOutputStream(file);
                byte [] buffer = new byte [8192];
                int r;

                int downloadedBytes = 0;
                while ((r = response.body().byteStream().read(buffer)) > 0) {
                    if (!needStop) {
                        fos.write(buffer, 0, r);
                        downloadedBytes += r;
                        publishProgress(downloadedBytes, fileSize);
                    } else {
                        fos.close();
                        file.delete();
                        publishError("Download aborted");
                        return;
                    }
                }
                fos.flush();
                fos.close();

                // save path to realm
                saveAttachmentPathToRealm(file.getPath());
            } else publishError("File not created");

        } catch (IOException e) {
            Log.d(TAG, "download onFailure " + e.getMessage());
            publishError(e.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        if(!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
        needStop = true;
    }

    private void errors(Throwable throwable) {
        publishError(throwable.getMessage());
        sendBroadcastMessage(syntax,"Error in file upload " + throwable.getMessage());
        Log.e(TAG, "onErrors: ", throwable);
    }

    public FileDownloadService() {
        super();
    }

    /*
        private void onProgress(Double progress) {
            int percentage = (int) Math.round((double) progress * 100.d);
            Bundle resultData = new Bundle();
            resultData.putInt(KEY_PROGRESS, percentage);
            receiver.send(UPDATE_PROGRESS_CODE, resultData);
        }
        */
    private void success(Result result) {
        Log.i(TAG, "download start");
        // create dir
        File directory = new File(getDownloadDirPath());
        if (!directory.exists())
            if (!directory.mkdir()) {
                publishError("Directory not created");
                return;
            }
        if (result.getData() == null) {
            publishError("Data is null");
                return;
        }

        // create file
        File file = newFile(fileName);
        String filePath = file.getAbsolutePath();



        try {
                // download
                FileOutputStream fos = new FileOutputStream(file);
                byte [] buffer =  Base64.decode(result.getData(),Base64.DEFAULT);
                fos.write(buffer);
                /* byte [] buffer = new byte [8192];
                int r;

                int downloadedBytes = 0;
                while ((r = responseBody.byteStream().read(buffer)) > 0) {
                    if (!needStop) {
                        fos.write(buffer, 0, r);
                        downloadedBytes += r;
                        publishProgress(downloadedBytes, fileSize);
                    } else {
                        fos.close();
                        file.delete();
                        Log.d(TAG, "download aborted: "+filePath);
                        publishError("Download aborted");
                        return;
                    }
                }*/
                fos.flush();
                fos.close();
                Log.d(TAG, "download to: " + filePath);
                // save path to realm
                saveAttachmentPathToRealm(filePath);
        } catch (IOException e) {
            Log.d(TAG, "download onFailure " + e.getMessage());
            publishError(e.getMessage());
        }
        Log.i(TAG, "download completed");
        publishCompleted();
    }

    public File newFile(String fileName) {
        Calendar cal = Calendar.getInstance();
        long timeInMillis = cal.getTimeInMillis();
        String mFileName = fileName==null?String.valueOf(timeInMillis) + ".jpeg":fileName;
        File mFilePath = new File(getDownloadDirPath());
        try {
            File newFile = new File(mFilePath.getAbsolutePath(), mFileName);
            newFile.createNewFile();
            return newFile;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void sendBroadcastMessage(String path, ResponseBody responseBody) {
        Intent localIntent = new Intent("my.own.broadcast");
        localIntent.putExtra("contentType",responseBody.contentType().toString());
        localIntent.putExtra("realPath", path);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


    public void sendBroadcastMessage(String path, String message) {
        Intent localIntent = new Intent("my.own.broadcast");
        localIntent.putExtra("result", message);
        localIntent.putExtra("realPath", path);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
/*
    private RequestBody createRequestBodyFromFile(File file, String mimeType) {
        return RequestBody.create(MediaType.parse(mimeType), file);
    }

    private RequestBody createRequestBodyFromText(String mText) {
        return RequestBody.create(MediaType.parse("text/plain"), mText);
    }

    private RequestBody createCountingRequestBody(File file, String mimeType, FlowableEmitter<Double> emitter) {
        RequestBody requestBody = createRequestBodyFromFile(file, mimeType);
        return new CountingRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (1.0 * bytesWritten) / contentLength;
            emitter.onNext(progress);
        });
    }
*/
    private void saveAttachmentPathToRealm(final String path) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileRealm attachment = realm.where(FileRealm.class)
                        .equalTo(FileRealm.Fields.UID, attachmentId).findFirst();
                if(attachment !=null) attachment.setFilePath(path);
                publishCompleted();
            }
        });
    }

    private void publishProgress(long downloadedBytes, long fileSize) {
        int progress = (int) Math.round((double) downloadedBytes / (double) fileSize * 100.d);
        Bundle resultData = new Bundle();
        resultData.putInt(KEY_PROGRESS, progress);
        //Log.d(TAG, "download process: " + progress);
        receiver.send(UPDATE_PROGRESS_CODE, resultData);
    }

    private void publishCompleted() {
        Bundle resultData = new Bundle();
        receiver.send(COMPLETE_CODE, resultData);
    }

    private void publishError(String error) {
        Bundle resultData = new Bundle();
        resultData.putString(KEY_ERROR, error);
        receiver.send(ERROR_CODE, resultData);
    }

    private static String getDownloadDirPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
                + File.separator + DAJANA_DIR;
    }
}
