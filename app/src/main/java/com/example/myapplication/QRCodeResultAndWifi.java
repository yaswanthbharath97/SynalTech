package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class QRCodeResultAndWifi extends AppCompatActivity {
    private ListView wifiList1;
    private WifiManager wifiManager;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_result_and_wifi);
        wifiList1 = findViewById(R.id.listWifi);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Activity.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(QRCodeResultAndWifi.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(QRCodeResultAndWifi.this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(QRCodeResultAndWifi.this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(QRCodeResultAndWifi.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
             enableWifi();
            startWifiScan();
        }

    }
    private void enableWifi() {
        // If Wi-Fi is not enabled, enable it
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }
    private void connectToWifi(String ssid) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (wifiConfiguration.SSID.equals("\"" + ssid + "\"")) {
                wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                break;
            }
        }
    }
    private void startWifiScan() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startWifiScan();
            }
        }
    }
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityCompat.checkSelfPermission(QRCodeResultAndWifi.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return;
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();


            List<ScanResult> scanResults = wifiManager.getScanResults();
            List<String>wifiList=new ArrayList<>();
            for(ScanResult scanResult:scanResults)
            {
                int signalStrength = WifiManager.calculateSignalLevel(scanResult.level, 5);
                String ssid = scanResult.SSID;
                if (ssid != null && !ssid.isEmpty()) {
                    wifiList.add(ssid + " ( " + signalStrength + "/5)");
                }
            }
            ArrayAdapter<String>adapter=new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,wifiList);
                wifiList1.setAdapter(adapter);

            wifiList1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedWifi = (String) parent.getItemAtPosition(position);
                    String[] parts = selectedWifi.split("\\(");
                    String ssid = parts[0];
                    Log.d("SelectedWifi", "Selected Wi-Fi: " + ssid);

                    WifiConfiguration  wifiConfig=new WifiConfiguration();
                    wifiConfig.SSID="\"" + ssid + "\"";
                    wifiConfig.status = WifiConfiguration.Status.ENABLED;
                    wifiConfig.priority = 40;


                    final EditText passwordEditText=new EditText(QRCodeResultAndWifi.this);
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    new AlertDialog.Builder(QRCodeResultAndWifi.this)
                            .setTitle("Enter WiFi Password ") .setMessage("Enter password for " + ssid + ":")
                            .setView(passwordEditText)
                            .setPositiveButton("Connect", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password=passwordEditText.getText().toString();
                                    if(!password.isEmpty()) {
                                        wifiConfig.preSharedKey = "\"" + password + "\"";
                                    }
                                         int networkId = wifiManager.addNetwork(wifiConfig);
                                    if (networkId == -1) {
                                        Log.e("Wifi", "Failed to add network: " + wifiConfig);
                                        return;
                                    }
                                           wifiManager.disconnect();
                                           wifiManager.enableNetwork(networkId,true);
                                           wifiManager.reconnect();

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                }
                            })
                            .show();
                }
            });
        }
    };

    protected void onResume() {
        super.onResume();
        wifiManager.startScan();
    }

}