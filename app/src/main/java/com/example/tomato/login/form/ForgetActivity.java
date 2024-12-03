package com.example.tomato.login.form;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tomato.R;
import com.example.tomato.controller.NetworkController;
import com.example.tomato.global.Global;

import org.json.JSONObject;

public class ForgetActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText etEmail;
    private Button sendButton;
    private boolean isSendPressed;
    private InputMethodManager imm;
    private ProgressBar pbSending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        init();
        setToolbar();
        setSendBtn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pbSending.setVisibility(View.INVISIBLE);
    }

    private void init(){
        pbSending = findViewById(R.id.forget_progressBar);
        sendButton = findViewById(R.id.btn_forget_send);
        etEmail = findViewById(R.id.et_forget_email);
        isSendPressed = false;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void setSendBtn(){
        //按下login button, 若輸入的user name及password正確就把資料送到後端
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = etEmail.getText().toString();
                if(email.trim().isEmpty() || !email.contains("@")){
                    Toast.makeText(ForgetActivity.this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(sendButton.isClickable()){ //防連點
                    pbSending.setVisibility(View.VISIBLE);
                    imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0); //強制隱藏鍵盤
                    sendButton.setClickable(false);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("send clicked");
                            sendButton.setClickable(true);
                            send(email);
                        }
                    }, 3000);
                }
            }
        });
    }

    private void setToolbar(){
        toolbar = findViewById(R.id.forget_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_go_left_40dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        toolbar.inflateMenu(R.menu.menu_confirm);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0); //強制隱藏鍵盤
                return false;
            }
        });
    }

    public void send(final String email){
        try {
            String apiCommand = Global.Parameter.API_COMMAND_FORGET_PASSWORD;
            int requestType = Global.Parameter.API_POST;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);

            NetworkController.getInstance().requestSync(apiCommand, requestType, jsonObject.toString(), new NetworkController.CCallback() {
                @Override
                public void fail(final int responseCode, String message) {
                    System.out.println(responseCode);
                    System.out.println(message);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (responseCode){
                                case -1:
                                    Toast.makeText(ForgetActivity.this, R.string.network_error_connected_fail, Toast.LENGTH_SHORT).show();
                                    return;
                            }
                            Toast.makeText(ForgetActivity.this, R.string.network_error_send_email_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void success(int responseCode, JSONObject jsonData) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Dialog dialog = new AlertDialog.Builder(ForgetActivity.this)
                                    .setMessage(R.string.dialog_send)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                                            setResult(RESULT_CANCELED, intent);
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    });

                }

                @Override
                public void complete() {
                    pbSending.setVisibility(View.INVISIBLE);
                }
            });
        } catch (Exception e) { // JSONException

        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_to_right);
    }

}
