package wang.switchy.hin2n.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orhanobut.logger.Logger;
import com.wang.avi.AVLoadingIndicatorView;
import com.wave.fileuploadservice.MainActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import cn.pedant.SweetAlert.SweetAlertDialog;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.model.N2NSettingInfo;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.changeAction;

/**
 * Created by janiszhang on 2018/6/25.
 */

public class WebViewActivity extends BaseActivity {

    public static final String WEB_VIEW_TYPE = "web_view_type";

    public static final int TYPE_WEB_VIEW_ABOUT = 0;
    public static final int TYPE_WEB_VIEW_FEEDBACK = 1;
    public static final int TYPE_WEB_VIEW_SHARE = 2;
    public static final int TYPE_WEB_VIEW_CONTACT = 3;
    public static final int TYPE_WEB_VIEW_CLOUD= 4;
    public static final int TYPE_WEB_VIEW_WEB= 5;

    public static final String ABOUT_URL = "https://dajana.cn/";
    public static final String SHARE_URL = "https://github.com/switch-iot/hin2n/wiki/Welcome-to-hin2n";
    public static final String CONTACT_URL = "https://github.com/switch-iot/hin2n/wiki/Feedback-&-Contact-Us";
    //public static final String FEEDBACK_URL = "https://support.qq.com/products/38470";
    public static String TAG = "WebViewActivity";
    N2NSettingInfo n2NSettingInfo = null;
    private WebView mWebView;
    private AVLoadingIndicatorView mLoadingView;
    private CommonTitleTemplate mCommonTitleTemplate;
    private ValueCallback<Uri> mUploadMessage;

    @Override
    protected BaseTemplate createTemplate() {
        mCommonTitleTemplate = new CommonTitleTemplate(mContext, "About");

        mCommonTitleTemplate.mLeftImg.setVisibility(View.VISIBLE);
        mCommonTitleTemplate.mLeftImg.setImageResource(R.drawable.titlebar_icon_return_selector);
        mCommonTitleTemplate.mLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    finish();
                }
            }
        });

        mCommonTitleTemplate.mRightImg.setVisibility(View.VISIBLE);
        mCommonTitleTemplate.mRightImg.setImageResource(R.drawable.error_center_x);
        mCommonTitleTemplate.mRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    finish();
            }
        });


        return mCommonTitleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        mLoadingView = (AVLoadingIndicatorView) findViewById(R.id.loading_view);

        mWebView = (WebView) findViewById(R.id.web_view);

        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.endsWith("upload-from-computer.php")) {
                    /*
                    Intent intent =new Intent(WebViewActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    String cookies = CookieManager.getInstance().getCookie(url);
                    intent.putExtra("Cookie",cookies);
                    intent.putExtra("username",n2NSettingInfo.getUsername());
                    bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
                    intent.putExtra("Setting", bundle);
                    startActivity(intent);
                    */
                    Intent i = new Intent(Intent.ACTION_VIEW);

                    i.setData(Uri.parse(url));

                    startActivity(i);
                    Log.d(TAG,url);

                    //return;
                }else
                    view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG,"started:"+url);
                if(url.endsWith("upload-from-computer.php")) {
                    /*
                    Intent intent =new Intent(WebViewActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    String cookies = CookieManager.getInstance().getCookie(url);
                    intent.putExtra("Cookie",cookies);
                    bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
                    intent.putExtra("Setting", bundle);
                    startActivity(intent);
                    */
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    String cookies = CookieManager.getInstance().getCookie(url);
                    i.putExtra("Cookie",cookies);
                    startActivity(i);
                    Log.d(TAG,url);

                    return;
                }
                super.onPageStarted(view, url, favicon);
                mLoadingView.setVisibility(View.VISIBLE);

                if (mWebView != null) {
                    mWebView.setVisibility(View.GONE);

                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG,"Finished:"+url);
                mLoadingView.setVisibility(View.GONE);
                if (mWebView != null) {
                    mWebView.setVisibility(View.VISIBLE);
                    if(mWebView.getUrl().endsWith("/cloud/") || mWebView.getUrl().endsWith("/index.php"))
                        mWebView.loadUrl("javascript:(function() {document.getElementById('username').value = '"+n2NSettingInfo.getUsername()+"'; ;})()");
                    if(url.endsWith("process.php?do=login")) {
                        Log.d(TAG,"login");
                    }
                }

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }


        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            }

        );


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        int webViewType = getIntent().getIntExtra(WEB_VIEW_TYPE, -1);
        Bundle bundle = getIntent().getBundleExtra("Setting");

        switch (webViewType) {
            case TYPE_WEB_VIEW_ABOUT:
                mCommonTitleTemplate.setTitleText(R.string.about);
                mWebView.loadUrl(ABOUT_URL);
                break;
            case TYPE_WEB_VIEW_FEEDBACK:
                //mCommonTitleTemplate.setTitleText("Feedback");
                //mWebView.loadUrl(FEEDBACK_URL);
                break;
            case TYPE_WEB_VIEW_SHARE:
                //mCommonTitleTemplate.setTitleText("Share");
                //mWebView.loadUrl(SHARE_URL);
                break;
            case TYPE_WEB_VIEW_CONTACT:
                mCommonTitleTemplate.setTitleText(R.string.contacts);
                mWebView.loadUrl(CONTACT_URL);
                break;
            case TYPE_WEB_VIEW_CLOUD:
                mCommonTitleTemplate.setTitleText(R.string.you_cloud);
                if(bundle !=null) {
                    n2NSettingInfo = bundle.getParcelable("n2nSettingInfo");
                    if(n2NSettingInfo != null) {
                        Log.d(TAG,"loading:http://"+n2NSettingInfo.getLocalIP()+"/cloud/");
                        mWebView.loadUrl("http://"+n2NSettingInfo.getLocalIP()+"/cloud/");
                    }
                }else {
                    new SweetAlertDialog(WebViewActivity.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(R.string.bundle_not_found)
                            .setContentText(getString(R.string.something_went_wrong))
                            .show();
                }
                //mWebView.loadUrl(CONTACT_URL);
                break;
            case TYPE_WEB_VIEW_WEB:
                mCommonTitleTemplate.setTitleText(R.string.you_web);
                if(bundle !=null) {
                    n2NSettingInfo = bundle.getParcelable("n2nSettingInfo");
                    if(n2NSettingInfo != null) {
                        mWebView.loadUrl("http://"+n2NSettingInfo.getLocalIP()+"/wordpress/");
                    }
                }else {
                    new SweetAlertDialog(WebViewActivity.this,SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(R.string.bundle_not_found)
                            .setContentText(getString(R.string.something_went_wrong))
                            .show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();

            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_about;
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Log.e(TAG,mUploadMessage.toString());
    }


}
