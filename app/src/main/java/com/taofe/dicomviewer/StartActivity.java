package com.taofe.dicomviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends Activity {
    private Timer timer = new Timer();
    private TimerTask showADTask = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        final ImageView imageView = (ImageView)findViewById(R.id.image_advertisment);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showADTask.cancel();
                Intent intent = new Intent(StartActivity.this,  WebViewActivity.class);
                intent.putExtra("URL", "http://www.szhsfd.com/");
                startActivity(intent);
            }
        });
        showADTask = new TimerTask() {
            @Override
            public void run() {
                start();
            }
        };
        timer.schedule(showADTask, 1500);
    }

    private void start(){
        if (isScreenOriatationPortrait(StartActivity.this)){
            switchToPortrait();
        }else{
            switchToLandspace();
        }
    }

    private void switchToLandspace(){
        Intent intent = new Intent();
        intent.setClass(StartActivity.this, DicomViewerActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToPortrait(){
        Intent intent = new Intent();
        intent.setClass(StartActivity.this, DicomViewerActivity.class);
        startActivity(intent);
        finish();
    }

    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
