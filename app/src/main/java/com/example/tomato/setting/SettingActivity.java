package com.example.tomato.setting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tomato.R;
import com.example.tomato.controller.NetworkController;
import com.example.tomato.global.Global;
import com.example.tomato.login.form.LoginActivity;
import com.example.tomato.main.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {
    private ImageView ivPhotograph;
    private TextView tvName;
    private TextView tvEmail;
    private Toolbar toolbar;
    private TextView tvLogout;
    private NumberPicker npTomatoHour;
    private NumberPicker npTomatoMin;
    private NumberPicker npShortRelexHour;
    private NumberPicker npShortRelexMin;
    private NumberPicker npLongRelexHour;
    private NumberPicker npLongRelexMin;
    private boolean firstTimeLogin;
    private Setting setting;
    private boolean isBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
        loadDataFromBundle();
        setSettingValue();
        setBtnLogout();
        setNavigationIcon();
        setAccount();
        openAlbumAfterClickPhoto();
        setInfoWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncSetting(Global.Parameter.DOWNLOAD_SETTING, Global.Parameter.API_GET); //抓setting資料
    }

    private void init(){
        setting = Setting.getInstance();
        ivPhotograph = findViewById(R.id.setting_iv_photograph);
        tvName = findViewById(R.id.setting_tv_name);
        tvEmail = findViewById(R.id.setting_tv_email);
        toolbar = findViewById(R.id.setting_toolbar);
        tvLogout = findViewById(R.id.setting_tv_logout);
        firstTimeLogin = false;
        isBackPressed = false;
    }

    private void loadDataFromBundle(){
        Bundle bundle = getIntent().getExtras();

        try {
            firstTimeLogin = bundle.getBoolean(Global.Parameter.FIRST_TIME_LOGIN);
        } catch (NullPointerException e) {}
    }

    private void setSettingValue(){
        //從後端拿資料
        if(firstTimeLogin){
            syncSetting(Global.Parameter.BUILD_SETTING, Global.Parameter.API_POST); //創setting資料
            syncSetting(Global.Parameter.BUILD_TOMATO, Global.Parameter.API_GET); //創tomato資料
        }
        syncSetting(Global.Parameter.DOWNLOAD_SETTING, Global.Parameter.API_GET); //抓setting資料
    }

    private void setBtnLogout(){
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncSetting(Global.Parameter.EDIT, Global.Parameter.API_PATCH); //上傳setting資料
                Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void setAccount(){
        tvName.setText(setting.getAccount().getUseName());
        tvEmail.setText(setting.getAccount().getUserEmail());
    }

    private void setNumberPickers(final NumberPicker npHour, final NumberPicker npMin, int initHour, int initMin, final int npType){
        setNumberPickerTextColor(npHour, getResources().getColor(R.color.defaultYellow,null));
        setNumberPickerTextColor(npMin, getResources().getColor(R.color.defaultYellow,null));
        npHour.setMaxValue(3);
        setMinRange(npMin, initHour);
        npHour.setValue(initHour);
        npMin.setValue(initMin);

        npHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setMinRange(npMin, newVal);
                setTime(npType, newVal, npMin.getValue());
            }
        });

        npMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setTime(npType, npHour.getValue(), newVal);
            }
        });
    }

    private void setMinRange(NumberPicker npMin, int hour){
        if(hour==0){
            npMin.setMinValue(1);
        }else{
            npMin.setMinValue(0);
        }
        if(hour!=3){
            npMin.setMaxValue(59);
        }else{
            npMin.setMaxValue(0);
        }
    }

    private void setTime(int npType, int hour, int min){
        int time = hour * Global.Parameter.TIME_UNIT  + min;
        switch (npType){
            case 0:
                setting.setTomatoTime(time);
                return;
            case 1:
                setting.setShortRelaxTime(time);
                return;
            case 2:
                setting.setLongRelaxTime(time);
                return;
        }

    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNumberPickerTextColor", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }

    private void setInfoWindow(){
        setPopupWindow(R.id.setting_ll_tomato_info, R.id.setting_tv_tomato_time, R.string.setting_tomato_info);
        setPopupWindow(R.id.setting_ll_short_relex_info, R.id.setting_tv_short_relex_time, R.string.setting_short_relex_info);
        setPopupWindow(R.id.setting_ll_long_relex_info, R.id.setting_tv_long_relex_time, R.string.setting_long_relex_info);
    }

    private void setPopupWindow(int clickedViewId, int relativeViewId, int showTextId){
        View clickedView = findViewById(clickedViewId);
        final View relativeView = findViewById(relativeViewId);

        View windowView = LayoutInflater.from(this).inflate(R.layout.popupwindow_setting_info, null);
        TextView tvShowText = windowView.findViewById(R.id.pop_setting_info_tv_showText);
        tvShowText.setText(showTextId);//設定popupwindow中顯示的內容

        final PopupWindow window = new PopupWindow(windowView);
        window.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable(true);
        window.setOutsideTouchable(false);

        clickedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.showAsDropDown(relativeView, 0, 0, Gravity.LEFT);
            }
        });
    }

    private void setNavigationIcon(){ //左上的返回圖示
        toolbar.setNavigationIcon(R.drawable.ic_go_left_40dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isBackPressed){ //防連點
                    isBackPressed = true;

                    syncSetting(Global.Parameter.EDIT, Global.Parameter.API_PATCH); //上傳setting資料
                    syncSetting(Global.Parameter.UPDATE_ALL, Global.Parameter.API_POST); //批次修改番茄鐘
                    Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                    if(firstTimeLogin){
                        startActivity(intent);
                    } else {
                        setResult(RESULT_OK, intent);
                    }
                    finish();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isBackPressed = false;
                        }
                    }, 1000);
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_to_right);
    }

    private void openAlbumAfterClickPhoto(){
        ivPhotograph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //讓使用者在相片總管中選擇相簿
                startActivityForResult(intent, Global.Parameter.OPEN_ALBUM_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case Global.Parameter.OPEN_ALBUM_REQUEST:  //裁剪完的圖片更新到ImageView
                    Uri uri = data.getData();

                    //上傳PATH
                    setting.setPhotoURI(uri.toString());
                    syncSetting(Global.Parameter.EDIT_PIC, Global.Parameter.API_PATCH);
                    setIvPhotograph(uri);
            }
        }
    }

    private void setIvPhotograph(Uri uri){
        try {
            ContentResolver cr = this.getContentResolver();
            InputStream inputStream = cr.openInputStream(uri); //開啟檔案位置, 讀取檔案, 轉成inputStream輸入串流
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream); //透過inputStream輸入串流, 將檔案轉成bitmap
            //更新ImageView
            ivPhotograph.setImageBitmap(bitmap); /* 將Bitmap設定到ImageView */
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
    }

    public void syncSetting(int action, int requestType) { // 加入int position 每次新增, 修改都要把位置存進去, 讀資料的時候才能知道怎麼排
        if(requestType != Global.Parameter.API_GET && action!=Global.Parameter.BUILD_SETTING && action!=Global.Parameter.BUILD_TOMATO){
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("minute", setting.getTomatoTime()); //一顆蕃茄時間
                if (action == Global.Parameter.UPDATE_ALL){
                    String dateformat = "yyyy-MM-dd";
                    SimpleDateFormat df = new SimpleDateFormat(dateformat);
                    String today = df.format(Calendar.getInstance().getTime());
                    jsonObject.put("today", today);
                } else {
                    jsonObject.put("path", setting.getPhotoURI());
                    jsonObject.put("short", setting.getShortRelaxTime());  //短休息時間
                    jsonObject.put("long", setting.getLongRelaxTime());  //長休息時間
                    jsonObject.put("battle", setting.getDirectlyGoToNextTask());
                    jsonObject.put("ring", setting.isRing());
                    jsonObject.put("vibration", setting.isVibration());
                }

                requestSync(action, requestType, jsonObject.toString());
            } catch (Exception e) { // JSONException
                System.out.println("資料有誤");
            }
        }else{
            requestSync(action, requestType, null);
        }
    }

    public void requestSync(final int action, final int requestType, final String data) {
        String apiCommand;
        if(action == Global.Parameter.BUILD_TOMATO){
            apiCommand = Global.Parameter.API_COMMAND_BUILDER;
        } else if (action == Global.Parameter.UPDATE_ALL){
            apiCommand = Global.Parameter.API_COMMAND_SYNC_ALL__TOMATO;
        } else {
            apiCommand = Global.Parameter.API_COMMAND_SYNC_SETTING + (action == Global.Parameter.DOWNLOAD_SETTING || action == Global.Parameter.BUILD_SETTING ? "": "/" + setting.getSettingId());
        }
//        System.out.println("apiCommand:" + apiCommand);

        NetworkController.getInstance().requestSync(apiCommand, requestType, data, new NetworkController.CCallback() {
            @Override
            public void fail(final int responseCode, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (responseCode){
                            case -1:
                                Toast.makeText(SettingActivity.this, R.string.network_error_connected_fail, Toast.LENGTH_SHORT).show();
                                return;
                        }
                        Toast.makeText(SettingActivity.this, R.string.network_error_sync_data_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void success(int responseCode, final JSONObject jsonData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            switch (action){
                                case Global.Parameter.DOWNLOAD_SETTING:
                                    JSONObject settingObject = jsonData.getJSONArray("setting").getJSONObject(0);
                                    int id = settingObject.getInt("id");
                                    setting.setSettingId(id);
                                    String path = settingObject.getString("path");
                                    setting.setPhotoURI(path);
//                                    System.out.println("path:" + path);
                                    if(path.equals(Global.Parameter.DEFAULT)){
                                        ivPhotograph.setImageBitmap(null);
                                    }else{
                                        Uri uri = Uri.parse(path);
                                        setIvPhotograph(uri);
                                    }

                                    int minute = settingObject.getInt("minute");
                                    setting.setTomatoTime(minute);
//                                    System.out.println("一顆蕃茄時間:"+ minute);

                                    int shortRelex = settingObject.getInt("short");
                                    setting.setShortRelaxTime(shortRelex);
//                                    System.out.println("短休息時間:"+ shortRelex);

                                    int longRelex = settingObject.getInt("long");
                                    setting.setLongRelaxTime(longRelex);
//                                    System.out.println("長休息時間:"+ longRelex);

                                    setNumberPicker();
                                    boolean battle = settingObject.getInt("battle") == 0 ? false:true;
//                                    System.out.println("battle:" + battle);
                                    setSwBattle(battle);

//                                    boolean ring = settingObject.getInt("ring") == 0 ? false:true;
//                                    setting.setRing(ring);
//
//                                    boolean vibration = settingObject.getInt("vibration") == 0 ? false:true;
//                                    setting.setVibration(vibration);
                                    break;
                                case Global.Parameter.UPDATE_ALL:
                                    System.out.println("批次更新蕃茄成功");
                                    break;
                            }
                        } catch(JSONException e){
                            Toast.makeText(SettingActivity.this, R.string.network_error_sync_data_fail, Toast.LENGTH_SHORT).show();
//                            System.out.println("Action:"+action);
                        }
                    }
                });

            }

            @Override
            public void complete() {}
        });
    }

    private void setSwBattle(boolean isBattleChecked){
        final Switch swNextTomato = findViewById(R.id.setting_sw_directly_go_to_next_task);
        final LinearLayout llLongRelex = findViewById(R.id.setting_ll_long_relex);
        setDirectlyGoToNextTask(llLongRelex, swNextTomato, isBattleChecked);

        swNextTomato.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDirectlyGoToNextTask(llLongRelex, swNextTomato, isChecked);
            }
        });
    }

    private void setDirectlyGoToNextTask(LinearLayout llLongRelex, Switch swNextTomato, boolean isChecked){
        if (isChecked) {
            llLongRelex.setVisibility(View.VISIBLE);
            setting.setDirectlyGoToNextTask(true);
            swNextTomato.setChecked(true);
        }else {
            llLongRelex.setVisibility(View.GONE);
            setting.setDirectlyGoToNextTask(false);
            swNextTomato.setChecked(false);
        }
    }

    private void setNumberPicker(){
//        System.out.println("getTomatoTime:" + setting.getTomatoTime());
//        System.out.println("getShortTime:" + setting.getShortRelaxTime());
//        System.out.println("getLongTime:" + setting.getLongRelaxTime());

        npTomatoHour = findViewById(R.id.setting_np_tomato_hour);
        npTomatoMin = findViewById(R.id.setting_np_tomato_min);
        setNumberPickers(npTomatoHour, npTomatoMin, setting.getTomatoTimeHour(), setting.getTomatoTimeMin(),0);
        npShortRelexHour = findViewById(R.id.setting_np_short_relex_hour);
        npShortRelexMin = findViewById(R.id.setting_np_short_relex_min);
        setNumberPickers(npShortRelexHour, npShortRelexMin, setting.getShortRelaxHour(), setting.getShortRelaxMin(),1);
        npLongRelexHour = findViewById(R.id.setting_np_long_relex_hour);
        npLongRelexMin = findViewById(R.id.setting_np_long_relex_min);
        setNumberPickers(npLongRelexHour, npLongRelexMin, setting.getLongRelaxHour(), setting.getLongRelaxMin(),2);
    }
}
