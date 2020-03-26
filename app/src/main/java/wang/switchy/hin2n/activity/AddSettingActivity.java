package wang.switchy.hin2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zerotier.libzt.ZeroTier;
import com.zerotier.libzt.ZeroTierEventListener;

import wang.switchy.hin2n.R;
import wang.switchy.hin2n.storage.db.base.model.ZerotierSettingModel;

public class AddSettingActivity extends AppCompatActivity implements ZeroTierEventListener {
    public static int TYPE_SETTING_ADD = 0;
    public static int TYPE_SETTING_MODIFY = 1;
    private int type = TYPE_SETTING_ADD;
    private ZerotierSettingModel mZerotierSettingModel;
    private EditText mDeviceID;
    private Button mFetchBtn;
    private SharedPreferences mHin2nSp;
    private SharedPreferences.Editor mHin2nEdit;

    // Debug log tag.
    private static final String TAG_HTTP_URL_CONNECTION = "HTTP_URL_CONNECTION";
    // Child thread sent message type value to activity main thread Handler.
    private static final int REQUEST_CODE_SHOW_RESPONSE_TEXT = 1;

    // The key of message stored server returned data.
    private static final String KEY_RESPONSE_TEXT = "KEY_RESPONSE_TEXT";
    private static final String KEY_RESPONSE_CONTENTTYPE = "KEY_RESPONSE_CONTENTTYPE";
    private static final String KEY_REQ_URL = "KEY_REQ_URL";


    // Request method GET. The value must be uppercase.
    private static final String REQUEST_METHOD_GET = "GET";
    // This handler used to listen to child thread show return page html text message and display those text in responseTextView.
    private Handler uiUpdater = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddSettingActivity.this, QRScanActivity.class);
                intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_ADD);
                startActivity(intent);
            }
        });

        mDeviceID = (EditText) findViewById(R.id.editText_device_id);
        mFetchBtn = (Button) findViewById(R.id.button_fetch);
        mFetchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetch();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected boolean fetch() {
        mDeviceID = findViewById(R.id.editText_device_id);


        return true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onZeroTierEvent(long id, int eventCode) {
        if (eventCode == ZeroTier.EVENT_NODE_UP) {
            // Safe to ignore this callback
            //System.out.println("EVENT_NODE_UP");
        }
        if (eventCode == ZeroTier.EVENT_NODE_ONLINE) {
            // The core service is running properly and can join networks now
            System.out.println("EVENT_NODE_ONLINE: nodeId=" + Long.toHexString(id));
        }
        if (eventCode == ZeroTier.EVENT_NODE_OFFLINE) {
            // Network does not seem to be reachable by any available strategy
            System.out.println("EVENT_NODE_OFFLINE");
        }
        if (eventCode == ZeroTier.EVENT_NODE_DOWN) {
            // Called when the node is shutting down
            System.out.println("EVENT_NODE_DOWN");
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
        }
        if (eventCode == ZeroTier.EVENT_NETWORK_READY_IP6) {
            // We have at least one assigned address and we've received a network configuration
            System.out.println("ZTS_EVENT_NETWORK_READY_IP6: nwid=" + Long.toHexString(id));

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
}
