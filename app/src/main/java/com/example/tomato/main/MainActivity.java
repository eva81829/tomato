package com.example.tomato.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.tomato.controller.NetworkController;
import com.example.tomato.controller.NfcController;
import com.example.tomato.R;
import com.example.tomato.setting.Setting;
import com.example.tomato.setting.SettingActivity;
import com.example.tomato.global.Global;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private NfcController nfcController;
    private NavController navController;
    private boolean isSettingPressed;
    private boolean isCalendarPressed;
    private Fragment currentFragment;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Global.Function.setTransparentStatusBar(this, R.color.defaultWhite);
        setContentView(R.layout.activity_main);
        init();
        setToolbar();
        setFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switch (nfcController.checkTagIsPaired(intent)) {
            case Global.Parameter.NFC_TAG_NOT_MATCH:
                Toast.makeText(this, Global.Parameter.NFC_ERROR_NOT_OUR_TAG, Toast.LENGTH_SHORT).show();
                return;
            case Global.Parameter.NFC_TAG_IS_TOMATO:
                return;
            case Global.Parameter.NFC_TAG_IS_RELEX:
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
    }

    private void init(){
        isSettingPressed = false;
        isCalendarPressed = false;
        nfcController = new NfcController(this, getClass());
    }

    private void setToolbar(){
        toolbar = findViewById(R.id.main_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_setting_35dp);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() { //左上圖示
            @Override
            public void onClick(View v) {
                if(!isSettingPressed){ //防連點
                    isSettingPressed = true;

                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivityForResult(intent, Global.Parameter.GO_TO_SETTING_ACTIVITY);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.no_anim);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isSettingPressed = false;
                        }
                    }, 1000);
                }
            }
        });

        //跳到報表的網頁
        toolbar.inflateMenu(R.menu.menu_calendar);  //右上圖示
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(!isCalendarPressed){ //防連點
                    isCalendarPressed = true;

                    String uriString = Global.Parameter.API_URL_ROOT + Global.Parameter.API_COMMAND_CALENDAR + "/" + Setting.getInstance().getAccount().getUserId();
                    Uri uri = Uri.parse(uriString);
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(intent);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isCalendarPressed = false;
                        }
                    }, 1000);
                }
                return false;
            }
        });
    }

    private void setFragment(){
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.navigation_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
