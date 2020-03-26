package dajana.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.zerotier.libzt.ZeroTier;
import com.zerotier.libzt.ZeroTierEventListener;
import com.zerotier.libzt.ZeroTierSocketAddress;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import dajana.model.ZerotierStatus;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.activity.MainActivity;
import wang.switchy.hin2n.event.ErrorEvent;
import wang.switchy.hin2n.event.StartEvent;
import wang.switchy.hin2n.event.StopEvent;
import wang.switchy.hin2n.event.SupernodeDisconnectEvent;
import wang.switchy.hin2n.model.EdgeStatus;

import static dajana.model.ZerotierStatus.RunningStatus.OFFLINE;
import static dajana.model.ZerotierStatus.RunningStatus.ONLINE;
import static dajana.model.ZerotierStatus.RunningStatus.NODEDOWN;
import static dajana.model.ZerotierStatus.RunningStatus.ISREADY;
import static wang.switchy.hin2n.model.EdgeStatus.RunningStatus.DISCONNECT;

public class ZerotierService extends VpnService implements ZeroTierEventListener {

    public static ZerotierService INSTANCE;

    private String nwid;
    private ZerotierStatus.RunningStatus mLastStatus = ZerotierStatus.RunningStatus.OFFLINE;
    private ZerotierStatus.RunningStatus mCurrentStatus = ZerotierStatus.RunningStatus.OFFLINE;

    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private static final int sNotificationId = 1;
    private NotificationManager mNotificationManager;

