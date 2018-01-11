package com.appscomm.selence.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appscomm.selence.R;
import com.necer.ncalendar.utils.SPUtils;
import com.necer.ncalendar.calendar.MonthCalendar;
import com.necer.ncalendar.listener.OnMonthCalendarChangedListener;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarFragment extends Fragment {

    private MonthCalendar monthcalendar;
    //大姨妈持续时长
    private String selectDay;
    //两次大姨妈间隔
    private String selectPeriodDay;
    //上次大姨妈日期
    private String selectPreday;
    private TextView title;
    private TextView today;
    private TextView year;
    private ImageView nowday;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initDate();
    }

    private void initDate() {
        selectDay = SPUtils.getInstance().getString("days");
        selectPeriodDay = SPUtils.getInstance().getString("periodDay");
        selectPreday = SPUtils.getInstance().getString("preday");
        Calendar calendar = Calendar.getInstance();
        Date dateTime = new Date();
        calendar.setTime(dateTime);
        today.setText((calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日");
        year.setText(calendar.get(Calendar.YEAR) + "年" + "\n今天");
        if (selectDay == null || selectPeriodDay == null || selectPeriodDay == null) {
            Toast.makeText(getActivity(), "请先设置你的月经信息才能进行预测", Toast.LENGTH_SHORT).show();
        }

    }

    private void initView(View view) {
        monthcalendar = (MonthCalendar) view.findViewById(R.id.monthcalendar);
        title = (TextView) view.findViewById(R.id.tv_title);
        nowday = (ImageView) view.findViewById(R.id.iv_nowday);
        today = (TextView) view.findViewById(R.id.tv_day);
        year = (TextView) view.findViewById(R.id.tv_year);
        nowday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monthcalendar.toToday();
            }
        });
        title.setText("经期管理");
    }

    @Override
    public void onResume() {
        super.onResume();

        monthcalendar.setOnMonthCalendarChangedListener(new OnMonthCalendarChangedListener() {
            @Override
            public void onMonthCalendarChanged(DateTime dateTime) {
                today.setText(dateTime.toLocalDate().getMonthOfYear() + "月" + dateTime.toLocalDate().getDayOfMonth() + "日");
                year.setText(dateTime.toLocalDate().getYear() + "年" + "\n今天");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
