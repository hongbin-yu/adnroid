package wang.switchy.hin2n.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
//import com.tencent.bugly.beta.Beta;
//import com.umeng.socialize.ShareAction;
//import com.umeng.socialize.UMShareListener;
//import com.umeng.socialize.bean.SHARE_MEDIA;
//import com.umeng.socialize.media.UMImage;
//import com.umeng.socialize.media.UMWeb;
//import com.zerotier.libzt.ZeroTier;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;
import com.zhy.m.permission.ShowRequestPermissionRationale;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import cn.pedant.SweetAlert.SweetAlertDialog;
import dajana.activity.DocumentActivity;
import dajana.activity.ui.login.LoginActivity;
import dajana.model.ZerotierStatus;
import dajana.service.MyZeroTierEventListener;
import dajana.service.ZerotierService;
import wang.switchy.hin2n.Application;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.event.ConnectingEvent;
import wang.switchy.hin2n.event.ErrorEvent;
import wang.switchy.hin2n.event.StartEvent;
import wang.switchy.hin2n.event.StopEvent;
import wang.switchy.hin2n.event.SupernodeDisconnectEvent;
import wang.switchy.hin2n.model.EdgeStatus;
import wang.switchy.hin2n.model.N2NSettingInfo;
import wang.switchy.hin2n.service.N2NService;
import wang.switchy.hin2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;
import wang.switchy.hin2n.tool.N2nTools;


public class MainActivity extends BaseActivity  implements View.OnClickListener {

    private N2NSettingModel mCurrentSettingInfo;
    private RelativeLayout mCurrentSettingItem;
    private TextView mCurrentSettingName;
    private ImageView mConnectBtn;
    private ImageView mMoreBtn;
    private ImageView mCloud;
    private ImageView mWeb;
//    private ImageView mUpload;
    private TextView mSupernodeDisconnectNote;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LinearLayout mLeftMenu;

    private static final int REQUECT_CODE_SDCARD = 1;
    private static final int REQUECT_CODE_VPN = 2;

