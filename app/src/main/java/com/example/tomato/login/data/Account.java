package com.example.tomato.login.data;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class Account {
    private String userEmail;
    private String userId;
    private String userName;
    private String accessToken;
    private String tokenExpiresDate;


    public Account(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Account setUserName(String useName) {
        this.userName = useName;
        return this;
    }

    public Account setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Account setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public Account setTokenExpiresDate(String tokenExpiresDate) {
        this.tokenExpiresDate = tokenExpiresDate;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public String getUseName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
