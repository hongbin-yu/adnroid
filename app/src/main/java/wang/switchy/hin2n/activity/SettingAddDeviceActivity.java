package wang.switchy.hin2n.activity;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.view.View;

import wang.switchy.hin2n.R;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;

public class SettingAddDeviceActivity extends BaseActivity implements View.OnClickListener {

    public static int TYPE_SETTING_ADD = 0;
    public static int TYPE_SETTING_MODIFY = 1;
    private int type = TYPE_SETTING_ADD;
    private TextInputLayout mDeviceID;


    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(mContext, getString(R.string.title_add_device));
        titleTemplate.mLeftImg.setVisibility(View.VISIBLE);
        titleTemplate.mLeftImg.setImageResource(R.drawable.titlebar_icon_return_selector);
        titleTemplate.mLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        titleTemplate.mRightImg.setVisibility(View.VISIBLE);
        titleTemplate.mRightImg.setImageResource(R.drawable.qr_scan_40);

        titleTemplate.mRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingAddDeviceActivity.this, ScanQRActivity.class);
                intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_ADD);
                startActivity(intent);
            }
        });
        return titleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {
       //setContentView(R.layout.activity_add_device);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_add_device;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }

}
