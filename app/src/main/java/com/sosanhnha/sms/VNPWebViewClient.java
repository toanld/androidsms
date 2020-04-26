package com.sosanhnha.sms;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class VNPWebViewClient extends WebViewClient {
    private String mUrl;
    private boolean mError;
    private WebViewClientListener mWebViewClientListener;
    private Context mContext;
    //ProgressBar mProgressBar;

    public VNPWebViewClient(Context context) {
        //this.mProgressDialog = new ProgressDialog(context);
        //mProgressDialog.setIndeterminate(true);
        mContext = context;
        //mProgressBar = ProgressBar;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        //mProgressBar.setVisibility(View.VISIBLE);
        view.getSettings().setAllowFileAccess(true);
        view.getSettings().setAppCacheEnabled(true);
        view.getSettings().setJavaScriptEnabled(true);

        this.mError = false;

        if (mWebViewClientListener != null)
            mWebViewClientListener.onNavigateStarted(url);

        if (url.startsWith("http") || url.startsWith("https")) {
            this.mUrl = url;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.e("Chien", "onPageFinished url ="+ url);
        super.onPageFinished(view, url);
        if (!mError) {
            view.setVisibility(View.VISIBLE);

            if (mWebViewClientListener != null)
                mWebViewClientListener.onNavigateCompleted(url);
        }

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        if (mWebViewClientListener != null)
            mWebViewClientListener.onLoadResource(url);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        this.mError = true;

        if (mWebViewClientListener != null)
            mWebViewClientListener.onError(failingUrl);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());

    }

    public String getUrl() {
        return mUrl;
    }

    public void setWebViewClientListener(
            WebViewClientListener listener) {
        this.mWebViewClientListener = listener;
    }

    public interface WebViewClientListener {
        void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults);

        void onError(String url);
        void onNavigateStarted(String url);
        void onNavigateCompleted(String url);
        void onLoadResource(String url);
    }
}
