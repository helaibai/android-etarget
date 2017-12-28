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
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

public class BluetoothService extends Service {

    private final static String TAG = "xxoo";
    public final static String ACTION_GATT_CONNECTED    = "com.example.hello.hello.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.hello.hello.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCOVERED   = "com.example.hello.hello.ACTION_GATT_DISCOVERED";
    public final static String ACTION_GATT_DATA_AVAILABLE      = "com.example.hello.hello.ACTION_GATT_DATA_OK";
    public final static String ACTION_GATT_DATA_AVAILABLE_READ = "com.example.hello.hello.ACTION_GATT_DATA_OK_READ";
    private final static String muuid = "00002902-0000-1000-8000-00805f9b34fb";


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
        }else{
            Log.i(TAG, "Serice mbluetoothGatt is sucess .");
        }
        currentDeviceAddress = address;
        return true;
    }
    public void BluetoothServiceReadRssi(){
        if(mbluetoothAdapter == null || mbluetoothGatt == null){
            Log.e(TAG, "BluetoothServiceReadRssi is null");
            return;
        }
        mbluetoothGatt.readRemoteRssi();
    }
    public void BluetoothServiceRead(BluetoothGattCharacteristic characteristic){
        if(mbluetoothAdapter == null || mbluetoothGatt == null){
            Log.e(TAG, "BluetoothServiceRead is null");
            return;
        }
        mbluetoothGatt.readCharacteristic(characteristic);
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
            Log.e(TAG, "Service BluetoothServiceDisconnect() empty");
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
    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mbluetoothAdapter == null || mbluetoothGatt == null)
        {
            Log.e(TAG, "readCharacteristic not initialized");
            return;
        }
        mbluetoothGatt.readCharacteristic(characteristic);

    }
    public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor){
        if(mbluetoothAdapter == null || mbluetoothGatt == null){
            Log.e(TAG, "Service getCharacteristicDescriptor() empty");
            return ;
        }
        mbluetoothGatt.readDescriptor(descriptor);
    }
    public boolean setCharacteristcNotification(BluetoothGattCharacteristic characteristic, boolean enabled){
        if(mbluetoothAdapter == null || mbluetoothGatt == null){
            Log.d(TAG, "Service setCharacteristcNotification is null .");
            return false;
        }
        mbluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor clinetConfig = characteristic.getDescriptor(UUID.fromString(muuid));
        if(enabled){
            clinetConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{
            clinetConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mbluetoothGatt.writeDescriptor(clinetConfig);
    }
    private void BluetoothBroadcast(final String action){
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void BluetoothBroadcast(final String action, BluetoothGattCharacteristic characteristic){
        Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if(data != null && data.length < 0){
            intent.putExtra(ACTION_GATT_DATA_AVAILABLE_READ, data);
        }
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
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.d(TAG, currentDeviceAddress + " 链接成功.");
                BluetoothBroadcast(ACTION_GATT_CONNECTED);
            }
            if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.d(TAG, currentDeviceAddress + " 断开链接");
                BluetoothBroadcast(ACTION_GATT_DISCONNECTED);
            }
            Log.i(TAG,"Service onConnectionStateChange() .");
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG,"Service onServicesDiscovered() status :" + status);
            if(status == BluetoothGatt.GATT_SUCCESS){
                BluetoothBroadcast(ACTION_GATT_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG,"Service onCharacteristicRead() status :" + status);
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d(TAG, "onCharacteristicRead Data success");
                BluetoothBroadcast(ACTION_GATT_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "Service onCharacteristicWrite() .");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG,"Service onCharacteristicChanged() .");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG,"Service onDescriptorRead() .");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG,"Service onDescriptorWrite .");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.i(TAG,"Service onReliableWriteCompleted .");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(TAG,"Service onReadRemoteRssi .");
        }

    };
}
