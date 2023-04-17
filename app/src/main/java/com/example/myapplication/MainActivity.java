package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION = 2;

    Button startButton;

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.buttonOn);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisable();
            }
        });


    }

    private void enableDisable() {
        if (bluetoothAdapter == null) {
            Log.d(TAG, "enableDisable:Does not Have Bt capabilities");
            return;

        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION);

                return;
            }
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
            IntentFilter BtIntent=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
             registerReceiver(mBroadcastReceiver1,BtIntent);
        }
        else {
            bluetoothAdapter.disable();
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
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT)
        {
            if(resultCode==RESULT_OK)
            {
                Log.d(TAG,"onActivityResult:BT enabled");
            }else
            {
                Log.d(TAG,"onActivityResult:Bt not enabled");
            }
        }
    }

}