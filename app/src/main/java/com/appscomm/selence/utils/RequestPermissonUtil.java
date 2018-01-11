package com.appscomm.selence.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by wumu on 17-6-18.
 */

public class RequestPermissonUtil {

    private static final int REQUEST_FINE_LOCATION = 0;
    public static void mayRequestLocation(Activity activity)
    {
        String[] permissionString = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= 23) {
            for (int i = 0; i < permissionString.length; i++) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(activity, permissionString[i]);
                if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要 向用户解释，为什么要申请该权限
                    // if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    //Toast.makeText(context,R.string.ble_need, 1).show();
                    ActivityCompat.requestPermissions(activity, permissionString, REQUEST_FINE_LOCATION);
                    return;
                } else {
                }
            }
        } else {

        }
    }

}
