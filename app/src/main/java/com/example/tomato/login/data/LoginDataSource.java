package com.example.tomato.login.data;

import com.example.tomato.controller.NetworkController;
import com.example.tomato.global.Global;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private Result<Account> result;

    public Result<Account> login(final String email, final String password) {
        result = new Result.Error(Global.Parameter.LOGIN_FAIL);

        try {
            String apiCommand = Global.Parameter.API_COMMAND_LOGIN;
            int requestType = Global.Parameter.API_POST;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("password", password);

            NetworkController.getInstance().requestSync(apiCommand, requestType, jsonObject.toString(), new NetworkController.CCallback() {
                @Override
                public void fail(int responseCode, String message) {
                    System.out.println(responseCode);
                    System.out.println(message);
                    result = new Result.Error(Global.Parameter.LOGIN_FAIL);
                }

                @Override
                public void success(int responseCode, JSONObject jsonData) {
                    try {
                        Account account = new Account(email);
                        account.setAccessToken(jsonData.getString("access_token"));
                        account.setTokenExpiresDate(jsonData.getString("expires_at"));
                        JSONObject user = jsonData.getJSONObject("user");
                        account.setUserId(user.getString("id"));
                        account.setUserName(user.getString("name"));

                        result = new Result.Success<Account>(account);
                    } catch (JSONException e) {
                    }
                }

                @Override
                public void complete() {}
            });
        } catch (Exception e) { // JSONException
            result = new Result.Error(e.getMessage());
        }

        return result;
    }

    public Result<Account> register(final String email, final String password, String name) {
        result = new Result.Error(Global.Parameter.LOGIN_FAIL);

        try {
            String apiCommand = Global.Parameter.API_COMMAND_REGISTER;
            int requestType = Global.Parameter.API_POST;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("name", name);

            NetworkController.getInstance().requestSync(apiCommand, requestType, jsonObject.toString(), new NetworkController.CCallback() {
                @Override
                public void fail(int responseCode, String message) {
                    System.out.println(message);
                    if(responseCode==422){
                        message = Global.Parameter.REGISTER_FAIL;
                    } else {
                        message = Global.Parameter.LOGIN_FAIL;
                    }
                    result = new Result.Error(message );
                }

                @Override
                public void success(int responseCode, JSONObject jsonData) {
                    System.out.println(responseCode);
                    result = login(email, password);
                }

                @Override
                public void complete() {}
            });
        } catch (Exception e) { // JSONException
            result = new Result.Error(e.getMessage());
        }
        return result;
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
