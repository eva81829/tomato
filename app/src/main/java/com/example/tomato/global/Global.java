package com.example.tomato.global;

import android.app.Activity;
import android.os.Build;
import android.view.WindowManager;

import com.example.tomato.R;


public class Global {
    public static class Parameter{
        public static final boolean PRESENT_MODE = true; //demo模式, default番茄時間5秒
        public static final boolean BLOCK_NFC_NOT_SUPPORT_DEVICE = false; //當裝置不包含NFC的時候是否阻擋
        public static final boolean USE_DEFAULT_TOKEN = true; //使用測試用的token

        //Setting
        public static final int DEFAULT_TOMATO_TIME = 25; //單位分鐘
        public static final int DEFAULT_SHORT_RELAX_TIME = 5; //單位分鐘
        public static final int DEFAULT_LONG_RELAX_TIME = 60; //單位分鐘

        public static final int DEFAULT_PRESENT_TOMATO_TIME = 5; //單位秒鐘
        public static final int DEFAULT_PRESENT_SHORT_RELAX_TIME = 3; //單位秒鐘
        public static final int DEFAULT_PRESENT_LONG_RELAX_TIME = 5; //單位秒鐘
        public static final boolean DEFAULT_HIDE_STOP_TASK_OPTION = true; //學霸模式: 無法隨意關閉(NFC/手搖)
        public static final boolean DEFAULT_DIRECTLY_GO_TO_NEXT_TASK = true; //嚴格模式: 直接開始下一個任務

        //Login
        public static final String LOGIN_FAIL = "請重新檢查您的帳號密碼";
        public static final String REGISTER_FAIL = "帳號已註冊";

        //Account
        public static final String DEFAULT_USER_EMAIL = "@";
        public static final String DEFAULT_USER_NAME = "123";
        public static final String DEFAULT_PASSWORD = "12345678";
        public static final String DEFAULT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6Ijc4Y2U1OTkyZTY0NmIzNDI4ZTc5YzhkMTIwZmRiZjNlMTdkMGQ1ZDQ0ZGZmNGVjOTUzOTg0ZTdlNGU3ODU5ZmI3ZTdkYzEzZDAzNWJhYTA0In0.eyJhdWQiOiIxIiwianRpIjoiNzhjZTU5OTJlNjQ2YjM0MjhlNzljOGQxMjBmZGJmM2UxN2QwZDVkNDRkZmY0ZWM5NTM5ODRlN2U0ZTc4NTlmYjdlN2RjMTNkMDM1YmFhMDQiLCJpYXQiOjE1ODAzNzM2NzMsIm5iZiI6MTU4MDM3MzY3MywiZXhwIjoxNjExOTk2MDczLCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.Dl2k4KHamoequwvH6s_um_-q9XXRjKvyf6oWe8VQYIb97M5I9TCp03-NosaOT11bWMrxdull7vKWvmme2ljk2CTTGloPccaXW3dbiwFS2nU3Ej7h1JgmbgiyCdVmmbDMeJOI7iobXVNhN9aHapIOP5A9hMHBDwKdoHSlJL1x3zmdqoKoMMQ5tBVnoKoln_WNfTBBTRrBszYQur7alDsn9VrHv5sYbki7d14KpNZ-igPTljbV5dbGoTvFGuQn-h6zbpg2A7Huu4lrELf8WGkUfDBA3STuGFdSfAs4Ml-Ii5553jvD1cVYsXDddSrgj5l0Su3EmTetnNlYIZWhMYBBPOFLRn-Rq2tivEhn9ZY81dTNoMZnDRt-3t_ZTz5QEjLIN6tR0sW5J94RLt8557OXK-zO1-VEulRhDV0LlCV-t3hYTCYxDQm6rOujLOUlwN-7cR6PeTvo12cw3_UQdA31qUrHKJemj6t8M8z3CCq8LcLcaVQ-ahnBynRU-CDJKBYbJlpxkv1XHORPYSfIflqxijixVB4khLxNaD4lr89FVmH1XMSyB5BnArlTGmogjr2wa4WkIP90UMIDE6icu8fGldEP758FNnK9-1uek5kB3trcjyipGLJOVFWJchplZYxFHAB8oQPtZW-Z7hzdY5caAe3X_7kLePakR8T75CUSvDM";

