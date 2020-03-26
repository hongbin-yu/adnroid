package com.wave.fileuploadservice;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.wave.fileuploadservice.service.CountingRequestBody;
import com.wave.fileuploadservice.service.RestApiService;
import com.wave.fileuploadservice.service.RetrofitInstance;

import dajana.data.database.realm.FileRealm;
import dajana.data.database.realm.MyCloud;
import dajana.data.database.sqlite.FileTable;
import dajana.utils.FileManager;
import dajana.utils.MIMEType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Queue;

import dajana.utils.UploadManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import wang.switchy.hin2n.R;

public class FileUploadService extends JobIntentService {
    public static final int UPDATE_PROGRESS_CODE = 3132;
    public static final int ERROR_CODE = 3133;
    public static final int COMPLETE_CODE = 3134;
    public final static String KEY_ATTACHMENT_ID = "attachment_id";
    public final static String KEY_RECEIVER = "receiver";
    public final static String KEY_PROGRESS = "progress";
    public final static String KEY_MYCLOUD_ID = "mycloud_id";
    public final static String KEY_USERNAME = "username";
    public final static String KEY_FILE_PATH = "file_path";
    public final static String KEY_FILE_NAME = "file_name";
    public final static String KEY_FILE_SIZE = "file_size";
    public final static String KEY_URL = "url";
    public final static String KEY_URI = "uri";
    public final static String KEY_ERROR = "error";

    public static Queue<String> qFileToUpload = new LinkedList<String>();
    private static final String TAG = "FileUploadService";
    Disposable mDisposable;
    String realPath;
    String username;
    ResponseBody response;
    FileTable fileRealm;
    public static Context context;
    static int total;
    static int count;
    private int attachmentId;
    private ResultReceiver receiver;
    private static FileUploadService instance;
    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 102;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static void enqueueWork(Context context, Intent intent) {
        FileUploadService.context = context;
        total = qFileToUpload.size();
        enqueueWork(context, FileUploadService.class, JOB_ID, intent);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i(TAG,"Service started");
    }

    public static FileUploadService getInstance() {
        return instance;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        /**
         * Download/Upload of file
         * The system or framework is already holding a wake lock for us at this point
         */
        if (intent == null) return;
        this.receiver = intent.getParcelableExtra(KEY_RECEIVER);
        this.attachmentId = intent.getIntExtra(KEY_ATTACHMENT_ID,0);
        this.realPath = intent.getStringExtra(KEY_FILE_PATH);
        String fileName = intent.getStringExtra(KEY_FILE_NAME);
        username = intent.getStringExtra(KEY_USERNAME);
        long fileSize = intent.getLongExtra(KEY_FILE_SIZE, 0);
        String url = intent.getStringExtra(KEY_URL);
        int myId = intent.getIntExtra(KEY_MYCLOUD_ID,-1);
        Log.i(TAG, "onHandleWork: my cloud id="+myId+",realPath="+realPath);

        if(myId >= 0) {
            Realm realm = Realm.getDefaultInstance();
            MyCloud myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.UID,myId).findFirst();
            if(myCloud == null) {
                Log.e(TAG, "onHandleWork: Invalid my cloud id");
                return;
            }

            username = myCloud.getUsername();
        }else if(realPath == null){
            // get file file here
            realPath = qFileToUpload.poll();//intent.getStringExtra("mFilePath");
            count = total - qFileToUpload.size();
        }


        if (realPath == null) {
            sendBroadcastMessage(realPath,"onHandleWork: Invalid file URI");
            Log.e(TAG, "onHandleWork: Invalid file URI");
            return;
        }
        if (username == null) {
            sendBroadcastMessage(realPath,"onHandleWork: "+getString(R.string.Invalid_username));
            Log.e(TAG, "onHandleWork: Invalid username");
            return;
        }
        File file = new File(realPath);
        if(!file.exists()) {
            sendBroadcastMessage(realPath,file.getName()+" : "+getString(R.string.file_not_exists));
            Log.e(TAG, "onHandleWork: Invalid username");
            return;
        }
        sendBroadcastMessage(realPath,count+"/"+total+ " "+ getString(R.string.File_uploading));
        if(receiver != null)
            publishProgress(0.0);
        RestApiService apiService = RetrofitInstance.getApiService();
        Flowable<Double> fileObservable = Flowable.create(emitter -> {
            fileRealm = apiService.onFileUpload(username.toLowerCase(),realPath,file.lastModified(), createMultipartBody(file, emitter)).blockingGet();
            emitter.onComplete();
        }, BackpressureStrategy.LATEST);

