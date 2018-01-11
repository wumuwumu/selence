package com.appscomm.selence.service;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.clj.fastble.BleManager;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.conn.BleRssiCallback;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.ListScanCallback;
import com.clj.fastble.utils.HexUtil;

public class BluetoothService extends Service {
    private static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String UUID_NOTIFY = "0000fff4-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE = "0000fff1-0000-1000-8000-00805f9b34fb";



    public BluetoothBinder mBinder = new BluetoothBinder();
    public BleManager bleManager;
    private Handler threadHandler = new Handler(Looper.getMainLooper());
    private Callback mCallback = null;
    private Callback2 mCallback2 = null;

    private String name;
    private String mac;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    @Override
    public void onCreate() {
        bleManager = new BleManager(this);
        bleManager.enableBluetooth();
    }

    @Override
    public void onDestroy() {
        bleManager.closeBluetoothGatt();
        super.onDestroy();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }



    public class BluetoothBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public void setScanCallback(Callback callback) {
        mCallback = callback;
    }

//    public void setConnectCallback(Callback2 callback) {
//        mCallback2 = callback;
//    }

    public interface Callback {

        void onStartScan();

        void onScanning(ScanResult scanResult);

        void onScanComplete();

        void onConnecting();

        void onConnectFail();

        void onDisConnected();

        void onServicesDiscovered();
    }

    public interface Callback2 {

        void onDisConnected();
    }

//    public void scanDevice() {
////        resetInfo();
//
//        if (mCallback != null) {
//            mCallback.onStartScan();
//        }
//
//        boolean b = bleManager.scanDevice(new ListScanCallback(5000) {
//
//            @Override
//            public void onDeviceFound(BluetoothDevice[] devices) {
//
//            }
//
////            @Override
////            public void onScanning(final ScanResult result) {
//////                runOnMainThread(new Runnable() {
//////                    @Override
//////                    public void run() {
////                        if (mCallback != null) {
////                            mCallback.onScanning(result);
////                        }
//////                    }
//////                });
////            }
//
//            @Override
//            public void onScanComplete(final ScanResult[] results) {
////                runOnMainThread(new Runnable() {
////                    @Override
////                    public void run() {
//                        if (mCallback != null) {
//                            mCallback.onScanComplete();
//                        }
////                    }
////                });
//            }
//        });
//        if (!b) {
//            if (mCallback != null) {
//                mCallback.onScanComplete();
//            }
//        }
//    }



    public void scanAndConnect2(String name) {
//        resetInfo();

        if (mCallback != null) {
            mCallback.onStartScan();
        }

        bleManager.scanNameAndConnect(name, 10000, false, new BleGattCallback() {

//            @Override
//            public void onFoundDevice(ScanResult scanResult) {
////                runOnMainThread(new Runnable() {
////                    @Override
////                    public void run() {
//                        if (mCallback != null) {
//                            mCallback.onScanComplete();
//                        }
////                    }
////                });
//                BluetoothService.this.name = scanResult.getDevice().getName();
//                BluetoothService.this.mac = scanResult.getDevice().getAddress();
////                runOnMainThread(new Runnable() {
////                    @Override
////                    public void run() {
//                        if (mCallback != null) {
//                            mCallback.onConnecting();
////                        }
//                    }
////                });
//            }
//
//            @Override
//            public void onConnecting(BluetoothGatt gatt, int status) {
//
//            }
//
//            @Override
//            public void onConnectError(BleException exception) {
////                runOnMainThread(new Runnable() {
////                    @Override
////                    public void run() {
//                        if (mCallback != null) {
//                            mCallback.onConnectFail();
//                        }
////                    }
////                });
//            }

            @Override
            public void onNotFoundDevice() {

            }

            @Override
            public void onFoundDevice(BluetoothDevice device) {

            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                gatt.discoverServices();
            }

            @Override
            public void onConnectFailure(BleException exception) {
                if (mCallback != null) {
                    mCallback.onDisConnected();
                }
            }
//
//            @Override
//            public void onDisConnected(BluetoothGatt gatt, int status, BleException exception) {
////                runOnMainThread(new Runnable() {
////                    @Override
////                    public void run() {
//                        if (mCallback != null) {
//                            mCallback.onDisConnected();
//                        }
//                        if (mCallback2 != null) {
//                            mCallback2.onDisConnected();
//                        }
////                    }
////                });
//            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                BluetoothService.this.gatt = gatt;
//                runOnMainThread(new Runnable() {
//                    @Override
//                    public void run() {
                        if (mCallback != null) {
                            mCallback.onServicesDiscovered();
                        }
                    }
//                });
//            }
        });
    }


    public boolean read(String uuid_service, String uuid_read, BleCharacterCallback callback) {
        return bleManager.readDevice(uuid_service, uuid_read, callback);
    }

    public boolean write(String uuid_service, String uuid_write, String hex, BleCharacterCallback callback) {
        return bleManager.writeDevice(uuid_service, uuid_write, HexUtil.hexStringToBytes(hex), callback);
    }
    public boolean write2(String uuid_service, String uuid_write, String hex, BleCharacterCallback callback) {
        return bleManager.writeDevice(uuid_service, uuid_write, HexUtil.hexStringToBytes(hex), callback);
    }

    public boolean notify(String uuid_service, String uuid_notify, BleCharacterCallback callback) {
        return bleManager.notify(uuid_service, uuid_notify, callback);
    }

    public boolean indicate(String uuid_service, String uuid_indicate, BleCharacterCallback callback) {
        return bleManager.indicate(uuid_service, uuid_indicate, callback);
    }

    public boolean stopNotify(String uuid_service, String uuid_notify) {
        return bleManager.stopNotify(uuid_service, uuid_notify);
    }

    public boolean stopIndicate(String uuid_service, String uuid_indicate) {
        return bleManager.stopIndicate(uuid_service, uuid_indicate);
    }

//    public boolean readRssi(BleRssiCallback callback) {
//        return bleManager.readRssi(callback);
//    }







    public String getMac() {
        return mac;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }

    public int getCharaProp() {
        return charaProp;
    }




}
