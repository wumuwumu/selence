package com.appscomm.selence.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appscomm.selence.R;
import com.appscomm.selence.SettingActivity;
import com.appscomm.selence.bean.BleDate;
import com.appscomm.selence.bean.BleReslutDate;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.google.gson.Gson;
import com.necer.ncalendar.utils.SPUtils;

import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConsoleFragment extends Fragment {
    private static final String TAG = ConsoleFragment.class.getSimpleName();
    private TextView title;
    private ImageView switchX;
    private BleDate mBleDate;
    private Dialog loadingDialog;
    private String bleName = "BLE";

    private RelativeLayout unconnect;
    private RelativeLayout connect;
    private BluetoothGattCharacteristic characteristic;

    private static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String UUID_NOTIFY = "0000fff4-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE = "0000fff1-0000-1000-8000-00805f9b34fb";

    private static final int MSG_DATA = 166;
    private static final int MSG_SEND = 200;
    private static final int MSG_UNLOCK = 222;
    private int nowIndex;
    private byte[] notifyContent = new byte[1024];
    ;
    private BleReslutDate bleReslutDate = new BleReslutDate();
    private ImageView addDevice;
    private ImageView minusDevice;
    private ImageView pauseDevice;
    private ImageView gearDevice;

    private Boolean isPlay = true;
    private ImageView setDeviceColor;
    private ImageView battery;
    private RelativeLayout deviceBg;
    private boolean isConnect = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BleDevice bleSaveDevice;

    private boolean LOCKED = false;
    private ValueAnimator valueAnimator;

    int color = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_console, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

    }

    private void initView(View view) {
        title = (TextView) view.findViewById(R.id.tv_title);
        title.setText("善月 Selence");
        deviceBg = (RelativeLayout) view.findViewById(R.id.rl_device_bg);
        unconnect = (RelativeLayout) view.findViewById(R.id.rl_unconnect);
        unconnect.setOnClickListener(mOnClickListener);
        connect = (RelativeLayout) view.findViewById(R.id.rl_connect);
        switchX = (ImageView) view.findViewById(R.id.iv_shutdown);
        switchX.setOnClickListener(mOnClickListener);
        addDevice = (ImageView) view.findViewById(R.id.iv_add);
        addDevice.setOnClickListener(mOnClickListener);
        minusDevice = (ImageView) view.findViewById(R.id.iv_minus);
        minusDevice.setOnClickListener(mOnClickListener);
        pauseDevice = (ImageView) view.findViewById(R.id.iv_pause);
        pauseDevice.setOnClickListener(mOnClickListener);
        gearDevice = (ImageView) view.findViewById(R.id.iv_gear);
        setDeviceColor = (ImageView) view.findViewById(R.id.iv_setting);
        setDeviceColor.setVisibility(View.VISIBLE);
        setDeviceColor.setOnClickListener(mOnClickListener);
        battery = (ImageView) view.findViewById(R.id.iv_battery);
        mBleDate = new BleDate();
        bleReslutDate = new BleReslutDate();
        bleReslutDate.setGear("0");
        bleReslutDate.setCharge("");
        bleReslutDate.setPower("");
        bleReslutDate.setSwitchx("0");
        valueAnimator = ValueAnimator.ofInt(1, 100);
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LOCKED = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.setDuration(2000);
        valueAnimator.start();
        color = SPUtils.getInstance().getInt("devicecolor");
        if (color != -1) {
            if (color == 1) {
                pauseDevice.setImageResource(R.mipmap.pause1);
            } else if (color == 2) {
                pauseDevice.setImageResource(R.mipmap.pause2);
            } else if (color == 3) {
                pauseDevice.setImageResource(R.mipmap.pause3);
            }
        } else {
            pauseDevice.setImageResource(R.mipmap.pause1);
        }

    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_shutdown:
                    if (LOCKED || !isConnect) {
                        return;
                    }
                    mBleDate.setSwitchX("0");
                    mBleDate.setKey("0");
                    mBleDate.setGear(bleReslutDate.getGear());
                    String date = new Gson().toJson(mBleDate);
                    writeToDevice(date);
                    break;
                case R.id.rl_unconnect:
                    connectNameDevice2(bleName);
//                    scan();
                    break;
                case R.id.iv_add:
                    if (LOCKED || !isConnect) {
                        return;
                    }
                    mBleDate.setSwitchX("1");
                    mBleDate.setKey("1");
                    int nowIndex = Integer.parseInt(bleReslutDate.getGear()) + 1;
                    if (nowIndex > 12) {
                        nowIndex = 12;
                    }
                    mBleDate.setGear(nowIndex + "");
                    String addDate = new Gson().toJson(mBleDate);
                    writeToDevice(addDate);
                    break;
                case R.id.iv_minus:
                    if (LOCKED || !isConnect) {
                        return;
                    }
                    mBleDate.setSwitchX("1");
                    mBleDate.setKey("1");
                    int nowMinusIndex = Integer.parseInt(bleReslutDate.getGear()) - 1;
                    if (nowMinusIndex < 0) {
                        nowMinusIndex = 0;
                    }
                    mBleDate.setGear(nowMinusIndex + "");
                    String minusDate = new Gson().toJson(mBleDate);
                    writeToDevice(minusDate);
                    break;
                case R.id.iv_pause:
                    if (LOCKED || !isConnect) {
                        return;
                    }
                    if (isPlay) {
                        mBleDate.setSwitchX("1");
                        mBleDate.setKey("0");
                        mBleDate.setGear(bleReslutDate.getGear());
                        String pauseDate = new Gson().toJson(mBleDate);
                        writeToDevice(pauseDate);
                    } else {
                        mBleDate.setSwitchX("1");
                        mBleDate.setKey("1");
                        mBleDate.setGear(bleReslutDate.getGear());
                        String pauseDate = new Gson().toJson(mBleDate);
                        writeToDevice(pauseDate);
                    }

                    break;
                case R.id.iv_setting:
                    startActivity(new Intent(getActivity(), SettingActivity.class));
                    break;
            }
        }


    };

    private void writeToDevice(String date) {
        LOCKED = true;
        if (date.length() > 20) {
            Message message = new Message();
            message.what = MSG_SEND;
            message.obj = date.substring(0, 20);
            handler.sendMessage(message);
            date = date.substring(20);
        }
        Message message = new Message();
        message.what = MSG_SEND;
        message.obj = date;
        handler.sendMessageDelayed(message, 250);

    }

    @Override
    public void onResume() {
        super.onResume();
        setDeviceBgColor();
        if (!isConnect && BleManager.getInstance().isBlueEnable()) {
            scan();
        }
//            connectNameDevice2(bleName);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DATA:
                    String recoverContent = msg.obj.toString();
                    Log.d(TAG, "run2:" + recoverContent);
                    if (recoverContent.length() > 20) {
                        try {
                            bleReslutDate = new Gson().fromJson(recoverContent, BleReslutDate.class);
//                            if (bleReslutDate.getGear() != null) {
//                                int nowIndex = Integer.parseInt(bleReslutDate.getGear());
//                                setGearIcon(nowIndex);
//                                if (bleReslutDate.getPower() != null && bleReslutDate.getCharge() != null) {
//                                    setBatteryIcon(Integer.parseInt(bleReslutDate.getPower()));
//                                    if (Integer.parseInt(bleReslutDate.getCharge()) == 1) {
//                                        battery.setImageResource(R.mipmap.battery6);
//                                    }
//                                }
//                            }
                            if (recoverContent.contains("power")) {
                                writeToDevice(recoverContent);
                            }
                            if (bleReslutDate.getPower() != null && bleReslutDate.getPower() != "") {
                                setBatteryIcon(Integer.parseInt(bleReslutDate.getPower()));
                                if (Integer.parseInt(bleReslutDate.getCharge()) == 1) {
                                    battery.setImageResource(R.mipmap.battery6);
                                }
                                mBleDate.setGear(bleReslutDate.getGear());
                                setGearIcon(Integer.parseInt(bleReslutDate.getGear()));
                            }
                            if (bleReslutDate.getSwitchx() != null && bleReslutDate.getSwitchx() != "") {
                                if (Integer.parseInt(mBleDate.getKey()) == 1) {
                                    isPlay = true;
                                    if (color != -1) {
                                        if (color == 1) {
                                            pauseDevice.setImageResource(R.mipmap.pause1);
                                        } else if (color == 2) {
                                            pauseDevice.setImageResource(R.mipmap.pause2);
                                        } else if (color == 3) {
                                            pauseDevice.setImageResource(R.mipmap.pause3);
                                        }
                                    } else {
                                        pauseDevice.setImageResource(R.mipmap.pause1);
                                    }
                                } else {
                                    isPlay = false;
                                    if (color != -1) {
                                        if (color == 1) {
                                            pauseDevice.setImageResource(R.mipmap.play1);
                                        } else if (color == 2) {
                                            pauseDevice.setImageResource(R.mipmap.play2);
                                        } else if (color == 3) {
                                            pauseDevice.setImageResource(R.mipmap.play3);
                                        }
                                    } else {
                                        pauseDevice.setImageResource(R.mipmap.play1);
                                    }
                                }
                                mBleDate.setGear(mBleDate.getGear());
                                setGearIcon(Integer.parseInt(bleReslutDate.getGear()));
                            }
                        } catch (Exception e) {

                        }
                    } else {

                    }
                    break;
                case MSG_SEND:
                    String data = msg.obj.toString();
                    Log.d(TAG, "run1: " + data);
                    BleManager.getInstance().write(
                            bleSaveDevice,
                            UUID_SERVICE,
                            UUID_WRITE,
                            data.getBytes(),
                            false,
                            new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess() {
                                    // 发送数据到设备成功
                                    Log.d(TAG, "onWriteSuccess: ");
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    // 发送数据到设备失败
                                    Log.d(TAG, "onWriteFailure: " + exception.getDescription());
                                }
                            });
                    if (data.length() != 20) {
                        Message message = new Message();
                        message.what = MSG_UNLOCK;
                        handler.sendMessageDelayed(message, 150);
                    }
                    if (!valueAnimator.isRunning()) {
                        valueAnimator.start();
                    }
                    break;
                case MSG_UNLOCK:
                    LOCKED = false;
                    valueAnimator.end();
                    valueAnimator.start();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void setDeviceBgColor() {
        int color = SPUtils.getInstance().getInt("devicecolor");
        if (color != -1) {
            if (color == 1) {
                addDevice.setImageResource(R.mipmap.add1);
                minusDevice.setImageResource(R.mipmap.minus1);
                pauseDevice.setImageResource(R.mipmap.play1);
                switchX.setImageResource(R.mipmap.power1);
                deviceBg.setBackground(getResources().getDrawable(R.mipmap.devicebg1));
            } else if (color == 2) {
                addDevice.setImageResource(R.mipmap.add2);
                minusDevice.setImageResource(R.mipmap.minus2);
                pauseDevice.setImageResource(R.mipmap.play2);
                switchX.setImageResource(R.mipmap.power2);
                deviceBg.setBackground(getResources().getDrawable(R.mipmap.devicebg2));
            } else if (color == 3) {
                addDevice.setImageResource(R.mipmap.add3);
                minusDevice.setImageResource(R.mipmap.minus3);
                pauseDevice.setImageResource(R.mipmap.play3);
                switchX.setImageResource(R.mipmap.power3);
                deviceBg.setBackground(getResources().getDrawable(R.mipmap.devicebg3));
            }
        } else {
            addDevice.setImageResource(R.mipmap.add1);
            minusDevice.setImageResource(R.mipmap.minus1);
            pauseDevice.setImageResource(R.mipmap.play1);
            switchX.setImageResource(R.mipmap.power1);
            deviceBg.setBackground(getResources().getDrawable(R.mipmap.devicebg1));
        }
    }

    public void scan() {
        showDialog();
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setDeviceName(true, bleName)   // 只扫描指定广播名的设备，可选
//                .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
                .setServiceUuids(new UUID[]{UUID.fromString(UUID_SERVICE)})
                .setScanTimeOut(30000)// 扫描超时时间，可选，默认10秒
                .build();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.getInstance().cancelScan();
                dimissDialog();
            }
        }, 20000);
        BleManager.getInstance().initScanRule(scanRuleConfig);
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                // 开始扫描（主线程）
//                Toast.makeText(getActivity(),"扫描开始",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                // 扫描到一个符合扫描规则的BLE设备（主线程）
                Toast.makeText(getActivity(), bleDevice.getName(), Toast.LENGTH_SHORT).show();
                if (bleDevice.getName() != null) {
                    if (bleDevice.getName().contains(bleName)) {
                        BleManager.getInstance().cancelScan();
//                    dimissDialog();
                        connect(bleDevice);
                    }
                }

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                // 扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程）
            }
        });
    }

    public void connect(BleDevice bleDevice) {
        showDialog();
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                dimissDialog();
                Toast.makeText(getActivity(), "服务发现", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartConnect() {
                // 开始连接
                Toast.makeText(getActivity(), "开始连接", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectFail(BleException exception) {
                // 连接失败
                dimissDialog();
                Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                // 连接成功，BleDevice即为所连接的BLE设备
//                Toast.makeText(getActivity(), "连接成功", Toast.LENGTH_SHORT).show();
                dimissDialog();
                initConnect(bleDevice, gatt);
                unconnect.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
                bleSaveDevice = bleDevice;
                isConnect = true;
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                // 连接中断，isActiveDisConnected表示是否是主动调用了断开连接方法
                dimissDialog();
                Toast.makeText(getActivity(), "连接断开", Toast.LENGTH_SHORT).show();

//                        Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                gearDevice.setImageResource(R.mipmap.gear_zero);
                connect.setVisibility(View.GONE);
                unconnect.setVisibility(View.VISIBLE);
                isConnect = false;
            }
        });
    }

    public void connectNameDevice2(String name) {
        showDialog();
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setDeviceName(true, bleName)   // 只扫描指定广播名的设备，可选
                .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)// 扫描超时时间，可选，默认10秒
                .setServiceUuids(new UUID[]{UUID.fromString(UUID_SERVICE)})
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanStarted(boolean success) {
                Toast.makeText(getActivity(), "扫描开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScanFinished(BleDevice scanResult) {
                Toast.makeText(getActivity(), "扫描结束", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                super.onScanning(bleDevice);
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onStartConnect() {
                Toast.makeText(getActivity(), "开始连接", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onConnectFail(BleException exception) {
                dimissDialog();
                Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Toast.makeText(getActivity(), "连接成功", Toast.LENGTH_SHORT).show();
                dimissDialog();
                initConnect(bleDevice, gatt);
                unconnect.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
                isConnect = true;

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                dimissDialog();
                Toast.makeText(getActivity(), "连接断开", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                gearDevice.setImageResource(R.mipmap.gear_zero);
                connect.setVisibility(View.GONE);
                unconnect.setVisibility(View.VISIBLE);
                isConnect = false;
            }
        });
    }


    private void initConnect(BleDevice bleDevice, BluetoothGatt gatt) {
        BleManager.getInstance().notify(
                bleDevice,
                UUID_SERVICE,
                UUID_NOTIFY,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (data == null || data.length == 0) {
                            return;
                        }
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG, "onCharacteristicChanged: " + new String(data));
                        byte[] content = data;
                        Log.d(TAG, "run: " + new String(content));
                        if (content[content.length - 1] != 10) {
                            System.arraycopy(content, 0, notifyContent, nowIndex, content.length);

                            nowIndex = content.length + nowIndex;
                            Log.d(TAG, "runindex: " + nowIndex);
                        } else if (true) {
                            System.arraycopy(content, 0, notifyContent, nowIndex, content.length);
                            int nowIndex2 = nowIndex + content.length;
                            byte[] resultContent = new byte[nowIndex2];
                            System.arraycopy(notifyContent, 0, resultContent, 0, nowIndex2);
                            for (int i = 0; i < notifyContent.length; i++) {
                                notifyContent[i] = 0;
                            }
                            nowIndex2 = 0;
                            nowIndex = 0;
//                                        String recoverContent = HexUtil.hexStringToString(String.valueOf(HexUtil.encodeHex(resultContent)));
                            String recoverContent = new String(resultContent);
                            if (recoverContent.length() < 32) {
                                Log.d(TAG, "runerror: " + recoverContent.length());
                                return;
                            }
                            Message message = new Message();
                            message.what = MSG_DATA;
                            message.obj = recoverContent;
                            handler.sendMessage(message);
                        }
                    }
                });
    }


    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
        isConnect = false;

    }

    private void setBatteryIcon(int power) {
        if (power < 20) {
            battery.setImageResource(R.mipmap.battery1);
        } else if (power >= 20 && power < 40) {
            battery.setImageResource(R.mipmap.battery2);
        } else if (power >= 40 && power < 60) {
            battery.setImageResource(R.mipmap.battery3);
        } else if (power >= 60 && power < 80) {
            battery.setImageResource(R.mipmap.battery4);
        } else if (power >= 80 && power < 100) {
            battery.setImageResource(R.mipmap.battery5);
        }


    }

    private void setGearIcon(int gear) {
        switch (gear) {
            case 0:
                gearDevice.setImageResource(R.mipmap.gear_zero);
                break;
            case 1:
                gearDevice.setImageResource(R.mipmap.gear_one);
                break;
            case 2:
                gearDevice.setImageResource(R.mipmap.gear_two);
                break;
            case 3:
                gearDevice.setImageResource(R.mipmap.gear_three);
                break;
            case 4:
                gearDevice.setImageResource(R.mipmap.gear_four);
                break;
            case 5:
                gearDevice.setImageResource(R.mipmap.gear_five);
                break;
            case 6:
                gearDevice.setImageResource(R.mipmap.gear_six);
                break;
            case 7:
                gearDevice.setImageResource(R.mipmap.gear_seven);
                break;
            case 8:
                gearDevice.setImageResource(R.mipmap.gear_eight);
                break;
            case 9:
                gearDevice.setImageResource(R.mipmap.gear_nine);
                break;
            case 10:
                gearDevice.setImageResource(R.mipmap.gear_ten);
                break;
            case 11:
                gearDevice.setImageResource(R.mipmap.gear_eleven);
                break;
            case 12:
                gearDevice.setImageResource(R.mipmap.gear_full);
                break;

        }
    }


    private Dialog createLoadingDialog(Context context) {
        Dialog dialog;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.dialog_loading, null);
        dialog = new Dialog(context, R.style.loading_dialog);
        dialog.setContentView(linearLayout);
        return dialog;
    }

    private void showDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dimissDialog();
                loadingDialog = createLoadingDialog(getActivity());
                loadingDialog.setCancelable(true);
                loadingDialog.setCanceledOnTouchOutside(false);
                loadingDialog.show();
            }
        });

    }


    private void dimissDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        });
    }
}
