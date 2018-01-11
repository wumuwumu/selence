package com.necer.ncalendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.necer.ncalendar.listener.OnClickMonthViewListener;
import com.necer.ncalendar.utils.Attrs;
import com.necer.ncalendar.utils.SPUtils;
import com.necer.ncalendar.utils.Utils;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.necer.ncalendar.utils.SPUtils.getInstance;

public class MonthView extends CalendarView {

    private List<String> lunarList;
    private List<String> localDateList;
    List<Long> millisList = new LinkedList<>();
    List<Long> blueMillisList = new LinkedList<>();
    List<Long> blue2MillisList = new LinkedList<>();
    List<Long> nPeriodMillisList = new LinkedList<>();

    private int mRowNum;
    private OnClickMonthViewListener mOnClickMonthViewListener;


    public MonthView(Context context, DateTime dateTime, OnClickMonthViewListener onClickMonthViewListener) {
        super(context);
        this.mInitialDateTime = dateTime;

        //0周日，1周一
        Utils.NCalendar nCalendar2 = Utils.getMonthCalendar2(dateTime, Attrs.firstDayOfWeek);
        mOnClickMonthViewListener = onClickMonthViewListener;

        lunarList = nCalendar2.lunarList;
        localDateList = nCalendar2.localDateList;
        dateTimes = nCalendar2.dateTimeList;
        mRowNum = dateTimes.size() / 7;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        //绘制高度
        mHeight = getDrawHeight();
        mRectList.clear();
        millisList.clear();
        for (int i = 0; i < mRowNum; i++) {
            for (int j = 0; j < 7; j++) {
                Rect rect = new Rect(j * mWidth / 7, i * mHeight / mRowNum, j * mWidth / 7 + mWidth / 7, i * mHeight / mRowNum + mHeight / mRowNum);
                DateTime dateTime = dateTimes.get(i * 7 + j);
                Paint.FontMetricsInt fontMetrics = mSorlarPaint.getFontMetricsInt();

                int baseline;//让6行的第一行和5行的第一行在同一直线上，处理选中第一行的滑动
                if (mRowNum == 5) {
                    baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                } else {
                    baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2 + (mHeight / 5 - mHeight / 6) / 2;
                }

                //当月和上下月的颜色不同
                if (Utils.isEqualsMonth(dateTime, mInitialDateTime)) {

                    //当天和选中的日期不绘制农历
                    if (Utils.isToday(dateTime)) {
                        mSorlarPaint.setColor(mSelectCircleColor);
                        int centerY = mRowNum == 5 ? rect.centerY() : (rect.centerY() + (mHeight / 5 - mHeight / 6) / 2);
                        canvas.drawCircle(rect.centerX(), centerY, mSelectCircleRadius, mSorlarPaint);
                        mSorlarPaint.setColor(Color.WHITE);
                        canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                    } else if (mSelectDateTime != null && dateTime.toLocalDate().equals(mSelectDateTime.toLocalDate())) {

                        mSorlarPaint.setColor(mSelectCircleColor);
                        int centerY = mRowNum == 5 ? rect.centerY() : (rect.centerY() + (mHeight / 5 - mHeight / 6) / 2);
                        canvas.drawCircle(rect.centerX(), centerY, mSelectCircleRadius, mSorlarPaint);
                        mSorlarPaint.setColor(mHollowCircleColor);
                        canvas.drawCircle(rect.centerX(), centerY, mSelectCircleRadius - mHollowCircleStroke, mSorlarPaint);

                        mSorlarPaint.setColor(mSolarTextColor);
                        canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                    } else {
                        mSorlarPaint.setColor(mSolarTextColor);
                        canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                        drawLunar(canvas, rect, baseline, mLunarTextColor, i, j);
                        //绘制节假日
                        drawHolidays(canvas, rect, dateTime, baseline);
                        //绘制圆点
                        drawPoint(canvas, rect, dateTime, baseline);
                    }

                } else {
                    mSorlarPaint.setColor(mHintColor);
                    canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mSorlarPaint);
                    drawLunar(canvas, rect, baseline, mHintColor, i, j);
                    //绘制节假日
                    drawHolidays(canvas, rect, dateTime, baseline);
                    //绘制圆点
                    drawPoint(canvas, rect, dateTime, baseline);
                }

                Date mdate = dateTime.toDate();
                long millis = getStringToDate(getTime(mdate), "yyyy-MM-dd") / 1000;
                long mSelectMillis = SPUtils.getInstance().getLong("millis");          /*上次大姨妈时间*/
                String days = SPUtils.getInstance().getString("days");                  /*大姨妈时长*/
                String periodDay = SPUtils.getInstance().getString("periodDay");        /*两次大姨妈间隔时长*/
                if (mSelectMillis != -1 && days != null && periodDay != null) {
                    long mSelectBlueMillis = mSelectMillis - 14 * 24 * 60 * 60;             /*上次排卵时间*/
                    int mdays = Integer.parseInt(days);
                    int mPeriodDay = Integer.parseInt(periodDay);
                    long thirdMonthMillis = mSelectMillis - 3 * 24 * 60 * 60 * mPeriodDay;
                    long thirdMonthBlueMillis = mSelectBlueMillis - 3 * 24 * 60 * 60 * mPeriodDay;


                /*现在往后一年推算的大姨妈期*/
                    for (int r = 1; r <= 18; r++) {
                        for (int n = 1; n <= mdays; n++) {
                            long nPeriodMillis = mSelectMillis + (mPeriodDay * (r - 1) + (n - 1)) * 24 * 60 * 60;
                            if (millis <= nPeriodMillis && nPeriodMillis <= (millis + 31 * 24 * 3600)) {
                                nPeriodMillisList.add(nPeriodMillis);
                            }
                        }
                    }
                 /*现在之前三周期大姨妈时间*/
                    for (int r = 1; r <= 3; r++) {
                        for (int n = 1; n <= mdays; n++) {
                            long nPeriodMillis = thirdMonthMillis + (mPeriodDay * (r - 1) + (n - 1)) * 24 * 60 * 60;
                            if (millis <= nPeriodMillis && nPeriodMillis <= (millis + 31 * 24 * 3600)) {
                                nPeriodMillisList.add(nPeriodMillis);
                            }
                        }
                    }
                /*现在往后一年推算的排卵期*/
                    for (int r = 1; r <= 18; r++) {
                        for (int n = 1; n <= 10; n++) {
                            long nPeriodMillis = mSelectBlueMillis - 5 * 24 * 60 * 60 + (mPeriodDay * (r - 1) + (n - 1)) * 24 * 60 * 60;
                            long mPeriodMillis = mSelectBlueMillis + (mPeriodDay * (r - 1)) * 24 * 60 * 60;
                            if (millis <= nPeriodMillis && nPeriodMillis <= (millis + 31 * 24 * 3600) && nPeriodMillis != mPeriodMillis) {
                                blueMillisList.add(nPeriodMillis);
                            }
                        }
                    }

                    for (int r = 1; r <= 18; r++) {
                        for (int n = 1; n <= 10; n++) {
                            long nPeriodMillis = mSelectBlueMillis + (mPeriodDay * (r - 1)) * 24 * 60 * 60;
                            if (millis <= nPeriodMillis && nPeriodMillis <= (millis + 31 * 24 * 3600)) {
                                blue2MillisList.add(nPeriodMillis);
                            }
                        }
                    }

                 /*现在之前三周期排卵时间*/
                    for (int r = 1; r <= 3; r++) {
                        for (int n = 1; n <= 10; n++) {
                            long nPeriodMillis = thirdMonthBlueMillis - 5 * 24 * 60 * 60 + (mPeriodDay * (r - 1) + (n - 1)) * 24 * 60 * 60;
                            if (millis <= nPeriodMillis && nPeriodMillis <= (millis + 31 * 24 * 3600)) {
                                blueMillisList.add(nPeriodMillis);
                            }
                        }
                    }
                    for (int r = 1; r <= 3; r++) {
                        for (int n = 1; n <= 10; n++) {
                            long nPeriodMillis = thirdMonthBlueMillis + (mPeriodDay * (r - 1)) * 24 * 60 * 60;
                            if (millis <= nPeriodMillis && nPeriodMillis <= (millis + 31 * 24 * 3600)) {
                                blue2MillisList.add(nPeriodMillis);
                            }
                        }
                    }

                    for (int t = 0; t < nPeriodMillisList.size(); t++) {
                        if (millis == nPeriodMillisList.get(t)) {
                            mRedPaint.setColor(mSelectCircleRedColor);
                            int centerY = mRowNum == 5 ? rect.centerY() : (rect.centerY() + (mHeight / 5 - mHeight / 6) / 2);
                            canvas.drawCircle(rect.centerX(), centerY, mSelectCircleRadius, mRedPaint);
                            mRedPaint.setColor(Color.WHITE);
                            canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mRedPaint);
                        }
                    }
                    for (int t = 0; t < blueMillisList.size(); t++) {
                        if (millis == blueMillisList.get(t)) {
                            mBluePaint.setColor(mSelectCircleBlueColor);
                            int centerY = mRowNum == 5 ? rect.centerY() : (rect.centerY() + (mHeight / 5 - mHeight / 6) / 2);
                            canvas.drawCircle(rect.centerX(), centerY, mSelectCircleRadius, mBluePaint);
                            mBluePaint.setColor(Color.WHITE);
                            canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mBluePaint);
                        }
                    }
                    for (int t = 0; t < blue2MillisList.size(); t++) {
                        if (millis == blue2MillisList.get(t)) {
                            mBlue2Paint.setColor(mSelectCircleBlue2Color);
                            int centerY = mRowNum == 5 ? rect.centerY() : (rect.centerY() + (mHeight / 5 - mHeight / 6) / 2);
                            canvas.drawCircle(rect.centerX(), centerY, mSelectCircleRadius, mBlue2Paint);
                            mBlue2Paint.setColor(Color.WHITE);
                            canvas.drawText(dateTime.getDayOfMonth() + "", rect.centerX(), baseline, mBlue2Paint);
                        }
                    }
                }
            }
        }
        millisList.clear();
        nPeriodMillisList.clear();
        blueMillisList.clear();
        blue2MillisList.clear();
        invalidate();
    }


    /**
     * 月日历高度
     *
     * @return
     */

    public int getMonthHeight() {
        return Attrs.monthCalendarHeight;
    }

    /**
     * 月日历的绘制高度，
     * 为了月日历6行时，绘制农历不至于太靠下，绘制区域网上压缩一下
     *
     * @return
     */
    public int getDrawHeight() {
        return (int) (getMonthHeight() - Utils.dp2px(getContext(), 10));
    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public long getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date.getTime();
    }

    private void drawLunar(Canvas canvas, Rect rect, int baseline, int color, int i, int j) {
        if (isShowLunar) {
            mLunarPaint.setColor(color);
            String lunar = lunarList.get(i * 7 + j);
            canvas.drawText(lunar, rect.centerX(), baseline + getMonthHeight() / 20, mLunarPaint);
        }
    }

    private void drawHolidays(Canvas canvas, Rect rect, DateTime dateTime, int baseline) {
        if (isShowHoliday) {
            if (holidayList.contains(dateTime.toLocalDate().toString())) {
                mLunarPaint.setColor(mHolidayColor);
                canvas.drawText("休", rect.centerX() + rect.width() / 4, baseline - getMonthHeight() / 20, mLunarPaint);

            } else if (workdayList.contains(dateTime.toLocalDate().toString())) {
                mLunarPaint.setColor(mWorkdayColor);
                canvas.drawText("班", rect.centerX() + rect.width() / 4, baseline - getMonthHeight() / 20, mLunarPaint);
            }
        }
    }

    //绘制圆点
    public void drawPoint(Canvas canvas, Rect rect, DateTime dateTime, int baseline) {
        if (pointList != null && pointList.contains(dateTime.toLocalDate().toString())) {
            mLunarPaint.setColor(mPointColor);
            canvas.drawCircle(rect.centerX(), baseline - getMonthHeight() / 15, mPointSize, mLunarPaint);
        }
    }


    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (int i = 0; i < mRectList.size(); i++) {
                Rect rect = mRectList.get(i);
                if (rect.contains((int) e.getX(), (int) e.getY())) {
                    DateTime selectDateTime = dateTimes.get(i);
                    if (Utils.isLastMonth(selectDateTime, mInitialDateTime)) {
                        mOnClickMonthViewListener.onClickLastMonth(selectDateTime);
                    } else if (Utils.isNextMonth(selectDateTime, mInitialDateTime)) {
                        mOnClickMonthViewListener.onClickNextMonth(selectDateTime);
                    } else {
                        mOnClickMonthViewListener.onClickCurrentMonth(selectDateTime);
                    }
                    break;
                }
            }
            return true;
        }
    });

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public int getRowNum() {
        return mRowNum;
    }

    public int getSelectRowIndex() {
        if (mSelectDateTime == null) {
            return 0;
        }
        int indexOf = localDateList.indexOf(mSelectDateTime.toLocalDate().toString());
        return indexOf / 7;
    }


}
