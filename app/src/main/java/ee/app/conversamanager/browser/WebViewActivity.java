package ee.app.conversamanager.browser;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;
import ee.app.conversamanager.R;
import ee.app.conversamanager.extendables.BaseActivity;
import ee.app.conversamanager.view.RegularTextView;

/**
 * Created by edgargomez on 11/24/16.
 */

public class WebViewActivity extends BaseActivity implements View.OnClickListener {

    WebView mWebView;
    String url;

    public static final String EXTRA_URL =
            "com.hitherejoe.tabby.ui.activity.WebViewActivity.EXTRA_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWebView = (WebView) findViewById(R.id.web_view);
        url = getIntent().getStringExtra(EXTRA_URL);
        mWebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl(url);
        initialization();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void initialization() {
        super.initialization();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RegularTextView mTitleTextView = (RegularTextView) toolbar.findViewById(R.id.rtvUrl);
        FrameLayout mBackButton = (FrameLayout) toolbar.findViewById(R.id.flBack);
        ImageButton mIbBackButton = (ImageButton) toolbar.findViewById(R.id.ibBack);
        mIbBackButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mTitleTextView.setText(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibBack:
            case R.id.flBack:
                onBackPressed();
                break;
        }
    }
}