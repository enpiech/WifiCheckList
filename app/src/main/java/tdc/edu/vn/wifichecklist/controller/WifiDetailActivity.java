package tdc.edu.vn.wifichecklist.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import tdc.edu.vn.wifichecklist.R;
import tdc.edu.vn.wifichecklist.dal.DataSource;
import tdc.edu.vn.wifichecklist.dal.SQLiteDBManager;
import tdc.edu.vn.wifichecklist.model.Wifi;

public class WifiDetailActivity extends AppCompatActivity {
    private boolean isExist = false;

    private ListView lstWifiProperties;
    private Button btnConnect;
    private EditText edtPassword;

    private Wifi wifiData;

    private WifiManager wifiManager;
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Current: " + wifiManager.getConnectionInfo().getSSID(), Toast.LENGTH_SHORT).show();
            if (wifiManager.getConnectionInfo().getSSID().equals("\"" + wifiData.getSsid() + "\"")) {
                Toast.makeText(context, "You are already connected to " + wifiData.getSsid(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_detail_layout);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        loadView();
        loadData();
        setEvent();
    }

    private void loadView() {
        // Set action barr
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lstWifiProperties = findViewById(R.id.lstWifiProperties);
        btnConnect = findViewById(R.id.btnConnect);
        edtPassword = findViewById(R.id.edtPassword);
    }

    private void loadData() {
        ArrayList<String> dataSet = new ArrayList<>();

        int selected = (int) getIntent().getSerializableExtra(WifiListActivity.EXTRA_WIFI_DATA);
        wifiData = DataSource.getCurrentWifiList().get(selected);

        // Check if current wifi is known
        SQLiteDBManager db = new SQLiteDBManager(this);
        Wifi dbWifi = db.selectWifiData(wifiData);
        if (dbWifi != null) {
            isExist = true;
            edtPassword.setText(dbWifi.getPassword());

            Log.d("Detail", "Known wifi, pass" + dbWifi.getPassword());
        } else {
            isExist = false;
            Log.d("Detail", "Unknown wifi");
        }

        dataSet.add("SSID: " + wifiData.getSsid());
        dataSet.add("BSSID: " + wifiData.getBssid());
        dataSet.add("Security: " + wifiData.getSecurity());
        dataSet.add("Frequency: " + wifiData.getFrequency());
        dataSet.add("RSSI: " + wifiData.getRssi());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataSet);
        lstWifiProperties.setAdapter(arrayAdapter);
    }

    private void setEvent() {
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareToConnectWifi(wifiData);
            }
        });
    }

    private void prepareToConnectWifi(final Wifi wifi) {
        // Get input password
        final String password = edtPassword.getText().toString();

        requestConnectToWifi(wifi, password);
    }

    private void requestConnectToWifi(final Wifi wifi, String password) {
        // If already connect to this network
        if (wifiManager.getConnectionInfo().getSSID().equals(String.format("\"%s\"", wifi.getSsid()))) {
            Toast.makeText(this, "You are already connected to this wi-fi network", Toast.LENGTH_SHORT).show();
            return;
        }

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = String.format("\"%s\"", wifi.getSsid());
        wifiConfiguration.preSharedKey = String.format("\"%s\"", password);

        int netId = -1;

        // Check if this network is known
        for (WifiConfiguration conf : wifiManager.getConfiguredNetworks()) {
            if (conf.SSID.equals(String.format("\"%s\"", wifi.getSsid()))) {
                netId = conf.networkId;
                break;
            }
        }

        // If this is new network
        if (netId == -1) {
            netId = wifiManager.addNetwork(wifiConfiguration);
        }

        // If failed to add new network
        if (netId == -1) {
            edtPassword.setText("");
            edtPassword.setError("Wrong password");
            edtPassword.requestFocus();

            Toast.makeText(this, "Wrong password! Connect failed to " + wifi.getWifiName(), Toast.LENGTH_SHORT).show();
        } else {
            connectToWifi(netId);

            if (isExist) {
                DataSource.updateWifiData(this, wifi);
            } else {
                DataSource.createWifiData(this, wifi);
            }

            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }

    private void connectToWifi(int netId) {
        wifiManager.enableNetwork(netId, true);
        wifiManager.disconnect();
        wifiManager.reconnect();

        Log.d("Detail", "Connect to wifi " + netId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wifi_detail_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_forget) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setNegativeButton(R.string.btn_reject, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton(R.string.btn_confirm, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataSource.forgetWifi(getApplicationContext(), wifiData);
                }
            });
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // TODO remove try catch
        try {
            unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }
    }
}
