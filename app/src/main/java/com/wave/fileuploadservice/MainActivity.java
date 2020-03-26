package com.wave.fileuploadservice;

import android.Manifest;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import dajana.data.database.realm.FileRealm;
import dajana.data.database.realm.MyCloud;
import dajana.model.DataHelper;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import wang.switchy.hin2n.BuildConfig;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.activity.BaseActivity;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQUEST_TAKE_DIRECTORY = 100;
    static final int REQUEST_TAKE_PHOTO = 101;
    static final int REQUEST_GALLERY_PHOTO = 102;
    static final int REQUEST_GALLERY_DOCUMENT = 103;
    static final int REQUEST_GALLERY_VIDEO = 104;
    static final int REQUEST_GALLERY_AUDIO = 105;
    String username= null;
    File mPhotoFile;
    ImageView ivDisplayImage;
    Button buttonUpload;
    Button buttonReset;
    TextView tvSelectedFilePath;
    ImageView ivSelectImage;
    TextView txvResult;
    private CommonTitleTemplate mCommonTitleTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadfile);
        ivDisplayImage = findViewById(R.id.ivDisplayImage);
        buttonUpload = findViewById(R.id.buttonUpload);
        buttonReset = findViewById(R.id.buttonRemove);
        tvSelectedFilePath = findViewById(R.id.tvSelectedFilePath);
        ivSelectImage = findViewById(R.id.imageView2);
        txvResult = findViewById(R.id.txvResult);
        buttonUpload.setOnClickListener(this);
        ivSelectImage.setOnClickListener(this);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileUploadService.qFileToUpload.clear();
                tvSelectedFilePath.setText(getString(R.string.Select_files));
                txvResult.setText(getString(R.string.Result));
                Glide.with(MainActivity.this).load(R.drawable.photo).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
                buttonReset.setVisibility(View.GONE);
                buttonUpload.setVisibility(View.GONE);

                Realm realm = Realm.getDefaultInstance();

                MyCloud myCloud = realm.where(MyCloud.class).findFirst();
                for(FileRealm f:myCloud.getFileList()) {
                    DataHelper.deleteItemAsync(realm,f.getUid());

                }
            }
        });
