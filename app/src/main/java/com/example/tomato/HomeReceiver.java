package com.example.tomato;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.tomato.global.Global;
import com.example.tomato.main.MainActivity;

public class HomeReceiver extends BroadcastReceiver
{
    static public final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static public final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    static public final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction ();
        //按下Home键会发送ACTION_CLOSE_SYSTEM_DIALOGS的广播
        if (action.equals (Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        {
            String reason = intent.getStringExtra (SYSTEM_DIALOG_REASON_KEY);
            if (reason != null)
            {
                //前者是按Home鍵, 後者是長按Home鍵
                if (reason.equals (SYSTEM_DIALOG_REASON_HOME_KEY) || reason.equals( SYSTEM_DIALOG_REASON_RECENT_APPS ))
                {
//                    System.out.println("按了home");
                    Intent intent2 = new Intent (context, ClockActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity (context, 0, intent2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                    try
                    {
                        pendingIntent.send ();
                    } catch (PendingIntent.CanceledException e)
                    {
                        e.printStackTrace ();
                    }
                }
            }
        }

    }
}