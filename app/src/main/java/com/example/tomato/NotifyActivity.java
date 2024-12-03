package com.example.tomato;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.example.tomato.R;
import com.example.tomato.global.Global;

public class NotifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        setNotification(); //自訂義新的推播方式
        View view = getWindow().getDecorView(); //得到當前的view
        sendComposeMsg(view); //執行推播
    }

    private void setNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "compose"; //給定目前設定的推播方式一個ID, 以便之後用到
            String channelName = "推播"; //推播方式的名稱
            int importance = NotificationManager.IMPORTANCE_HIGH; //推播的重要度
            createNotificationChannel(channelId, channelName, importance); //創建新的推播方式
            openChannelSetting("compose"); //開啟推播方式設定頁面
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription("最重要的人");

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    public void openChannelSetting(String channelId)
    {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
        if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null)
            startActivity(intent);
    }

    public void sendComposeMsg(View view) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "compose") // 傳入之前定義的推播方式ID, 宣告執行此推播時的推播方式
                .setContentTitle("私信") //推播標題
                .setContentText("有人私信向你提出问题") //推播內容
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_person_24dp) //推播的圖示
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_person_24dp))
                .build();
        manager.notify(101, notification);
    }
}
