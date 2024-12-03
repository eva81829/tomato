package com.example.tomato.login.form;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tomato.global.Global;
import com.example.tomato.R;
import com.example.tomato.setting.Setting;
import com.example.tomato.setting.SettingActivity;

public class RegisterActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private Toolbar toolbar;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirm;
    private EditText etName;
    private Button registerButton;
    private ProgressBar loadingProgressBar; //loading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        setToolbar();
        setErrorMsg(); //監聽是否有資料錯誤的通知, 跳對應的錯誤訊息
        setRegisterBtn(); //按下Login Button後判斷Input是否正確, 若錯誤則發出通知, 給上一步的監聽, 正確則把資料給後端
        setRegisterResult(); //後端連線結果正確則跳轉頁面
    }

    private void init(){
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        etConfirm = findViewById(R.id.et_register_confirm_password);
        etName = findViewById(R.id.et_register_name);
        registerButton = findViewById(R.id.btn_register);
        loadingProgressBar = findViewById(R.id.register_loading);
    }

    private void setToolbar(){
        toolbar = findViewById(R.id.register_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_go_left_40dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        toolbar.inflateMenu(R.menu.menu_confirm);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                imm.hideSoftInputFromWindow(toolbar.getWindowToken(), 0); //強制隱藏鍵盤
                return false;
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_to_right);
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
                if (loginFormState.getConfirmError() != null) {
                    etConfirm.requestFocus();
                    etConfirm.setError(getString(loginFormState.getConfirmError()));
                }
                if (loginFormState.getNameError() != null) {
                    etName.requestFocus();
                    etName.setError(getString(loginFormState.getNameError()));
                }
            }
        });
    }

    private void setRegisterBtn(){
        //按下login button, 若輸入的user name及password正確就把資料送到後端
        System.out.println("setRegisterBtn");
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("register clicked");

                //判斷輸入的user name及password是否正確, 錯誤則發出loginFormState change通知
                loginViewModel.registerDataChanged(etEmail.getText().toString(),
                        etPassword.getText().toString(),
                        etConfirm.getText().toString(),
                        etName.getText().toString());

                if(loginViewModel.getLoginFormStates().getValue().getIsDataValid()){ //正確就把資料送到後端
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginViewModel.registerResult(etEmail.getText().toString(),
                            etPassword.getText().toString(),
                            etName.getText().toString()
                    );
                }
            }
        });
    }

    private void setRegisterResult() {
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

                    Intent intent = new Intent(RegisterActivity.this, SettingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Global.Parameter.FIRST_TIME_LOGIN, true);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    updateUiWithUser(loginResult.getSuccess());
                }
                finish();
            }
        });
    }

    //跟後端連線結果正確
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
