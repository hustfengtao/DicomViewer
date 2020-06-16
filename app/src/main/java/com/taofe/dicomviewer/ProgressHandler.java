package com.taofe.dicomviewer;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;

public class ProgressHandler extends Handler {
    static final int SHOW_PROGRESS = 0;
    static final int MAX_PROGRSS = 1;
    static final int UPDATE_PROGRESS = 2;
    static final int HIDE_PROGRESS = 3;
    private ProgressBar progressBar;
    private OnProgressOverListener onProgressOverListener = null;
    public ProgressHandler(ProgressBar progressBar){
        this.progressBar = progressBar;
    }
    @Override
    public void handleMessage(final Message msg) {
        switch (msg.what) {
            case SHOW_PROGRESS:{
                progressBar.setVisibility(View.VISIBLE);
                break;
            }
            case MAX_PROGRSS:{
                progressBar.setMax(msg.arg1);
                break;
            }
            case UPDATE_PROGRESS: {
                progressBar.setProgress(msg.arg1);
                break;
            }
            case HIDE_PROGRESS:{
                progressBar.setVisibility(View.GONE);
                if (onProgressOverListener!=null) {
                    onProgressOverListener.onFinish();
                }
                break;
            }
        }
    }

    public void setOnProgressOverListener(OnProgressOverListener onProgressOverListener){
        this.onProgressOverListener = onProgressOverListener;
    }

    public void sendMessage(int type, int arg1){
        Message callbackmsg = new Message();
        callbackmsg.what = type;
        callbackmsg.arg1 = arg1;
        sendMessage(callbackmsg);
    }
}