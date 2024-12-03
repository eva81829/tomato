package com.example.tomato.login.form;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tomato.controller.NfcController;
import com.example.tomato.global.Global;
import com.example.tomato.main.MainActivity;
import com.example.tomato.R;
import com.example.tomato.setting.Setting;
import com.example.tomato.setting.SettingActivity;

import java.sql.SQLOutput;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvRegister;
    private TextView tvForgetPassword;
    private Button loginButton;
    private ProgressBar loadingProgressBar; //loading
    private NfcController nfcController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setForgetPassword();
        setTvRegister();
        setErrorMsg(); //監聽是否有資料錯誤的通知, 跳對應的錯誤訊息
        setLoginBtn(); //按下Login Button後判斷Input是否正確, 若錯誤則發出通知, 給上一步的監聽, 正確則把資料給後端
        setLoginResult(); //後端連線結果正確則跳轉頁面
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            finish();
        }
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
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        tvRegister = findViewById(R.id.tv_register_page);
        tvForgetPassword = findViewById(R.id.tv_forget_password);
        loginButton = findViewById(R.id.btn_login);
        loadingProgressBar = findViewById(R.id.login_loading);
        nfcController = new NfcController(this, getClass());
    }

    private void setForgetPassword(){
        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.no_anim);
            }
        });
    }

    private void setTvRegister(){
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.no_anim);
            }
        });
    }

    private void setErrorMsg(){
        //監聽到LoginFormState onChanged, 則跳出錯誤訊息
        loginViewModel.getLoginFormStates().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
//                System.out.println("onChanged");
                if (loginFormState == null) {
                    return;
                }
                if (loginFormState.getEmailError() != null) {
                    etEmail.requestFocus();
                    etEmail.setError(getString(loginFormState.getEmailError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    etPassword.requestFocus();
                    etPassword.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });
    }

//    private void setKeyDone(){
        //每次按鍵盤上的勾勾後, 判斷是否可登入, 若輸入的user name及password正確就登入
//        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                        //判斷輸入的user name及password是否正確
//                        loginViewModel.loginDataChanged(etEmail.getText().toString(),
//                                etPassword.getText().toString());
//
//                        if(loginViewModel.getLoginFormStates().getValue().getIsDataValid()){
//                            loginViewModel.login(etEmail.getText().toString(),
//                                    etPassword.getText().toString());
//                        }
//                }
//                return false;
//            }
//        });
//    }

    private void setLoginBtn(){
        //按下login button, 若輸入的user name及password正確就把資料送到後端
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判斷輸入的user name及password是否正確, 錯誤則發出loginFormState change通知
                loginViewModel.loginDataChanged(etEmail.getText().toString(),
                        etPassword.getText().toString());

                if(loginViewModel.getLoginFormStates().getValue().getIsDataValid()){ //正確就把資料送到後端
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginViewModel.login(etEmail.getText().toString(),
                            etPassword.getText().toString());
                }
            }
        });
    }

    private void setLoginResult() {
        //和後端連線, 判斷登入是否成功
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) { //後端連線失敗
                    showLoginFailed(loginResult.getError());
                    return;
                }
                if (loginResult.getSuccess() != null) { //後端連線成功
                    Setting.getInstance().setAccount(loginResult.getSuccess().getAccount()); //把取得的account設定給setting, 當作唯一合法的account

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); //跳轉頁面
                    startActivity(intent);
                    updateUiWithUser(loginResult.getSuccess());
                }
                // setResult(Activity.RESULT_OK); //Activity 之間互傳值的時候用到
                //Complete and destroy login activity once successful
                finish();
            }
        });
    }

    //跟後端連線結果正確, 秀成功吐司
    private void updateUiWithUser(LoggedInAccountView model) {
        String welcome =getString(R.string.welcome) + "  " + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_SHORT).show();
    }

    //跟後端連線結果錯誤
    private void showLoginFailed(String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }
}
