package dajana.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import dajana.data.database.realm.FileRealm;
import dajana.dialog.SearchDialog;
import dajana.service.FileManageService;
import dajana.utils.DownloadManager;
import dajana.utils.PermissionsRequester;

import dajana.adapter.FileRealmRecylerViewAdapter;
import dajana.data.database.realm.MyCloud;
import dajana.model.DataHelper;
import dajana.utils.UploadManager;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import wang.switchy.hin2n.R;
import dajana.dialog.AttachDialog;
import dajana.fragment.DocumentActivityFragment;
import wang.switchy.hin2n.activity.AddDevicesActivity;
import wang.switchy.hin2n.activity.ScanQRActivity;
import wang.switchy.hin2n.activity.SettingDetailsActivity;

public class DocumentActivity extends AppCompatActivity {
    public static final int FILE_SELECT_ACTIVITY_REQUEST_CODE = 11;
    private static final int REQUEST_IMAGE_CAPTURE = 12;
    public static final int SHARE_ACTIVITY_REQUEST_CODE = 25;

    private static final int PERMISSIONS_REQUEST_ATTACH_FILE = 21;
    private static final int PERMISSIONS_REQUEST_CAMERA = 23;
    private static final int PERMISSIONS_REQUEST_DOWNLOAD_FILE = 24;

