package tdc.edu.vn.wifichecklist.Controller;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tdc.edu.vn.wifichecklist.Adapter.ItemClickListener;
import tdc.edu.vn.wifichecklist.Adapter.WifiAdapter;
import tdc.edu.vn.wifichecklist.Model.Wifi;
import tdc.edu.vn.wifichecklist.R;

public class WifiListActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION = 0;
    private ListView lstWifi;
    private ArrayList<Wifi> wifiList;
    private WifiAdapter adapter;
    private Button btnDetail;

    private WifiManager wifiManager;
    private LocationManager locationManager;

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                List<ScanResult> results = wifiManager.getScanResults();
                wifiList.clear();
                for (ScanResult result : results) {
                    wifiList.add(new Wifi(result.SSID, result.BSSID, result.capabilities, result.frequency, result.level));
                }
                Collections.sort(wifiList, new Comparator<Wifi>() {
                    @Override
                    public int compare(Wifi wifi, Wifi t1) {
                        return t1.getRssi() - wifi.getRssi();
                    }
                });
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list_layout);

        loadServices();

        scanWifiEvery(30);

        loadView();
        loadData();
    }

    private void scanWifiEvery(final int seconds) {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                turnOnRequireServices();
                handler.postDelayed(this, seconds * 1000);
            }
        };
        //Start
        handler.postDelayed(runnable, 0);
    }

    private void loadServices() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    private void turnOnRequireServices() {
        turnOnGPS();
        turnOnWifi();
    }

    private void turnOnWifi() {
        // Turn on wifi if it is not enabled
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        // Register receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Start scan
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        }else{
            wifiManager.startScan();
        }
    }

    private void turnOnGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent turnOnGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(turnOnGPSIntent);
        }
    }

    private void loadView() {
        // Set action barr
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get list wifi view
        lstWifi = findViewById(R.id.lstWifi);

        // Get detail button view
        btnDetail = findViewById(R.id.btnDetail);
    }

    private void loadData() {
        // Get wifi list
//        wifiList = GetWifiDataFromJson.getWifiDataFromJSON(this);
        wifiList = new ArrayList<>();
        adapter = new WifiAdapter(wifiList, getApplicationContext());
        // If current selected item is changed, toggle button
        adapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int selectedPosition) {
                if (selectedPosition == -1) {
                    btnDetail.setEnabled(false);
                } else {
                    btnDetail.setEnabled(true);
                }
            }
        });
        lstWifi.setAdapter(adapter);

        // Button is disable by default
        btnDetail.setEnabled(false);
        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WifiListActivity.this, WifiDetailActivity.class);
                intent.putExtra("wifiData", adapter.getItem(adapter.getSelectedPosition()));
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION) {
            wifiManager.startScan();
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(wifiReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }
}
