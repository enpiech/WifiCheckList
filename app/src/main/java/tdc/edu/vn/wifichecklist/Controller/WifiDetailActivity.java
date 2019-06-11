package tdc.edu.vn.wifichecklist.Controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import tdc.edu.vn.wifichecklist.Model.Wifi;
import tdc.edu.vn.wifichecklist.R;

public class WifiDetailActivity extends AppCompatActivity {
    private ListView lstWifiProperties;
    private Button btnConnect;
    private EditText edtPassword;

    private Wifi wifiData;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> dataSet;

    private WifiManager wifiManager;

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
        connectToWifi(wifi, password);
    }

    private void connectToWifi(Wifi wifi, String password) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = wifi.getSsid();
        wifiConfiguration.preSharedKey = String.format("\"%s\"", password);

        // netId == -1 if connected failed
        int netId = wifiManager.addNetwork(wifiConfiguration);

        if (netId != -1) {
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

            wifiConfiguration.SSID = "\"\"" + wifi.getSsid() + "\"\"";
            wifiConfiguration.preSharedKey = "\"\"" + password + "\"\"";
            wifiManager.addNetwork(wifiConfiguration);

            saveWifi(wifi, password);

            Toast.makeText(this, "Connect success to " + wifi.getWifiName(), Toast.LENGTH_SHORT).show();
        } else {

            edtPassword.setText("");
            edtPassword.setError("Wrong password");
            edtPassword.requestFocus();

            Toast.makeText(this, "Wrong password! Connect failed to " + wifi.getWifiName(), Toast.LENGTH_SHORT).show();
        }

    }

    private void saveWifi(Wifi wifi, String password) {
        //TODO Save password and wifi name to SQLite


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
