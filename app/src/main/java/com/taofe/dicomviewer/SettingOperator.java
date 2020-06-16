package com.taofe.dicomviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;


public class SettingOperator {
    private Context context;
    private String serverIP;
    private String userID;
    private String userPWD;
    private String ftpID;
    private String ftpPWD;
    private String port;
    private String switchOnTime;
    private String switchOffTime;
    private int autoSwitch;
    private int firstStart;
    private int roomTextSize;
    private int checkTextSize;
    private int waitTextSize;
    private int bottomTextSize;
    private int fullScreenSize;
    private int titleMargin;
    private int contentMargin;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    public SettingOperator(Context context){
        this.context = context;
        preferences = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public String getServerIP(){
        serverIP=preferences.getString("SERVERIP","172.20.17.131");
        return serverIP;
    }
    public void setServerIP(String ip){
        if(!editor.putString("SERVERIP", ip).commit()){
            Toast.makeText(context, "服务器ip改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public String getFtpID(){
        ftpID=preferences.getString("FTPID","Administrator");
        return ftpID;
    }
    public void setFtpID(String id){
        if(!editor.putString("FTPID", id).commit()){
            Toast.makeText(context, "FTP用户改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public String getFtpPWD(){
        ftpPWD=preferences.getString("FTPPWD","321321");
        return ftpPWD;
    }
    public void setFtpPWD(String pwd){
        if (!editor.putString("FTPPWD", pwd).commit()){
            Toast.makeText(context, "FTP密码改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public String getUserID(){
        userID=preferences.getString("USERID","sa");
        return userID;
    }
    public void setUserID(String id){
        if(!editor.putString("USERID", id).commit()){
            Toast.makeText(context, "数据库用户改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public String getUserPWD(){
        userPWD=preferences.getString("USERPWD","1qaz@WSX");
        return userPWD;
    }
    public void setUserPWD(String pwd){
        if (!editor.putString("USERPWD", pwd).commit()){
            Toast.makeText(context, "数据库密码改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public String getPort(){
        port=preferences.getString("PORT","9113");
        return port;
    }
    public void setPort(String port){
        if (!editor.putString("PORT", port).commit()){
            Toast.makeText(context, "本地监听端口改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public String getSwitchOnTime(){
        switchOnTime=preferences.getString("SWITCHON","07:00:00");
        return switchOnTime;
    }
    public void setSwitchOnTime(String port){
        if (!editor.putString("SWITCHON", port).commit()){
            Toast.makeText(context, "定时启动时间改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public String getSwitchOffTime(){
        switchOffTime=preferences.getString("SWITCHOFF","18:30:00");
        return switchOffTime;
    }
    public void setSwitchOffTime(String port){
        if (!editor.putString("SWITCHOFF", port).commit()){
            Toast.makeText(context, "定时待机时间改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    //
    public int getAutoSwitchValue(){
        autoSwitch=preferences.getInt("AUTOSWITCH",0);
        return autoSwitch;
    }
    public void setAutoSwitchValue(int value){
        if (!editor.putInt("AUTOSWITCH", value).commit()){
            Toast.makeText(context, "自启动设置改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getFirstStartState(){
        firstStart = preferences.getInt("FIRSTSTART", 1);
        return firstStart;
    }
    public void setFirstStartState(int value){
        if (!editor.putInt("FIRSTSTART", value).commit()){
            Toast.makeText(context, "初始状态设置改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getRoomTextSize(){
        roomTextSize = preferences.getInt("ROOMTEXTSIZE", 80);
        return roomTextSize;
    }
    public void setRoomTextSize(int value){
        if (!editor.putInt("ROOMTEXTSIZE", value).commit()){
            Toast.makeText(context, "字体大小改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getCheckTextSize(){
        checkTextSize = preferences.getInt("CHECKTEXTSIZE", 80);
        return checkTextSize;
    }
    public void setCheckTextSize(int value){
        if (!editor.putInt("CHECKTEXTSIZE", value).commit()){
            Toast.makeText(context, "字体大小改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getWaitTextSize(){
        waitTextSize = preferences.getInt("WAITTEXTSIZE", 80);
        return waitTextSize;
    }
    public void setWaitTextSize(int value){
        if (!editor.putInt("WAITTEXTSIZE", value).commit()){
            Toast.makeText(context, "字体大小改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getBottomTextSize(){
        bottomTextSize = preferences.getInt("BOTTOMTEXTSIZE", 80);
        return bottomTextSize;
    }
    public void setBottomTextSize(int value){
        if (!editor.putInt("BOTTOMTEXTSIZE", value).commit()){
            Toast.makeText(context, "字体大小改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getFullScreenSize(){
        fullScreenSize = preferences.getInt("FULLSCREENSIZE", 150);
        return fullScreenSize;
    }
    public void setFullScreenSize(int value){
        if(!editor.putInt("FULLSCREENSIZE", value).commit()){
            Toast.makeText(context, "字体大小改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getTitleMargin(){
        titleMargin = preferences.getInt("TITLEMARGIN", 0);
        return titleMargin;
    }
    public void setTitleMargin(int value){
        if (!editor.putInt("TITLEMARGIN", value).commit()){
            Toast.makeText(context, "标题边距改写失败", Toast.LENGTH_SHORT).show();
        }
    }
    //
    public int getContentMargin(){
        contentMargin = preferences.getInt("CONTENTMARGIN", 0);
        return contentMargin;
    }
    public void setContentMargin(int value){
        if (!editor.putInt("CONTENTMARGIN", value).commit()){
            Toast.makeText(context, "正文边距改写失败", Toast.LENGTH_SHORT).show();
        }
    }
}
