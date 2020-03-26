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
    public void onZeroTierEvent(long l, int i) {

    }
}
