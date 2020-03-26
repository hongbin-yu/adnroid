package dajana.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.wave.fileuploadservice.FileUploadService;
import com.wave.fileuploadservice.service.CountingRequestBody;
import com.wave.fileuploadservice.service.RestApiService;
import com.wave.fileuploadservice.service.RetrofitInstance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dajana.data.database.realm.FileRealm;
import dajana.data.database.realm.MyCloud;
import dajana.data.database.realm.UserRealm;
import dajana.data.database.sqlite.FileTable;
import dajana.model.DataHelper;
import dajana.model.Result;
import dajana.model.User;
import dajana.utils.FileManager;
import dajana.utils.MIMEType;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import wang.switchy.hin2n.Application;
import wang.switchy.hin2n.R;


public class FileManageService extends JobIntentService {
    public static final int UPDATE_PROGRESS_CODE = 3132;
    public static final int ERROR_CODE = 3133;
    public static final int COMPLETE_CODE = 3134;
    private static final int JOB_ID = 104;
    public final static String KEY_RECEIVER = "receiver";
    public final static String KEY_USERNAME = "USERNAME";
    public final static String KEY_SERVER = "server";
    public final static String KEY_FILE_UID = "file_uid";
    public final static String KEY_FILE_ID = "file_id";
    public final static String KEY_CLIENT_ID = "client_id";
    public final static String KEY_FILE_NAME = "file_name";
    public final static String KEY_FILE_PATH = "file_path";
    public final static String KEY_NAME = "name";
    public final static String KEY_DESCRIPTION = "description";
    public final static String KEY_ACTION = "action";
    public final static String KEY_ERROR = "error";
    private static final String TAG = FileManageService.class.getSimpleName();
    private static FileManageService instance;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    Disposable mDisposable;
    private ResultReceiver receiver;
    private Context context;
    private Realm realm;
    private MyCloud myCloud;
    private Result result;
    private String file_name;
    private int file_id;
    private int client_id;
    private String realPath;
    private String username;
    private String name;
    private String description;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, FileManageService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        realm = Realm.getDefaultInstance();
        Log.i(TAG,"Service started");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        this.file_id = intent.getIntExtra(KEY_FILE_ID,-1);
        long uid = intent.getLongExtra(KEY_FILE_UID,new Long(-1));
        this.file_name = intent.getStringExtra(KEY_FILE_NAME);
        this.name = intent.getStringExtra(KEY_NAME);
        this.description = intent.getStringExtra(KEY_DESCRIPTION);
        this.realPath = intent.getStringExtra(KEY_FILE_PATH);
        this.client_id = intent.getIntExtra(KEY_CLIENT_ID,-1);
        this.username = intent.getStringExtra(KEY_USERNAME);
        String server = intent.getStringExtra(KEY_SERVER);
        String action = intent.getStringExtra(KEY_ACTION);
        this.receiver = intent.getParcelableExtra(KEY_RECEIVER);
        RestApiService apiService = RetrofitInstance.getApiService();
        Log.d("FileManageService","Action:"+action+",UID:"+uid+",file_id="+file_id+",file_name:"+file_name+",username:"+username+",server:"+server);
        /*if(username == null) {
            Toast.makeText(context,"Username can not be null!",Toast.LENGTH_LONG);
            return;
        }*/
        if("delete".equals(action)) {
            int batch[] = new int[1];
            batch[0] = file_id;
/*
            Flowable<Double> fileObservable1 = Flowable.create(new FlowableOnSubscribe<Double>() {

                @Override
                public void subscribe(FlowableEmitter<Double> emitter) throws Exception {
                    result = apiService.onFileDelete(username.toLowerCase(), "delete",file_id,file_name).blockingGet();
                    emitter.onComplete();
                }

            }, BackpressureStrategy.LATEST);
*/
            Flowable<Double> fileObservable = Flowable.create(emitter -> {
                result = apiService.onFileDelete(username.toLowerCase(), "delete",file_id,file_name,createMultipartBody(file_name, emitter)).blockingGet();
                emitter.onComplete();
            }, BackpressureStrategy.LATEST);

            mDisposable = fileObservable.subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(progress -> onProgress(progress), throwable -> onErrors(throwable), () -> onSuccess());
            /*
            apiService.onFileDelete(username.toLowerCase(),"delete",file_id,file_name).observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<Result>() {

                @Override
                public void onSubscribe(Disposable d) {
                    compositeDisposable.add(d);
                }

                @Override
                public void onSuccess(Result result) {
                    //Log.d("FileManageService","Delete "+uid+",ok:"+result.getOk()+",error:"+result.getError());
                    sendBroadcastMessage(file_id,file_name,result);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e("FileManageService",e.getMessage());
                    Result error = new Result();
                    error.setError(e.getMessage());
                    sendBroadcastMessage(file_id,file_name,error);

                }
            });*/
        }
        else if("sync".equals(action)) {
            int batch[] = new int[1];
            batch[0] = file_id;
            apiService.tableFiles(username.toLowerCase(),file_id).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<FileRealm[]>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(FileRealm[] fileTables) {
                            int count = 0;
                            realm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.USERNAME,username).equalTo(MyCloud.Fields.SERVER,server==null?"10.7.0.1":server).findFirst();
                                    RealmResults<FileRealm> oldList = realm.where(FileRealm.class).findAll();
                                    if(myCloud == null) {
                                        myCloud = MyCloud.createMyCloud(realm);
                                        myCloud.setUsername(username);
                                        myCloud.setServer(server);
                                        Log.d(TAG,"Create MyCloud:"+username+"/"+server);
                                    }

                                    RealmList<FileRealm> fileList = new RealmList<FileRealm>();
                                    //myCloud.getFileList().clear();

                                    for(FileRealm file:fileTables) {
                                       FileRealm db_file = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ID,file.getId()).or().endsWith(FileRealm.Fields.ORIGINALURL,file.getOriginalUrl()).findFirst();
                                       if(db_file == null) {
                                           file.setUid(file.getId());
                                           Log.d(TAG,"add id="+file.getId()+",url="+file.getUrl());
                                           FileRealm item = realm.createObject(FileRealm.class,FileRealm.increment());//FileRealm.createFileRealm(realm);
                                           item.setId(file.getId());
                                           item.setUrl(file.getUrl());
                                           item.setFilename(file.getFilename());
                                           item.setSize(file.getSize());
                                           item.setContentType(FileManager.getMimeType(file.getUrl()));
                                           item.setTimestamp(file.getTimestamp());
                                           item.setUploader(file.getUploader());
                                           fileList.add(item);
                                       }else {
                                           fileList.add(db_file);
                                       }
                                    }

