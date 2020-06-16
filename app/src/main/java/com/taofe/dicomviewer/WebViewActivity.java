package com.taofe.dicomviewer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WebViewActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private WebView webView;
    private BottomSheetBehavior bottomSheetBehavior;
    private String url;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("URL");
        setContentView(R.layout.activity_webview);
        InitView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.webview_setting: {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
                break;
            case android.R.id.home:{
                onBackPressed();
            }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        }else{
            Intent intent = new Intent();
            intent.setClass(WebViewActivity.this, DicomViewerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void InitView(){
        Toolbar toolbar = findViewById(R.id.webview_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar)findViewById(R.id.progress_web);
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                WebView.HitTestResult hit = view.getHitTestResult();
                if (Build.VERSION.SDK_INT >= 24){
                    if (request.isRedirect()){
                        //redirect
                    }
                }else {
                    if (hit.getType() == 0 || hit.getExtra().equals("")) {
                        //redirect
                    }
                }
                if (request.getUrl().toString().startsWith("https://")||request.getUrl().toString().startsWith("http://")){
                    view.loadUrl(request.getUrl().toString());
                    return false;
                }
                return true;
            }
        });

        webView.loadUrl(url);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.webview_bottom_sheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
