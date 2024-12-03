package com.example.tomato.controller;

import com.example.tomato.global.Global;
import com.example.tomato.setting.Setting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkController {
    private static final String TAG = "NetworkController";

    private static NetworkController networkController;
    private OkHttpClient client;

    private NetworkController(){
        client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static NetworkController getInstance(){
        if(networkController == null){
            networkController = new NetworkController();
        }
        return networkController;
    }

    private static class CallbackAdapter implements Callback{
        private CCallback cCallback;
        public CallbackAdapter(CCallback cCallback){
            this.cCallback = cCallback;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            cCallback.fail(-1, e.getMessage());
            cCallback.complete();
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response){
            try{
                analyzeResponse(response, cCallback);
            } catch (IOException e) {
                cCallback.fail(-1, e.getMessage());
            }
        }
    }

    public interface CCallback{ // 自己定義的CCallback  Class, 擴充原本的Callback (也是新的對外的接口)
        public void fail(int responseCode, String message); //將原本onFailure/onResponse, 根據拿到的data&error msg, 進一步判斷並將結果重新分為fail/success
        public void success(int responseCode, JSONObject jsonData);
        public void complete(); //新增complete, 不論是onFailure/onResponse, 最後一步都要執行
    }

    public void requestSync(final String apiCommand, final int requestType, final String data, final CCallback cCallback){ //同步多執行緒, 等到response完才繼續進行Main thread(在主執行緒不能用網路連線, 要自己另開子執行緒)
        Thread thread = new Thread(){
            @Override
            public void run(){
                Request request = setRequest(apiCommand, requestType, data);
                try {
                    Response response = client.newCall(request).execute(); //主要差別
                    analyzeResponse(response, cCallback);
                } catch (IOException e) {
                    System.out.println("檢查你的wifi有沒有開!!!");
                    cCallback.fail(-1, e.getMessage());
                }
            }
        };

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            cCallback.fail(-1, e.getMessage());
        }
    }

    public void requestASync(String apiCommand, int requestType, String data, CCallback cCallback){ //異步多執行緒, response同時進行Main thread
        Request request = setRequest(apiCommand, requestType, data);
        client.newCall(request).enqueue(new CallbackAdapter(cCallback)); //主要差別
    }

    private Request setRequest(String apiCommand, int requestType, String data){
        String accessToken = Setting.getInstance().getAccount().getAccessToken();

        Request.Builder requestBuilder = new Request.Builder()
                .header(Global.Parameter.API_HEADER_KEY_RETURN_JSON, Global.Parameter.API_HEADER_VALUE_RETURN_JSON)
                .header(Global.Parameter.API_HEADER_KEY_AUTHORIZE, Global.Parameter.API_HEADER_VALUE_AUTHORIZE + accessToken);

        return setRequestBody(requestBuilder, apiCommand, requestType, data);
    }

    private Request setRequestBody(Request.Builder requestBuilder, String apiCommand, int requestType, @Nullable String data){
        String url = Global.Parameter.API_URL_ROOT + apiCommand;
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        if(data == null){
            data = "";
        }
        RequestBody requestBody = FormBody.create(data, mediaType);
        switch (requestType){
            case Global.Parameter.API_GET:
                return requestBuilder.url(url).get().build();
            case Global.Parameter.API_POST:
                return requestBuilder.url(url).post(requestBody).build();
            case Global.Parameter.API_PATCH:
                return requestBuilder.url(url).patch(requestBody).build();
            case Global.Parameter.API_DELETE:
                return requestBuilder.url(url).delete(requestBody).build();
        }
        System.out.println("null");
        return null;
    }


    public static void analyzeResponse(Response response, CCallback cCallback) throws IOException{
        int responseCode = response.code();
        System.out.println(responseCode);
        String stringData = response.body().string();  //只能呼叫response.body().string()一次, 否則程式會報錯
        try {
            JSONObject jsonData = new JSONObject(stringData); //把response的str轉成JSONObject
            String message = jsonData.getString("message");
            System.out.println(stringData.toString());

            if(message.equals("success")){ //jsonObject.getString("statusCode").equals("200")
                System.out.println("Success");
                cCallback.success(responseCode, jsonData);
            }else {
                System.out.println("Fail");
                cCallback.fail(responseCode, message); //如果message不為success, 就把錯誤訊息傳入cCallback.onFailure
            }
        } catch (JSONException e) {
            System.out.println("Jasonfail");
            cCallback.fail(responseCode, e.getMessage());
        } finally {
//            System.out.println("Completed");
            cCallback.complete();
        }
    }
}
