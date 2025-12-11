package com.example.app6hu.Activities;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app6hu.R;

public class WebViewHocPhiActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_hoc_phi);

        webView = findViewById(R.id.webViewHocPhi);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        // ✅ Link cổng thanh toán học phí
        webView.loadUrl("https://sv.haui.edu.vn/student/recharge/inpatientpayment");
    }
}
