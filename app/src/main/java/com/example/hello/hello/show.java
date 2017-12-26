package com.example.hello.hello;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hello.hello.BluetoothService;
/**
 * Created by shenjunwei on 17/12/21.
 */

public class show extends Activity {

    public static String TAG = "xxoo";
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Bundle bundle;
    private String mDeviceAddress,mDeviceName;
    private static BluetoothService mbluetoothService;
    private


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
            return;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mbluetoothService = null;
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
                Log.i(TAG,"action connected");
            }
            if(BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG,"action disconnected");
            }
            if(BluetoothService.ACTION_GATT_DATA_OK.equals(action)){
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
        intentFilter.addAction(BluetoothService.ACTION_GATT_DATA_OK);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCOVERED);
        registerReceiver(mBluetoothSericeRecevier, intentFilter);
        if(mbluetoothService != null && mDeviceAddress != null){
            mbluetoothService.BluetoothServiceConnect(mDeviceAddress);
        }
        return;
    }

}
