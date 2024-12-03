package com.example.tomato.main;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
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

import com.example.tomato.R;
import com.example.tomato.controller.NetworkController;
import com.example.tomato.global.Global;
import com.example.tomato.recycleview.BaseInfo;
import com.example.tomato.recycleview.BaseViewHolder;
import com.example.tomato.recycleview.ButtonShow;
import com.example.tomato.recycleview.CustomAdapter;
import com.example.tomato.recycleview.ListTask;
import com.example.tomato.recycleview.ListViewHolder;
import com.example.tomato.recycleview.ShowDoneTaskViewHolder;
import com.example.tomato.setting.Setting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

public class ListFragment extends Fragment {
    private View fragmentView;
    private LinearLayout fragmentLLAddList;
    private PopupWindow window;

    private RecyclerView rv;
    private CustomAdapter customAdapter;
    private ArrayList<BaseInfo> tasks;
    private ArrayList<BaseInfo> hidedTasks;
    private int unDoneTask;
    private int newPosition;
    private int doneTask;
    private int clickedIndex;
    private ListTask listTask;
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
    public void onResume() {
        super.onResume();
        syncTask(Global.Parameter.DOWNLOAD, Global.Parameter.API_GET);
    }

    private void init(LayoutInflater inflater, ViewGroup container){
        fragmentView = inflater.inflate(R.layout.fragment_list, container, false);
        fragmentLLAddList = fragmentView.findViewById(R.id.fragment_list_layout_add);
        buttonShow = new ButtonShow();
    }