    @Override
    public void onZeroTierEvent(long id, int eventCode) {
        if (eventCode == ZeroTier.EVENT_NODE_UP) {
            // Safe to ignore this callback
            //System.out.println("EVENT_NODE_UP");
        }
        if (eventCode == ZeroTier.EVENT_NODE_ONLINE) {
            // The core service is running properly and can join networks now
            System.out.println("EVENT_NODE_ONLINE: nodeId=" + Long.toHexString(id));
            Toast.makeText(INSTANCE, "EVENT_NODE_ONLINE: nodeId=" + Long.toHexString(id), Toast.LENGTH_SHORT).show();
            nwid = Long.toHexString(id);
            mLastStatus = ZerotierStatus.RunningStatus.ONLINE;
        }
        if (eventCode == ZeroTier.EVENT_NODE_OFFLINE) {
            // Network does not seem to be reachable by any available strategy
            System.out.println("EVENT_NODE_OFFLINE");
            mLastStatus = ZerotierStatus.RunningStatus.OFFLINE;

        }
        if (eventCode == ZeroTier.EVENT_NODE_DOWN) {
            // Called when the node is shutting down
            System.out.println("EVENT_NODE_DOWN");
            mLastStatus = ZerotierStatus.RunningStatus.NODEDOWN;
        }
        if (eventCode == ZeroTier.EVENT_NODE_IDENTITY_COLLISION) {
            // Another node with this identity already exists
            System.out.println("EVENT_NODE_IDENTITY_COLLISION");
        }
        if (eventCode == ZeroTier.EVENT_NODE_UNRECOVERABLE_ERROR) {
            // Try again
            System.out.println("EVENT_NODE_UNRECOVERABLE_ERROR");
        }
        if (eventCode == ZeroTier.EVENT_NODE_NORMAL_TERMINATION) {
            // Normal closure
            System.out.println("EVENT_NODE_NORMAL_TERMINATION");
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_READY_IP4) {
            // We have at least one assigned address and we've received a network configuration
            System.out.println("ZTS_EVENT_NETWORK_READY_IP4: nwid=" + Long.toHexString(id));

            mCurrentStatus = ZerotierStatus.RunningStatus.ISREADY;
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_READY_IP6) {
            // We have at least one assigned address and we've received a network configuration
            System.out.println("ZTS_EVENT_NETWORK_READY_IP6: nwid=" + Long.toHexString(id));
            mCurrentStatus = ZerotierStatus.RunningStatus.ISREADY;
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_DOWN) {
            // Someone called leave(), we have no assigned addresses, or otherwise cannot use this interface
            System.out.println("EVENT_NETWORK_DOWN: nwid=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_REQUESTING_CONFIG) {
            // Waiting for network configuration
            System.out.println("EVENT_NETWORK_REQUESTING_CONFIG: nwid=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_OK) {
            // Config received and this node is authorized for this network
            System.out.println("EVENT_NETWORK_OK: nwid=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_ACCESS_DENIED) {
            // You are not authorized to join this network
            System.out.println("EVENT_NETWORK_ACCESS_DENIED: nwid=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_NOT_FOUND) {
            // The virtual network does not exist
            System.out.println("EVENT_NETWORK_NOT_FOUND: nwid=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_CLIENT_TOO_OLD) {
            // The core version is too old
            System.out.println("EVENT_NETWORK_CLIENT_TOO_OLD: nwid=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_PEER_P2P) {
            System.out.println("EVENT_PEER_P2P: id=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_PEER_RELAY) {
            System.out.println("EVENT_PEER_RELAY: id=" + Long.toHexString(id));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void stop() {
        mLastStatus = mCurrentStatus = OFFLINE;
        showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);

        try {
            if (mParcelFileDescriptor != null) {
                mParcelFileDescriptor.close();
                mParcelFileDescriptor = null;
            }
        } catch (IOException e) {
            EventBus.getDefault().post(new ErrorEvent());
            return;
        }

        EventBus.getDefault().post(new StopEvent());
    }

    public void reportEdgeStatus(ZerotierStatus status) {
        mLastStatus = mCurrentStatus;
        mCurrentStatus = status.runningStatus;

        if (mLastStatus == mCurrentStatus) {
            return;
        }

        switch (status.runningStatus) {
            case ONLINE:
            case ISREADY:
                EventBus.getDefault().post(new StartEvent());
                if (mLastStatus == ISREADY) {
                    showOrRemoveNotification(CMD_UPDATE_NOTIFICATION);
                }
                break;
            case NODEDOWN:
                showOrRemoveNotification(CMD_ADD_NOTIFICATION);
                EventBus.getDefault().post(new SupernodeDisconnectEvent());
                break;
            case OFFLINE:
                EventBus.getDefault().post(new StopEvent());
                if (mLastStatus == OFFLINE) {
                    showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);
                }
                break;
            case DENIED:
                EventBus.getDefault().post(new StopEvent());
                if (mLastStatus == NODEDOWN) {
                    showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);
                }
                break;
            default:
                break;
        }
    }

    private static final int CMD_REMOVE_NOTIFICATION = 0;
    private static final int CMD_ADD_NOTIFICATION = 1;
    private static final int CMD_UPDATE_NOTIFICATION = 2;
    //supernode连接断开 supernode连接恢复 连接断开/失败--清除通知栏
    private void showOrRemoveNotification(int cmd) {
        switch (cmd) {
            case CMD_REMOVE_NOTIFICATION:
                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }
                mNotificationManager.cancel(sNotificationId);
                break;
            case CMD_ADD_NOTIFICATION:
                Intent mainIntent = new Intent(this, MainActivity.class);
                PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_default))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_state_supernode_diconnect))
                        .setColor(ContextCompat.getColor(this, R.color.colorSupernodeDisconnect))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notify_disconnect))
                        .setFullScreenIntent(null, false)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(false)
                        .setContentIntent(mainPendingIntent);

                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_NO_CLEAR;
                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }
                mNotificationManager.notify(sNotificationId, notification);
                break;
            case CMD_UPDATE_NOTIFICATION:
                Intent mainIntent1 = new Intent(this, MainActivity.class);
                PendingIntent mainPendingIntent1 = PendingIntent.getActivity(this, 0, mainIntent1, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_default))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notify_reconnected))
                        .setFullScreenIntent(null, false)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(true)
                        .setContentIntent(mainPendingIntent1);

                Notification notification2 = builder2.build();
                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }
                mNotificationManager.notify(sNotificationId, notification2);
                break;
            default:
                break;
        }
    }

    public ZerotierStatus.RunningStatus getmCurrentStatus() {
        return mCurrentStatus;
    }

}
