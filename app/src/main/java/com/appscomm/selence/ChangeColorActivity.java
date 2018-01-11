package com.appscomm.selence;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.necer.ncalendar.utils.SPUtils;

public class ChangeColorActivity extends FragmentActivity {

    private ImageView backArrow;
    private TextView title;
    private RelativeLayout rl_shoose1;
    private RelativeLayout rl_shoose2;
    private RelativeLayout rl_shoose3;
    private ImageView iv_choose1;
    private ImageView iv_choose2;
    private ImageView iv_choose3;
    private RelativeLayout choose_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_color);
        initView();
    }

    private void initView() {
        backArrow = (ImageView) findViewById(R.id.iv_arrow);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(mOnClickListener);
        title = (TextView) findViewById(R.id.tv_title);
        title.setText("换肤");
        choose_device = (RelativeLayout) findViewById(R.id.rl_choose_device);
        rl_shoose1 = (RelativeLayout) findViewById(R.id.rl_choose1);
        rl_shoose2 = (RelativeLayout) findViewById(R.id.rl_choose2);
        rl_shoose3 = (RelativeLayout) findViewById(R.id.rl_choose3);
        iv_choose1 = (ImageView) findViewById(R.id.iv_choose1);
        iv_choose2 = (ImageView) findViewById(R.id.iv_choose2);
        iv_choose3 = (ImageView) findViewById(R.id.iv_choose3);
        rl_shoose1.setOnClickListener(mOnClickListener);
        rl_shoose2.setOnClickListener(mOnClickListener);
        rl_shoose3.setOnClickListener(mOnClickListener);

    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_arrow:
                    onBackPressed();
                    finish();
                    break;

                case R.id.rl_choose1:
                    iv_choose1.setVisibility(View.VISIBLE);
                    iv_choose2.setVisibility(View.INVISIBLE);
                    iv_choose3.setVisibility(View.INVISIBLE);
                    choose_device.setBackground(getResources().getDrawable(R.mipmap.choose1));
                    SPUtils.getInstance().put("devicecolor", 1);
                    break;
                case R.id.rl_choose2:
                    iv_choose1.setVisibility(View.INVISIBLE);
                    iv_choose2.setVisibility(View.VISIBLE);
                    iv_choose3.setVisibility(View.INVISIBLE);
                    choose_device.setBackground(getResources().getDrawable(R.mipmap.choose2));
                    SPUtils.getInstance().put("devicecolor", 2);
                    break;
                case R.id.rl_choose3:
                    iv_choose1.setVisibility(View.INVISIBLE);
                    iv_choose2.setVisibility(View.INVISIBLE);
                    iv_choose3.setVisibility(View.VISIBLE);
                    choose_device.setBackground(getResources().getDrawable(R.mipmap.choose3));
                    SPUtils.getInstance().put("devicecolor", 3);
                    break;
            }
        }
    };
}