        mDisposable = fileObservable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(progress -> onProgress(progress), throwable -> onErrors(throwable), () -> onSuccess());
    }

    private void onErrors(Throwable throwable) {
        if(receiver != null) {
            publishError(throwable.getMessage());
    }else
            sendBroadcastMessage(realPath,"Error in file upload " + throwable.getMessage());
        Log.e(TAG, "onErrors: ", throwable);
    }

    private void onProgress(Double progress) {
        if(receiver != null) {
            publishProgress(progress);
        }else
            sendBroadcastMessage(realPath,count+"/"+total+" "+getString(R.string.Uploading_in_progress)+"... " + (int) (100 * progress));
        //Log.d(TAG, "onProgress: " + progress*100);
    }

    private void onSuccess() {
        sendBroadcastMessage(realPath,getString(R.string.File_uploading_successful));
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileRealm fileDB = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ORIGINALURL,realPath).findFirst();
                if(fileDB != null) {
                    fileDB.setId(fileRealm.getId());
                    fileDB.setUrl(fileRealm.getUrl());
                    fileDB.setPublic_token(fileRealm.getPublic_token());
                    fileDB.setResult("uploaded");
                }
            }
        });
        Log.i(TAG, "onSuccess: File Uploaded : id:"+fileRealm.getId()+",url:"+fileRealm.getUrl());
        if(receiver != null) {
            publishCompleted();
        }else {
            if(!FileUploadService.qFileToUpload.isEmpty()) {
                sendBroadcastMessage(realPath,getString(R.string.File_uploading_successful));
                onNext();
            }else {
                sendBroadcastMessage(realPath,count+"/"+total+" "+getString(R.string.All_done));
            }
        }

    }

    public void sendBroadcastMessage(String path, String message) {
        Intent localIntent = new Intent("my.own.broadcast");
        localIntent.putExtra("result", message);
        localIntent.putExtra("realPath", path);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private RequestBody createRequestBodyFromFile(File file, String mimeType) {
        return RequestBody.create(MediaType.parse(mimeType), file);
    }

    private RequestBody createRequestBodyFromText(String mText) {
        return RequestBody.create(MediaType.parse("text/plain"), mText);
    }


    private Response uploadFile(final String username,final String url, final File file, MediaType contentType)
            throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(contentType, file))
                .header("Authorization",username)
                .build();

        if (client != null) return client.newCall(request).execute();
        else throw new IOException("Upload failed: failed to create httpclient");
    }

    /**
     * return multi part body in format of FlowableEmitter
     *
     * @param filePath
     * @param emitter
     * @return
     */
    private MultipartBody.Part createMultipartBody(File file, FlowableEmitter<Double> emitter) throws IOException {

        String mimeType = FileManager.getMimeType(file.getName());
        String value = MIMEType.FILE.value;
        if(mimeType.startsWith("image/")) {
            value = MIMEType.IMAGE.value;
        }else if(mimeType.startsWith("audio/")) {
            value = MIMEType.AUDIO.value;
        }else if(mimeType.startsWith("video/")) {
            value = MIMEType.VIDEO.value;
        }
        return MultipartBody.Part.createFormData("myFile", file.getName(), createCountingRequestBody(file, value, emitter));
    }

    private RequestBody createCountingRequestBody(File file, String mimeType, FlowableEmitter<Double> emitter) {
        RequestBody requestBody = createRequestBodyFromFile(file, mimeType);
        return new CountingRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (1.0 * bytesWritten) / contentLength;
            emitter.onNext(progress);
        });
    }

    private void onNext() {
        Intent intent = new Intent(context, FileUploadService.class);
        intent.putExtra("username",username);
        enqueueWork(this, FileUploadService.class, JOB_ID, intent);

    }

    private void publishProgress(Double progressing) {
        int progress = (int) Math.round((double) progressing * 100.d);
        Bundle resultData = new Bundle();
        resultData.putInt(KEY_PROGRESS, progress);
        receiver.send(UPDATE_PROGRESS_CODE, resultData);
        Log.d(TAG, "onProgress: " + progress);
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


}
