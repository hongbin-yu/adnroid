package com.wave.fileuploadservice.service;

import dajana.activity.data.model.LoggedInUser;
import dajana.data.database.realm.FileRealm;
import dajana.data.database.sqlite.FileTable;

import dajana.model.Result;
import dajana.model.User;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created on : Feb 25, 2019
 * Author     : AndroidWave
 */
public interface RestApiService {


    @Multipart
    @POST("file-upload.php")
    Single<FileTable> onFileUpload(@Header("Authorization") String username,@Query("filepath") String filepath, @Query("lastModified") long lastModified, @Part MultipartBody.Part file);

    @GET("file-download.php")
    Single<Result> onFileDownload(@Query("url") String uri);

    @Multipart
    @POST("file-manage.php")
    Single<Result> onFileDelete(@Header("Authorization") String username, @Query("action") String action, @Query("batch") int batch, @Query("file_name") String file_name,@Part MultipartBody.Part file);

    @GET("file-manage.php")
    Single<Result> onFileUnassign(@Header("Authorization") String username, @Query("action") String action, @Query("batch") int batch, @Query("modify_type") String modify_type,@Query("modify_id") int modify_id);

    @POST("file-manage.php")
    Single<Result> onFileUpdate(@Header("Authorization") String username, @Query("action") String action, @Query("file_id") int file_id, @Query("file_name") String file_name,@Query("name") String filename,@Query("description") String description);

    @POST("file-table.php")
    Single<FileRealm[]> tableFiles(@Header("Authorization") String username, @Query("maxId") int id);

    @POST("user-table.php")
    Single<User[]> tableUsers(@Header("Authorization") String username);

    @POST("user-file.php")
    Single<User[]> fileUsers(@Header("Authorization") String username,@Query("file_id") int file_id);

    @POST("http://dajana.ca/cloud/loginOrRegister.php")
    Single<LoggedInUser> loginOrRegister(@Header("Authorization") String apk, @Query("username") String username, @Query("password") String password, @Query("email") String email);
}
