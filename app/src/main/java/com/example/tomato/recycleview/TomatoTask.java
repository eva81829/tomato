package com.example.tomato.recycleview;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.tomato.global.Global;
import com.example.tomato.setting.Setting;

public class TomatoTask implements BaseInfo, Parcelable {
    public static final String TYPE = "TomatoTask";
    private String taskName; //必填
    private int taskAmount; //必填
    private String taskTimeString;
    private int taskTime;

    private int isDone; // 0未完成, 1時間內完成, 2 延後完成
    private int taskId;
    private int taskPosition;
    private int taskColorId;
    private String taskIcon;
    private int taskRepeatMode;

    public TomatoTask(){
    }
    public TomatoTask(int isDone){
        this.isDone = isDone;
        this.taskId = -1;
    }

    @Override
    public String getType() {
        return TYPE;
    }
    public String getTaskName() {
        return taskName;
    }
    public int getTaskAmount() {
        return taskAmount;
    }
    public String getTaskTimeString(){
        setTaskTimeString(taskAmount, taskTime); //如果setting的時候更新時間, 就要更改重新顯示的TaskTime
        return taskTimeString;
    }
    public int getTaskTime(){
        return taskTime;
    }
    public int getIsDone() {
        return isDone;
    }
    public int getTaskId() {
        return taskId;
    }
    public int getTaskPosition() {
        return taskPosition;
    }
    public int getTaskColorId() {
        return taskColorId;
    }
    public String getTaskIcon() {
        return taskIcon;
    }

    public TomatoTask setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }
    public TomatoTask setTaskTimeString(int taskAmount, int taskTime) { //taskAmount給未完成用, taskTime給完成用
        if(isDone==0){
//            System.out.println("一顆蕃茄多長:" + Setting.getInstance().getTomatoTime());
            this.taskAmount = taskAmount;
            this.taskTime = taskAmount * Setting.getInstance().getTomatoTime(); //如果setting的時候更新時間, 就要重算一次TaskTime
        }else{
            this.taskTime = taskTime;
        }
        int taskHour = Global.Function.getHour(taskTime);
        int taskMin = Global.Function.getMin(taskTime);
        taskTimeString = (taskHour==0 ? "": taskHour + "小時") + (taskMin==0 ? "": taskMin + "分鐘");
        taskTimeString = taskTimeString.equals("") ? "0分鐘" : taskTimeString;
        return this;
    }
    public TomatoTask setIsDone(int done) {
        isDone = done;
        return this;
    }
    public TomatoTask setTaskId(int taskId) {
        this.taskId = taskId;
        return this;
    }
    public TomatoTask setTaskPosition(int taskPosition) {
        this.taskPosition = taskPosition;
        return this;
    }

    public TomatoTask setTaskColorId(int taskColorId) {
        this.taskColorId = taskColorId;
        return this;
    }
    public TomatoTask setTaskIcon(String taskIcon) {
        this.taskIcon = taskIcon;
        return this;
    }

    public static final Creator<TomatoTask> CREATOR = new Creator<TomatoTask>() {
        @Override
        public TomatoTask createFromParcel(Parcel in) {
            TomatoTask tomatoTask = new TomatoTask();
            tomatoTask.taskName = in.readString();
            tomatoTask.taskAmount = in.readInt();
            tomatoTask.taskTimeString = in.readString();
            tomatoTask.taskTime = in.readInt();
            tomatoTask.isDone = in.readInt();
            tomatoTask.taskId = in.readInt();
            tomatoTask.taskPosition = in.readInt();
            tomatoTask.taskColorId = in.readInt();
            tomatoTask.taskIcon = in.readString();

            return tomatoTask;
        }

        @Override
        public TomatoTask[] newArray(int size) {
            return new TomatoTask[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskName);
        dest.writeInt(taskAmount);
        dest.writeString(taskTimeString);
        dest.writeInt(taskTime);
        dest.writeInt(isDone);
        dest.writeInt(taskId);
        dest.writeInt(taskPosition);
        dest.writeInt(taskColorId);
        dest.writeString(taskIcon);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