    private static String TAG = "MAINACTIVITY";
    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(this, getString(R.string.app_name));
        titleTemplate.mRightImg.setImageResource(R.mipmap.ic_add);
        titleTemplate.mRightImg.setVisibility(View.VISIBLE);
        /*
        titleTemplate.mRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingDetailsActivity.class);
                intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_ADD);
                startActivity(intent);
            }
        });
        */
        titleTemplate.mRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddSettingActivity.class);
                intent.putExtra("type", AddSettingActivity.TYPE_SETTING_ADD);
                startActivity(intent);
            }
        });
        titleTemplate.mLeftImg.setImageResource(R.mipmap.ic_menu);
        titleTemplate.mLeftImg.setVisibility(View.VISIBLE);
        titleTemplate.mLeftImg.setVisibility(View.VISIBLE);
        titleTemplate.mLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerOpen(mLeftMenu)) {
                    mDrawerLayout.closeDrawer(mLeftMenu);
                } else {
                    mDrawerLayout.openDrawer(mLeftMenu);
                }
            }
        });

        return titleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        /*
        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.mipmap.ic_launcher, R.string.open, R.string.close) {
            //菜单打开
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            // 菜单关闭
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        mLeftMenu = (LinearLayout) findViewById(R.id.ll_menu_left);

        mConnectBtn = (ImageView) findViewById(R.id.iv_connect_btn);


        mMoreBtn = (ImageView) findViewById(R.id.iv_more_btn);

        mCloud = (ImageView) findViewById(R.id.iv_cloud);

        mWeb = (ImageView) findViewById(R.id.iv_web);

        //mUpload = (ImageView) findViewById(R.id.iv_upload);

        if (N2NService.INSTANCE == null) {
            mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
        } else {
            EdgeStatus.RunningStatus status = N2NService.INSTANCE.getCurrentStatus();
            if (status == EdgeStatus.RunningStatus.CONNECTED) {
                mConnectBtn.setImageResource(R.mipmap.ic_state_connect);
                mCloud.setVisibility(View.VISIBLE);
                mWeb.setVisibility(View.VISIBLE);
                //mUpload.setVisibility(View.VISIBLE);
            } else if (status == EdgeStatus.RunningStatus.SUPERNODE_DISCONNECT) {
                mConnectBtn.setImageResource(R.mipmap.ic_state_supernode_diconnect);
                mCloud.setVisibility(View.GONE);
                mWeb.setVisibility(View.GONE);
                //mUpload.setVisibility(View.GONE);
            } else {
                mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
                mCloud.setVisibility(View.GONE);
                mWeb.setVisibility(View.GONE);
                //mUpload.setVisibility(View.GONE);
            }
        }

        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences n2nSp = getSharedPreferences("Hin2n", MODE_PRIVATE);
                SharedPreferences ztSp = getSharedPreferences("zerotier", MODE_PRIVATE);
                if(n2nSp != null) {
                    String displayName = n2nSp.getString("displayName",null);
                    //if(displayName == null) {
                    //    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    //    startActivity(intent);
                        //return;
                    //}
                }
                if (mCurrentSettingName.getText().equals(getResources().getString(R.string.no_setting))) {
                    Toast.makeText(mContext, "no setting selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                ZerotierStatus.RunningStatus ztstatus = ZerotierService.INSTANCE == null? ZerotierStatus.RunningStatus.OFFLINE : ZerotierService.INSTANCE.getmCurrentStatus();
                if (ZerotierService.INSTANCE != null && ztstatus != ZerotierStatus.RunningStatus.OFFLINE && ztstatus != ZerotierStatus.RunningStatus.NODEDOWN) {
                    ZerotierService.INSTANCE.stop();
                    mCloud.setVisibility(View.GONE);
                    mWeb.setVisibility(View.GONE);
                    //mUpload.setVisibility(View.GONE);
                } else {
                    Intent vpnPrepareIntent = VpnService.prepare(MainActivity.this);
                    if (vpnPrepareIntent != null) {
                        startActivityForResult(vpnPrepareIntent, REQUECT_CODE_VPN);
                    } else {
                        onActivityResult(REQUECT_CODE_VPN, RESULT_OK, null);
                    }
                }

                EdgeStatus.RunningStatus status = N2NService.INSTANCE == null ? EdgeStatus.RunningStatus.DISCONNECT : N2NService.INSTANCE.getCurrentStatus();
                if (N2NService.INSTANCE != null && status != EdgeStatus.RunningStatus.DISCONNECT && status != EdgeStatus.RunningStatus.FAILED) {
                    N2NService.INSTANCE.stop();
                    mCloud.setVisibility(View.GONE);
                    mWeb.setVisibility(View.GONE);
                    //mUpload.setVisibility(View.GONE);
                } else {
                    /*
                    Intent vpnPrepareIntent = VpnService.prepare(MainActivity.this);
                    if (vpnPrepareIntent != null) {
                        startActivityForResult(vpnPrepareIntent, REQUECT_CODE_VPN);
                    } else {
                        onActivityResult(REQUECT_CODE_VPN, RESULT_OK, null);
                    }
                    */
                    //mCloud.setVisibility(View.VISIBLE);
                    //mWeb.setVisibility(View.VISIBLE);
                }
            }
        });

        mSupernodeDisconnectNote = (TextView) findViewById(R.id.tv_supernode_disconnect_note);

        mCurrentSettingItem = (RelativeLayout) findViewById(R.id.rl_current_setting_item);

        mCurrentSettingItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    startActivity(new Intent(MainActivity.this, ListActivity.class));

            }
        });
        mMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));
            }
        });

        mCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(N2NService.INSTANCE != null && N2NService.INSTANCE.getCurrentStatus() == EdgeStatus.RunningStatus.CONNECTED) {

                    Intent intent = new Intent(MainActivity.this, DocumentActivity.class);
                    Bundle bundle = new Bundle();
                    N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
                    bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
                    intent.putExtra("Setting", bundle);
                    intent.putExtra(WebViewActivity.WEB_VIEW_TYPE,WebViewActivity.TYPE_WEB_VIEW_CLOUD);
                    intent.putExtra("username",n2NSettingInfo.getUsername());
                    intent.putExtra("server",n2NSettingInfo.getLocalIP());
                    //if(ping(n2NSettingInfo.getLocalIP())) {
                    startActivity(intent);

                }
            }
        });

        mWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(N2NService.INSTANCE != null && N2NService.INSTANCE.getCurrentStatus() == EdgeStatus.RunningStatus.CONNECTED) {
                    Intent intent =new Intent(MainActivity.this, WebViewActivity.class);
                    Bundle bundle = new Bundle();
                    N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
                    bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
                    intent.putExtra("Setting", bundle);
                    intent.putExtra(WebViewActivity.WEB_VIEW_TYPE,WebViewActivity.TYPE_WEB_VIEW_WEB);
                    startActivity(intent);

                }

            }
        });
