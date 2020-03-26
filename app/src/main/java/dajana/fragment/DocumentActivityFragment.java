package dajana.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import dajana.activity.FileEditActivity;
import dajana.activity.ImageViewActivity;
import dajana.activity.VideoActivity;
import dajana.adapter.FileRealmRecylerViewAdapter;
import dajana.data.database.realm.FileRealm;
import dajana.data.database.realm.MyCloud;
import dajana.dialog.SearchDialog;
import dajana.utils.DownloadManager;
import dajana.utils.FileInformation;
import dajana.utils.FileManager;
import dajana.utils.FileUtils;
import dajana.utils.PermissionsRequester;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;


import dajana.model.DataHelper;
import dajana.utils.UploadManager;
import dajana.utils.UriUtils;
import io.realm.Realm;

import wang.switchy.hin2n.BuildConfig;
import wang.switchy.hin2n.Application;
import wang.switchy.hin2n.R;
import dajana.dialog.AttachDialog;


/**
 * A placeholder fragment containing a simple view.
 */
public class DocumentActivityFragment extends Fragment implements AttachDialog.Listener, SearchDialog.Listener, FileRealmRecylerViewAdapter.FileListListener {
    private static final String LOG_TAG = DocumentActivityFragment.class.getSimpleName();
    public static final int FILE_SELECT_ACTIVITY_REQUEST_CODE = 11;
    public static final int IMAGE_SELECT_ACTIVITY_REQUEST_CODE = 13;
    private static final int REQUEST_IMAGE_CAPTURE = 12;
    public static final int SHARE_ACTIVITY_REQUEST_CODE = 25;
    private static final int PERMISSIONS_REQUEST_ATTACH_FILE = 21;
    private static final int PERMISSIONS_REQUEST_CAMERA = 23;
    private static final int PERMISSIONS_REQUEST_DOWNLOAD_FILE = 24;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USERNAME";
    private static final String ARG_PARAM2 = "SERVER";


    private String currentPicturePath;
    private File mPhotoFile;
    public String username;
    public String server;
    public MyCloud myCloud;
    public Realm realm;
    public RecyclerView recyclerView;


