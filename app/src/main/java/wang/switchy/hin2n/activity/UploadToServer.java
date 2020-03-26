package wang.switchy.hin2n.activity;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DownloadManager.Request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import wang.switchy.hin2n.R;
import wang.switchy.hin2n.model.N2NSettingInfo;

public class UploadToServer extends AppCompatActivity {
    TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;

    String upLoadServerUri = null;
    String cookie = null;
    N2NSettingInfo n2NSettingInfo = null;
    /**********  File Path *************/
    final String uploadFilePath = "/mnt/sdcard/";
    private String uploadFileName = "service_lifecycle.png";
    final int ACTIVITY_CHOOSE_FILE = 1;
    final static String TAG="UploadToServer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_server);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        uploadButton = (Button)findViewById(R.id.uploadButton);
        messageText  = (TextView)findViewById(R.id.messageText);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBrowse(view);
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBrowse(view);
            }
        });

        Bundle bundle = getIntent().getBundleExtra("Setting");
        n2NSettingInfo = bundle.getParcelable("n2nSettingInfo");
        cookie = getIntent().getStringExtra("Cookie");
        upLoadServerUri = "http://"+n2NSettingInfo.getLocalIP()+"/cloud/upload-process-form.php";
        //showFileChooser();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile,"Choose a File");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(intent,ACTIVITY_CHOOSE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE && resultCode == RESULT_OK && data != null) {
            //dialog = ProgressDialog.show(UploadToServer.this, "", "Uploading file...", true);
            //dialog.getWindow().setGravity(Gravity.CENTER);
            uploadFile(data.getData());
            //copyFile(data.getData());
        }
        if(requestCode == ACTIVITY_CHOOSE_FILE) {

            if(resultCode == Activity.RESULT_OK) {
                if(data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    int currentItem = 0;
                    while(currentItem <count) {
                        Uri uri = data.getClipData().getItemAt(currentItem).getUri();
                        currentItem = currentItem+1;
                    }
                }else {
                    String path = data.getData().getPath();
                    messageText.setText(path);
                    //dialog = ProgressDialog.show(UploadToServer.this, "", "Uploading file...", true);
                    uploadFile(data.getData());
                }
            }

        }
    }

    public void uploadFile(final Uri sourceFileUri)  {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new UploadFileAsyncTask().execute(sourceFileUri);
            }
        }, 100);

    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        String[] projection = { MediaStore.Images.Media.DATA };
        CursorLoader cursorLoader = new CursorLoader(this, contentURI, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        //Cursor cursor = getContentResolver().query(contentURI, projection, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            // Get columns name by uri type.
            String columnName = MediaStore.Images.Media.DATA;

            if( contentURI==MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
            {
                columnName = MediaStore.Images.Media.DATA;
            }else if( contentURI==MediaStore.Audio.Media.EXTERNAL_CONTENT_URI )
            {
                columnName = MediaStore.Audio.Media.DATA;
            }else if( contentURI==MediaStore.Video.Media.EXTERNAL_CONTENT_URI )
            {
                columnName = MediaStore.Video.Media.DATA;
            }

            // Get column index.
            int imageColumnIndex = cursor.getColumnIndex(columnName);
            //int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(imageColumnIndex);
            cursor.close();
        }
        return result;
    }

    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public String getName(Uri sourceFileUri) {
        String fileName = null;
        String scheme = sourceFileUri.getScheme();
        Log.i("Scheme",scheme);
        if (scheme.equals("file")) {
            fileName = sourceFileUri.getLastPathSegment();
        }
        else if (scheme.equals("content")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(sourceFileUri, new String[]{
                        MediaStore.Images.ImageColumns.DISPLAY_NAME
                }, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    Log.d(TAG, "name is " + fileName);
                }
            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return fileName;
    }

    private int copyStreams(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
        int copiedBytesSize = 0;
        int currentByte = 0;

        while (-1 != (currentByte = in.read(bytes))) {
            out.write(bytes, 0, currentByte);
            copiedBytesSize += currentByte;
        }

        return copiedBytesSize;
    }

    //private static final String TAG = "CopyFileActivity";
    private static final int RESULT_CODE = 0x321;
    private static final int DEFAULT_BUFFER_SIZE = 1024/* bytes */* 1024/* kilobytes */* 10/* megabytes */;
    private static final String DEFAULT_MIME_TYPE = "*/*";
    public static final String RESULT = "FILE_NAME";
/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showFileChooser();
    }
