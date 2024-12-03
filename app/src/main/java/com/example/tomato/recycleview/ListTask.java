package com.example.tomato.recycleview;


public class ListTask implements BaseInfo{
    public static final String TYPE = "ListTask";
    private String taskName; //必填
    private int taskId;
    private int taskPosition;
    private int taskColorId;
    private int taskRepeatMode;
    private boolean isDone; //必填

    public ListTask(boolean isDone){
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
    public boolean getIsDone() {
        return isDone;
    }
    public int getTaskId() {
        return taskId;
    }
    public int getTaskPosition() {
        return taskPosition;
    }
    public int getTaskRepeatMode() {
        return taskRepeatMode;
    }
    public int getTaskColorId() {
        return taskColorId;
    }

    public ListTask setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }
    public ListTask setIsDone(boolean done) {
        isDone = done;
        return this;
    }
    public ListTask setTaskId(int taskId) {
        this.taskId = taskId;
        return this;
    }
    public ListTask setTaskPosition(int taskPosition) {
        this.taskPosition = taskPosition;
        return this;
    }
    public ListTask setTaskRepeatMode(int taskRepeatMode) {
        this.taskRepeatMode = taskRepeatMode;
        return this;
    }
    public ListTask setTaskColorId(int taskColorId) {
        this.taskColorId = taskColorId;
        return this;
    }
}
