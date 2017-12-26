package com.example.hello.hello;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BluetoothService extends Service {

    private final static String TAG = "xxoo";
    public final static String ACTION_GATT_CONNECTED    = "com.example.hello.hello.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.hello.hello.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCOVERED   = "com.example.hello.hello.ACTION_GATT_DISCOVERED";
    public final static String ACTION_GATT_DATA_OK      = "com.example.hello.hello.ACTION_GATT_DATA_OK";

    private String currentDeviceAddress;
    private BluetoothManager mbluetoothManager;
    private BluetoothAdapter mbluetoothAdapter;
    private BluetoothGatt mbluetoothGatt;

    public boolean BluetoothServiceInit() {
        if(mbluetoothManager == null){
            mbluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(mbluetoothManager == null){
                return false;
            }
        }
        mbluetoothAdapter = mbluetoothManager.getAdapter();
        if(mbluetoothAdapter == null){
            return false;
        }
        Log.i(TAG,"Service BluetoothServiceInit() true .");
        return true;
    }
    public boolean BluetoothServiceConnect(String address){
        if(mbluetoothAdapter == null || address == null){
            Log.i(TAG,"Service BluetoothServiceConnect false");
            return false;
        }
        Log.i(TAG, "Service BluetoothServiceConnect address :" + address);
        final BluetoothDevice device = mbluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            Log.i(TAG, "Service BluetoothServiceConnect " + address + "is null");
            return false;
        }
        mbluetoothGatt = device.connectGatt(this, false, mbluetoothgattcallback);
        if(mbluetoothGatt == null){
            Log.i(TAG,"Service mbluetoothGatt is null .");
        }
        currentDeviceAddress = address;
        return true;
    }
    public boolean BluetoothServiceWrite(BluetoothGattCharacteristic characteristic){
        if(mbluetoothAdapter == null || mbluetoothGatt == null){
            Log.i(TAG,"Service BluetoothServiceWrite is null");
            return false;
        }
        return mbluetoothGatt.writeCharacteristic(characteristic);
    }
    public void BluetoothServiceDisconnect(){
        if(mbluetoothAdapter == null || mbluetoothGatt == null){
            Log.i(TAG, "Service BluetoothServiceDisconnect() empty");
            return ;
        }
    }
    public void BluetoothServiceClose(){
        if(mbluetoothGatt == null){
            return ;
        }
        mbluetoothGatt.close();
        mbluetoothGatt = null;
    }
    private void BluetoothBroadcast(final String action){
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void BluetoothBroadcast(final String action, BluetoothGattCharacteristic characteristic){
        Intent intent = new Intent(action);

        byte[] data = characteristic.getValue();

        sendBroadcast(intent);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mserviceBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        BluetoothServiceClose();
        return super.onUnbind(intent);
    }
    public class ServiceBinder extends Binder {
        public BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    final ServiceBinder mserviceBinder = new ServiceBinder();

    private final BluetoothGattCallback mbluetoothgattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG,"Service onConnectionStateChange .");
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG,"Service onServicesDiscovered .");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG,"Service onCharacteristicRead . ");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "Service onCharacteristicWrite .");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG,"Service onCharacteristicChanged .");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(TAG,"Service onDescriptorRead .");

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG,"Service onDescriptorWrite .");

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.i(TAG,"Service onReliableWriteCompleted .");

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(TAG,"Service onReadRemoteRssi .");

        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG,"Service onMtuChanged .");

        }

    };
}