/*
        mUpload.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(N2NService.INSTANCE != null && N2NService.INSTANCE.getCurrentStatus() == EdgeStatus.RunningStatus.CONNECTED) {
                   //Intent intent = new Intent(MainActivity.this, DocumentActivity.class);
                   Intent intent = new Intent(MainActivity.this, com.wave.fileuploadservice.MainActivity.class);
                   N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
                   intent.putExtra("username", n2NSettingInfo.getUsername());
                   startActivity(intent);
               }

           }
       });
*/
        mCurrentSettingName = (TextView) findViewById(R.id.tv_current_setting_name);
        mCurrentSettingName.setText(R.string.no_setting);

        initLeftMenu();
/*
        String cameraPath = "DCIM";//getCameraFolder(Environment.getExternalStorageDirectory());
        Log.i(TAG,"Camera folder = "+cameraPath +" from "+cameraPath);
        if(cameraPath != null) {
            Intent intent = new Intent();
            intent.putExtra(FileSystemObserverService.INTENT_EXTRA_FILEPATH,cameraPath);
            FileSystemObserverService fileObserver = new FileSystemObserverService();
            fileObserver.onStartCommand(intent,0,0);
        }
*/

    }


    @Override
    public void onBackPressed() {
        Toast.makeText(this,"BackPress",Toast.LENGTH_LONG);
        //super.onBackPressed();
    }


    private void initLeftMenu() {
        TextView appVersion = (TextView) findViewById(R.id.tv_app_version);
        appVersion.setText(N2nTools.getVersionName(this));

        RelativeLayout shareItem = (RelativeLayout) findViewById(R.id.rl_share);
        shareItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("shareItem onClick~");

                if (Build.VERSION.SDK_INT >= 23) {
                    String[] permissionList = new String[]{
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.CALL_PHONE,
//                            Manifest.permission.READ_LOGS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.SET_DEBUG_APP,
//                            Manifest.permission.SYSTEM_ALERT_WINDOW,
//                            Manifest.permission.GET_ACCOUNTS,
//                            Manifest.permission.WRITE_APN_SETTINGS
                    };
//                    String[] DeniedPermissions = new String[]{};
//                    for (int i = 0; i < permissionList.length; i++) {
//                        if (ContextCompat.checkSelfPermission(MainActivity.this,
//                                permissionList[i])
//                                != PackageManager.PERMISSION_GRANTED) {
//                            DeniedPermissions
//                        }
//                    }
                    ActivityCompat.requestPermissions(MainActivity.this, permissionList, 123);


                } /*else {
                    doOnClickShareItem();

                }*/

            }
        });
        shareItem.setVisibility(View.GONE);     // @TODO 暂时不显示

        RelativeLayout contactItem = (RelativeLayout) findViewById(R.id.rl_contact);
        contactItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                /*
                boolean b = joinQQGroup("5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo");
                if (!b) {
                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_CONTACT);
                    startActivity(intent);
                }*/
            }
        });

        RelativeLayout feedbackItem = (RelativeLayout) findViewById(R.id.rl_feedback);
        feedbackItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_FEEDBACK);
                startActivity(intent);
                */
            }
        });

        RelativeLayout checkUpdateItem = (RelativeLayout) findViewById(R.id.rl_check_update);


        RelativeLayout aboutItem = (RelativeLayout) findViewById(R.id.rl_about);
        aboutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_ABOUT);
                startActivity(intent);
            }
        });
    }


    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUECT_CODE_VPN && resultCode == RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, N2NService.class);
            Bundle bundle = new Bundle();
            N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
            bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
            intent.putExtra("Setting", bundle);

            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"Resume");
        SharedPreferences n2nSp = getSharedPreferences("Hin2n", MODE_PRIVATE);
        if(n2nSp !=null) {
            Long currentSettingId = n2nSp.getLong("current_setting_id", -1);
            if (currentSettingId != -1) {
                mCurrentSettingInfo = Application.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
                if (mCurrentSettingInfo != null) {
                    mCurrentSettingName.setText(mCurrentSettingInfo.getName());
                } else {
                    mCurrentSettingName.setText(R.string.no_setting);
                }

                mConnectBtn.setVisibility(View.VISIBLE);
                mSupernodeDisconnectNote.setVisibility(View.GONE);
                if (N2NService.INSTANCE == null) {
                    mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
                } else {
                    EdgeStatus.RunningStatus status = N2NService.INSTANCE.getCurrentStatus();
                    if (status == EdgeStatus.RunningStatus.CONNECTED) {
                        mConnectBtn.setImageResource(R.mipmap.ic_state_connect);
                        mCloud.setVisibility(View.VISIBLE);
                        mWeb.setVisibility(View.VISIBLE);
                        //mUpload.setVisibility(View.VISIBLE);
                    } else if (status == EdgeStatus.RunningStatus.SUPERNODE_DISCONNECT) {
                        mConnectBtn.setImageResource(R.mipmap.ic_state_supernode_diconnect);
                        mSupernodeDisconnectNote.setVisibility(View.VISIBLE);
                        mCloud.setVisibility(View.GONE);
                        mWeb.setVisibility(View.GONE);
                        //mUpload.setVisibility(View.GONE);
                    } else {
                        mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
                        mCloud.setVisibility(View.GONE);
                        mWeb.setVisibility(View.GONE);
                        //mUpload.setVisibility(View.GONE);
                    }
                }
            }
        }

    }

    @Override
    protected void onPause() {
            Log.d(TAG,"Pause");
            super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartEvent(StartEvent event) {
        mConnectBtn.setVisibility(View.VISIBLE);
        mConnectBtn.setImageResource(R.mipmap.ic_state_connect);
        mSupernodeDisconnectNote.setVisibility(View.GONE);
        mCloud.setVisibility(View.VISIBLE);
        mWeb.setVisibility(View.VISIBLE);
        //mUpload.setVisibility(View.VISIBLE);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopEvent(StopEvent event) {
        mConnectBtn.setVisibility(View.VISIBLE);
        mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
        mSupernodeDisconnectNote.setVisibility(View.GONE);
        mCloud.setVisibility(View.GONE);
        mWeb.setVisibility(View.GONE);
        //mUpload.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        mConnectBtn.setVisibility(View.VISIBLE);
        mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
        mSupernodeDisconnectNote.setVisibility(View.GONE);
        mCloud.setVisibility(View.GONE);
        mWeb.setVisibility(View.GONE);
        //mUpload.setVisibility(View.GONE);
        Toast.makeText(mContext, getString(R.string.toast_connect_failed), Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectingEvent(ConnectingEvent event) {
        mConnectBtn.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSupernodeDisconnectEvent(SupernodeDisconnectEvent event) {
        mConnectBtn.setVisibility(View.VISIBLE);
        mConnectBtn.setImageResource(R.mipmap.ic_state_supernode_diconnect);
        mSupernodeDisconnectNote.setVisibility(View.VISIBLE);
        mCloud.setVisibility(View.GONE);
        mWeb.setVisibility(View.GONE);
        //mUpload.setVisibility(View.GONE);
        Toast.makeText(mContext, getString(R.string.toast_disconnect_and_retry), Toast.LENGTH_SHORT).show();
    }

    /****************
     * 发起添加群流程。群号：手机版n2n(hin2n)交流群(769731491) 的 key 为： 5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo
     * 调用 joinQQGroup(5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo) 即可发起手Q客户端申请加群 手机版n2n(hin2n)交流群(769731491)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }


    /**
     * check permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            for (int i = 0; i < grantResults.length; i++) {
//                Log.e("zhangbzshare", "permission[" + i + "] = " + permissions[i] + ",grantResult[" + i + "] = " + grantResults[i]);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //doOnClickShareItem();
        }

    }


    @PermissionGrant(REQUECT_CODE_SDCARD)
    public void requestSdcardSuccess() {
        Toast.makeText(this, "GRANT ACCESS SDCARD!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUECT_CODE_SDCARD)
    public void requestSdcardFailed() {
        Toast.makeText(this, "DENY ACCESS SDCARD!", Toast.LENGTH_SHORT).show();
//        finish();
    }

    @ShowRequestPermissionRationale(REQUECT_CODE_SDCARD)
    public void ShowRequestPermissionRationale() {
        Toast.makeText(this, "ShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
        Logger.d("ShowRequestPermissionRationale");

//        mConnectBtn.setImageResource(R.mipmap.ic_state_supernode_diconnect);
//        mSupernodeDisconnectNote.setVisibility(View.VISIBLE);

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        sweetAlertDialog
                .setTitleText("I need permission!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        MPermissions.requestPermissions(MainActivity.this, REQUECT_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    }
                })
                .show();
    }

    private boolean ping(String ip) {
        Toast.makeText(mContext,"ping "+ip,Toast.LENGTH_LONG).show();
        Process p1 = null;
        try {
            p1 = Runtime.getRuntime().exec("ping -c 2 "+ip);
            int returnVal = p1.waitFor();
            Scanner scanner = new Scanner(p1.getInputStream());
            p1.waitFor();
            ArrayList<String> strings = new ArrayList<>();
            String data = "";
            //
            while (scanner.hasNextLine()) {
                String string = scanner.nextLine();
                data = data + string + "\n";
                strings.add(string);
            }
            Log.d(TAG,data);
            Toast.makeText(mContext,data,Toast.LENGTH_LONG).show();

            boolean reachable = (returnVal==0);
            return reachable;
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        } catch (InterruptedException e) {
            Log.e(TAG,e.getMessage());
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this,"view id="+view.getId(),Toast.LENGTH_LONG);
        switch (view.getId()) {
            case R.id.iv_connect_btn:
                connectPress();
                break;
            case R.id.rl_current_setting_item:
                startActivity(new Intent(MainActivity.this, ListActivity.class));
                break;
            case R.id.more_setting_check_box:
                startActivity(new Intent(MainActivity.this, ListActivity.class));
                break;
            case R.id.iv_upload:
                if(N2NService.INSTANCE != null && N2NService.INSTANCE.getCurrentStatus() == EdgeStatus.RunningStatus.CONNECTED) {

                    Intent intent = new Intent(MainActivity.this, com.wave.fileuploadservice.MainActivity.class);
                    N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
                    intent.putExtra("username", n2NSettingInfo.getUsername());
                    startActivity(intent);
                }
                break;
            case R.id.iv_cloud:
                if(N2NService.INSTANCE != null && N2NService.INSTANCE.getCurrentStatus() == EdgeStatus.RunningStatus.CONNECTED) {
                    Intent intent =new Intent(MainActivity.this, WebViewActivity.class);
                    Bundle bundle = new Bundle();
                    N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
                    bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
                    intent.putExtra("Setting", bundle);
                    intent.putExtra(WebViewActivity.WEB_VIEW_TYPE,WebViewActivity.TYPE_WEB_VIEW_CLOUD);
                    if(ping(n2NSettingInfo.getLocalIP())) {
                        startActivity(intent);
                    }else {
                        Log.d(TAG,n2NSettingInfo.getLocalIP()+" is not reachable!");
                        Toast.makeText(mContext,n2NSettingInfo.getLocalIP()+" is not reachable!",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.iv_web:
                if(N2NService.INSTANCE != null && N2NService.INSTANCE.getCurrentStatus() == EdgeStatus.RunningStatus.CONNECTED) {
                    Intent intent =new Intent(MainActivity.this, WebViewActivity.class);
                    Bundle bundle = new Bundle();
                    N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
                    bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
                    intent.putExtra("Setting", bundle);
                    intent.putExtra(WebViewActivity.WEB_VIEW_TYPE,WebViewActivity.TYPE_WEB_VIEW_WEB);
                    startActivity(intent);
                }
                break;
        }
    }

    private void connectPress() {
        if (mCurrentSettingName.getText().equals(getResources().getString(R.string.no_setting))) {
            Toast.makeText(mContext, "no setting selected", Toast.LENGTH_SHORT).show();
            return;
        }

        EdgeStatus.RunningStatus status = N2NService.INSTANCE == null ? EdgeStatus.RunningStatus.DISCONNECT : N2NService.INSTANCE.getCurrentStatus();
        if (N2NService.INSTANCE != null && status != EdgeStatus.RunningStatus.DISCONNECT && status != EdgeStatus.RunningStatus.FAILED) {
            N2NService.INSTANCE.stop();
            mCloud.setVisibility(View.GONE);
            mWeb.setVisibility(View.GONE);
            //mUpload.setVisibility(View.GONE);
        } else {
            /*
            Intent vpnPrepareIntent = VpnService.prepare(MainActivity.this);
            if (vpnPrepareIntent != null) {
                startActivityForResult(vpnPrepareIntent, REQUECT_CODE_VPN);
            } else {
                onActivityResult(REQUECT_CODE_VPN, RESULT_OK, null);
            }*/
            mCloud.setVisibility(View.VISIBLE);
            mWeb.setVisibility(View.VISIBLE);
        }
    }
/*
    private ServerSocket serverSocket;

    Handler updateConversationHandler;

    Thread serverThread = null;

    //private TextView text;

    public static final int SERVERPORT = 6000;
    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        }

    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            //text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.dialog_add_succeed))
                    .setContentText("message:"+msg)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            finish();
                            sweetAlertDialog.dismiss();
                        }
                    }).show();

        }
    }
    */
    static void sleep(int ms)
    {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }
}