    private void setRecycleView(){
        rv = fragmentView.findViewById(R.id.rv_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        rv.getItemAnimator().setChangeDuration(0); //去掉動畫閃爍時間

        customAdapter = new CustomAdapter(
                Arrays.asList(
                        new ListViewHolder.Factory(
                        new BaseViewHolder.TriggerFuncBuilder()
                                .add(R.id.vh_list_iv_edit, new BaseViewHolder.TriggerEventHandler() {
                                    @Override
                                    public void edit(int index) {
                                        clickedIndex = index;
                                        setListEditWindow();
                                    }
                                })
                                .add(R.id.vh_list_checkbox, new BaseViewHolder.TriggerEventHandler() {
                                    @Override
                                    public void done(int index){
                                        clickedIndex = index;
                                        tasksDone();
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

    public void tasksDone(){
        listTask = (ListTask) tasks.get(clickedIndex);
        listTask.setIsDone(true);
        listTask.setTaskPosition(doneTask++);
        syncTask(Global.Parameter.DONE, Global.Parameter.API_PATCH);
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

    public void setListEditWindow(){
        final int action =  Global.Parameter.EDIT;
        ListTask listTask = (ListTask) tasks.get(clickedIndex);
        setPopUpWindow(action, listTask.getTaskName(), listTask.getTaskPosition(), listTask.getTaskId(),">", R.string.list_edit);
    }

    //Fragmet所在的Activity已經完成了,初始化,並且它的UI也已經完成構件, 才能做的事情
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAddWindow();
    }

    public void setListAddWindow(){
        final int action = Global.Parameter.ADD;
        fragmentLLAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPopUpWindow(action, "", newPosition, -1,"+", R.string.list_add);
            }
        });
    }

    private void setPopUpWindow(int action, String taskName, int position, int id, String icon, int hint){
        View windowView = LayoutInflater.from(getContext()).inflate(R.layout.popupwindow_list, null);
        EditText popEtAddList = windowView.findViewById(R.id.pop_list_et_add);
        window = new PopupWindow(windowView);
        window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        window.setFocusable(true);
        window.showAtLocation(windowView, Gravity.TOP, 0, 0);

        listTask = new ListTask(false).setTaskName(taskName).setTaskPosition(position).setTaskId(id);
        //設定popupwindow上的操作
        setPopToolbar(action, popEtAddList, windowView);
        setLinearLayoutCancel(windowView);
        setKeyDone(icon, hint, action, popEtAddList, windowView);

        //顯示鍵盤
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    private void setPopToolbar(final int action, final EditText popEtAddList, View windowView){
        Toolbar toolbar = windowView.findViewById(R.id.pop_list_toolbar);
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
                        setDonePressed(action, popEtAddList);
                        return true;
                    case R.id.delete:
                        syncTask(Global.Parameter.REMOVE, Global.Parameter.API_DELETE);
                        return true;
                }
                return false;
            }
        });
    }

    public void setDonePressed(int action, EditText popEtAddList){
        String input = popEtAddList.getText().toString();
        if(!input.trim().equals("")){
            listTask.setTaskName(input);
            if(action== Global.Parameter.ADD){
                syncTask(action, Global.Parameter.API_POST);
            } else if (action== Global.Parameter.EDIT){
                syncTask(action, Global.Parameter.API_PATCH);
            }
        }else{
            Toast.makeText(getContext(), R.string.list_toast_empty_data, Toast.LENGTH_SHORT).show();
        }
    }

    private void setLinearLayoutCancel(View windowView){
        LinearLayout linearLayout1 = windowView.findViewById(R.id.pop_list_ll_cancel1);
        LinearLayout linearLayout2 = windowView.findViewById(R.id.pop_list_ll_cancel2);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        };

        linearLayout1.setOnClickListener(onClickListener);
        linearLayout2.setOnClickListener(onClickListener);
    }

    private void setKeyDone(String icon, int hint, final int action, final EditText popEtAddList, View windowView){
        //按新增後, 重置popwindow顯示的資料
        final TextView popTvAddList = windowView.findViewById(R.id.pop_list_tv_add);
        popTvAddList.setText(icon);
        popEtAddList.setHint(hint);
        popEtAddList.setText(listTask.getTaskName());
        popEtAddList.setSelection(listTask.getTaskName().length());

        //每次按鍵盤上的勾勾後, 新增資料
        popEtAddList.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) { // 按下完成按鈕
                    setDonePressed(action, popEtAddList);
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
                jsonObject.put("name", listTask.getTaskName());
                jsonObject.put("result", listTask.getIsDone());
                jsonObject.put("position", listTask.getTaskPosition());

                requestSync(action, requestType, jsonObject.toString());
            } catch (Exception e) { // JSONException
                System.out.println("資料有誤");
            }
        }else{
            requestSync(action, requestType, null);
        }
    }

    private void requestSync(final int action, final int requestType, final String data){
        String apiCommand = Global.Parameter.API_COMMAND_SYNC_LIST + (action == Global.Parameter.ADD || action == Global.Parameter.DOWNLOAD ? "": "/" + listTask.getTaskId());
//        System.out.println(apiCommand);
        NetworkController.getInstance().requestSync(apiCommand, requestType, data, new NetworkController.CCallback() {
            @Override
            public void fail(final int responseCode, String message) {
                System.out.println(responseCode);

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
                System.out.println(responseCode);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            switch (action){
                                case Global.Parameter.ADD:
                                    int id = jsonData.getJSONObject("task").getInt("id");
                                    listTask.setTaskId(id);
                                    newPosition++;
                                    customAdapter.insertData(unDoneTask++, listTask);
                                    rv.smoothScrollToPosition(unDoneTask);
                                    window.dismiss();
                                    updateStatisticsData();
                                    return;
                                case Global.Parameter.EDIT:
                                    customAdapter.replaceData(clickedIndex, listTask);
                                    window.dismiss();
                                    return;
                                case Global.Parameter.DONE:
                                    if(buttonShow.getTaskIsShowed()){
                                        customAdapter.addData(listTask);
                                    }else{
                                        hidedTasks.add(listTask);
                                    }
                                    customAdapter.removeData(clickedIndex);
                                    unDoneTask--;
                                    updateStatisticsData();
                                    return;
                                case Global.Parameter.REMOVE:
                                    customAdapter.removeData(clickedIndex);
                                    unDoneTask--;
                                    window.dismiss();
                                    updateStatisticsData();
                                    return;
                                case Global.Parameter.DOWNLOAD:
                                    refreshRV();
                                    JSONArray unFinishedLists = jsonData.getJSONArray("unfinished");
                                    downloadTasks(unFinishedLists);
                                    customAdapter.addData(buttonShow);
                                    JSONArray finishedLists = jsonData.getJSONArray("finished");
                                    downloadTasks(finishedLists);
                                    updateStatisticsData();
                                    return;
                            }
                        } catch(JSONException e){
                            Toast.makeText(getContext(), R.string.network_error_sync_data_fail, Toast.LENGTH_SHORT).show();
                            System.out.println(e.toString());
                        }
                    }
                });
            }

            @Override
            public void complete() {}
        });
    }

    private void refreshRV(){
        unDoneTask = 0;
        doneTask = 0;
        tasks = new ArrayList<>();
        customAdapter.bindDataSource(tasks);
        hidedTasks = new ArrayList<>();
        newPosition = unDoneTask;
        clickedIndex = -1;
    }

    private void downloadTasks (JSONArray lists) throws JSONException{
        int totalLists = lists.length();
        for(int i=0; i<totalLists; i++){
            JSONObject list = lists.getJSONObject(i);
            String name = list.getString("name");
            int id = list.getInt("id");
            boolean result = list.getInt("result")==0 ? false : true;
            int color;
            if(!result){
                unDoneTask++;
                color = R.color.defaultWhite;
            }else {
                doneTask++;
                color = R.color.defaultGray;
            }
            int position = list.getInt("position");
            if(position>=newPosition){
                newPosition = position;
                newPosition++;
            }

            listTask = new ListTask(result).setTaskName(name).setTaskColorId(color)
                    .setTaskPosition(position).setTaskId(id);
            if(result && !buttonShow.getTaskIsShowed()){
                hidedTasks.add(listTask);
                continue;
            }
            customAdapter.addData(listTask);
        }
    }

    private void updateStatisticsData(){
        TextView tvGoal = getActivity().findViewById(R.id.fragment_list_tv_goal);
        tvGoal.setText(doneTask+unDoneTask+"");
        TextView tvDone = getActivity().findViewById(R.id.fragment_list_tv_done);
        tvDone.setText(doneTask+"");
        TextView tvUnDone = getActivity().findViewById(R.id.fragment_list_tv_undone);
        tvUnDone.setText(unDoneTask+"");
    }
}
