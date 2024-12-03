package com.example.tomato.login.form;

import com.example.tomato.login.data.Account;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInAccountView {
    private Account account;
    //... other data fields that may be accessible to the UI

    LoggedInAccountView(Account account) {
        this.account = account;
    }

    public String getDisplayName() {
        return account.getUseName();
    }

    public Account getAccount() {
        return account;
    }
}
