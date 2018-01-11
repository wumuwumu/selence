package com.appscomm.selence.fragment;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import com.appscomm.selence.service.BluetoothService;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleConnector;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.exception.BleException;
import com.google.gson.Gson;
import com.necer.ncalendar.utils.SPUtils;

import java.util.UUID;

import static com.appscomm.selence.MainActivity.bleManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConsoleFragment extends Fragment {
    private static final String TAG = BleConnector.class.getSimpleName();
    private TextView title;
    private ImageView switchX;
    private BleDate mBleDate;
    private Dialog loadingDialog;
    private String bleName = "ST_BLE";

    private RelativeLayout unconnect;
    private RelativeLayout connect;
    private BluetoothGattCharacteristic characteristic;

    private static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String UUID_NOTIFY = "0000fff4-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE = "0000fff1-0000-1000-8000-00805f9b34fb";

    private static final  int MSG_DATA =166;
    private static final int MSG_SEND=200;
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

    private boolean LOCKED = false;

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
//        String reslut = SPUtils.getInstance().getString("recoverContent", "0");
//        if (reslut != "0") {
//            bleReslutDate = new Gson().fromJson(reslut, BleReslutDate.class);
//            mBleDate.setGear(bleReslutDate.getGear());
//        }
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
                    if(LOCKED){
                        return;
                    }
                    mBleDate.setSwitchX("0");
                    mBleDate.setKey("0");
                    mBleDate.setGear(bleReslutDate.getGear());
                    String date = new Gson().toJson(mBleDate);
                    writeToDevice(date);
                    break;
                case R.id.rl_unconnect:
                    connectNameDevice(bleName);
                    break;
                case R.id.iv_add:
                    if(LOCKED){
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
                    if(LOCKED){
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
                    if(LOCKED){
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
    private void writeToDevice( String date) {
            LOCKED = true;
                if (date.length() > 20) {
                    Message message = new Message();
                    message.what = MSG_SEND;
                    message.obj = date.substring(0,20);
                    handler.sendMessage(message);
                    date = date.substring(20);
                }
                Message message = new Message();
                message.what = MSG_SEND;
                message.obj = date;
                handler.sendMessageDelayed(message,300);
//                boolean suc = bleManager.writeDevice2(
//                        UUID_SERVICE,
//                        UUID_WRITE,
//                        subData,
//                        new BleCharacterCallback() {
//                            @Override
//                            public void onFailure(BleException exception) {
//
//                            }
//
//                            @Override
//                            public void onSuccess(final BluetoothGattCharacteristic characteristic) {


//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                dimissDialog();
//                            }
//                        }, 500);

//                            }
//
//
//                        });
//                if (suc) {
//                    bleManager.stopListenCharacterCallback(UUID_NOTIFY);
//                    startNotify(UUID_SERVICE, UUID_NOTIFY);
//                }


    }

    @Override
    public void onResume() {
        super.onResume();
        setDeviceBgColor();
        if(!bleManager.isConnected()){
            connectNameDevice(bleName);
        }
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_DATA:
                    String recoverContent = msg.obj.toString();
                    Log.d(TAG, "run2:"+recoverContent);
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
                            if(bleReslutDate.getPower() != null && bleReslutDate.getPower() != "" ){
                                setBatteryIcon(Integer.parseInt(bleReslutDate.getPower()));
                                if (Integer.parseInt(bleReslutDate.getCharge()) == 1) {
                                    battery.setImageResource(R.mipmap.battery6);
                                }
                                mBleDate.setGear(bleReslutDate.getGear());
                                setGearIcon(Integer.parseInt(bleReslutDate.getGear()));
                            }
                            if(bleReslutDate.getSwitchx() != null && bleReslutDate.getSwitchx() != ""){
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
                                }else {
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

                            if (recoverContent.contains("power")) {
                                writeToDevice(recoverContent);
                            }
                        } catch (Exception e) {

                        }
                    } else {

                    }
                    break;
                case MSG_SEND:
                     String data = msg.obj.toString();
                    Log.d(TAG, "run1: "+ data);
                    boolean suc = bleManager.writeDevice2(
                            UUID_SERVICE,
                            UUID_WRITE,
                            data,
                            new BleCharacterCallback() {
                                @Override
                                public void onFailure(BleException exception) {

                                }

                                @Override
                                public void onSuccess(final BluetoothGattCharacteristic characteristic) {
                                }


                            });
                if (suc) {
                    bleManager.stopListenCharacterCallback(UUID_NOTIFY);
                    startNotify(UUID_SERVICE, UUID_NOTIFY);
                }
                    if(data.length() != 20){
                       Message message = new Message();
                        message.what = MSG_UNLOCK;
                        handler.sendMessageDelayed(message,120);
                    }
                    break;
                case MSG_UNLOCK:
                    LOCKED = false;
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

//    private void checkPermissions() {
//        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
//        List<String> permissionDeniedList = new ArrayList<>();
//        for (String permission : permissions) {
//            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), permission);
//            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                onPermissionGranted(permission);
//            } else {
//                permissionDeniedList.add(permission);
//            }
//        }
//        if (!permissionDeniedList.isEmpty()) {
//            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
//            ActivityCompat.requestPermissions(getActivity(), deniedPermissions, 12);
//        }
//    }





    public void connectNameDevice(String name){
        showDialog();
        bleManager.scanNameAndConnect(name, 10000, false, new BleGattCallback(){

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dimissDialog();
                        initConnect(bleName, gatt);
                        unconnect.setVisibility(View.GONE);
                        connect.setVisibility(View.VISIBLE);
                        isConnect = true;

                    }
                });
            }


            @Override
            public void onNotFoundDevice() {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        dimissDialog();
                        Toast.makeText(getActivity(), "没有发现设备", Toast.LENGTH_LONG).show();
                    }
                });
            }




            @Override
            public void onFoundDevice(BluetoothDevice device) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "发现设备", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                gatt.discoverServices();

            }

            @Override
            public void onConnectFailure(BleException exception) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dimissDialog();
                        Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
                            gearDevice.setImageResource(R.mipmap.gear_zero);
                            connect.setVisibility(View.GONE);
                            unconnect.setVisibility(View.VISIBLE);
                        isConnect = false;
                    }
                });
            }
        });
    }




    private void initConnect(String deviceName, BluetoothGatt gatt) {
        bleManager.getBluetoothState();
        if (gatt != null) {
            for (final BluetoothGattService service : gatt.getServices()) {
                for (final BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().equals(UUID.fromString(UUID_NOTIFY))) {
                        startNotify(service.getUuid().toString(), characteristic.getUuid().toString());

                    }
                }
            }
//            BluetoothGattService service = mBluetoothService.getGatt().getService(UUID.fromString(UUID_SERVICE));
//            BluetoothGattCharacteristic notifyCharacteristic = service.getCharacteristic(UUID.fromString(UUID_NOTIFY));
//            if (notifyCharacteristic == null) {
//                return;
//            }
//            startNotify(notifyCharacteristic.getService().getUuid().toString(),notifyCharacteristic.getUuid().toString());
        }
    }


    private void startNotify(String serviceUUID, final String characterUUID) {
        final boolean suc = bleManager.notify(
                serviceUUID,
                characterUUID,
                new BleCharacterCallback() {
                    @Override
                    public void onSuccess(final BluetoothGattCharacteristic characteristic) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                byte[] content = characteristic.getValue();
                                Log.d(TAG, "run: "+ new String(content));
                                if (content[content.length - 1] != 10) {
                                    System.arraycopy(content, 0, notifyContent, nowIndex, content.length);

                                    nowIndex = content.length +nowIndex ;
                                    Log.d(TAG, "runindex: "+ nowIndex);
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
                                    String recoverContent =  new String(resultContent);
                                    if(recoverContent.length() <32){
                                        Log.d(TAG, "runerror: "+ recoverContent.length());
                                        return;
                                    }
                                    Message message = new Message();
                                    message.what= MSG_DATA;
                                    message.obj = recoverContent;
                                    handler.sendMessage(message);

                                }

                            }
                        });

                    }

                    @Override
                    public void onFailure(BleException exception) {

                        bleManager.handleException(exception);
                    }
                });


    }







