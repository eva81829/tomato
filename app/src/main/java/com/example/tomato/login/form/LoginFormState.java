package com.example.tomato.login.form;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    @Nullable
    private Integer emailError;
    @Nullable
    private Integer passwordError;
    @Nullable
    private Integer confirmError;
    @Nullable
    private Integer nameError;
    private boolean isDataValid; //用來判斷所有的值是否都正確, 正確才跳轉頁面

    public LoginFormState() {
        isDataValid = false;
    }

    public void setEmailError(@Nullable Integer emailError) {
        this.emailError = emailError;
    }

    public void setConfirmError(@Nullable Integer confirmError) {
        this.confirmError = confirmError;
    }

    public void setPasswordError(@Nullable Integer passwordError) {
        this.passwordError = passwordError;
    }

    public void setNameError(@Nullable Integer nameError) {
        this.nameError = nameError;
    }

    public void setDataValid(boolean isDataValid) {
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getEmailError() {
        return emailError;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    public Integer getConfirmError() {
        return confirmError;
    }

    @Nullable
    public Integer getNameError() {
        return nameError;
    }


    public boolean getIsDataValid() {
        return isDataValid;
    }
}
