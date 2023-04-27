package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class QRCodeResultAndWifi extends AppCompatActivity {
    private static final int MY_PERMISSIONS_CHANGE_WIFI_STATE = 2;
    private ListView wifiList1;
    private WifiConfiguration wifiConfiguration;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_result_and_wifi);

        // Check if the app has the required permission to access location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
              enableWifi();
        }

    }

    private void enableWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, MY_PERMISSIONS_CHANGE_WIFI_STATE);
        } else {
            try {
                if (!wifiManager.isWifiEnabled()) {


                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(QRCodeResultAndWifi.this, "Enabled WiFi", Toast.LENGTH_SHORT).show();
                }
                List<ScanResult> scanResults = wifiManager.getScanResults();
                wifiList1 = findViewById(R.id.listWifi);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(QRCodeResultAndWifi.this, android.R.layout.simple_list_item_1);

                for (ScanResult result : scanResults) {
                    int signalStrength = WifiManager.calculateSignalLevel(result.level, 5);
                    String ssid = result.SSID;
                    if (ssid != null && !ssid.isEmpty()) {
                        adapter.add(ssid+ " ( " + signalStrength + "/5)");
                    }
                }
                wifiList1.setAdapter(adapter);
                wifiList1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String item = (String) parent.getItemAtPosition(position);
                        String ssid = item.split(" ")[0]; // get the SSID from the selected item
                       // replace "password" with the actual password
                        AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeResultAndWifi.this);
                        builder.setTitle("Enter Wi-Fi Password");

                        // Set up the input
                        final EditText input = new EditText(QRCodeResultAndWifi.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String password = input.getText().toString();
                                connectToWifi(ssid, password);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                });
                // process scan results here
            } catch (SecurityException e) {
                Toast.makeText(this, "Could not enable WiFi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectToWifi(String ssid, String password) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";
        wifiConfig.preSharedKey = "\"" + password + "\"";
        int networkId = wifiManager.addNetwork(wifiConfig);
        if (networkId == -1)
        {
            Toast.makeText(this, "Failed to add Wi-Fi network configuration", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        List<WifiConfiguration> configuredNetwork = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configuredNetwork)
        {
            if (config.networkId == networkId)
            {
                wifiManager.enableNetwork(networkId, true);
            }
            else
            {
                wifiManager.disableNetwork(config.networkId);
            }
        }
        wifiManager.reconnect();
        Toast.makeText(this, "Connected to Wi-Fi network " + ssid, Toast.LENGTH_SHORT).show();
    }

     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_ACCESS_COARSE_LOCATION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                enableWifi();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
