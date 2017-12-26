package com.example.hello.hello;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.BassBoost;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class Start extends AppCompatActivity {

    private Button button_scan;
    private BluetoothAdapter mbluetoothAdapter;
    private static String TAG = "xxoo";
    private Handler mhandler;
    private boolean scanning = false;
    ListView lv;
    DeviceListAdapter mdeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Log.i(TAG, "Start.onCreate()");
        mhandler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mbluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothManager == null || !mbluetoothAdapter.isEnabled()) {
            Log.i(TAG, "enable bluetooth .");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            final int REQUEST_ENABLE_BT = 1;
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
        mdeviceListAdapter = new DeviceListAdapter();
        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(mdeviceListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg, View view, int pos, long id){
                final BluetoothDevice device = mdeviceListAdapter.getDevice(pos);
                if(device == null)
                    return;
                if(scanning) {
                    mbluetoothAdapter.stopLeScan(mbluetoothscanCallback);
                    scanning = false;
                    button_scan.setText("开始扫描");
                }
                final Intent intent = new Intent(Start.this, show.class);
                intent.putExtra(show.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(show.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                try {
                    startActivity(intent);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        button_scan = (Button) findViewById(R.id.button_scan);
        button_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanning == false) {
                    Log.i(TAG, "start scan ...");
                    button_scan.setText("正在扫描 ...");
                    mhandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "stop scan by handler .");
                            scanning = false;
                            button_scan.setText("开始扫描");
                            mbluetoothAdapter.stopLeScan(mbluetoothscanCallback);
                        }
                    }, 10000);
                    mbluetoothAdapter.startLeScan(mbluetoothscanCallback);
                    scanning = true;
                }
            }

        });
    }
    private BluetoothAdapter.LeScanCallback mbluetoothscanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "scan callback -" + device.getName() + "-" + device.getAddress() + "-" + rssi);
                    mdeviceListAdapter.addDevice(device, rssi);
                    mdeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mdevices;
        private LayoutInflater mInflator;

        public DeviceListAdapter()
        {
            super();
            mdevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }
        public void addDevice(BluetoothDevice device, int rssi)
        {
            if(!mdevices.contains(device)){
                Log.i(TAG, "addDevice" + device.getAddress());
                mdevices.add(device);
            }
        }
        public void clear()
        {
            mdevices.clear();
        }
        public BluetoothDevice getDevice(int pos)
        {
            return mdevices.get(pos);
        }
        @Override
        public long getItemId(int i)
        {
            return i;
        }
        @Override
        public Object getItem(int i)
        {
            return mdevices.get(i);
        }
        @Override
        public int getCount()
        {
            return mdevices.size();
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            Log.i(TAG, "getView().");
            view = mInflator.inflate(R.layout.listitem, null);
            TextView deviceInformation = (TextView) view.findViewById(R.id.device_information);
            BluetoothDevice device = mdevices.get(i);
            deviceInformation.setText(device.getAddress());
            return view;
        }
    }
}
