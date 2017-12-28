package com.example.hello.hello;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hello.hello.BluetoothService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shenjunwei on 17/12/21.
 */

public class show extends Activity {

    public static String TAG = "xxoo";

    private static String BLUETOOTH_ETARGET_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private static String C_STATUS_CONNECTED = "连接";
    private static String C_STATUS_DISCONNECTED = "断开";

    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static int MSG_STATUS = 0x01;

    private Bundle bundle;
    private String mDeviceAddress,mDeviceName;
    private static BluetoothService mbluetoothService;
    private boolean connect_status = false;
    private Handler mhandler;
    private static BluetoothGattCharacteristic target_chara = null;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();


    TextView show_value ;
    TextView show_count ;
    TextView show_status;
    Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show);

        bundle = getIntent().getExtras();
        mDeviceAddress = bundle.getString(EXTRAS_DEVICE_ADDRESS);
        mDeviceName = bundle.getString(EXTRAS_DEVICE_NAME);

        show_value = (TextView) findViewById(R.id.show_value);
        show_count = (TextView) findViewById(R.id.show_count);
        show_status = (TextView) findViewById(R.id.show_status);

        reset = (Button) findViewById(R.id.button_reset);
        reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                show_status.setText("status");
                show_count.setText("0");
                show_value.setText("0.0");
                Log.i(TAG, "Reset button click .");
            }
        });

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mbluetoothService = ((BluetoothService.ServiceBinder) iBinder).getService();
            if(!mbluetoothService.BluetoothServiceInit()){
                Log.e(TAG, "mbluetoothService.BluetoothServiceInit() false");
                finish();
                return;
            }
            if(!mbluetoothService.BluetoothServiceConnect(mDeviceAddress)){
                Log.e(TAG, "mbluetoothService.BluetoothServiceConnect " + mDeviceAddress + " false");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mbluetoothService = null;
        }
    };
    private void updateBluetoothData(String data){
        Log.i(TAG, "R[" + data + "]");
    }
    private void updateConnectionStatus(int what, String status){
        Message msg = new Message();
        msg.what = what;
        Bundle bundle = new Bundle();
        bundle.putString("C_STATUS", status);
        msg.setData(bundle);
        msghander.sendMessage(msg);
        Log.i(TAG,"Changed status " + status);
    }
    private Handler msghander = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_STATUS:
                    String status = msg.getData().getString("C_STATUS");
                    show_status.setText(status);
                    break;
            }
        }
    };
    private final BroadcastReceiver mBluetoothSericeRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(BluetoothService.ACTION_GATT_DISCOVERED.equals(action)){
                Log.i(TAG,"action discovered");
            }
            if(BluetoothService.ACTION_GATT_CONNECTED.equals(action)){
                connect_status = true;
                updateConnectionStatus(MSG_STATUS, C_STATUS_CONNECTED);
                Log.i(TAG,"action connected");
            }
            if(BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connect_status = false;
                updateConnectionStatus(MSG_STATUS, C_STATUS_DISCONNECTED);
                Log.i(TAG,"action disconnected");
            }
            if(BluetoothService.ACTION_GATT_DATA_AVAILABLE.equals(action)){
                updateBluetoothData(intent.getExtras().getString(BluetoothService.ACTION_GATT_DATA_AVAILABLE_READ));
                Log.i(TAG,"action ok");
            }
        }
    };
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mBluetoothSericeRecevier);
        mbluetoothService = null;
    }
    @Override
    protected  void onResume(){
        super.onResume();
        Log.i(TAG, "Show onResume .");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCOVERED);
        registerReceiver(mBluetoothSericeRecevier, intentFilter);
        if(mbluetoothService != null && mDeviceAddress != null){
            mbluetoothService.BluetoothServiceConnect(mDeviceAddress);
        }
        return;
    }
    private void DisplayGattServices(List<BluetoothGattService> gattServices){
        if(gattServices == null){
            return;
        }
        String uuid = null;
        String unknownServiceString = "unkownService";
        String unknownCharaString = "unknownCharacteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        for(BluetoothGattService gattService : gattServices){
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            gattServiceData.add(currentServiceData);
            Log.i(TAG, "Service UUID : " + uuid);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            for(final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                if (gattCharacteristic.getUuid().toString().equals(BLUETOOTH_ETARGET_UUID))
                {
                    mhandler.postDelayed(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            mbluetoothService.readCharacteristic(gattCharacteristic);
                        }
                    }, 200);
                    mbluetoothService.setCharacteristcNotification(
                            gattCharacteristic, true);
                    target_chara = gattCharacteristic;
                    // mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                }
                List<BluetoothGattDescriptor> descriptors = gattCharacteristic
                        .getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors)
                {
                    Log.i(TAG, "---descriptor UUID:" + descriptor.getUuid());
                    mbluetoothService.getCharacteristicDescriptor(descriptor);
                    // mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,
                    // true);
                }

                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

}
