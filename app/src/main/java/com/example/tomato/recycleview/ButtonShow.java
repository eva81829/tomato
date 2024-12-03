package com.example.tomato.recycleview;


public class ButtonShow implements BaseInfo{
    public static final String TYPE = "ButtonShow";

    private boolean taskIsShowed;

    public ButtonShow(){
        taskIsShowed = false;
    }

    public ButtonShow setTaskIsShowed(boolean taskIsShowed) {
        this.taskIsShowed = taskIsShowed;
        return this;
    }

    public boolean getTaskIsShowed() {
        return taskIsShowed;
    }



    @Override
    public String getType() {
        return TYPE;
    }
}