//
//
//
//    private BluetoothService.Callback callback = new BluetoothService.Callback() {
//
//        private BluetoothGattCharacteristic notifyCharacteristic;
//
//        @Override
//        public void onStartScan() {
//
//        }
//
//        @Override
//        public void onScanning(ScanResult scanResult) {
//
//        }
//
//
//        @Override
//        public void onScanComplete() {
//            getActivity().runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(getActivity(), "没有发现设备", Toast.LENGTH_LONG).show();
//                }
//            });
//        }
//
//        @Override
//        public void onConnecting() {
//
//        }
//
//        @Override
//        public void onConnectFail() {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    dimissDialog();
//                    Toast.makeText(getActivity(), "连接失败", Toast.LENGTH_SHORT).show();
//                    if (mBluetoothService != null) {
//                        gearDevice.setImageResource(R.mipmap.gear_zero);
//                        connect.setVisibility(View.GONE);
//                        unconnect.setVisibility(View.VISIBLE);
////                unbindService();
////                mBluetoothService = null;
//                    }
//                    isConnect = false;
//                }
//            });
//
//
//        }
//
//        @Override
//        public void onDisConnected() {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getActivity(), "连接断开", Toast.LENGTH_SHORT).show();
//                    if (mBluetoothService != null) {
//                        gearDevice.setImageResource(R.mipmap.gear_zero);
//                        connect.setVisibility(View.GONE);
//                        unconnect.setVisibility(View.VISIBLE);
////                unbindService();
////                mBluetoothService = null;
//                    }
//                }
//            });
//
//        }
//
//        @Override
//        public void onServicesDiscovered() {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    dimissDialog();
//                    unconnect.setVisibility(View.GONE);
//                    connect.setVisibility(View.VISIBLE);
//                    if (mBluetoothService == null) {
//                        return;
//                    }
//                    BluetoothGattService service = mBluetoothService.getGatt().getService(UUID.fromString(UUID_SERVICE));
//                    notifyCharacteristic = service.getCharacteristic(UUID.fromString(UUID_NOTIFY));
//                    if (notifyCharacteristic == null) {
//                        return;
//                    }
//                    isConnect = true;
//                    mBluetoothService.notify(
//                            notifyCharacteristic.getService().getUuid().toString(),
//                            notifyCharacteristic.getUuid().toString(),
//                            new BleCharacterCallback() {
//
//                                @Override
//                                public void onFailure(BleException exception) {
//
//                                }
//
//                                @Override
//                                public void onSuccess(final BluetoothGattCharacteristic characteristic) {
//
//                                    getActivity().runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            byte[] content = characteristic.getValue();
//                                            if (content[content.length - 1] != 10) {
//                                                System.arraycopy(content, 0, notifyContent, 0, content.length);
//                                                nowIndex = content.length;
//                                            } else if (nowIndex != 0) {
//                                                System.arraycopy(content, 0, notifyContent, nowIndex, content.length);
//                                                int nowIndex2 = nowIndex + content.length;
//                                                byte[] resultContent = new byte[nowIndex2];
//                                                System.arraycopy(notifyContent, 0, resultContent, 0, nowIndex2);
//                                                for (int i = 0; i < notifyContent.length; i++) {
//                                                    notifyContent[i] = 0;
//                                                }
//                                                nowIndex2 = 0;
//                                                nowIndex = 0;
////                                        String recoverContent = HexUtil.hexStringToString(String.valueOf(HexUtil.encodeHex(resultContent)));
//                                                String recoverContent = HexUtil.encodeHexStr(resultContent);
//                                                if (resultContent.length > 20) {
//                                                    try {
//                                                        bleReslutDate = new Gson().fromJson(recoverContent, BleReslutDate.class);
//                                                        SPUtils.getInstance().put("recoverContent", recoverContent);
//                                                        if (bleReslutDate.getGear() != null) {
//                                                            int nowIndex = Integer.parseInt(bleReslutDate.getGear());
//                                                            setGearIcon(nowIndex);
//                                                            if (bleReslutDate.getPower() != null && bleReslutDate.getCharge() != null) {
//                                                                setBatteryIcon(Integer.parseInt(bleReslutDate.getPower()));
//                                                                if (Integer.parseInt(bleReslutDate.getCharge()) == 1) {
//                                                                    battery.setImageResource(R.mipmap.battery6);
//                                                                }
//                                                            }
//                                                        }
//                                                        mBleDate = new Gson().fromJson(recoverContent, BleDate.class);
//                                                        int color = SPUtils.getInstance().getInt("devicecolor");
//                                                        if (mBleDate.getKey() != null && mBleDate.getSwitchX() != null) {
//                                                            if (Integer.parseInt(mBleDate.getKey()) == 1) {
//                                                                isPlay = true;
//                                                                if (color != -1) {
//                                                                    if (color == 1) {
//                                                                        pauseDevice.setImageResource(R.mipmap.pause1);
//                                                                    } else if (color == 2) {
//                                                                        pauseDevice.setImageResource(R.mipmap.pause2);
//                                                                    } else if (color == 3) {
//                                                                        pauseDevice.setImageResource(R.mipmap.pause3);
//                                                                    }
//                                                                } else {
//                                                                    pauseDevice.setImageResource(R.mipmap.pause1);
//                                                                }
//                                                            }
//
//                                                            if (Integer.parseInt(mBleDate.getKey()) == 0) {
//                                                                isPlay = false;
//                                                                if (color != -1) {
//                                                                    if (color == 1) {
//                                                                        pauseDevice.setImageResource(R.mipmap.play1);
//                                                                    } else if (color == 2) {
//                                                                        pauseDevice.setImageResource(R.mipmap.play2);
//                                                                    } else if (color == 3) {
//                                                                        pauseDevice.setImageResource(R.mipmap.play3);
//                                                                    }
//                                                                } else {
//                                                                    pauseDevice.setImageResource(R.mipmap.play1);
//                                                                }
//                                                            }
//                                                        }
//                                                        if (recoverContent.contains("power")) {
//                                                            writeToDevice(recoverContent);
//                                                        }
//                                                    } catch (Exception e) {
//
//                                                    }
//                                                } else {
//
//                                                }
//                                                BleLog.d(TAG, "设备返回1" + recoverContent);
//                                            }
//
//                                        }
//                                    });
//                                }
//
//
//                            });
//                }
//            });
//
//        }
//    };

//    @Override
//    public final void onRequestPermissionsResult(int requestCode,
//                                                 @NonNull String[] permissions,
//                                                 @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case 12:
//                if (grantResults.length > 0) {
//                    for (int i = 0; i < grantResults.length; i++) {
//                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                            onPermissionGranted(permissions[i]);
//                        }
//                    }
//                }
//                break;
//        }
//    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        bleManager.stopListenConnectCallback();
        bleManager.closeBluetoothGatt();
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
