package dajana.activity.data;

import android.util.Log;

import com.wave.fileuploadservice.service.RestApiService;
import com.wave.fileuploadservice.service.RetrofitInstance;

import dajana.activity.data.model.LoggedInUser;
import dajana.data.database.realm.FileRealm;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {

            OkHttpClient client = new OkHttpClient();

            String url="http://10.7.0.1/cloud/login.php";
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("username", username)
                    .addFormDataPart("password",password)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.d("Login or Register", "onFailure " + response.toString());
                    //publishError(response.toString());
                    return new Result.Error(new IOException("Error logging in"));
                }

            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
