package com.turen.reflektd;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    WebView myWebView;
    String strSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = (WebView) findViewById(R.id.webview);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},  MY_PERMISSIONS_REQUEST_LOCATION);

        myWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        myWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setAppCacheEnabled(true);
        myWebView.getSettings().setDatabaseEnabled(true);
        myWebView.getSettings().setGeolocationEnabled(true);

        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        myWebView.getSettings().setGeolocationDatabasePath( getApplicationContext().getFilesDir().getPath() );

        Intent intent = this.getIntent();

        if (intent != null && intent.getExtras() != null && intent.getExtras().getBoolean("fromNotification")){
                String conversationID = intent.getExtras().getString("CONVERSATIONID");
                myWebView.loadUrl("https://reflektd.com/messenger/" + conversationID);
        }else{
            myWebView.loadUrl("https://reflektd.com/");
        }

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);
        mDialog.show();

        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                if(mDialog!=null && mDialog.isShowing())
                {
                    mDialog.hide();

                    if (url.equals("https://reflektd.com/account") ){

                        CookieSyncManager.getInstance().sync();
                        // Get the cookie from cookie jar.
                        String cookie = CookieManager.getInstance().getCookie(url);

                        if (cookie == null) {
                            return;
                        }
                        // Cookie is a string like NAME=VALUE [; NAME=VALUE]
                        String[] pairs = cookie.split(";");
                        for (int i = 0; i < pairs.length; ++i) {
                            String[] parts = pairs[i].split("=", 2);
                            // If token is found, return it to the calling activity.
                            if (parts.length == 2 && parts[0].equalsIgnoreCase("PHPSESSID")) {

                                strSession = parts[1];

                            }
                        }

                        registerToken();
                    }

                }
            }

        });
    }

    public void registerToken(){

        // Get token
        String token = FirebaseInstanceId.getInstance().getToken();

        // Log and toast
        Log.d("Tag", token);

        AsyncHttpClient client = new AsyncHttpClient();

        client.addHeader("Cookie", "PHPSESSID=" + strSession);

        RequestParams params = new RequestParams();
        params.put("android_token", token);

        client.post("https://reflektd.com/api/v1/push-notifications.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                Log.i("xml","StatusCode : "+i);
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                Log.i("xml","Sending failed");
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                Log.i("xml","Progress : "+bytesWritten);
            }
        });

    }
}
