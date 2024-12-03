package com.example.tomato.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tomato.ClockActivity;
import com.example.tomato.R;
import com.example.tomato.controller.NetworkController;
import com.example.tomato.global.Global;
import com.example.tomato.recycleview.BaseInfo;
import com.example.tomato.recycleview.BaseViewHolder;
import com.example.tomato.recycleview.ButtonShow;
import com.example.tomato.recycleview.CustomAdapter;
import com.example.tomato.recycleview.ShowDoneTaskViewHolder;
import com.example.tomato.recycleview.TomatoTask;
import com.example.tomato.recycleview.TomatoViewHolder;
import com.example.tomato.setting.Setting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class TomatoFragment extends Fragment {
    private View fragmentView;
    private LinearLayout fragmentLLAddTomato;
    private PopupWindow window;

    private RecyclerView rv;
    private CustomAdapter customAdapter;
    private ArrayList<BaseInfo> tasks;
    private ArrayList<BaseInfo> hidedTasks;
    private int unDoneTask;
    private int newPosition;
    private int doneTask;
    private int delayTask;
    private int clickedIndex;
    private TomatoTask tomatoTask;
    private ButtonShow buttonShow;

    //在這個方法中可以獲得所在的activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //初始化Fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //fragment在其中建立自己的layout, 填充UI
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        init(inflater, container);
        setRecycleView();
        return fragmentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) { // onActivityResult完會呼叫onResume更新資料
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode==Global.Parameter.GO_TO_CLOCK_ACTIVITY){
            loadDataFromBundle(intent);
        }
    }

    private void loadDataFromBundle(Intent intent){ // 任務完成
        Bundle bundle = intent.getExtras();
        ArrayList<TomatoTask> tomatoTasks = bundle.getParcelableArrayList(Global.Parameter.TOMATO_TASKS);
        if(tomatoTasks != null){
            for(int i=0; i<tomatoTasks.size(); i++){
                tasks.set(clickedIndex, tomatoTasks.get(i)); //把原本的array資料, 替換成BUNDLE帶過來的資料
                tomatoTask = (TomatoTask) tasks.get(clickedIndex);
                syncTask(Global.Parameter.DONE, Global.Parameter.API_PATCH);
            }
        }
        return;
    }

    private void init(LayoutInflater inflater, ViewGroup container){
        fragmentView = inflater.inflate(R.layout.fragment_tomato, container, false);
        fragmentLLAddTomato = fragmentView.findViewById(R.id.fragment_tomato_layout_add);
        buttonShow = new ButtonShow();
    }

    private void setRecycleView(){
        rv = fragmentView.findViewById(R.id.rv_tomato);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        rv.getItemAnimator().setChangeDuration(0); //去掉動畫閃爍時間

        customAdapter = new CustomAdapter(
                Arrays.asList(new TomatoViewHolder.Factory(
                        new BaseViewHolder.TriggerFuncBuilder()
                                .add(R.id.vh_tomato_iv_edit, new BaseViewHolder.TriggerEventHandler() {
                                    @Override
                                    public void edit(int index) {
                                        clickedIndex = index;
                                        setTomatoEditWindow(); //向下轉型, 父轉子(最好加instance of)
                                    }
                                })
                                .add(R.id.vh_tomato_ll, new BaseViewHolder.TriggerEventHandler() {
                                    @Override
                                    public void countTime(int index) {
                                        clickedIndex = index;
                                        jumpToClockActivity();
                                    }
                                })
                ), new ShowDoneTaskViewHolder.Factory(
                        new BaseViewHolder.TriggerFuncBuilder()
                                .add(R.id.vh_btn_show, new BaseViewHolder.TriggerEventHandler() {
                                    @Override
                                    public void show() {
                                        showTasks();
                                    }
                                    @Override
                                    public void hide() {
                                        hideTasks();
                                    }
                                })
                ))
        );
        rv.setAdapter(customAdapter);
        refreshRV();
    }

    @Override
    public void onResume() {
        super.onResume();
//        System.out.println("onResume");
        syncTask(Global.Parameter.DOWNLOAD_SETTING, Global.Parameter.API_GET);
        syncTask(Global.Parameter.DOWNLOAD, Global.Parameter.API_GET);
    }

    public void jumpToClockActivity(){
        Intent intent = new Intent(getActivity(), ClockActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Global.Parameter.TOMATO_TASKS, getProcessingTasks());
        bundle.putInt(Global.Parameter.FINISHED_TASKS, doneTask);
        intent.putExtras(bundle);
        startActivityForResult(intent, Global.Parameter.GO_TO_CLOCK_ACTIVITY);
    }

    public ArrayList<TomatoTask> getProcessingTasks(){
        ArrayList<TomatoTask> processingTasks = new ArrayList<>();
        if (Setting.getInstance().getDirectlyGoToNextTask()){
            for(int i = clickedIndex; i<unDoneTask; i++){
                processingTasks.add((TomatoTask) tasks.get(i)); //戰鬥模式
            }
        } else {
            processingTasks.add((TomatoTask) tasks.get(clickedIndex)); //一般模式
        }
        return processingTasks;
    }

    public void showTasks(){
        customAdapter.addDatas(hidedTasks);
        rv.scrollToPosition(customAdapter.getItemCount()-1);
        hidedTasks = new ArrayList<>();
    }

    public void hideTasks(){
        int length = tasks.size();
        int doneTaskindex = unDoneTask+1;
        for (int i=doneTaskindex; i<length ; i++){ //unDoneTask的index是show/hide button, 所以要加一
            hidedTasks.add(tasks.get(doneTaskindex));
            customAdapter.removeData(doneTaskindex);
        }
    }

    public void setTomatoEditWindow(){
        final int action =  Global.Parameter.EDIT;
        TomatoTask tomatoTask = (TomatoTask)tasks.get(clickedIndex);
        setPopUpWindow(action, tomatoTask.getTaskAmount(), tomatoTask.getTaskName(), tomatoTask.getTaskColorId(),
                tomatoTask.getTaskPosition(), tomatoTask.getTaskId(), tomatoTask.getTaskIcon(),">", R.string.tomato_edit);
    }

    //Fragmet所在的Activity已經完成了,初始化,並且它的UI也已經完成構件, 才能做的事情
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTomatoAddWindow();
    }

    public void setTomatoAddWindow(){
        final int action = Global.Parameter.ADD;
        fragmentLLAddTomato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPopUpWindow(action, 0, "", R.color.tomatoGreen, newPosition, -1, "icon_read","+", R.string.tomato_add);
           }
        });
    }

    private void setPopUpWindow(int action, int taskAmount, String taskName, int taskColor, int position, int id, String icon, String mark, int hint){
        View windowView = LayoutInflater.from(getContext()).inflate(R.layout.popupwindow_tomato, null);
        EditText popEtAddTomato = windowView.findViewById(R.id.pop_tomato_et_add);
        window = new PopupWindow(windowView);
        window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        window.setFocusable(true);
        window.showAtLocation(windowView, Gravity.TOP, 0, 0);

        tomatoTask = new TomatoTask(Global.Parameter.TASK_UNDONE).setTaskTimeString(taskAmount, 0).setTaskName(taskName).setTaskColorId(taskColor)
                .setTaskPosition(position).setTaskId(id).setTaskIcon(icon);

        //顯示鍵盤
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);

        //設定popupwindow上的操作
        setPopToolbar(action, popEtAddTomato, windowView);
        setLinearLayoutCancel(windowView);
        setTomatoAmountListener(windowView);
        setKeyDone(mark, hint, action, popEtAddTomato, windowView);
        setColorWindow(windowView, imm);
        setIconWindow(windowView, imm);
    }

    private void setPopToolbar(final int action, final EditText popEtAddTomato, View windowView){
        Toolbar toolbar = windowView.findViewById(R.id.pop_tomato_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_30dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });

        // 設定右上角的 menu

        toolbar.inflateMenu(action==Global.Parameter.ADD ? R.menu.menu_confirm : R.menu.menu_delete);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.confirm:
                        setDonePressed(action, popEtAddTomato);
                        return true;
                    case R.id.delete:
                        syncTask(Global.Parameter.REMOVE, Global.Parameter.API_DELETE);
                        return true;
                }
                return false;
            }
        });
    }

    public void setColorWindow(final View windowView, final InputMethodManager imm){
        final TextView tvColor = windowView.findViewById(R.id.pop_tomato_tv_color);
        ColorStateList colorStateList = ColorStateList.valueOf(getContext().getResources().getColor(tomatoTask.getTaskColorId(),null));
        tvColor.setBackgroundTintList(colorStateList);

        LinearLayout llcolor = windowView.findViewById(R.id.pop_tomato_ll_color);
        llcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View colorWindowView = LayoutInflater.from(getContext()).inflate(R.layout.popupwindow_color, null);
                final PopupWindow window = new PopupWindow(colorWindowView);
                window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                window.setFocusable(true);
                window.showAtLocation(colorWindowView, Gravity.TOP, 0, 0);
                imm.hideSoftInputFromWindow(windowView.getWindowToken(), 0); //強制隱藏鍵盤

                ConstraintLayout constraintLayout = colorWindowView.findViewById(R.id.pop_color_cl_color);
                constraintLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });

                LinearLayout linearLayout = colorWindowView.findViewById(R.id.pop_color_ll_color);
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });

                for(int i=0; i<6; i++){
                    final TextView tvColorPicker = findTV(i+1, colorWindowView, "pop_color_tv_color");
                    tvColorPicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            window.dismiss();
                            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                            String color = tvColorPicker.getTag().toString();
//                            System.out.println(color);
                            int resId = getContext().getResources().getIdentifier(color,"color", "com.example.tomato");
                            tomatoTask.setTaskColorId(resId);
                            ColorStateList colorStateList = ColorStateList.valueOf(getContext().getResources().getColor(resId,null));
                            tvColor.setBackgroundTintList(colorStateList);
                        }
                    });
                }
            }
        });
    }

    public void setIconWindow(final View windowView, final InputMethodManager imm){
        final TextView tvIcon = windowView.findViewById(R.id.pop_tomato_tv_icon);
        String icon = tomatoTask.getTaskIcon();
        int resId = getContext().getResources().getIdentifier(icon,"drawable", "com.example.tomato");
        tvIcon.setBackground(ContextCompat.getDrawable(getContext(), resId));

        LinearLayout llicon = windowView.findViewById(R.id.pop_tomato_ll_icon);
        llicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View colorWindowView = LayoutInflater.from(getContext()).inflate(R.layout.popupwindow_icon, null);
                final PopupWindow window = new PopupWindow(colorWindowView);
                window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                window.setFocusable(true);
                window.showAtLocation(colorWindowView, Gravity.TOP, 0, 0);
                imm.hideSoftInputFromWindow(windowView.getWindowToken(), 0); //強制隱藏鍵盤

                ConstraintLayout constraintLayout = colorWindowView.findViewById(R.id.pop_icon_cl_icon);
                constraintLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });

                LinearLayout linearLayout = colorWindowView.findViewById(R.id.pop_icon_ll_icon);
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });

                for(int i=0; i<10; i++){
                    final TextView tvIconPicker = findTV(i+1, colorWindowView, "pop_icon_tv_icon");
                    tvIconPicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            window.dismiss();
                            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                            String icon = tvIconPicker.getTag().toString();
                            tomatoTask.setTaskIcon(icon);
                            int resId = getContext().getResources().getIdentifier(icon,"drawable", "com.example.tomato");
                            tvIcon.setBackground(ContextCompat.getDrawable(getContext(), resId));
                        }
                    });
                }
            }
        });
    }

    private TextView findTV(int index, View windowView, String name){
        String amountIdName = name + index;
        int resId = getContext().getResources().getIdentifier(amountIdName,"id", "com.example.tomato");
        return windowView.findViewById(resId);
    }

    public void setDonePressed(int action, EditText popEtAddTomato){
        String input = popEtAddTomato.getText().toString();
        if(!input.trim().equals("") && tomatoTask.getTaskTimeString()!="0分鐘"){
            tomatoTask.setTaskName(input);
            if(action== Global.Parameter.ADD){
                syncTask(action, Global.Parameter.API_POST);
            } else if (action== Global.Parameter.EDIT){
                syncTask(action, Global.Parameter.API_PATCH);
            }
        }else{
            Toast.makeText(getContext(), R.string.tomato_toast_empty_data, Toast.LENGTH_SHORT).show();
        }
    }

    private void setLinearLayoutCancel(View windowView){
        LinearLayout linearLayout = windowView.findViewById(R.id.pop_tomato_ll_cancel);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });
    }

    private void setTomatoAmountListener(final View windowView){
        setRedTomatoAmount(tomatoTask.getTaskAmount(), windowView);
        final TextView popTvTotalTime = windowView.findViewById(R.id.pop_tomato_tv_task_time);
        popTvTotalTime.setText(tomatoTask.getTaskTimeString());

        View.OnClickListener clickListener = new View.OnClickListener() { //每個amount按下去的結果
            @Override
            public void onClick(View v) {
                String amountIdName = getContext().getResources().getResourceEntryName(v.getId());
                String amountId = amountIdName.substring(amountIdName.length()-1);
                int amount = Integer.valueOf(amountId);

                setRedTomatoAmount(amount, windowView);
                tomatoTask.setTaskTimeString(amount, 0);
                popTvTotalTime.setText(tomatoTask.getTaskTimeString());
            }
        };

        for(int i=1 ;i<=Global.Parameter.TOMATO_MAXIMUM; i++){
            TextView popTvAmount = findTV(i, windowView, "pop_tomato_tv_amount");
            popTvAmount.setOnClickListener(clickListener);
        }
    }

    private void setRedTomatoAmount(int amount, View windowView){
        for(int i=1; i<=Global.Parameter.TOMATO_MAXIMUM; i++){
            TextView popTvAmount = findTV(i, windowView, "pop_tomato_tv_amount");
            if(i<=amount){
                popTvAmount.setBackgroundResource(R.drawable.tomato_30px);
            }else{
                popTvAmount.setBackgroundResource(R.drawable.tomato_gray_30px);
            }
        }
    }

    private void setKeyDone(String icon, int hint, final int action, final EditText popEtAddTomato, View windowView){
        //按新增後, 重置popwindow顯示的資料
        final TextView popTvAddTomato = windowView.findViewById(R.id.pop_tomato_tv_add);
        popTvAddTomato.setText(icon);
        popEtAddTomato.setHint(hint);
        popEtAddTomato.setText(tomatoTask.getTaskName());
        popEtAddTomato.setSelection(tomatoTask.getTaskName().length());

        //每次按鍵盤上的勾勾後, 新增資料
        popEtAddTomato.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setDonePressed(action, popEtAddTomato);
                    return true;
                }
                return false;
            }
        });
    }


    public void syncTask(int action, int requestType) { // 加入int position 每次新增, 修改都要把位置存進去, 讀資料的時候才能知道怎麼排
        if(requestType!=Global.Parameter.API_GET){
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", tomatoTask.getTaskName());
                jsonObject.put("result", tomatoTask.getIsDone());
                jsonObject.put("position", tomatoTask.getTaskPosition());
                jsonObject.put("length", tomatoTask.getTaskTime()); //總長
                jsonObject.put("pcs", tomatoTask.getTaskAmount()); //幾顆蕃茄
                jsonObject.put("minute", Setting.getInstance().getTomatoTime()); //一顆蕃茄多長
                jsonObject.put("color", tomatoTask.getTaskColorId()+"");
                jsonObject.put("icon", tomatoTask.getTaskIcon());

                requestSync(action, requestType, jsonObject.toString());
            } catch (Exception e) { // JSONException
//                System.out.println("資料有誤");
            }
        }else{
            requestSync(action, requestType, null);
        }
    }

    private void requestSync(final int action, final int requestType, final String data){
        String apiCommand;
        if(action == Global.Parameter.DOWNLOAD_SETTING){
            apiCommand = Global.Parameter.API_COMMAND_SYNC_SETTING;
        }else if(action == Global.Parameter.BUILD_TOMATO){
            apiCommand = Global.Parameter.API_COMMAND_BUILDER;
        } else {
            apiCommand = Global.Parameter.API_COMMAND_SYNC_TOMATO + (action == Global.Parameter.ADD || action == Global.Parameter.DOWNLOAD? "": "/" + tomatoTask.getTaskId());
        }
//        System.out.println(apiCommand);
        NetworkController.getInstance().requestSync(apiCommand, requestType, data, new NetworkController.CCallback() {
            @Override
            public void fail(final int responseCode, String message) {
//                System.out.println(responseCode);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (responseCode){
                            case -1:
                                Toast.makeText(getContext(), R.string.network_error_connected_fail, Toast.LENGTH_SHORT).show();
                                return;
                        }
                        Toast.makeText(getContext(), R.string.network_error_sync_data_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void success(int responseCode, final JSONObject jsonData) {
//                System.out.println(responseCode);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            switch (action){
                                case Global.Parameter.ADD:
                                    int id = jsonData.getJSONObject("tomato").getInt("id");
                                    tomatoTask.setTaskId(id);
                                    newPosition++;
                                    customAdapter.insertData(unDoneTask++, tomatoTask);
                                    rv.smoothScrollToPosition(unDoneTask);
                                    window.dismiss();
                                    updateStatisticsData();
                                    return;
                                case Global.Parameter.EDIT:
                                    customAdapter.replaceData(clickedIndex, tomatoTask);
                                    window.dismiss();
                                    return;
                                case Global.Parameter.REMOVE:
                                    customAdapter.removeData(clickedIndex);
                                    unDoneTask--;
                                    window.dismiss();
                                    updateStatisticsData();
                                    return;
                                case Global.Parameter.DOWNLOAD_SETTING:
                                    JSONObject settingObject = jsonData.getJSONArray("setting").getJSONObject(0);
                                    id = settingObject.getInt("id");
                                    Setting.getInstance().setSettingId(id);

                                    boolean battle = settingObject.getInt("battle") == 0 ? false:true;
                                    Setting.getInstance().setDirectlyGoToNextTask(battle);

                                    int minute = settingObject.getInt("minute");
                                    Setting.getInstance().setTomatoTime(minute);
//                                    System.out.println("一顆蕃茄時間:"+ minute);

                                    int shortRelex = settingObject.getInt("short");
                                    Setting.getInstance().setShortRelaxTime(shortRelex);
//                                    System.out.println("短休息時間:"+ shortRelex);

                                    int longRelex = settingObject.getInt("long");
                                    Setting.getInstance().setLongRelaxTime(longRelex);
                                    return;

                                case Global.Parameter.DOWNLOAD:
                                    refreshRV();
                                    JSONArray unFinishedTomatoes = jsonData.getJSONArray("unfinished");
                                    downloadTasks(unFinishedTomatoes);
                                    customAdapter.addData(buttonShow);
                                    JSONArray finishedTomatoes = jsonData.getJSONArray("finished");
                                    downloadTasks(finishedTomatoes);
                                    updateStatisticsData();
                                    return;
                            }
                        } catch(JSONException e){
                            Toast.makeText(getContext(), R.string.network_error_sync_data_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void complete() {
            }
        });
    }

    private void refreshRV(){
        unDoneTask = 0;
        doneTask = 0;
        delayTask = 0;
        tasks = new ArrayList<>();
        customAdapter.bindDataSource(tasks);
        hidedTasks = new ArrayList<>();
        newPosition = unDoneTask;
        clickedIndex = -1;
    }

    private void downloadTasks (JSONArray tomatoes) throws JSONException{
        int totalTomatoes = tomatoes.length();
        for(int i=0; i<totalTomatoes; i++){
            JSONObject tomato = tomatoes.getJSONObject(i);
            String name = tomato.getString("name");
            name = (name.equals("往左滑刪除") ? "進入編輯頁面刪除任務" : name);
            int id = tomato.getInt("id");
            int result = tomato.getInt("result");
            if(result==0){
                unDoneTask++;
            }else if(result==1){
                doneTask++;
            }else {
                delayTask++;
            }
            int position = tomato.getInt("position");
            if(position>=newPosition){
                newPosition = position;
                newPosition++;
            }
            String colorString = tomato.getString("color");
            int color = colorTransfer(colorString);
            String iconString = tomato.getString("icon");
            String icon = iconString;
            int amount = tomato.getInt("pcs");
            int time = tomato.getInt("length");
            tomatoTask = new TomatoTask(result).setTaskTimeString(amount, time).setTaskName(name).setTaskColorId(color)
                    .setTaskPosition(position).setTaskId(id).setTaskIcon(icon);
            if(result!=0 && !buttonShow.getTaskIsShowed()){
                hidedTasks.add(tomatoTask);
                continue;
            }
            customAdapter.addData(tomatoTask);
        }
    }

    private void updateStatisticsData(){
        TextView tvGoal = getActivity().findViewById(R.id.fragment_tomato_tv_goal);
        tvGoal.setText(doneTask+delayTask+unDoneTask+"");
        TextView tvDone = getActivity().findViewById(R.id.fragment_tomato_tv_done);
        tvDone.setText(doneTask+"");
        TextView tvDelay = getActivity().findViewById(R.id.fragment_tomato_tv_delay);
        tvDelay.setText(delayTask+"");
        TextView tvUnDone = getActivity().findViewById(R.id.fragment_tomato_tv_undone);
        tvUnDone.setText(unDoneTask+"");
    }

    public int colorTransfer(String colorString){
        switch (colorString){
            case "activity_1":
                return R.color.tomatoYellow;
            case "activity_2":
                return R.color.tomatoSkin;
            case "activity_3":
                return R.color.tomatoRed;
            case "activity_5":
                return R.color.tomatoLake;
            case "activity_4":
                return R.color.tomatoGreen;
        }
        return Integer.valueOf(colorString);
    }
}
