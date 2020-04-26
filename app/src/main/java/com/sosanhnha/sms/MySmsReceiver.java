/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sosanhnha.sms;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;

public class MySmsReceiver extends BroadcastReceiver {
    private static final String TAG = MySmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";
    private String fromName = null;
    private String fromMessage = null;
    private String strMessage = null;
    private Context myContext;
    private Boolean checkSend = false;

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context  The Context in which the receiver is running.
     * @param intent   The Intent received.
     */
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        myContext = context;
        Log.d(TAG, "onReceive: nhan thong tin");
        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;

        String format = bundle.getString("format");
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            // Check the Android version.
            boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                // Build the message to show.
                fromName = msgs[i].getOriginatingAddress();
                fromMessage = msgs[i].getMessageBody();
                strMessage = "SMS from " + fromName;
                strMessage += " :" + fromMessage + "\n";
                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + strMessage);
                String urlPost = getValue(context,"urlapi");
                String token = getValue(context,"token");
                String branchString = getValue(context,"branch");
                //nếu không có branch thì gán 1 chỗi linh tinh đỡ lỗi
                if(branchString == null || branchString == "") branchString = "fjdsljfldls2322423jrewrew";

                if(!isNullOrEmpty(branchString))
                {
                    Toast.makeText(context, branchString, Toast.LENGTH_LONG).show();
                    String alphabet[] = branchString.trim().toLowerCase().split(",");
                    List<String> list = Arrays.asList(alphabet);
                    //nếu nhận từ các branch name thì mới bắn lên server
                    if(list.contains(fromName.toLowerCase().trim()) && !isNullOrEmpty(urlPost)){

                        //kiểm tra xem tồn tại tài khoản trong tin nhắn không
                        String idString = getValue(myContext,"listId");
                        if(isNullOrEmpty(idString)) idString = "nobranchfldsllllls";
                        idString = idString.trim().toLowerCase();
                        String[] listId = idString.split(",");
                        List<String> arrAccId = Arrays.asList(listId);
                        //*
                        for (String object: arrAccId) {
                            if(idString.indexOf(object) >= 0){
                                checkSend = true;
                            }
                        }
                        //*/

                        if(checkSend){
                            //lưu tin nhắn cuối cùng vào
                            AndroidNetworking.initialize(context);
                            // Adding an Network Interceptor for Debugging purpose :
                            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                    .addInterceptor(new LoggingInterceptor())
                                    .build();
                            AndroidNetworking.initialize(context, okHttpClient);
                            AndroidNetworking.post(urlPost)
                                    //.addHeaders("Authorization", "1234")
                                    .addBodyParameter("name", fromName)
                                    .addBodyParameter("message", fromMessage)
                                    .addBodyParameter("access_token", token)
                                    .setPriority(Priority.MEDIUM)
                                    .build()
                                    .getAsJSONObject(new JSONObjectRequestListener() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            // do anything with response
                                            saveValue(myContext,"lastMessage",strMessage);
                                            Toast.makeText(myContext, "Đã gửi thành công!", Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onError(ANError error) {
                                            // handle error
                                            saveValue(myContext,"lastError",error.getMessage());
                                        }
                                    });
                        }else{
                            Toast.makeText(context, "Không thấy số tài khoản!", Toast.LENGTH_LONG).show();
                        }


                    }
                }else {
                    saveValue(context,"lastError","Bạn chưa cập nhật url api hoặc branch!<br>"+strMessage);
                    Toast.makeText(context, "Bạn chưa cập nhật url api hoặc branch!", Toast.LENGTH_LONG).show();
                }

                // // Call AsyncTask
            }
        }
    }
    public boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }
    public String getValue(Context mContext,String key) {
        SharedPreferences pref = mContext.getSharedPreferences("MyPrefSSN", 0); // 0 - for private mode
        String value = pref.getString(key, null);
        //Toast.makeText(this.mContext,value,Toast.LENGTH_LONG).show();
        return value;
    }
    public void saveValue(Context mContext,String key,String value) {
        SharedPreferences pref = mContext.getSharedPreferences("MyPrefSSN", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

}