    public DocumentActivityFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static DocumentActivityFragment newInstance(String username, String server) {
        DocumentActivityFragment fragment = new DocumentActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, username);
        args.putString(ARG_PARAM2, server);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_PARAM1);
            server = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        realm = Realm.getDefaultInstance();

        return inflater.inflate(R.layout.fragment_document, container, false);

    }


    @Override
    public void onRecentPhotosSend(List<String> paths) {
        ConnectivityManager connManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        for (String path : paths) {
            File file = new File(path);
            if(file.exists()) {
                if (DataHelper.exists(realm, username, server, path)) {
                    Toast.makeText(getActivity(), new File(path).getName() + " already in cloud!", Toast.LENGTH_SHORT).show();
                } else {
                    DataHelper.addItemAsync(realm, getActivity(),username, server, path);
                }
            }else {
                Toast.makeText(getActivity(), new File(path).getName() + " does not exists!", Toast.LENGTH_SHORT).show();
            }


        }
        recyclerView.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (recyclerView.getAdapter().getItemCount() > 0)
                    recyclerView.smoothScrollToPosition(0);
                if(mWifi.isConnected()) {
                    UploadManager.getInstance().start(username,getActivity());
                }

            }
        }, 500);


    }

    /**
     * ActivityResult
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        final List<String> paths = new ArrayList<>();
        final List<Uri> uris = new ArrayList<>();
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                //addMediaToGallery(currentPicturePath);
                //uploadFile(currentPicturePath);
                paths.add(mPhotoFile.getAbsolutePath());
                //DataHelper.addItemAsync(realm, username, server, mPhotoFile.getAbsolutePath());
                break;

            case IMAGE_SELECT_ACTIVITY_REQUEST_CODE:
                ClipData clipData = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    clipData = result.getClipData();
                }


                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        //uris.add(uri);

                        String path = FileUtils.getPath(getContext(), uri);//UriUtils.getRealPathFromUri(uri);
                        if(path != null)
                            paths.add(path);
                        else
                            Toast.makeText(getActivity(), R.string.could_not_get_path_to_file, Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Uri fileUri = result.getData();
                    //uris.add(fileUri);

                    String path = FileUtils.getPath(getContext(), fileUri);//UriUtils.getRealPathFromUri(fileUri);
                    if(path != null)
                        paths.add(path);
                    else
                        Toast.makeText(getActivity(), R.string.could_not_get_path_to_file, Toast.LENGTH_SHORT).show();

                }

                if (paths.size() == 0) {
                    Toast.makeText(getActivity(), R.string.could_not_get_path_to_file, Toast.LENGTH_SHORT).show();
                    return;
                }


                break;
                case FILE_SELECT_ACTIVITY_REQUEST_CODE:
                    ClipData fclipData = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        fclipData = result.getClipData();
                    }

                    //final List<Uri> uris = new ArrayList<>();
                    if (fclipData != null) {
                        for (int i = 0; i < fclipData.getItemCount(); i++) {
                            ClipData.Item item = fclipData.getItemAt(i);
                            Uri uri = item.getUri();
                            uris.add(uri);


                        }
                    } else {
                        Uri fileUri = result.getData();
                        uris.add(fileUri);

                    }

                    if (uris.size() == 0) {
                        Toast.makeText(getActivity(), R.string.could_not_get_path_to_file, Toast.LENGTH_SHORT).show();
                        return;
                    }

                break;
        }

        onRecentPhotosSend(paths);


        for(Uri uri : uris ) {
            String type = UriUtils.getMimeType(uri);
            String path = FileUtils.getPath(getContext(),uri); //UriUtils.getRealPathFromUri(uri);
            String name = UriUtils.getFullFileName(uri);

            String filePath = null;
            //if(path == null) path = UriUtils.getRealPathFromUri(uri);
            if(path == null || !new File(path).exists() || uri.getScheme().equals("content") && type.startsWith("video/")) {
                try {
                    path = UriUtils.copyFileToLocalStorage(getContext(),uri)  ;
                    filePath = path;
                } catch (IOException e) {
                    Log.e("CopyToLocalStorage",e.getMessage());
                    Toast.makeText(getActivity(), uri.toString()+ " "+e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            File f = new File(path);
            String lastModified = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", new java.util.Date(f.lastModified())).toString();
            long size = f.length();
            Log.d("Uri File information","name="+name+",type="+type+",path="+path+",size="+FileInformation.getSize(getContext(),uri));

            if (DataHelper.exists(realm, username, server, path,uri.toString())) {
                Toast.makeText(getActivity(), new File(path).getName() + " exists!", Toast.LENGTH_SHORT).show();
            }else {
                DataHelper.addItemAsync(realm,getActivity(), username, server, path, uri.toString(), type, filePath, lastModified, size);
            }
        }

    }

    @Override
    public void onGalleryClick() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(pickPhoto, IMAGE_SELECT_ACTIVITY_REQUEST_CODE);

/*
        Intent intent = (new Intent(Intent.ACTION_GET_CONTENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, FILE_SELECT_ACTIVITY_REQUEST_CODE);
*/
    }

    @Override
    public void onFilesClick() {
        Intent intent = (new Intent(Intent.ACTION_GET_CONTENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE));
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, FILE_SELECT_ACTIVITY_REQUEST_CODE);

    }

    @Override
    public void onCameraClick() {
        if (PermissionsRequester.requestCameraPermissionIfNeeded(this,
                PERMISSIONS_REQUEST_CAMERA)) startCamera();
    }

    private void startCamera() {

        mPhotoFile = newFile();//generatePicturePath();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            if (mPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        BuildConfig.APPLICATION_ID + ".fileProvider", mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
        /*
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File image = generatePicturePath();
        if (image != null) {
            Uri photoURI = FileProvider.getUriForFile(getContext(),
                    BuildConfig.APPLICATION_ID + ".fileProvider", image);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileManager.getFileUri(image));
            currentPicturePath = image.getAbsolutePath();
        }

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        */

    }

    public File newFile() {
        Calendar cal = Calendar.getInstance();
        long timeInMillis = cal.getTimeInMillis();
        String mFileName = String.valueOf(timeInMillis) + ".jpeg";
        File mFilePath = getFilePath();
        try {
            File newFile = new File(mFilePath.getAbsolutePath(), mFileName);
            newFile.createNewFile();
            return newFile;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public File getFilePath() {
        return getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }


    private static File generatePicturePath() {
        try {
            File storageDir = Application.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);//getAlbumDir();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            return new File(storageDir.getAbsolutePath(), "IMG_" + timeStamp + ".jpg");
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
    }

    private static File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    Application.getInstance().getString(R.string.app_name));
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.e(LOG_TAG, "failed to create directory");
                    return null;
                }
            }
        } else {
            Log.w(LOG_TAG, "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private static void addMediaToGallery(String fromPath) {
        if (fromPath == null) {
            return;
        }
        File f = new File(fromPath);
        Uri contentUri = Uri.fromFile(f);
        addMediaToGallery(contentUri);
    }

    private static void addMediaToGallery(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            Application.getInstance().sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void uploadFile(String path) {
        List<String> paths = new ArrayList<>();
        paths.add(path);
        //HttpFileUploadManager.getInstance().uploadFile(account, user, paths, getActivity());
    }

    @Override
    public void onFileClick(FileRealm attachment,int position) {
        Log.d(LOG_TAG,"url="+attachment.getOriginalUrl());
        //if(attachment.getUrl() == null && attachment.getOriginalUrl() != null) {
        //    UploadManager.getInstance().uploadFile(attachment,myCloud.getId(),getActivity());
        //}
        openFileOrDownload(attachment,position);
    }

    @Override
    public void onFileLongClick(FileRealm attachment, View caller) {

    }

    @Override
    public void onIconLongClick(FileRealm attachment, View caller) {
        FileRealmRecylerViewAdapter adapter = (FileRealmRecylerViewAdapter)recyclerView.getAdapter();
        adapter.filterResults(attachment.getContentType());
    }

    @Override
    public void onEditButtonClick(FileRealm attachment, Set<Integer> selected) {
        Intent intent = new Intent(getActivity(), FileEditActivity.class);
        intent.putExtra("UID",attachment.getUid());
        ArrayList<Integer> uids = new ArrayList<Integer>();
        uids.addAll(selected);
        intent.putIntegerArrayListExtra("SELECTED",uids);
        intent.putExtra("USERNAME",username);
        startActivity(intent);
    }

    @Override
    public void onDownloadCancel() {
        DownloadManager.getInstance().cancelDownload(getActivity());
        UploadManager.getInstance().cancelUpload(getActivity());
    }

    @Override
    public void onDownloadError(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(FileRealm attachment, View caller) {
        DataHelper.deleteLocalStorageAsync(realm,getActivity(),attachment.getUid());
    }

    @Override
    public void onDownloadClick(FileRealm attachment, int position) {
        DownloadManager.getInstance().downloadFile(attachment, username,server,myCloud.getId(), getActivity());
    }

    @Override
    public void onUploadClick(FileRealm attachment, View caller) {
        if(!UploadManager.pathQueue.contains(attachment)) {
            UploadManager.pathQueue.add(attachment);
        }
        UploadManager.getInstance().start(username,getActivity());
    }

    private void openFileOrDownload(FileRealm attachment, int position) {
        String source = getSource(attachment);
        if (source == null && attachment.getUrl() == null) {
            showMessage("Filepath is empty!");
            return;
        }


        if (source != null) {
            File data = new File(source);
            try {
                File file = new File(source);
                if (!file.exists()) {
                    showMessage(file.getName() + " not exists!");
                    return;
                }

                Intent i = new Intent(Intent.ACTION_VIEW);
                //String path = attachment.getOriginalUrl();
                Uri uri = FileManager.getFileUri(data); //FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileProvider", data);
                //Toast.makeText(getActivity(), "Uri "+uri+"/"+attachment.getUri(), Toast.LENGTH_LONG).show();
                if (attachment.getContentType() == null) {
                    Toast.makeText(getActivity(), "ContentType is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (uri == null && attachment.getUri() != null ) {
                    uri = Uri.parse(attachment.getUri());
                    //Toast.makeText(getActivity(), "Uri is null", Toast.LENGTH_SHORT).show();
                    //return;
                }

                i.setDataAndType(uri, attachment.getContentType());
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


                startActivity(i);
            } catch (ActivityNotFoundException e) {
                Log.e(LOG_TAG, e.getMessage());
                Toast.makeText(getActivity(), R.string.toast_could_not_open_file, Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, e.getMessage());
                Toast.makeText(getActivity(), "Null exception," + R.string.toast_could_not_open_file, Toast.LENGTH_SHORT).show();
            }
        }else if(attachment.getContentType()!=null && attachment.getContentType().startsWith("image/")) {
            Intent intent = ImageViewActivity.createIntent(getActivity(), username, server, attachment.getUid());
            getContext().startActivity(intent);
        /*}else if(attachment.getContentType()!=null && attachment.getContentType().startsWith("video/")) {
            Intent intent = VideoActivity.createIntent(getActivity(),attachment.getUrl(),server);
            getContext().startActivity(intent);*/
        } else DownloadManager.getInstance().downloadFile(attachment, username,server,myCloud.getId(), getActivity());
    }

    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onSearchSend(String keywords) {
        FileRealmRecylerViewAdapter adapter = (FileRealmRecylerViewAdapter)recyclerView.getAdapter();

        adapter.filterResults(keywords); //image filter
    }

    @Override
    public void onPhotoClick() {
        FileRealmRecylerViewAdapter adapter = (FileRealmRecylerViewAdapter)recyclerView.getAdapter();

        adapter.filterResults("image/"); //image filter
    }

    @Override
    public void onDocumentClick() {
        FileRealmRecylerViewAdapter adapter = (FileRealmRecylerViewAdapter)recyclerView.getAdapter();
        adapter.filterFileResults();
    }

    @Override
    public void onVideoClick() {
        FileRealmRecylerViewAdapter adapter = (FileRealmRecylerViewAdapter)recyclerView.getAdapter();

        adapter.filterResults("video/"); //video filter
    }

    @Override
    public void onAudioClick() {
        FileRealmRecylerViewAdapter adapter = (FileRealmRecylerViewAdapter)recyclerView.getAdapter();

        adapter.filterResults("audio/"); //audio filter
    }
}