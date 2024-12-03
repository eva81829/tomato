package com.example.tomato.login.form;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tomato.global.Global;
import com.example.tomato.login.data.LoginRepository;
import com.example.tomato.login.data.Result;
import com.example.tomato.login.data.Account;
import com.example.tomato.R;

public class LoginViewModel extends ViewModel {
    private static final int PASSWORD_LENGTH = 7;
    private MutableLiveData<LoginFormState> loginFormStates = new MutableLiveData<>(); //被觀察者
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>(); //被觀察者
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormStates() {
        return loginFormStates;
    }
    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        // can be launched in a separate asynchronous job
        Result<Account> result = loginRepository.login(email, password); //與後端連結後回傳的結果

        if (result instanceof Result.Success) {
            Account account = ((Result.Success<Account>) result).getData(); //與後端連結後回傳的帳號
            loginResult.setValue(new LoginResult(new LoggedInAccountView(account)));
        } else {
            String error = ((Result.Error) result).getError();
            loginResult.setValue(new LoginResult(error));
        }
    }

    public void registerResult(String username, String password, String name) {
        // can be launched in a separate asynchronous job
        Result<Account> result = loginRepository.register(username, password, name);

        if (result instanceof Result.Success) {
            Account account = ((Result.Success<Account>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInAccountView(account)));
        } else {
            String error = ((Result.Error) result).getError();
            loginResult.setValue(new LoginResult(error));
        }
    }

    public void loginDataChanged(String email, String password) {
        LoginFormState loginFormState = new LoginFormState();
        if (!isEmailValid(email)){
            loginFormState.setEmailError(R.string.invalid_email);
        }
        if (!isPasswordValid(password)){
            loginFormState.setPasswordError(R.string.invalid_password);
        }
        if(isEmailValid(email) && isPasswordValid(password)){
            loginFormState.setDataValid(true);
        }
        loginFormStates.setValue(loginFormState);
    }

    public void registerDataChanged(String email, String password, String confirm, String name) {
        LoginFormState loginFormState = new LoginFormState();
        if (!isPasswordValid(password)){
            loginFormState.setPasswordError(R.string.invalid_password);
        }
        if (!isEmailValid(email)){
            loginFormState.setEmailError(R.string.invalid_email);
        }
        if (!isConfirmValid(password, confirm)){
            loginFormState.setConfirmError(R.string.invalid_confirm);
        }
        if (!isNameValid(name)){
            loginFormState.setNameError(R.string.invalid_name);
        }
        if(isEmailValid(email) && isPasswordValid(password) && isConfirmValid(password, confirm) && isNameValid(name)){
            loginFormState.setDataValid(true);
        }
        loginFormStates.setValue(loginFormState);
    }

    // A placeholder confirm validation check
    private boolean isEmailValid(String email) {
        if (!email.trim().isEmpty() && email.contains("@")) { //至少包含@
            return true; //Patterns.EMAIL_ADDRESS.matcher(email).matches(); //符合格式 "_@_._"
        }
        return false; //!email.trim().isEmpty()  => trim()刪除頭尾空白
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return !password.trim().isEmpty() && password.trim().length() > PASSWORD_LENGTH;
    }

    // A placeholder email validation check
    private boolean isConfirmValid(String password, String confirm) {
        if (!confirm.trim().isEmpty() && password.equals(confirm)) {
            return true;
        }
        return false;
    }

    private boolean isNameValid(String name) {
        if (!name.trim().isEmpty()) {
            return true;
        }
        return false;
    }
}