/*
        ivDisplayImage.setOnTouchListener(new View.OnTouchListener() {
            public final static int SWIPED_LEFT = 1;
            public final static int SWIPED_RIGHT = 2;
            public final static int SWIPED_UP = 3;
            public final static int SWIPED_DOWN = 4;
            public final static int CLICKED = 5;
            private RelativeLayout.LayoutParams mLayoutParams;
            private View mMovingView = null;
            private int SWIPE_ACTION = 0;
            private float firstTouchX = 0;
            private float firstTouchY = 0;
            private float lastTouchX = 0;
            private float lastTouchY = 0;
            private float mInitialLeft = 0;
            private float mInitialTop = 0;
            private int activePointerID;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == motionEvent.ACTION_DOWN){
                    final float x = motionEvent.getX();
                    final float y = motionEvent.getY();
                    mMovingView = view;
                    firstTouchX = x;
                    firstTouchY = y;
                    mLayoutParams = (RelativeLayout.LayoutParams) mMovingView.getLayoutParams();
                    mInitialLeft = mLayoutParams.leftMargin;
                    mInitialTop = mLayoutParams.topMargin;
                    activePointerID = motionEvent.getPointerId(0);

                }
                if(motionEvent.getAction() == motionEvent.ACTION_UP){
                    float dX = lastTouchX - firstTouchX;
                    float dY = lastTouchY - firstTouchY;
                    mMovingView = null;
                    final int pointerIndex = motionEvent.findPointerIndex(activePointerID);

                    if((firstTouchX + 10) == motionEvent.getX(activePointerID) &&
                            (firstTouchY + 10) == motionEvent.getY(activePointerID)){

                        SWIPE_ACTION = CLICKED;

                    }else{
                        if(Math.abs(dX) > Math.abs(dY)){
                            if(dX > 0){
                                SWIPE_ACTION = SWIPED_RIGHT;
                            }else{
                                SWIPE_ACTION = SWIPED_LEFT;
                            }
                        }else{
                            if(dY > 0){
                                SWIPE_ACTION = SWIPED_DOWN;
                            }else{
                                SWIPE_ACTION = SWIPED_UP;
                            }
                        }
                    }
                    if(SWIPE_ACTION == SWIPED_RIGHT) {
                        String path = FileUploadService.qFileToUpload.poll();
                        tvSelectedFilePath.setText(path);
                        Glide.with(MainActivity.this).load(path).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
                        //FileUploadService.qFileToUpload.add(path);
                    }else if(SWIPE_ACTION == SWIPED_LEFT){
                        String path = FileUploadService.qFileToUpload.poll();
                        tvSelectedFilePath.setText(path);
                        Glide.with(MainActivity.this).load(path).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);

                    }

                }

                if(motionEvent.getAction() == motionEvent.ACTION_MOVE){
                    mLayoutParams = (RelativeLayout.LayoutParams) mMovingView.getLayoutParams();
                    mLayoutParams.leftMargin = (int) (mInitialLeft + motionEvent.getRawX() - firstTouchX);
                    mLayoutParams.topMargin = (int) (mInitialTop + motionEvent.getRawY() - firstTouchY);
                    view.setLayoutParams(mLayoutParams);
                }
                return false;
            }
        });
*/

        ivDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = FileUploadService.qFileToUpload.poll();
                if(path != null) {
                    tvSelectedFilePath.setText(path);
                    Glide.with(MainActivity.this).load(path).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
                    FileUploadService.qFileToUpload.add(path);
                }else {
                    Glide.with(MainActivity.this).load(R.drawable.photo).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
                }

            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(FileUploadService.qFileToUpload.size() > 0) {
            buttonReset.setVisibility(View.VISIBLE);
            buttonUpload.setVisibility(View.VISIBLE);
        }else {
            buttonReset.setVisibility(View.GONE);
            buttonUpload.setVisibility(View.GONE);
        }

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonUpload:
                if (FileUploadService.qFileToUpload.isEmpty()) {
                    Toast.makeText(this, R.string.Select_files_first, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent mIntent = new Intent(this, FileUploadService.class);
                mIntent.putExtra("mFilePath", tvSelectedFilePath.getText().toString());
                mIntent.putExtra("username",getIntent().getStringExtra("username"));
                FileUploadService.enqueueWork(this, mIntent);
                break;
            case R.id.buttonRemove:
                FileUploadService.qFileToUpload.clear();
                tvSelectedFilePath.setText(getString(R.string.Select_files_first));
                txvResult.setText(getString(R.string.Result));
                break;
            case R.id.imageView2:
                selectImage();
                break;
        }
    }


    /**
     * Alert dialog for capture or select from galley
     */
    private void selectImage() {
        final CharSequence[] items = {getString(R.string.Take_Photo), getString(R.string.Choose_from_Library),
                "Document",getString(R.string.Cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals(getString(R.string.Take_Photo))) {
                requestStoragePermission(REQUEST_TAKE_PHOTO);
            } else if (items[item].equals(getString(R.string.Choose_from_Library))) {
                requestStoragePermission(REQUEST_GALLERY_PHOTO);
            } else if (items[item].equals("Document")) {
                requestStoragePermission(REQUEST_GALLERY_DOCUMENT);
            } else if (items[item].equals(getString(R.string.Cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Requesting multiple permissions (storage and camera) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private void requestStoragePermission(int type) {
        Dexter.withActivity(this).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (type == REQUEST_TAKE_PHOTO) {
                                startCamera();
                            } else if(type == REQUEST_GALLERY_PHOTO){
                                chooseGallery();
                            } else if(type == REQUEST_GALLERY_DOCUMENT){
                                chooseDocument();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            chooseGallery();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(error -> Toast.makeText(getApplicationContext(), R.string.Error_occurred+"! ", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }

    public void startCamera() {
        mPhotoFile = newFile();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (mPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".fileProvider", mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }


    public void chooseGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    public void chooseDocument() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), REQUEST_GALLERY_DOCUMENT);
    }

    public void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_GALLERY_DOCUMENT);
    }

    public void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_GALLERY_DOCUMENT);
    }

    public void chooseDirectory() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose directory"), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                String realPath = mPhotoFile.getAbsolutePath();
                if(!FileUploadService.qFileToUpload.contains(realPath))
                    FileUploadService.qFileToUpload.add(realPath);
                tvSelectedFilePath.setText(mPhotoFile.getAbsolutePath());
                Glide.with(MainActivity.this).load(mPhotoFile).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
                txvResult.setText(FileUploadService.qFileToUpload.size()+" "+getString(R.string.files_ready_to_upload));
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                if(data.getClipData() != null) {
                   int count = data.getClipData().getItemCount();
                   for(int i = 0; i < count; i++) {
                       Uri selectedImage = data.getClipData().getItemAt(i).getUri();
                       String realPath = getRealPathFromUri(selectedImage);
                       if(!FileUploadService.qFileToUpload.contains(realPath))
                            FileUploadService.qFileToUpload.add(realPath);
                   }
                   String path = FileUploadService.qFileToUpload.peek();
                   Glide.with(MainActivity.this).load(path).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
                   tvSelectedFilePath.setText(path);
                   txvResult.setText(FileUploadService.qFileToUpload.size()+" "+getString(R.string.files_ready_to_upload));
                }else {
                    Uri selectedImage = data.getData();
                    String realPath = getRealPathFromUri(selectedImage);
                    if(!FileUploadService.qFileToUpload.contains(realPath))
                        FileUploadService.qFileToUpload.add(getRealPathFromUri(selectedImage));
                    tvSelectedFilePath.setText(realPath);
                    Glide.with(MainActivity.this).load(realPath).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
                    txvResult.setText(FileUploadService.qFileToUpload.size()+" "+getString(R.string.files_ready_to_upload));
                }

            }
            if(FileUploadService.qFileToUpload.size() > 0) {
                buttonReset.setVisibility(View.VISIBLE);
                buttonUpload.setVisibility(View.VISIBLE);
            }
        }
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
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter("my.own.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(myLocalBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver myLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String result = intent.getStringExtra("result");
            String realPath = intent.getStringExtra("realPath");
            if(realPath != null) {
                tvSelectedFilePath.setText(realPath);
                Glide.with(MainActivity.this).load(realPath).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);
            }
            if(FileUploadService.qFileToUpload.size() == 0) {
                buttonReset.setVisibility(View.GONE);
                buttonUpload.setVisibility(View.GONE);
                Glide.with(MainActivity.this).load(R.drawable.photo).apply(new RequestOptions().centerCrop().circleCrop()).into(ivDisplayImage);

            }
            txvResult.setText(result);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myLocalBroadcastReceiver);
    }


}