                                    if(file_id == 0) {
                                        Log.d("FileManager","Delete:"+oldList.size() +"/"+myCloud.getFileList().size());
                                        for(FileRealm file:oldList) {
                                            if(!fileList.contains(file)) {
                                                Log.d("FileManager", "Delete:" + file.getUid());
                                                DataHelper.deleteItemAsync(realm,file.getUid());
                                            }
                                        }
                                    }
                                }
                            });

                            Result result = new Result();
                            result.setOk("Synced:"+fileTables.length);
                            sendBroadcastMessage(file_id,file_name,result);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FileManageService",e.getMessage());
                            Result error = new Result();
                            error.setError(e.getMessage());
                            sendBroadcastMessage(file_id,file_name,error);

                        }
                    });
        }else if("user-table".equals(action)) {
            apiService.tableUsers(username.toLowerCase()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<User[]>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(User[] users) {
                            realm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for(User user:users) {
                                        UserRealm userDB = realm.where(UserRealm.class).endsWith(UserRealm.Fields.USERNAME,user.getUsername()).findFirst();
                                        if(userDB == null) {
                                            UserRealm item = realm.createObject(UserRealm.class, user.getId());
                                            item.setName(user.getName());
                                            item.setUsername(user.getUsername());
                                        }
                                    }
                                    Log.d("FileManageService","Sycn user-table:"+users.length);
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FileManageService",e.getMessage());
                            Result error = new Result();
                            error.setError(e.getMessage());
                            sendBroadcastMessage(file_id,file_name,error);
                        }
                    });
        }else if("user-file".equals(action)) {
        apiService.fileUsers(username.toLowerCase(),file_id).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<User[]>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(User[] users) {
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                FileRealm fileDB = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ID,file_id).findFirst();
                                fileDB.getUserList().clear();
                                //realm.commitTransaction();
                                for(User user:users) {
                                    UserRealm userDB = realm.where(UserRealm.class).equalTo(UserRealm.Fields.USERNAME,user.getUsername()).findFirst();
                                    if(userDB == null) {
                                        UserRealm item = realm.createObject(UserRealm.class, user.getId());
                                        item.setName(user.getName());
                                        item.setUsername(user.getUsername());
                                        if(!fileDB.getUserList().contains(item))
                                            fileDB.getUserList().add(item);
                                    }else {
                                        fileDB.getUserList().add(userDB);
                                    }
                                    //if(!fileDB.getUserList().contains(userDB))


                                }
                                Log.d("FileManageService","Sycn user-file:"+fileDB.getUserList().size());
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("FileManageService",e.getMessage());
                        Result error = new Result();
                        error.setError(e.getMessage());
                        sendBroadcastMessage(file_id,file_name,error);
                    }
                });
        }else if("unassign".equals(action) || "assign".equals(action) ) {
            apiService.onFileUnassign(username.toLowerCase(),action,file_id,"client_id",client_id).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Result>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(Result result) {
                            sendBroadcastMessage(file_id,file_name,result);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FileManageService",e.getMessage());
                            Result error = new Result();
                            error.setError(e.getMessage());
                            sendBroadcastMessage(file_id,file_name,error);
                        }
                    });
        }else if("update".equals(action) ) {
            apiService.onFileUpdate(username.toLowerCase(),action,file_id,file_name,name,description).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Result>() {

                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(Result result) {
                            sendBroadcastMessage(file_id,file_name,result);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("FileManageService",e.getMessage());
                            Result error = new Result();
                            error.setError(e.getMessage());
                            sendBroadcastMessage(file_id,file_name,error);
                        }
                    });
        }

    }

    private void onSuccess() {
        sendBroadcastMessage(file_id,file_name,result);
    }

    @Override
    public void onDestroy() {
        if(!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
    private void onErrors(Throwable throwable) {
        Result error = new Result();
        error.setError(throwable.getMessage());
        sendBroadcastMessage(file_id,file_name,error);
    }

    private void onProgress(Double progress) {
         Log.d(TAG, "onProgress: " + progress*100);
    }

    public void sendBroadcastMessage(long uid,String url,  Result result) {
        Intent localIntent = new Intent("manage.own.broadcast");
        localIntent.putExtra("ok", result.getOk());
        localIntent.putExtra("error", result.getError());
        localIntent.putExtra("url", url);
        localIntent.putExtra("uid", uid);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
    private MultipartBody.Part createMultipartBody(String filePath, FlowableEmitter<Double> emitter) throws IOException {
        String value = MIMEType.FILE.value;
        File file = File.createTempFile("tmp",".txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileOutputStream fos = new FileOutputStream(file);
        out.write(filePath.getBytes());
        out.writeTo(fos);
        out.close();
        /*
        File file = new File(filePath);
        String mimeType = FileManager.getMimeType(filePath);
        String value = MIMEType.FILE.value;
        if(mimeType.startsWith("image/")) {
            value = MIMEType.IMAGE.value;
        }else if(mimeType.startsWith("audio/")) {
            value = MIMEType.AUDIO.value;
        }else if(mimeType.startsWith("video/")) {
            value = MIMEType.VIDEO.value;
        }*/
        return MultipartBody.Part.createFormData("myFile", file.getName(), createCountingRequestBody(file, value, emitter));
    }

    private RequestBody createCountingRequestBody(File file, String mimeType, FlowableEmitter<Double> emitter) {
        RequestBody requestBody = createRequestBodyFromFile(file, mimeType);
        emitter.onNext(1.0);
        return requestBody;
        /*
        return new CountingRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (1.0 * bytesWritten) / contentLength;
            emitter.onNext(progress);
        });*/
    }

    private RequestBody createRequestBodyFromFile(File file, String mimeType) {
        return RequestBody.create(MediaType.parse(mimeType), file);
    }
    private MyCloud createMyCloud(String username, String server) {
        realm.beginTransaction();
        MyCloud mycloud = realm.createObject(MyCloud.class);
        mycloud.setUsername(username);
        mycloud.setServer(server);
        realm.commitTransaction();
        return mycloud;
    }
}