        //API
        public static final String API_URL_ROOT = "http://34.85.51.56/";
        public static final String API_COMMAND_CALENDAR= "charts";
        public static final String API_COMMAND_LOGIN = "api/auth/login";
        public static final String API_COMMAND_REGISTER = "api/auth/register";
        public static final String API_COMMAND_BUILDER = "api/auth/builder";
        public static final String API_COMMAND_FORGET_PASSWORD = "api/warning";
        public static final String API_COMMAND_SYNC_LIST = "api/v1/task";
        public static final String API_COMMAND_SYNC_TOMATO = "api/v1/exam/1/tomato";
        public static final String API_COMMAND_SYNC_ALL__TOMATO = API_COMMAND_SYNC_TOMATO + "/reset_tomatoes_minute";
        public static final String API_COMMAND_SYNC_SETTING= "api/v1/setting";

        public static final String API_HEADER_KEY_RETURN_JSON = "Accept";
        public static final String API_HEADER_VALUE_RETURN_JSON = "application/json";
        public static final String API_HEADER_KEY_AUTHORIZE = "Authorization";
        public static final String API_HEADER_VALUE_AUTHORIZE = "Bearer ";

        //API requestType
        public static final int API_GET = 0;
        public static final int API_POST = 1;
        public static final int API_PATCH = 2;
        public static final int API_DELETE = 3;

        //API action
        public static final int ADD = 0;
        public static final int EDIT = 1;
        public static final int EDIT_PIC = 7;
        public static final int REMOVE = 2;
        public static final int DONE = 3;
        public static final int BUILD_TOMATO = 4;
        public static final int BUILD_SETTING = 5;
        public static final int UPDATE_ALL = 8;
        public static final int DOWNLOAD_SETTING = 6;
        public static final int DOWNLOAD = 9;

        //RequestCode
        public static final int OPEN_ALBUM_REQUEST = 1;
        public static final int GO_TO_SETTING_ACTIVITY = 2;
        public static final int GO_TO_CLOCK_ACTIVITY = 3;

        //Bundle
        public static final String TOMATO_TASKS = "Tomatos";
        public static final String FINISHED_TASKS = "FinishedTasks";
        public static final String FIRST_TIME_LOGIN = "FirstTimelogin";
        
        //Tomato
        public static final int TOMATO_MAXIMUM = 8;
        public static final int TIME_UNIT = 60;
        public static final int TASK_UNDONE = 0;
        public static final int TASK_ONTIME = 1;
        public static final int TASK_DELAY = 2;
        public static final String DEFAULT = "default";

        //Clock
        public static final String RELEX_TASK = "休息";
        public static final String SHORT_RELEX_TASK = "短休息";
        public static final String LONG_RELEX_TASK = "長休息";
        public static final int STATUS_UNSTART = 0; //一進番茄鐘的狀態
        public static final int STATUS_PAUSE = 1;
        public static final int STATUS_TOMATO_PROCESSING = 2; //番茄鐘開始的狀態
        public static final int STATUS_TOMATO_DONE = 3; //番茄鐘完成的狀態
        public static final int STATUS_SHORT_RELAX_PROCESSING = 4; //短休息開始的狀態
        public static final int STATUS_SHORT_RELAX_DONE = 5; //短休息結束的狀態
        public static final int STATUS_LONG_RELAX_PROCESSING = 6; //長休息開始的狀態
        public static final int STATUS_LONG_RELAX_DONE = 7; //長休息結束的狀態
        public static final int STATUS_TOMATO_DELAY = 8; //延長狀態
        public static final boolean VISIBLE = true;
        public static final boolean INVISIBLE = false;

        //NFC
        public static final String NFC_NOT_SUPPORT = "您的裝置不支援NFC";
        public static final String NFC_NOT_LAUNCH = "請開啟NFC功能";
        public static final String NFC_ERROR_NOT_OUR_TAG = "不要亂嗶";
        public static final String NFC_ERROR_NOT_TOMATO_TAG = "這不是番茄鐘"; //該開始工作的時候, 嗶旁邊的休息tag
        public static final String NFC_ERROR_NOT_RELEX_TAG = "這不是休息鐘"; //該開始休息的時候, 嗶旁邊的工作tag
        public static final int NFC_NO_TAG_FOUND = 0;
        public static final int NFC_TAG_IS_TOMATO = 1;
        public static final int NFC_TAG_IS_RELEX = 2;
        public static final int NFC_TAG_NOT_MATCH = 3;
    }

    public static class Function {
        public static final void setTransparentStatusBar(Activity activity, int color) {
            //5.0及以上自訂顏色
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().setStatusBarColor(activity.getResources().getColor(color,null));
                //4.4到5.0變透明
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
                localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
            }
        }

        public static int getHour(int time){ //time單位: 分鐘
            return time/Global.Parameter.TIME_UNIT;
        }

        public static int getMin(int time){ //time單位: 分鐘
            return time%Global.Parameter.TIME_UNIT;
        }
    }
}
