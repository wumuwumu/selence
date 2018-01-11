package com.appscomm.selence;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appscomm.selence.bean.SmsBean;
import com.appscomm.selence.bean.SmsResultBean;
import com.appscomm.selence.utils.ShaUtils;
import com.google.gson.Gson;
import com.necer.ncalendar.utils.SPUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.Call;
import okhttp3.Response;

public class SettingActivity extends FragmentActivity {

    private AlertDialog alertDialog;
    private ImageView backArrow;
    private RelativeLayout contact;

    private String strMobile;
    private TextView title;
    private TextView contactNum;
    private RelativeLayout changeColor;
    private ImageView settingColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {
        backArrow = (ImageView) findViewById(R.id.iv_arrow);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(mOnClickListener);
        contact = (RelativeLayout) findViewById(R.id.rl_contact);
        contact.setOnClickListener(mOnClickListener);
        title = (TextView) findViewById(R.id.tv_title);
        title.setText("设置");
        changeColor = (RelativeLayout) findViewById(R.id.rl_change);
        changeColor.setOnClickListener(mOnClickListener);
        contactNum = (TextView) findViewById(R.id.tv_contact);
        settingColor = (ImageView) findViewById(R.id.iv_setting_color);

    }

    @Override
    protected void onResume() {
        super.onResume();
        int color = SPUtils.getInstance().getInt("devicecolor");
        if (color == 1) {
            settingColor.setBackgroundColor(getResources().getColor(R.color.device_bg_color1));
        } else if (color == 2) {
            settingColor.setBackgroundColor(getResources().getColor(R.color.device_bg_color2));
        } else if (color == 3) {
            settingColor.setBackgroundColor(getResources().getColor(R.color.device_bg_color3));
        }

    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_contact:
                    initSmsDialog();
                    break;
                case R.id.iv_arrow:
                    onBackPressed();
                    finish();
                    break;
                case R.id.rl_change:
                    startActivity(new Intent(SettingActivity.this, ChangeColorActivity.class));
                    break;
            }
        }
    };

    private void initSmsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.SMSDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_sms, null);
        Button btnCancle = (Button) view.findViewById(R.id.btn_cancle);
        Button btnAccept = (Button) view.findViewById(R.id.btn_accept);
        final EditText tle = (EditText) view.findViewById(R.id.et_tel);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strMobile = tle.getText().toString().trim();
                SPUtils.getInstance().put("tel", strMobile);
                contactNum.setText(strMobile);
                initSMS();
                alertDialog.dismiss();
            }
        });
        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();

    }

    private void initSMS() {
        String appid = "1400045063";
        String strRand = (int) ((Math.random() * 9 + 1) * 100000) + ""; //url中的random字段的值
        String msg = "温馨提示:朋友您好！姨妈来了，最近您的女神身体略感不适，请用您的关怀和爱意去温暖她，让她笑靥依旧！";
        strMobile = SPUtils.getInstance().getString("tel");
        String strAppKey = "e0a1e1ba438464d25be0ff8756e2a99a"; //sdkappid对应的appkey，需要业务方高度保密
        int strTime = (int) (System.currentTimeMillis() / 1000);
        String sig = ShaUtils.shaEncrypt("appkey=" + strAppKey + "&" + "random=" + strRand + "&" + "time=" + strTime + "&" + "mobile=" + strMobile);
        SmsBean smsBean = new SmsBean();
        SmsBean.TelBean mTelBean = new SmsBean.TelBean();
        mTelBean.setNationcode("86");
        mTelBean.setMobile(strMobile);
        Log.d("TAG", "TelBean = " + mTelBean.toString());
        smsBean.setTel(mTelBean);
        smsBean.setType(0);
        smsBean.setMsg(msg);
        smsBean.setSig(sig);
        smsBean.setTime(strTime);

        String url = "https://yun.tim.qq.com/v5/tlssmssvr/sendsms?sdkappid=" + appid + "&random=" + strRand;
        OkHttpUtils
                .postString()
                .url(url)
                .content(new Gson().toJson(smsBean))
                .build()
                .execute(new SmsResultCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.d("TAG", "e = " + e.toString());
                    }

                    @Override
                    public void onResponse(SmsResultBean response, int id) {
                        Log.d("TAG", "SmsResultBean = " + response.toString());
                    }
                });
    }

    public abstract class SmsResultCallback extends Callback<SmsResultBean> {
        @Override
        public SmsResultBean parseNetworkResponse(Response response, int id) throws Exception {

            String result = response.body().toString();
            SmsResultBean smsResultBean = new Gson().fromJson(result, SmsResultBean.class);
            Log.d("TAG", "SmsResultBean = " + smsResultBean.toString());
            return smsResultBean;
        }
    }
}
