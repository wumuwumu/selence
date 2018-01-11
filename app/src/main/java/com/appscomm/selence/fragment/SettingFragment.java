package com.appscomm.selence.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appscomm.selence.R;
import com.appscomm.selence.utils.DateUtil;
import com.necer.ncalendar.utils.SPUtils;
import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;
import com.bigkoo.pickerview.listener.CustomListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {

    private RelativeLayout menstruationDays;
    private RelativeLayout menstruationPeriod;
    private RelativeLayout menstruationPreday;
    private TimePickerView pvTime;
    private OptionsPickerView pvCustomOptions;
    private OptionsPickerView pvCustomOptions2;

    private TextView day;
    private TextView periodDay;
    private TextView preday;

    private String selectDay;
    private String selectPeriodDay;
    private String selectPreday;

    private List<Integer> days;
    private List<Integer> periodDays;
    private TextView title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        menstruationDays = (RelativeLayout) view.findViewById(R.id.rl_menstruation_days);
        menstruationPeriod = (RelativeLayout) view.findViewById(R.id.rl_menstruation_period);
        menstruationPreday = (RelativeLayout) view.findViewById(R.id.rl_menstruation_preday);
        preday = (TextView) view.findViewById(R.id.tv_preday);
        day = (TextView) view.findViewById(R.id.tv_day);
        periodDay = (TextView) view.findViewById(R.id.tv_period);
        title = (TextView) view.findViewById(R.id.tv_title);
        title.setText("经期信息");
        menstruationDays.setOnClickListener(mOnClickListener);
        menstruationPeriod.setOnClickListener(mOnClickListener);
        menstruationPreday.setOnClickListener(mOnClickListener);
        initTimePicker();
        initCustomOptionPicker();
        initCustomOptionPicker2();
        initSpUtils();
    }

    private void initSpUtils() {
        selectDay = SPUtils.getInstance().getString("days");
        selectPeriodDay = SPUtils.getInstance().getString("periodDay");
        selectPreday = SPUtils.getInstance().getString("preday");

        if (selectDay != null) {
            day.setText(selectDay);
        }
        if (selectPeriodDay != null) {
            periodDay.setText(selectPeriodDay);
        }
        if (selectPreday != null) {
            preday.setText(selectPreday);
        }
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.rl_menstruation_days:
                    pvCustomOptions.show();
                    break;
                case R.id.rl_menstruation_period:
                    pvCustomOptions2.show();
                    break;
                case R.id.rl_menstruation_preday:
                    pvTime.show();
                    break;
            }
        }
    };

    private void initTimePicker() {
        //控制时间范围(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
        //因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2000, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2200, 11, 31);
        //时间选择器
        pvTime = new TimePickerView.Builder(getActivity(), new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                // 这里回调过来的v,就是show()方法里面所添加的 View 参数，如果show的时候没有添加参数，v则为null
                /*btn_Time.setText(getTime(date));*/
                SPUtils.getInstance().put("preday", getTime(date));
                preday.setText(getTime(date));

                long millis = DateUtil.getStringToDate(getTime(date), "yyyy-MM-dd") / 1000;
                SPUtils.getInstance().put("millis", millis);
            }
        })
                .setLayoutRes(R.layout.pickerview_custom_time, new CustomListener() {

                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        ImageView ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvTime.returnData();
                                pvTime.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvTime.dismiss();
                            }
                        });
                    }
                })
                .setType(new boolean[]{true, true, true, false, false, false})
                .setLabel("", "", "", "", "", "") //设置空字符串以隐藏单位提示   hide label
                .setDividerColor(Color.DKGRAY)
                .setContentSize(20)
                .setDate(selectedDate)
                .setRangDate(startDate, selectedDate)
                .setBackgroundId(0x00000000)
                .setOutSideCancelable(false)
                .build();
    }


    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }


    private void initCustomOptionPicker() {//条件选择器初始化，自定义布局
        getDate();
        /**
         * @description
         *
         * 注意事项：
         * 自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针。
         * 具体可参考demo 里面的两个自定义layout布局。
         */
        pvCustomOptions = new OptionsPickerView.Builder(getActivity(), new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String tx = days.get(options1).toString();
                SPUtils.getInstance().put("days", tx);
                day.setText(tx);
            }
        })
                .setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        ImageView ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomOptions.returnData();
                                pvCustomOptions.dismiss();
                            }
                        });

                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomOptions.dismiss();
                            }
                        });
                    }
                })
                .build();
        pvCustomOptions.setPicker(days);//添加数据

    }

    private void getDate() {
        days = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            days.add(i);
        }
    }


    private void initCustomOptionPicker2() {//条件选择器初始化，自定义布局
        getperiodDate();
        /**
         * @description
         *
         * 注意事项：
         * 自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针。
         * 具体可参考demo 里面的两个自定义layout布局。
         */
        pvCustomOptions2 = new OptionsPickerView.Builder(getActivity(), new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String tx = periodDays.get(options1).toString();
                SPUtils.getInstance().put("periodDay", tx);
                periodDay.setText(tx);
            }
        })
                .setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        ImageView ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomOptions2.returnData();
                                pvCustomOptions2.dismiss();
                            }
                        });

                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomOptions2.dismiss();
                            }
                        });
                    }
                })
                .build();
        pvCustomOptions2.setPicker(periodDays);//添加数据

    }

    private void getperiodDate() {
        periodDays = new ArrayList<>();
        for (int i = 15; i <= 90; i++) {
            periodDays.add(i);
        }
    }

}
