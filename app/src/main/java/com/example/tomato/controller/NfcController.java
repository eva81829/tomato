package com.example.tomato.controller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.os.Parcelable;
import android.widget.Toast;

import com.example.tomato.global.Global;

import java.util.Arrays;

public class NfcController {
    private static final String NFC_TOMATO_PASSWORD = "tomato";
    private static final String NFC_RELEX_PASSWORD = "relax";
    private PendingIntent pendingIntent;
    private NfcAdapter nfcAdapter;
    private Activity activity;

    public NfcController(Activity activity, Class<?> cls){ //cls傳入原本的class, 如果在此用getClass(), 會得到NfcController.class
        this.activity = activity;
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        pendingIntent = PendingIntent.getActivity(activity, 0,  new Intent(activity, cls).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
    }

    private boolean checkNfcSupport(){
        if (nfcAdapter == null) { //如果手機不支援NFC的功能
            Toast.makeText(activity, Global.Parameter.NFC_NOT_SUPPORT, Toast.LENGTH_SHORT).show();
            if (Global.Parameter.BLOCK_NFC_NOT_SUPPORT_DEVICE) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() { //每次按下後1000豪秒後執行
                        activity.finish();
                    }
                }, 1800);
            }
            return false;
        }
        return true;
    }

    private boolean checkNfcEnable(){
        if(!checkNfcSupport()){ //如果手機不支援NFC的功能
            return false;
        } else if(!nfcAdapter.isEnabled()){ //如果手機NFC沒開啟
            Toast.makeText(activity, Global.Parameter.NFC_NOT_LAUNCH, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void enableForegroundDispatch(){
        if (checkNfcEnable()) {
            //註冊前台排程系統, 當系統偵測到NFC tag, 若此activity在前台, 則此activity優先處理這個(如果此activity是single top則呼叫onNewIntent)
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
        }
    }

    public void disableForegroundDispatch(){
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    public int checkTagIsPaired(Intent intent){
        int result = Global.Parameter.NFC_TAG_NOT_MATCH;

        //如果是讀到tag(不論是否有NDEF)則繼續
        if (checkAction(intent)==0) {
            return Global.Parameter.NFC_NO_TAG_FOUND;
        }

        //判斷NDEF Tag規格如果是MIFARE Ultralight type2才繼續
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); //獲取tag
        Ndef ndef = Ndef.get(tag); //得到ndef
        if(ndef == null || !ndef.getType().equals("org.nfcforum.ndef.type2")) {
            return result;
        }

        //若包含NDEF message才繼續
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(rawMsgs == null) {
            return result;
        }

        //判斷NDEF record內容, 是否等於預設的番茄tag密碼/休息tag密碼
        NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
        for (int i = 0; i < rawMsgs.length; i++) {
            msgs[i] = (NdefMessage) rawMsgs[i];
        }
        NdefRecord ndefRecord = msgs[0].getRecords()[0]; //取得NDEF message中第0個NDEF record
        return parseRecord(ndefRecord); //解析NDEF record的內容
    }

    private int parseRecord(NdefRecord ndefRecord){
        switch (getTextRecord(ndefRecord)){
            case NFC_TOMATO_PASSWORD:
                return Global.Parameter.NFC_TAG_IS_TOMATO;
            case  NFC_RELEX_PASSWORD:
                return Global.Parameter.NFC_TAG_IS_RELEX;
        }
        return Global.Parameter.NFC_TAG_NOT_MATCH;
    }

    private String getTextRecord(NdefRecord ndefRecord) { //取出nfc record中的TNF01, Type T的Text資料, 解碼後取出資料
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return "";
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return "";
        }

        try {
            //获得字节数组，然后进行分析
            byte[] payloads = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payloads[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payloads[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
//            String languageCode = new String(payloads, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payloads, languageCodeLength + 1,
                    payloads.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    private int checkAction(Intent intent){
        switch (intent.getAction()){
            case NfcAdapter.ACTION_NDEF_DISCOVERED: //tag 符合NDEF 格式傳輸
                return 1;
            case NfcAdapter.ACTION_TECH_DISCOVERED: //tag  符合設定的規範
                return 2;
            case NfcAdapter.ACTION_TAG_DISCOVERED: //只要是Tag
                return 3;
        }
        return 0;
    }


}
