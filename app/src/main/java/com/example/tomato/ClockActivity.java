package com.example.tomato;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tomato.controller.NfcController;
import com.example.tomato.global.Global;
import com.example.tomato.main.MainActivity;
import com.example.tomato.recycleview.TomatoTask;
import com.example.tomato.setting.Setting;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ClockActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ArrayList<TomatoTask> tomatoTasks;
    private TomatoTask tomatoTask;
    private LinearLayout llClock;
    private ConstraintLayout clClock;
    private Button btnStart;
    private TextView tvNFC;
    private TextView tvTaskName;
    private CountDownTimer timer;
    private ProgressBar progressBar;
    private int progress;
    private SoundPool sp;
    private int soundTomatoId;
    private int soundRelexId;
    private int soundStopId;
    private NfcController nfcController;
    private int currentFinishedTask; //現在完成幾個任務
    private int currentFinishedTomato; //現在完成幾顆番茄
    private int currentStatus; //休息中/ 進行番茄中
    private HomeReceiver innerReceiver;
    private boolean addOneTomatoTime;
    private int newPosition;
    private boolean isTaskOnTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        init();
        loadDataFromBundle();
        setBtnStart();
        setNextTaskInfo();

        innerReceiver = new HomeReceiver();//註冊廣播不讓使用者關閉
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(innerReceiver, intentFilter);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == null){
            return;
        }

        switch (nfcController.checkTagIsPaired(intent)) {
            case Global.Parameter.NFC_TAG_NOT_MATCH:
                Toast.makeText(this, Global.Parameter.NFC_ERROR_NOT_OUR_TAG, Toast.LENGTH_SHORT).show();
                return;

            case Global.Parameter.NFC_TAG_IS_TOMATO:
                switch (currentStatus){
                    case Global.Parameter.STATUS_LONG_RELAX_DONE:
                        changeStatus();
                        return;
                    case Global.Parameter.STATUS_SHORT_RELAX_DONE:
                        changeStatus();
                        return;
                    case Global.Parameter.STATUS_TOMATO_DELAY:
                        return;
                }
                Toast.makeText(this,Global.Parameter.NFC_ERROR_NOT_RELEX_TAG, Toast.LENGTH_SHORT).show();
                return;

            case Global.Parameter.NFC_TAG_IS_RELEX:
                switch (currentStatus){
                    case Global.Parameter.STATUS_PAUSE:
                        changeStatus();
                        return;
                    case Global.Parameter.STATUS_TOMATO_DELAY:
                        return;
                }
                Toast.makeText(this,Global.Parameter.NFC_ERROR_NOT_TOMATO_TAG, Toast.LENGTH_SHORT).show();
                return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcController.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcController.disableForegroundDispatch();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) { //如果不取消, 執行緒會一直存在, 消耗記憶體空間, 讓程式越跑越慢
            timer.cancel();
        }
        progress = 0;
        progressBar.setProgress(progress);
        sp.stop(soundStopId);

        unregisterReceiver(innerReceiver); //解除禁用Home鍵
    }


    //禁止返回前一頁
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }

    private void init() {
        setToolbar();

        llClock = findViewById(R.id.clock_ll);
        clClock = findViewById(R.id.clock_cl);
        btnStart = findViewById(R.id.clock_btn_start);
        tvNFC = findViewById(R.id.clock_tv_nfc);
        tvTaskName = findViewById(R.id.clock_tv_task_name);
        progressBar = findViewById(R.id.clock_pb_time);
        progress = 0;
        sp = new SoundPool.Builder().setMaxStreams(1).build();
        //maxStreams 同時播放流的最大數量，當播放的流的數目大於此值，則會選擇性停止優先順序較低的流
        soundTomatoId = sp.load(getApplicationContext(), R.raw.battle, 1);
        soundRelexId = sp.load(getApplicationContext(), R.raw.relex, 1);
        nfcController = new NfcController(this, getClass());
        currentStatus = Global.Parameter.STATUS_UNSTART;
        currentFinishedTask = 0;
        currentFinishedTomato = 0;
        isTaskOnTime = true;
    }

    private void setToolbar(){
        setNavigationIcon(Global.Parameter.VISIBLE);

        if(Global.Parameter.PRESENT_MODE){ //右上按鈕
            toolbar.inflateMenu(R.menu.menu_key);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    jumpToMainActivity(RESULT_OK);
                    return false;
                }
            });
        }
    }

    private void setNavigationIcon(Boolean isVisible){ //左上按鈕
        toolbar = findViewById(R.id.clock_toolbar);
        if(isVisible){
            toolbar.setNavigationIcon(R.drawable.ic_close_30dp);
        }else {
            toolbar.setNavigationIcon(null);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToMainActivity(RESULT_OK);
            }
        });
    }

    private void loadDataFromBundle(){
        Bundle bundle = getIntent().getExtras();
        // Parcelable的大BUG!!!! => 所有的參數都是複製的, 必須在Parcelable的class定義清楚那些參數要複製, 沒寫的就不複製
        tomatoTasks = bundle.getParcelableArrayList(Global.Parameter.TOMATO_TASKS);
        newPosition = bundle.getInt(Global.Parameter.FINISHED_TASKS);
        if(tomatoTasks == null){
            jumpToMainActivity(RESULT_CANCELED);
            return;
        }
        return;
    }

    public void jumpToMainActivity(int resultCode){
        Intent intent = new Intent(ClockActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Global.Parameter.TOMATO_TASKS, tomatoTasks);
        intent.putExtras(bundle);
        setResult(resultCode, intent);
        finish();
    }

    private void setNextTaskInfo(){
        //設定完成任務數與番茄數
        currentFinishedTomato = 0;
        tomatoTask = tomatoTasks.get(currentFinishedTask);
        //設定任務名稱
        tvTaskName.setText(tomatoTask.getTaskName());
        //設定顯示時間和計時器
        // totalTime一般模式: 分鐘轉成秒鐘, Demo模式:  分鐘當成秒鐘
        int totalTime = Setting.getInstance().getTomatoTime() * (Global.Parameter.PRESENT_MODE ? 1 : Global.Parameter.TIME_UNIT);
        setTimer(totalTime);

        //設定番茄數量
        llClock.setVisibility(View.VISIBLE);
        for(int i=1 ;i<=Global.Parameter.TOMATO_MAXIMUM; i++){
            TextView clockTvAmount = findTV(i);
            if(i<=tomatoTask.getTaskAmount()){
                clockTvAmount.setBackgroundResource(R.drawable.tomato_gray_30px);
                clockTvAmount.setVisibility(View.VISIBLE);
            }else{
                clockTvAmount.setVisibility(View.GONE);
            }
        }
    }

    private TextView findTV(int index){
        String amountIdName = "clock_tv_amount" + index;
        int resId = getResources().getIdentifier(amountIdName,"id", "com.example.tomato");
        return findViewById(resId);
    }

    private void setTimer(final int totalTime){
        if (timer != null) { //如果不取消, 執行緒會一直存在, 消耗記憶體空間, 讓程式越跑越慢
            timer.cancel();
        }
        setShowTime(totalTime, true);
        progressBar.setMax(totalTime); //max=5: 將進度條分成5份,  progress0代表完全沒進度, progress5代表完成進度
        progress = 0;
        progressBar.setProgress(progress);

        timer = new CountDownTimer(totalTime * 1000, 1000) { // 總長(毫秒), 間隔多久數一次(毫秒)
            @Override
            public void onTick(long millisUntilFinished) { //每1000毫秒跑一次, millisUntilFinished-> 剩餘毫秒
                //第一次呼叫onTick是按下去的那一刻
                int leftTime = (int)(Math.round((double) millisUntilFinished/ 1000)) - 1;
                progressBar.setProgress(++progress);
                setShowTime(leftTime, false);
            }

            @Override
            public void onFinish() { //數到0的時候不會呼叫onTick則是呼叫onFinish
                setShowTime(0, false);
                progressBar.setProgress(++progress);
                changeStatus();
            }
        };
    }

    public void setShowTime(int leftTime, boolean init){ //剩餘時間(秒)
        TextView tvTaskHour = findViewById(R.id.clock_tv_hour);
        TextView tvTaskHourColon = findViewById(R.id.clock_tv_hour_colon);
        TextView tvTaskMin = findViewById(R.id.clock_tv_min);
        TextView tvTaskMinColon = findViewById(R.id.clock_tv_min_colon);
        TextView tvTaskSec = findViewById(R.id.clock_tv_sec);

        int hour = leftTime/Global.Parameter.TIME_UNIT/Global.Parameter.TIME_UNIT;
        int min = leftTime/Global.Parameter.TIME_UNIT % Global.Parameter.TIME_UNIT;
        int sec = leftTime%Global.Parameter.TIME_UNIT;

        if(init){
            if(hour==0){
                tvTaskHour.setVisibility(View.GONE);
                tvTaskHourColon.setVisibility(View.GONE);
                if(!Global.Parameter.PRESENT_MODE && min==0){
                    tvTaskMin.setVisibility(View.GONE);
                    tvTaskMinColon.setVisibility(View.GONE);
                }
            }
        }

        tvTaskHour.setText(hour>=10 ? "" + hour : "0" + hour);
        tvTaskMin.setText(min>=10 ? "" + min : "0" + min);
        tvTaskSec.setText(sec>=10 ? "" + sec : "0" + sec);
    }

    private void setBtnStart(){
        tvNFC.setVisibility(View.INVISIBLE);
        btnStart.setVisibility(View.VISIBLE);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus();
                setNavigationIcon(false);
            }
        });
    }

    private void changeStatus(){
        int totalTime;
        boolean isLastTomato;
        boolean isLastTask;

        switch (currentStatus){
            case Global.Parameter.STATUS_UNSTART: //轉成開始倒數番茄鐘的狀態
                timer.start();
                tvNFC.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.INVISIBLE);
                currentStatus = Global.Parameter.STATUS_TOMATO_PROCESSING;
                System.out.println("開始進行番茄");
                break;

            case Global.Parameter.STATUS_TOMATO_PROCESSING: //轉成完成一顆番茄鐘的狀態
                soundStopId = sp.play(soundRelexId, 1.0f, 1.0f, 1, -1, 1.0f);
                findTV(++currentFinishedTomato).setBackgroundResource(R.drawable.tomato_30px);

                isLastTomato = (currentFinishedTomato >= tomatoTask.getTaskAmount());
                if(!isLastTomato){
                    tvTaskName.setText(Global.Parameter.SHORT_RELEX_TASK);
                }

                currentStatus = Global.Parameter.STATUS_PAUSE;
                System.out.println("一顆番茄完成");
                break;

           case Global.Parameter.STATUS_PAUSE: //轉成關掉鈴響的狀態
               sp.stop(soundStopId);
               isLastTomato = (currentFinishedTomato >= tomatoTask.getTaskAmount());
               if (isLastTomato){ //最後一顆蕃茄結束, dialog跳出來
                   setDelayDialog();
                   currentStatus = Global.Parameter.STATUS_TOMATO_DELAY;
                   return;
               }
               currentStatus = Global.Parameter.DONE;
               changeStatus();
               System.out.println("暫停");
               setNavigationIcon(true);
               break;

            case Global.Parameter.STATUS_TOMATO_DELAY: //轉成dialog關閉, 延長/休息的狀態
                if(addOneTomatoTime){ //延長
                    totalTime = Setting.getInstance().getTomatoTime() * (Global.Parameter.PRESENT_MODE ? 1 : Global.Parameter.TIME_UNIT);
                    setTimer(totalTime);
                    currentStatus = Global.Parameter.STATUS_UNSTART;
                    llClock.setVisibility(View.VISIBLE);
                    changeStatus();
                    break;
                }else { //不延長
                    currentStatus = Global.Parameter.STATUS_TOMATO_DONE;
                    changeStatus();
                }
                System.out.println("延長");
                break;

            case Global.Parameter.STATUS_TOMATO_DONE: //完成蕃茄狀態
                isLastTomato = (currentFinishedTomato >= tomatoTask.getTaskAmount());

                if(isLastTomato){
                    currentFinishedTask++;
                    if(isTaskOnTime){
                        tomatoTask.setIsDone(1);
                    }else {
                        tomatoTask.setIsDone(2);
                    }
                    isTaskOnTime = true;
                    tomatoTask.setTaskPosition(newPosition++);

                    isLastTask = (currentFinishedTask >= tomatoTasks.size());
                    if(isLastTask){
                        jumpToMainActivity(RESULT_OK);
                        return;
                    }

                    tvTaskName.setText(Global.Parameter.LONG_RELEX_TASK);
                    llClock.setVisibility(View.INVISIBLE);
                    setNavigationIcon(true);
//                    tomatoTask.setIsDone(Global.Parameter.TASK_ONTIME);
                }

                totalTime = (isLastTomato ? Setting.getInstance().getLongRelaxTime() : Setting.getInstance().getShortRelaxTime());
                setTimer(totalTime);
                timer.start();

                currentStatus = (isLastTomato ? Global.Parameter.STATUS_LONG_RELAX_PROCESSING : Global.Parameter.STATUS_SHORT_RELAX_PROCESSING);
                System.out.println("開始休息");
                break;

            case Global.Parameter.STATUS_SHORT_RELAX_PROCESSING:
                llClock.setVisibility(View.VISIBLE);
                soundStopId = sp.play(soundTomatoId, 1.0f, 1.0f, 1, -1, 1.0f);
                tvTaskName.setText(tomatoTask.getTaskName());
                totalTime = Setting.getInstance().getTomatoTime();
                setTimer(totalTime);
                currentStatus = Global.Parameter.STATUS_SHORT_RELAX_DONE;
                System.out.println("短休息結束");
                setNavigationIcon(false);
                break;

            case Global.Parameter.STATUS_SHORT_RELAX_DONE:
                sp.stop(soundStopId);
                timer.start();
                currentStatus = Global.Parameter.STATUS_TOMATO_PROCESSING;
                System.out.println("開始下一顆番茄");
                break;

            case Global.Parameter.STATUS_LONG_RELAX_PROCESSING:
                llClock.setVisibility(View.VISIBLE);
                soundStopId = sp.play(soundTomatoId, 1.0f, 1.0f, 1, -1, 1.0f);
                setNextTaskInfo();
                currentStatus = Global.Parameter.STATUS_LONG_RELAX_DONE;
                System.out.println("長休息結束");
                setNavigationIcon(false);
                break;

            case Global.Parameter.STATUS_LONG_RELAX_DONE:
                sp.stop(soundStopId);
                timer.start();
                currentStatus = Global.Parameter.STATUS_TOMATO_PROCESSING;
                System.out.println("開始下一項任務");
                break;
        }
    }

    private void setDelayDialog(){
        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.clock_dialog_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.clock_dialog_yes,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //需要增加蕃茄
                        isTaskOnTime = false;
                        addOneTomatoTime = true;
                        changeStatus();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.clock_dialog_no,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //不需要增加蕃茄
                        addOneTomatoTime = false;
                        changeStatus();
                        dialog.dismiss();
                    }
                })
                .show();

    }

}