    private DocumentActivityFragment documentFragment;
    private Realm realm;
    private RecyclerView recyclerView;
    private Menu menu;
    private FileRealmRecylerViewAdapter adapter;
    private MyCloud myCloud;
    private String username,server;
    private String contentType;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FileRealmRecylerViewAdapter)recyclerView.getAdapter()).filterResults(null);
                recyclerView.smoothScrollToPosition(0);
                fab.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fab.hide();
                    }
                },1000);
            }
        });
        //fab.hide();
        username = getIntent().getStringExtra("username");
        server = getIntent().getStringExtra("server");
        contentType = getIntent().getStringExtra("contentType");
        realm = Realm.getDefaultInstance();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                ((FileRealmRecylerViewAdapter)adapter).filterResults(null); //reset filter
                Number currentIdNum = realm.where(FileRealm.class).max(FileRealm.Fields.ID);
                UploadManager.getInstance().sync(getApplicationContext(),username,(currentIdNum==null?0:currentIdNum.intValue()),mWifi.isConnected());
                swipeRefreshLayout.setRefreshing(false);
                fab.hide();
            }
        });

        if(server == null) {
            server = "10.7.0.1";
        }
        DataHelper.setPrimaryKeyAsync(realm);
        myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.USERNAME,username).equalTo(MyCloud.Fields.SERVER,server).findFirst();
        if(myCloud == null) {
            myCloud = createMyCloud(username,server);
        }
        if(savedInstanceState == null) {
            documentFragment = (DocumentActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_document);
            documentFragment.server = server;
            documentFragment.username = username;
            documentFragment.realm = realm;
            documentFragment.myCloud = myCloud;
        }

        recyclerView = findViewById(R.id.file_messages_recycler_view);

        documentFragment.recyclerView = recyclerView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {


                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if(scrollY<=oldScrollY)
                    {
                        fab.show();
                    }
                    else {
                        fab.hide();
                    }
                }
            });

        }
        setUpRecyclerView();

        UploadManager.getInstance().reset();
        DownloadManager.getInstance().reset();
    }

    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        TouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            long uid = viewHolder.getItemId();
            FileRealm fileDB = DataHelper.getFileRealm(realm,uid);
            Log.d("DocumentActivity","UID="+uid+",url="+fileDB.getUrl()+",file_id="+fileDB.getId()+",username="+username);
            if(fileDB.getId() != null && fileDB.getUrl() != null) {
                Intent intent = new Intent();
                intent.putExtra(FileManageService.KEY_FILE_UID,uid);
                intent.putExtra(FileManageService.KEY_FILE_ID,fileDB.getId());
                intent.putExtra(FileManageService.KEY_FILE_NAME,fileDB.getUrl());
                intent.putExtra(FileManageService.KEY_FILE_PATH,fileDB.getOriginalUrl());
                intent.putExtra(FileManageService.KEY_USERNAME,username);
                intent.putExtra(FileManageService.KEY_ACTION,"delete");
                FileManageService.enqueueWork(getApplicationContext(),intent);
                DataHelper.deleteItemAsync(realm, viewHolder.getItemId());
            }else
                DataHelper.deleteItemAsync(realm, viewHolder.getItemId());
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if(!adapter.inDeletionMode)  return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.listview_options, menu);
        menu.setGroupVisible(R.id.group_normal_mode, true);
        menu.setGroupVisible(R.id.group_delete_mode, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add:
                //DataHelper.addItemAsync(realm);
                attachPressed();
                return true;
            case R.id.action_sync:
                UploadManager.getInstance().reset();

                ((FileRealmRecylerViewAdapter)adapter).filterResults(null); //reset filter
                recyclerView.scrollToPosition(0);
                UploadManager.getInstance().sync(getApplicationContext(),username,0,true);
                return true;
            case R.id.action_search:
                ((FileRealmRecylerViewAdapter)adapter).filterResults(null); //reset filter
                recyclerView.smoothScrollToPosition(0);
                searchPressed();
                return true;
            case R.id.action_start_delete_mode:
                adapter.enableDeletionMode(true);
                menu.setGroupVisible(R.id.group_normal_mode, false);
                menu.setGroupVisible(R.id.group_delete_mode, true);
                return true;
            case R.id.action_end_delete_mode:
                for(long uid:adapter.getCountersToDelete()) {
                    FileRealm fileDB = DataHelper.getFileRealm(realm,uid);
                    Intent intent = new Intent();
                    intent.putExtra(FileManageService.KEY_FILE_UID,uid);
                    intent.putExtra(FileManageService.KEY_FILE_ID,fileDB.getId());
                    intent.putExtra(FileManageService.KEY_FILE_NAME,fileDB.getUrl());
                    intent.putExtra(FileManageService.KEY_USERNAME,username);
                    intent.putExtra(FileManageService.KEY_ACTION,"delete");
                    FileManageService.enqueueWork(getApplicationContext(),intent);
                    DataHelper.deleteItemAsync(realm, (long)uid);
                }

                // Fall through
            case R.id.action_cancel_delete_mode:
                ((FileRealmRecylerViewAdapter)adapter).filterResults(null); //reset filter
                adapter.enableDeletionMode(false);
                menu.setGroupVisible(R.id.group_normal_mode, true);
                menu.setGroupVisible(R.id.group_delete_mode, false);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpRecyclerView() {
        if(contentType != null) {
            RealmResults results = myCloud.getFileList().where().equalTo("contentType",contentType).findAllAsync();
            adapter = new FileRealmRecylerViewAdapter(results,username,server);
        }else {
            RealmResults results = myCloud.getFileList().sort(FileRealm.Fields.UID, Sort.DESCENDING);
            adapter = new FileRealmRecylerViewAdapter(results, username, server);
        }

        adapter.setListener(documentFragment);
        adapter.filterResults("");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.postDelayed(new Runnable() {

            @Override
            public void run() {
                if(recyclerView.getAdapter().getItemCount()>0)
                    recyclerView.scrollToPosition(0);
            }
        },500);
    }


    private void attachPressed() {
        if (PermissionsRequester.requestFileReadPermissionIfNeeded(this, PERMISSIONS_REQUEST_ATTACH_FILE)) {
            AttachDialog dialog = AttachDialog.newInstance(documentFragment);
            dialog.show(getSupportFragmentManager(), "attach_fragment");
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.error_file_upload_not_support)
                    .setTitle(getString(R.string.error_sending_file))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    private void searchPressed() {
        if (PermissionsRequester.requestFileReadPermissionIfNeeded(this, PERMISSIONS_REQUEST_ATTACH_FILE)) {
            SearchDialog dialog = SearchDialog.newInstance(documentFragment);
            dialog.show(getSupportFragmentManager(), "search_fragment");
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.error_file_upload_not_support)
                    .setTitle(getString(R.string.error_sending_file))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    private MyCloud createMyCloud(String username, String server) {
        realm.beginTransaction();
        MyCloud mycloud = MyCloud.createMyCloud(realm);
        mycloud.setUsername(username);
        mycloud.setServer(server);
        realm.commitTransaction();
        return mycloud;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("manage.own.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(myLocalBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IntentFilter intentFilter = new IntentFilter("manage.own.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(myLocalBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver myLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String ok = intent.getStringExtra("ok");
            String error = intent.getStringExtra("error");
            String url = intent.getStringExtra("url");
            long uid = intent.getLongExtra("uid",-1l);
            Log.i("DocumentActivity","Ok="+ok+",uid="+uid+",url="+url+",error="+error);
            if(error != null) {
                Toast.makeText(getApplicationContext(),error, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), ok, Toast.LENGTH_SHORT).show();
                /*if(uid > 0) {
                    DataHelper.deleteItemAsync(realm, uid);
                }*/
            }


        }
    };

}
