package dajana.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import dajana.utils.DownloadManager;
import dajana.utils.FileManager;
import dajana.utils.PermissionsRequester;

import java.io.File;
import java.util.List;

import dajana.data.database.realm.FileRealm;
import dajana.data.database.realm.MyCloud;
import dajana.fragment.ImageViewFragment;

import io.realm.Case;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import wang.switchy.hin2n.R;


public class ImageViewActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    private static final String USERNAME = "USERNAME";
    private static final String SERVER = "SERVER";
    private static final String MYCLOUDID = "MYCLOUDID";
    private static final String IMAGE_URL = "IMAGE_URL";
    private static final String IMAGE_PATH = "IMAGE_PATH";
    private static final String IMAGE_DESCRIPTION = "IMAGE_DESCRIPTION";
    private static final String IMAGE_POSITION = "IMAGE_POSITION";
    private static final String IMAGE_UID = "IMAGE_UID";
    private static final int PERMISSIONS_REQUEST_DOWNLOAD_FILE = 24;
    public static final int SHARE_ACTIVITY_REQUEST_CODE = 25;

    private ViewPager viewPager;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageView ivCancelDownload;
    private boolean waitForSharing;
    private boolean isDownloading;
    private int myId;
    private String username;
    private String server;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private CompositeSubscription attachmentStateSubscription = new CompositeSubscription();
    private OrderedRealmCollection<FileRealm> fileList;


    @NonNull
    public static Intent createIntent(Context context,  int position) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        Bundle args = new Bundle();

        args.putInt(IMAGE_POSITION, position);
        intent.putExtras(args);
        return intent;
    }

    @NonNull
    public static Intent createIntent(Context context, String username,String server,int uid) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        args.putString(SERVER, server);
        args.putInt(IMAGE_UID, uid);
        intent.putExtras(args);
        return intent;
    }

    @NonNull
    public static Intent createIntent(Context context, int myId,int uid) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        Bundle args = new Bundle();
        args.putInt(MYCLOUDID, myId);
        args.putInt(IMAGE_UID, uid);
        intent.putExtras(args);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        // get params
        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        if (args == null) {
            finish();
            return;
        }

        this.username = args.getString(USERNAME);
        this.server = args.getString(SERVER);
        String imageUrl = args.getString(IMAGE_URL);
        String imagePath = args.getString(IMAGE_PATH);
        String description = args.getString(IMAGE_DESCRIPTION);
        int uid = args.getInt(IMAGE_UID);

        Realm realm = Realm.getDefaultInstance();
        fileList = realm.where(FileRealm.class).contains(FileRealm.Fields.CONTENTTYPE,"image/").findAllSorted(FileRealm.Fields.TIMESTAMP,Sort.ASCENDING);
        int imagePosition = 0;

        if(fileList.size() == 0) {
            Log.d("ImageView","Size == 0");
            /*MyCloud myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.USERNAME,username)
                    .equalTo(MyCloud.Fields.SERVER,server).findFirst();
            myId = myCloud.getId();
            fileList = myCloud.getFileList();*/
            //fileList = realm.where(FileRealm.class).findAll();
        }

        for(int i = 0; i < fileList.size(); i++) {
            if(fileList.get(i).getUid() == uid) {
                break;
            }else {
                imagePosition ++;
            }
        }
        // setup toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_default);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
        toolbar.inflateMenu(R.menu.menu_image_viewer);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //NavUtils.navigateUpFromSameTask(ImageViewActivity.this);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);
        ivCancelDownload = findViewById(R.id.ivCancelDownload);
        ivCancelDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelDownloadClick();
            }
        });
        viewPager = findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                FileRealm attachment = fileList.get(position);
                String url = "http://"+server+"/cloud/upload/files/"+attachment.getUrl();
                return ImageViewFragment.newInstance(attachment.getFilePath()==null?attachment.getOriginalUrl():attachment.getFilePath(),
                        url, attachment.getContentType(), attachment.getUid());
            }

            @Override
            public int getCount() {
                return fileList.size();
            }
        };
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(imagePosition);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                updateToolbar();
                unsubscribeAttachmentState();
                subscribeForAttachment(fileList.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        updateToolbar();
        if (fileList.size() > imagePosition) subscribeForAttachment(fileList.get(imagePosition));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateToolbar();
        subscribeForDownloadProgress();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unsubscribeAll();
        showProgress(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_DOWNLOAD_FILE:
                if (PermissionsRequester.isPermissionGranted(grantResults)) {
                    downloadImage();
                } else {
                    onNoWritePermissionError();
                }
                break;
        }
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download_image:
                onImageDownloadClick();
                break;

            case R.id.action_copy_link:
                onCopyLinkClick();
                break;

            case R.id.action_share:
                onShareClick();
                break;
        }
        return true;
    }

    private void onCopyLinkClick() {
        int position = viewPager.getCurrentItem();
        FileRealm attachment = fileList.get(position);
        String url = attachment.getUrl();

        ClipboardManager clipboardManager = ((ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE));
        if (clipboardManager != null) clipboardManager.setPrimaryClip(ClipData.newPlainText(url, url));
        Toast.makeText(this, R.string.toast_link_copied, Toast.LENGTH_SHORT).show();
    }

    private void onImageDownloadClick() {
        if (PermissionsRequester.requestFileWritePermissionIfNeeded(
                this, PERMISSIONS_REQUEST_DOWNLOAD_FILE)) downloadImage();
    }

    private void downloadImage() {
        int position = viewPager.getCurrentItem();
        FileRealm attachment = fileList.get(position);
        showProgress(true);
        DownloadManager.getInstance().downloadFile(attachment, username,server,myId, this);
    }

    private void onShareClick() {
        int position = viewPager.getCurrentItem();
        FileRealm attachment = fileList.get(position);
        String path = attachment.getOriginalUrl();

        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                startActivityForResult(FileManager.getIntentForShareFile(file),
                        SHARE_ACTIVITY_REQUEST_CODE);
                return;
            } else Toast.makeText(this, R.string.FILE_NOT_FOUND, Toast.LENGTH_SHORT).show();
        } else {
            waitForSharing = true;
            onImageDownloadClick();
        }
    }


    private void onCancelDownloadClick() {
        DownloadManager.getInstance().cancelDownload(this);
    }

    private void unsubscribeAll() {
        subscriptions.clear();
        unsubscribeAttachmentState();
    }

    private void subscribeForDownloadProgress() {
        subscriptions.add(DownloadManager.getInstance().subscribeForProgress()
                .doOnNext(new Action1<DownloadManager.ProgressData>() {
                    @Override
                    public void call(DownloadManager.ProgressData progressData) {
                        onProgressUpdated(progressData);
                    }
                }).subscribe());
    }

    private void updateToolbar() {
        int current = 0, total = 0;
        if (viewPager != null) current = viewPager.getCurrentItem() + 1;
        if (fileList != null) total = fileList.size();
        toolbar.setTitle(current + " of " + total);
        setUpMenuOptions(toolbar.getMenu());
    }
    private void setUpMenuOptions(Menu menu) {
        int position = viewPager.getCurrentItem();
        FileRealm attachment = fileList.get(position);
        String filePath = attachment.getFilePath();
        String orignal = attachment.getOriginalUrl();
        String url = attachment.getUrl();
        Long size = attachment.getSize();
        menu.findItem(R.id.action_download_image).setVisible(filePath == null && size != null && url != null);
        menu.findItem(R.id.action_download_image).setEnabled(!isDownloading);
        menu.findItem(R.id.action_done).setVisible((filePath != null && new File(filePath).exists()));
        menu.findItem(R.id.action_share).setVisible(size != null);
    }

    private void onProgressUpdated(DownloadManager.ProgressData progressData) {
        int position = viewPager.getCurrentItem();
        FileRealm attachment = fileList.get(position);

        if (progressData.getUid()== attachment.getUid()) {
            if (progressData.isCompleted()) {
                showProgress(false);
                isDownloading = false;
                updateToolbar();
            } else if (progressData.getError() != null) {
                showProgress(false);
                showToast(progressData.getError());
                isDownloading = false;
                updateToolbar();
            } else {
                progressBar.setProgress(progressData.getProgress());
                showProgress(true);
                isDownloading = true;
                updateToolbar();
            }
        } else showProgress(false);
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            ivCancelDownload.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            ivCancelDownload.setVisibility(View.GONE);
        }
    }
    private void onNoWritePermissionError() {
        Toast.makeText(this, R.string.no_permission_to_write_files, Toast.LENGTH_SHORT).show();
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }



    private void subscribeForAttachment(FileRealm attachment) {
        if (attachment == null) return;
        Realm realm = Realm.getDefaultInstance();
        FileRealm attachmentForSubscribe = realm.where(FileRealm.class)
                .equalTo(FileRealm.Fields.UID, attachment.getUid())
                .findFirst();

        if (attachmentForSubscribe == null) return;
        Observable<FileRealm> observable = attachmentForSubscribe.asObservable();

        attachmentStateSubscription.add(observable.doOnNext(new Action1<FileRealm>() {
            @Override
            public void call(FileRealm attachment) {
                updateToolbar();
                if (waitForSharing) {
                    waitForSharing = false;
                    onShareClick();
                }
            }
        }).subscribe());
    }

    private void unsubscribeAttachmentState() {
        attachmentStateSubscription.clear();
    }


    public void findImage(Realm realm,String text) {
        text = text == null ? null : text.toLowerCase().trim();
        RealmQuery<FileRealm> query = realm.where(FileRealm.class);
        if(!(text == null || "".equals(text))) {
            query.contains(FileRealm.Fields.CONTENTTYPE, text, Case.INSENSITIVE)
                    .or().contains(FileRealm.Fields.FILENAME, text, Case.INSENSITIVE)
                    .or().contains(FileRealm.Fields.DESCRIPTION, text, Case.INSENSITIVE)
                    .or().contains(FileRealm.Fields.TIMESTAMP, text, Case.INSENSITIVE);
        }
        fileList = query.findAllSortedAsync(FileRealm.Fields.TIMESTAMP, Sort.ASCENDING);
    }
}
