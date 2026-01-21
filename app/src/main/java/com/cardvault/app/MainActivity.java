package com.cardvault.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import android.graphics.Color;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class MainActivity extends Activity {

    private WebView webView;
    private PermissionRequest mPermissionRequest;
    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen with dark status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.parseColor("#0a0a0f"));
        getWindow().setNavigationBarColor(Color.parseColor("#0a0a0f"));
        
        // Create WebView
        webView = new WebView(this);
        setContentView(webView);
        
        // Configure WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        // Set clients
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                // Обработка запроса разрешений от WebView (камера)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Проверяем, запрашивается ли камера
                        for (String resource : request.getResources()) {
                            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
                                // Сохраняем запрос и проверяем Android permission
                                mPermissionRequest = request;
                                checkCameraPermission();
                                return;
                            }
                        }
                        // Если не камера — отклоняем
                        request.deny();
                    }
                });
            }
        });
        
        // Set background color
        webView.setBackgroundColor(Color.parseColor("#0a0a0f"));
        
        // Load app
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // Проверка и запрос разрешения камеры
    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем разрешение у пользователя
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            } else {
                // Разрешение уже есть — предоставляем доступ WebView
                grantWebViewCameraPermission();
            }
        } else {
            // Android < 6.0 — разрешение уже в манифесте
            grantWebViewCameraPermission();
        }
    }

    // Предоставление разрешения камеры WebView
    private void grantWebViewCameraPermission() {
        if (mPermissionRequest != null) {
            mPermissionRequest.grant(mPermissionRequest.getResources());
            mPermissionRequest = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Пользователь разрешил камеру
                grantWebViewCameraPermission();
            } else {
                // Пользователь отказал — отклоняем запрос WebView
                if (mPermissionRequest != null) {
                    mPermissionRequest.deny();
                    mPermissionRequest = null;
                }
            }
        }
    }
}
