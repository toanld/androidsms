package com.sosanhnha.sms;

import android.app.Activity;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class WebAppInterface {

    private Activity mContext;
    private WebView webView;
    private static final int LOCATION_PERMISSION_ID = 1001;
    private static final int RC_SIGN_IN = 9001;


    public WebAppInterface(Activity c, WebView w) {
        this.mContext = c;
        this.webView = w;
    }

    @JavascriptInterface
    public String getValue(String key) {
        SharedPreferences pref = this.mContext.getSharedPreferences("MyPrefSSN", 0); // 0 - for private mode
        String value = pref.getString(key, null);
        //Toast.makeText(this.mContext,value,Toast.LENGTH_LONG).show();
        return value;
    }

    @JavascriptInterface
    public void saveValue(String key,String value) {
        SharedPreferences pref = this.mContext.getSharedPreferences("MyPrefSSN", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
        Toast.makeText(this.mContext,"Lưu thành công!",Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void alert(String toast) {
        String value = getValue("key");
        Toast.makeText(this.mContext,value,Toast.LENGTH_LONG).show();
    }

}
