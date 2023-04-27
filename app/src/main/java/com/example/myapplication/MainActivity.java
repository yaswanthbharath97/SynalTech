package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;

    Button startButton;
    private boolean isReceiverRegistered = false;
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;

    private ArrayAdapter<String> listAdapter;
    private final ArrayList<String> deviceList = new ArrayList<>();

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.buttonOn);
        bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Activity.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter);
        isReceiverRegistered = true;
        ListView listView = findViewById(R.id.bluetoothList);
        listAdapter=new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,deviceList);
        listView.setAdapter(listAdapter);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_PERMISSION);
        }

        // Check for ACCESS_FINE_LOCATION permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        } else {
            // Permission has already been granted, so enable/disable Bluetooth
            enableDisable();

        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {

                    enableDisable();
                    Intent qrIntent = new Intent(MainActivity.this, QRCodeResultAndWifi.class);
                    startActivity(qrIntent);
                }
            }

        });


    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // Unregister the Bluetooth state change receiver
        if (isReceiverRegistered)
        {
            unregisterReceiver(mBroadcastReceiver1);
            isReceiverRegistered = false;
        }
    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF -> Log.d(TAG, "OnReceive:STATE_OFF");
                    case BluetoothAdapter.STATE_TURNING_OFF ->
                            Log.d(TAG, "OnReceive:STATE_TURNING_OFF");
                    case BluetoothAdapter.STATE_ON -> Log.d(TAG, "OnReceive:STATE_ON");
                    case BluetoothAdapter.STATE_TURNING_ON ->
                            Log.d(TAG, "OnReceive:STATE_TURNING_ON ");
                }

            }
        }
    };


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (isReceiverRegistered) {
            unregisterReceiver(mBroadcastReceiver1);
            isReceiverRegistered = false;
        }
        unregisterReceiver(mBroadcastReceiver2);
    }

    private boolean isReceiverRegistered(BroadcastReceiver receiver) {
        try {
            return isReceiverRegistered && receiver != null && this.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;

        } catch (Exception e)
        {
            return false;
        }
    }

    @SuppressLint("InlinedApi")
    private void enableDisable()
    {
        if (bluetoothAdapter == null)
        {
            Log.d(TAG, "enableDisable:Does not Have Bt capabilities");
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSION);
                return;
            } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                return;
            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            IntentFilter BtIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BtIntent);


            if (bluetoothAdapter.isDiscovering())
            {
                bluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "enableDisable: Canceling discovery");
                checkBTPermissions();
                bluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
                deviceList.clear();
                listAdapter.notifyDataSetChanged();
            }
        }

    }

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                if(device!=null) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    String deviceString = deviceName + " - " + deviceHardwareAddress;
                    if (!deviceList.contains(deviceString)) {
                        deviceList.add(deviceString);
                        listAdapter.notifyDataSetChanged();
                    }
                }

          }
      }
  };
    private void checkBTPermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableDisable();
            } else {
                Toast.makeText(MainActivity.this, "Bluetooth permission is required to enable Bluetooth", Toast.LENGTH_SHORT).show();
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult:BT enabled");
            } else {
                Log.d(TAG, "onActivityResult:Bt not enabled");
            }
        }
    }

}