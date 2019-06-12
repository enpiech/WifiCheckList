package tdc.edu.vn.wifichecklist.Controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import tdc.edu.vn.wifichecklist.DAL.DBManager;
import tdc.edu.vn.wifichecklist.Model.Wifi;
import tdc.edu.vn.wifichecklist.R;

public class WifiDetailActivity extends AppCompatActivity {
    private boolean isExist = false;

    private ListView lstWifiProperties;
    private Button btnConnect;
    private EditText edtPassword;

    private Wifi wifiData;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> dataSet;

    private WifiManager wifiManager;
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Current: " + wifiManager.getConnectionInfo().getSSID(), Toast.LENGTH_SHORT).show();
            if (wifiManager.getConnectionInfo().getSSID().equals("\"" + wifiData.getSsid() + "\"")) {
                Toast.makeText(context, "You are connected to " + wifiData.getSsid(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_detail_layout);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        dataSet = new ArrayList<>();

        loadView();

        loadData();
    }

    void loadView() {
        // Set action barr
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstWifiProperties = findViewById(R.id.lstWifiProperties);
        btnConnect = findViewById(R.id.btnConnect);
        edtPassword = findViewById(R.id.edtPassword);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareToConnectWifi(wifiData);
            }
        });
    }

    void loadData() {
        wifiData = (Wifi) getIntent().getSerializableExtra("wifiData");

        DBManager db = new DBManager(this);
        Wifi dbWifi = db.selectWifiData(wifiData);
        if (dbWifi != null) {
            isExist = true;
            edtPassword.setText(dbWifi.getPassword());
        } else {
            isExist = false;
            btnConnect.setEnabled(false);
        }

        db.selectAllWifiData();

        if (wifiData != null) {
            dataSet.add("SSID: " + wifiData.getSsid());
            dataSet.add("BSSID: " + wifiData.getBssid());
            dataSet.add("Security: " + wifiData.getSecurity());
            dataSet.add("Frequency: " + wifiData.getFrequency());
            dataSet.add("RSSI: " + wifiData.getRssi());
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataSet);
        lstWifiProperties.setAdapter(arrayAdapter);
    }

    private void prepareToConnectWifi(final Wifi wifi) {
        final String password = edtPassword.getText().toString();
        wifi.setPassword(password);
        connectToWifi(wifi);
    }

    private void connectToWifi(final Wifi wifi) {
        if (wifiManager.getConnectionInfo().getSSID().equals(String.format("\"%s\"", wifi.getSsid()))) {
            Toast.makeText(this, "You are already connected to this wi-fi network", Toast.LENGTH_SHORT).show();
            return;
        }

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = String.format("\"%s\"", wifi.getSsid());
        wifiConfiguration.preSharedKey = String.format("\"%s\"", wifi.getPassword());

        // netId == -1 if connection is added before or failed
        int netId = wifiManager.addNetwork(wifiConfiguration);


        // If add network is not success
        if (netId != -1) {
            wifiManager.enableNetwork(netId, true);
            wifiManager.disconnect();
            wifiManager.reconnect();
            createWifiData(wifi);
        } else {
            // Check if wifi network is added before
            for (WifiConfiguration conf : wifiManager.getConfiguredNetworks()) {
                if (conf.SSID.equals(String.format("\"%s\"", wifi.getSsid()))) {
                    wifiManager.enableNetwork(conf.networkId, true);
                    wifiManager.disconnect();
                    wifiManager.reconnect();

                    if (isExist) {
                        updateWifiData(wifi);
                    } else {
                        createWifiData(wifi);
                    }
                    return;
                }
            }

            edtPassword.setText("");
            edtPassword.setError("Wrong password");
            edtPassword.requestFocus();

            Toast.makeText(this, "Wrong password! Connect failed to " + wifi.getWifiName(), Toast.LENGTH_SHORT).show();
        }

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void createWifiData(Wifi wifi) {
        DBManager dbManager = new DBManager(this);
        if (dbManager.createWifiData(wifi) == -1) {
            Toast.makeText(this, "Created failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWifiData(Wifi wifi) {
        DBManager dbManager = new DBManager(this);
        if (dbManager.updateWifiData(wifi) == -1) {
            Toast.makeText(this, "Update data failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