*/
    private void showFileChooser() {
        try {
            startActivityForResult(
                    Intent.createChooser(getIntentForChoosingFiles(), "Select a file for GD App"),
                    RESULT_CODE);
        }
        catch (ActivityNotFoundException e) {
            cancelAndFinish(e);
        }
    }

    private void cancelAndFinish(ActivityNotFoundException e) {
        Log.e(TAG, e.getMessage());
        setResult(RESULT_CANCELED);
        finish();
    }

    private Intent getIntentForChoosingFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(DEFAULT_MIME_TYPE);
        return intent;
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE && resultCode == RESULT_OK && data != null) {
            copyFile(data.getData());
        }
    }
*/
    private void copyFile(final Uri uri) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new CopyFileAsyncTask().execute(uri);
            }
        }, 100);
    }

    private class UploadFileAsyncTask extends AsyncTask<Uri, String, String> {
        private static final String TYPE_CONTENT = "content";
        private static final String TYPE_FILE = "file";
        private ProgressDialog progressDialog;

        public UploadFileAsyncTask() {
            this.progressDialog = new ProgressDialog(UploadToServer.this);
            progressDialog.setMessage("Uploading files..");
            progressDialog.getWindow().setGravity(Gravity.CENTER);
        }

        @Override
        protected String doInBackground(Uri... uris) {
            uploadFile(uris[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setResultAndFinish(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        private void setResultAndFinish(String fileName) {
            progressDialog.dismiss();
            setResult(RESULT_OK, getResultIntent(fileName));
            Log.d(TAG, "If no errors before, file should be now in /" + fileName);
            finish();
        }

        private Intent getResultIntent(String fileName) {
            Intent intent = new Intent();
            intent.putExtra(RESULT, fileName);
            return intent;
        }

        public boolean isNetworkAvailable() {
            return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
        }

        private void copyFile(File oldFile, File newFile) {
            InputStream in = null;
            OutputStream out = null;
            int copiedBytesSize = 0;

            try {
                try {
                    in = new BufferedInputStream(new FileInputStream(oldFile));
                    out = new BufferedOutputStream(new FileOutputStream(newFile));
                    copiedBytesSize = copyStreams(in, out);

                    Log.d(TAG, "Copied bytes: " + copiedBytesSize);
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        private int copyStreams(InputStream in, OutputStream out) throws IOException {
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            int copiedBytesSize = 0;
            int currentByte = 0;

            while (-1 != (currentByte = in.read(bytes))) {
                out.write(bytes, 0, currentByte);
                copiedBytesSize += currentByte;
            }

            return copiedBytesSize;
        }

        private String getPath(Uri uri) {
            String scheme = uri.getScheme();

            if (TYPE_FILE.equalsIgnoreCase(scheme)) return uri.getPath();
            else if (TYPE_CONTENT.equalsIgnoreCase(scheme)) return getFilePathFromMedia(uri);

            return "";
        }

        private String getFilePathFromMedia(Uri uri) {
            String data = "_data";
            String[] projection = { data };
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndex(data);
            if (cursor.moveToFirst()) {
                String filePath = cursor.getString(column_index);
                cursor.close();
                return filePath;
            }
            return "";
        }

        public int uploadFile(Uri sourceFileUri)  {


            String fileName = null;

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(sourceFileUri.toString());
            //String path = getRealPathFromURI(sourceFileUri);
            uploadFileName = getName(sourceFileUri);
            //Log.d(TAG,"path="+path+",fileName="+uploadFileName);
            try {

                // open a URL connection to the Servlet
                InputStream fileInputStream = getContentResolver().openInputStream(sourceFileUri);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cookie",cookie);
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);


                OutputStream output = conn.getOutputStream();

                dos = new DataOutputStream(output);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                bytesAvailable = copyStreams(fileInputStream,dos);

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i(TAG, "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode +", size="+bytesAvailable);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    +uploadFileName;

                            messageText.setText(msg);
                            Toast.makeText(UploadToServer.this, "File Upload Complete.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(UploadToServer.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e(TAG, "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e(TAG, "Exception : "+upLoadServerUri+","
                        + e.getMessage(), e);
            }
            //dialog.dismiss();
            return serverResponseCode;

        }

    }

    private class CopyFileAsyncTask extends AsyncTask<Uri, Void, String> {
        private static final String TYPE_CONTENT = "content";
        private static final String TYPE_FILE = "file";

        private DownloadManager downloadManager;
        private ProgressDialog progressDialog;
        private boolean isDownloading = false;
        private long enqueue;
        private final BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    isDownloading = false;
                    String oldFileName = getFileFromDownloadManager(enqueue);
                    String fileName = copyFile(Uri.parse(oldFileName));
                    setResultAndFinish(fileName);
                    unregisterReceiver(this);
                }
            }
        };

        public CopyFileAsyncTask() {
            this.progressDialog = new ProgressDialog(UploadToServer.this);
            progressDialog.setMessage("Copying files..");
            progressDialog.getWindow().setGravity(Gravity.CENTER);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Uri... uris) {
            return copyFile(uris[0]);
        }

        @Override
        protected void onPostExecute(String fileName) {
            super.onPostExecute(fileName);
            if (!isDownloading) {
                setResultAndFinish(fileName);
            }
        }

        private void setResultAndFinish(String fileName) {
            progressDialog.dismiss();
            setResult(RESULT_OK, getResultIntent(fileName));
            Log.d(TAG, "If no errors before, file should be now in /" + fileName);
            finish();
        }

        private Intent getResultIntent(String fileName) {
            Intent intent = new Intent();
            intent.putExtra(RESULT, fileName);
            return intent;
        }

        private String copyFile(Uri uri) {
            String path = getPath(uri);

            if (TextUtils.isEmpty(path)) {
                Log.e(TAG, "Can't read path.");
                return null;
            }

            Log.d(TAG, "Copying from " + path);
            String fileName = Uri.parse(path).getLastPathSegment(); // in case of TYPE_CONTENT
            copyFile(path, fileName);

            return fileName;
        }

        private String getPath(Uri uri) {
            String scheme = uri.getScheme();

            if (TYPE_FILE.equalsIgnoreCase(scheme)) return uri.getPath();
            else if (TYPE_CONTENT.equalsIgnoreCase(scheme)) return getFilePathFromMedia(uri);

            return "";
        }

        private String getFilePathFromMedia(Uri uri) {
            String data = "_data";
            String[] projection = { data };
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndex(data);
            if (cursor.moveToFirst()) {
                String filePath = cursor.getString(column_index);
                cursor.close();
                return filePath;
            }
            return "";
        }

        private void copyFile(String oldFileName, String newFileName) {
            if (Patterns.WEB_URL.matcher(oldFileName).matches()) {
                if (isNetworkAvailable()) {
                    downloadFileFromUrl(oldFileName);
                }
                else {
                    Log.e(TAG, "No internet connection. File neither downloaded nor copied.");
                }
            }
            else {
                File oldFile = new File(oldFileName);
                File newFile = new File(getFilesDir(), newFileName);
                copyFile(oldFile, newFile);
            }
        }

        public boolean isNetworkAvailable() {
            return ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
        }

        private void downloadFileFromUrl(String fileUrl) {
            registerDownloadReceiver();
            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            Request request = new Request(Uri.parse(fileUrl));
            enqueue = downloadManager.enqueue(request);
            isDownloading = true;
        }

        private void registerDownloadReceiver() {
            registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }

        private String getFileFromDownloadManager(long enqueue) {
            Query query = new Query();
            query.setFilterById(enqueue);
            Cursor c = downloadManager.query(query);
            if (c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    c.close();
                    return uriString;
                }
            }
            Log.e(TAG, "Downloading file not successful. Malformed Url or no internet connection");
            return null;
        }

        private void copyFile(File oldFile, File newFile) {
            InputStream in = null;
            OutputStream out = null;
            int copiedBytesSize = 0;

            try {
                try {
                    in = new BufferedInputStream(new FileInputStream(oldFile));
                    out = new BufferedOutputStream(new FileOutputStream(newFile));
                    copiedBytesSize = copyStreams(in, out);

                    Log.d(TAG, "Copied bytes: " + copiedBytesSize);
                }
                finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        private int copyStreams(InputStream in, OutputStream out) throws IOException {
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            int copiedBytesSize = 0;
            int currentByte = 0;

            while (-1 != (currentByte = in.read(bytes))) {
                out.write(bytes, 0, currentByte);
                copiedBytesSize += currentByte;
            }

            return copiedBytesSize;
        }
    }

    public static void clearCache(final Context context) {
        new Thread() {
            @Override
            public void run() {
                File dataDirectory = context.getFilesDir();
                Log.d(TAG, "Deleting: " + dataDirectory.getPath());
                deleteRecursive(dataDirectory);
            }

            private void deleteRecursive(File rootPath) {
                if (rootPath.isDirectory()) {
                    for (File fileOrFolder : rootPath.listFiles()) {
                        deleteRecursive(fileOrFolder);
                    }
                }

                rootPath.delete();
            }
        }.start();
    }
}
