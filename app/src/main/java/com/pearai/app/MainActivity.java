package com.pearai.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
    private WebView webView;
    private ProgressBar progressBar;
    private static final String URL = "http://127.0.0.1:5808";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(-1, 6));
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        layout.addView(progressBar);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        layout.addView(webView);
        setContentView(layout);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " PearAI/1.0");

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new PearWebClient());
        webView.setWebChromeClient(new PearChromeClient());
        webView.loadUrl(URL);
    }

    private class PearWebClient extends WebViewClient {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
        }
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
        }
        public void onReceivedError(WebView view, WebResourceRequest req,
                                    android.webkit.WebResourceError err) {
            if (req.isForMainFrame()) {
                view.loadDataWithBaseURL(null,
                    "<html><body style='background:#0a0a0f;color:#e2e2f0;display:flex;" +
                    "align-items:center;justify-content:center;height:100vh;" +
                    "font-family:sans-serif;text-align:center;padding:20px'>" +
                    "<div><h2>无法连接</h2><p>请先启动 Termux 服务</p>" +
                    "<button onclick='location.reload()' style='background:#6366f1;" +
                    "color:#fff;border:none;padding:10px 24px;border-radius:8px;" +
                    "font-size:15px'>重试</button></div></body></html>",
                    "text/html", "UTF-8", null);
            }
        }
    }

    private ValueCallback<Uri[]> filePathCallback;

    private class PearChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int p) {
            progressBar.setProgress(p);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                         FileChooserParams params) {
            filePathCallback = callback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "选择图片"), 1);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[Math.min(count, 8)];
                    for (int i = 0; i < results.length; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
