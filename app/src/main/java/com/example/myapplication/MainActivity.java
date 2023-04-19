package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;

    Button startButton;

    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.buttonOn);
        bluetoothManager=(BluetoothManager)getApplicationContext().getSystemService(Activity.BLUETOOTH_SERVICE);
        bluetoothAdapter =bluetoothManager.getAdapter();

        // Check for ACCESS_FINE_LOCATION permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        } else {
            // Permission has already been granted, so enable/disable Bluetooth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                enableDisable();

            }
        }
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    enableDisable();
                    Intent qrIntent=new Intent(MainActivity.this,QRCodeResultAndWifi.class);
                    startActivity(qrIntent);
                }
                else {

                    Intent qrIntent=new Intent(MainActivity.this,QRCodeResultAndWifi.class);
                    startActivity(qrIntent);
                }
            }

        });


    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
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
    protected void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered(mBroadcastReceiver1)) {
            unregisterReceiver(mBroadcastReceiver1);
        }
    }

    private boolean isReceiverRegistered(BroadcastReceiver receiver) {
        try {
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            return registerReceiver(receiver, filter) != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void enableDisable() {
        if (bluetoothAdapter == null) {
            Log.d(TAG, "enableDisable:Does not Have Bt capabilities");
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            return;

        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION);
                return;
            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            IntentFilter BtIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BtIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    enableDisable();
                }
            } else {
                Toast.makeText(MainActivity.this, "Bluetooth permission is required to enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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