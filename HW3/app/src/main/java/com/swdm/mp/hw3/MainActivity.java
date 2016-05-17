package com.swdm.mp.hw3;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class MainActivity extends Activity {
sendSms sms = new sendSms(); //javascript connection class
WebView webView;//webView of html
    Context mContext = this; // for toast message
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView)findViewById(R.id.webView); // syncronize
        webView.getSettings().setJavaScriptEnabled(true);//In this webView enable javascript
        webView.addJavascriptInterface(sms, "sendSms"); //connction with javascript code
        webView.loadUrl("file:///android_asset/sendScreen.html");// Floating this web view

    }

    //Mobile phone number validation
    public static boolean isCellPhone(String str) {
     //010,016,017,018,019
        return Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", str);
    }

//Receive and save from javascript data
    class sendSms{
        String number="";//number initialization
        String txt="";//message initialization
        @JavascriptInterface
        public void inputNum(String num){
              number+=num;//receipt input number attatch

//            Toast.makeText(MainActivity.this,number,Toast.LENGTH_LONG).show(); //test
            }

        @JavascriptInterface
        public String getNum(){ //floating phone number in javascript phone number input window
            return number;
        }



    @JavascriptInterface //When user input message
        public void msgScreen(String msg){

            txt=msg;//Save at txt which receipt message from javascript

            if(isCellPhone(number)&&!txt.equals("")) //When user number input satisfied Mobile Phone Number format and Exist message
            {
                sendSMS(number, txt);//send SMS
                number="";//initialization number
                txt="";//initialization message
            }
            else if(!isCellPhone(number)) {//Unsatisfied phone number format
                Toast.makeText(MainActivity.this,"Invalid Number",Toast.LENGTH_LONG).show();


            }
            else if(txt.equals("")){//When user input is blank

                Toast.makeText(MainActivity.this,"Please input your message",Toast.LENGTH_LONG).show();

            }
/*
            else {//Unsatisfied phone number format
                Toast.makeText(MainActivity.this,"Invalid Number",Toast.LENGTH_LONG).show();


            }
*/

        }






        public void sendSMS(String smsNumber, String smsText){
            PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent("SMS_SENT_ACTION"), 0);//define sentIntent
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);//define deliverIntent

            /**
             * SMS가 발송될때 실행
             * When the SMS massage has been sent
             */
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch(getResultCode()){
                        case Activity.RESULT_OK:
                            // 전송 성공
                            Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            // 전송 실패
                            Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            // 서비스 지역 아님
                            Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            // 무선 꺼짐
                            Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            // PDU 실패
                            Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter("SMS_SENT_ACTION"));

            /**
             * SMS가 도착했을때 실행
             * When the SMS massage has been delivered
             */
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()){
                        case Activity.RESULT_OK:
                            // 도착 완료
                            Toast.makeText(mContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            // 도착 안됨
                            Toast.makeText(mContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter("SMS_DELIVERED_ACTION"));

            SmsManager mSmsManager = SmsManager.getDefault();
            mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
        }

    }
}
