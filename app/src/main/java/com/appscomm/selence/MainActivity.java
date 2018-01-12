package com.appscomm.selence;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.appscomm.selence.adapter.FragmentAdapter;
import com.appscomm.selence.fragment.CalendarFragment;
import com.appscomm.selence.fragment.ConsoleFragment;
import com.appscomm.selence.fragment.SettingFragment;
import com.appscomm.selence.utils.RequestPermissonUtil;
import com.appscomm.selence.view.NoScrollViewPager;
import com.clj.fastble.BleManager;
import com.necer.ncalendar.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private FragmentAdapter fragmentAdapter;
    private List<Fragment> fragmentList;
    private Fragment consoleFragment;
    private Fragment settingFragment;
    private Fragment mainFragment;
    private NoScrollViewPager vp;
    private ToggleButton tgbIndex;
    private ToggleButton tgbSetting;
    private ToggleButton tgbCalendar;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RequestPermissonUtil.mayRequestLocation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initFragment();
        initLisener();
        initBlue();
    }

    private void initBlue() {
        checkBLEFeature();
    }

    private void checkBLEFeature() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "蓝牙不支持", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙不支持", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(7)
                .setOperateTimeout(5000);
    }

    private void initLisener() {
        tgbIndex.setOnClickListener(mOnClickListener);
        tgbSetting.setOnClickListener(mOnClickListener);
        tgbCalendar.setOnClickListener(mOnClickListener);
    }

    private void initView() {
        vp = (NoScrollViewPager) findViewById(R.id.vp);
        tgbIndex = (ToggleButton) findViewById(R.id.tgb_index);
        tgbSetting = (ToggleButton) findViewById(R.id.tgb_setting);
        tgbCalendar = (ToggleButton) findViewById(R.id.tgb_calendar);
    }

    private void initFragment() {
        fragmentList = new ArrayList<Fragment>();
        consoleFragment = new ConsoleFragment();
        settingFragment = new SettingFragment();
        mainFragment = new CalendarFragment();
        fragmentList.add(consoleFragment);
        fragmentList.add(settingFragment);
        fragmentList.add(mainFragment);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragmentList);
        vp.setAdapter(fragmentAdapter);
        vp.setCurrentItem(0);
        vp.setOffscreenPageLimit(fragmentList.size());
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void selectedPage(int position) {
        switch (position) {
            case 0:
                setSelectedMenu(tgbIndex);
                break;
            case 1:
                setSelectedMenu(tgbSetting);
                break;
            case 2:
                setSelectedMenu(tgbCalendar);
                break;
        }
    }


    private void setSelectedMenu(View view) {
        disableChecked();
        switch (view.getId()) {
            case R.id.tgb_index:
                tgbIndex.setChecked(true);
                disableViewClick(tgbIndex);
//                title.setText("善月 selence");

                break;
            case R.id.tgb_setting:
                tgbSetting.setChecked(true);
                disableViewClick(tgbSetting);
//                title.setText("设置");

                break;
            case R.id.tgb_calendar:
                tgbCalendar.setChecked(true);
                disableViewClick(tgbCalendar);
//                title.setText("健康管理");

                break;

        }
    }


    private void disableViewClick(View view) {
        tgbIndex.setClickable(true);
        tgbSetting.setClickable(true);
        tgbCalendar.setClickable(true);
        switch (view.getId()) {
            case R.id.tgb_index:
                tgbIndex.setClickable(false);
                break;
            case R.id.tgb_setting:
                tgbSetting.setClickable(false);
                break;
            case R.id.tgb_calendar:
                tgbCalendar.setClickable(false);
                break;
        }
    }

    private void disableChecked() {
        tgbIndex.setChecked(false);
        tgbSetting.setChecked(false);
        tgbCalendar.setChecked(false);
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tgb_index:
                    vp.setCurrentItem(0);
                    break;
                case R.id.tgb_setting:
                    vp.setCurrentItem(1);
                    break;
                case R.id.tgb_calendar:
                    vp.setCurrentItem(2);
                    break;

            }

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        BleManager.getInstance().enableBluetooth();
    }

    @Override
    protected void onDestroy() {
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
        super.onDestroy();

    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    private static final int REQUEST_FINE_LOCATION = 0;

    private void mayRequestLocation() {
        String[] permissionString = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH_ADMIN,};
        if (Build.VERSION.SDK_INT >= 23) {
            for (int i = 0; i < permissionString.length; i++) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, permissionString[i]);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissionString, REQUEST_FINE_LOCATION);
                    return;
                } else {
                }
            }
        } else {

        }
    }
}
