package com.example.tomato.setting;

import com.example.tomato.global.Global;
import com.example.tomato.login.data.Account;

public class Setting {
    private static Setting setting;

    private int tomatoTime; //單位分鐘
    private int shortRelaxTime; //單位分鐘
    private int longRelaxTime; //單位分鐘
    private int settingId;
    private String photoURI;
    private boolean directlyGoToNextTask;
    private boolean ring;
    private boolean vibration;
    private Account account;
    private Setting(){
    }

    public static Setting getInstance(){
        if(setting == null){
            setting = new Setting();
            setting.tomatoTime = (Global.Parameter.PRESENT_MODE ? Global.Parameter.DEFAULT_PRESENT_TOMATO_TIME: Global.Parameter.DEFAULT_TOMATO_TIME);
            setting.shortRelaxTime = (Global.Parameter.PRESENT_MODE ? Global.Parameter.DEFAULT_PRESENT_SHORT_RELAX_TIME: Global.Parameter.DEFAULT_SHORT_RELAX_TIME);
            setting.longRelaxTime = (Global.Parameter.PRESENT_MODE ? Global.Parameter.DEFAULT_PRESENT_LONG_RELAX_TIME: Global.Parameter.DEFAULT_LONG_RELAX_TIME);
            setting.directlyGoToNextTask = Global.Parameter.DEFAULT_DIRECTLY_GO_TO_NEXT_TASK;
            setting.account = new Account(Global.Parameter.DEFAULT_USER_EMAIL).setAccessToken(Global.Parameter.DEFAULT_TOKEN).setUserName(Global.Parameter.DEFAULT_USER_NAME);
            setting.settingId = -1;
            setting.ring = true;
            setting.vibration = true;
        }
        return setting;
    }

    public int getTomatoTime(){
        return tomatoTime;
    } //單位分鐘
    public int getShortRelaxTime() {
        return shortRelaxTime;
    }
    public int getLongRelaxTime() {
        return longRelaxTime;
    }

    public int getTomatoTimeHour(){ //將TomatoTime轉成"幾小時"幾分
        return Global.Function.getHour(tomatoTime);
    }
    public int getShortRelaxHour(){
        return Global.Function.getHour(shortRelaxTime);
    }
    public int getLongRelaxHour(){
        return Global.Function.getHour(longRelaxTime);
    }
    public int getTomatoTimeMin(){ //將TomatoTime轉成幾小時"幾分"
        return Global.Function.getMin(tomatoTime);
    }
    public int getShortRelaxMin(){
        return Global.Function.getMin(shortRelaxTime);
    }
    public int getLongRelaxMin(){
        return Global.Function.getMin(longRelaxTime);
    }
    public boolean getDirectlyGoToNextTask() {
        return directlyGoToNextTask;
    }
    public Account getAccount() {
        return account;
    }
    public int getSettingId() {
        return settingId;
    }
    public String getPhotoURI() {
        return photoURI;
    }
    public boolean isRing() {
        return ring;
    }
    public boolean isVibration() {
        return vibration;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
    public void setTomatoTime(int time){
        this.tomatoTime = time;
    }
    public void setShortRelaxTime(int time){
        this.shortRelaxTime = time;
    }
    public void setLongRelaxTime(int time){
        this.longRelaxTime = time;
    }
    public void setDirectlyGoToNextTask(boolean directlyGoToNextTask) {
        this.directlyGoToNextTask = directlyGoToNextTask;
    }
    public void setSettingId(int settingId) {
        this.settingId = settingId;
    }
    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }
    public void setRing(boolean ring) {
        this.ring = ring;
    }

    public void setVibration(boolean vibration) {
        this.vibration = vibration;
    }
}
