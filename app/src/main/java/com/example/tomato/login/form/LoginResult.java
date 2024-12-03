package com.example.tomato.login.form;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class LoginResult {
    @Nullable
    private LoggedInAccountView success;
    @Nullable
    private String error;

    LoginResult(String error) {
        this.error = error;
    }

    LoginResult(@Nullable LoggedInAccountView success) {
        this.success = success;
    }

    @Nullable
    LoggedInAccountView getSuccess() {
        return success;
    }

    @Nullable
    String getError() {
        return error;
    }
}
