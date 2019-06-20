package tdc.edu.vn.wifichecklist.controller;

import android.Manifest.permission;
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
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.Data.Builder;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.List;
import java.util.concurrent.TimeUnit;
import tdc.edu.vn.wifichecklist.R;
import tdc.edu.vn.wifichecklist.adapter.ItemClickListener;
import tdc.edu.vn.wifichecklist.adapter.WifiAdapter;
import tdc.edu.vn.wifichecklist.dal.DataSource;
import tdc.edu.vn.wifichecklist.model.Wifi;
import tdc.edu.vn.wifichecklist.worker.UpdateDataFromFireBaseWorker;

public class WifiListActivity extends AppCompatActivity {
    public static String EXTRA_WIFI_DATA = "EXTRA_WIFI_DATA_ID";

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION = 0;
    private static final int WIFI_SCAN_INTERVAL_SECOND = 15;
    private static final int FIRE_BASE_UPDATE_INTERVAL = 15;

    private ListView lstWifi;
    private WifiAdapter adapter;
    private Button btnDetail;

    private WifiManager wifiManager;
    private LocationManager locationManager;

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                DataSource.clear();

                List<ScanResult> results = wifiManager.getScanResults();
                for (ScanResult result : results) {
                    DataSource.addWifi(new Wifi(result.SSID, result.BSSID, result.capabilities, result.frequency, result.level));
                }
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list_layout);

        loadServices();
        turnOnRequireServices();
        scanWifiEvery(WIFI_SCAN_INTERVAL_SECOND);

        loadView();
        loadData();
        setEvent();
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
        if (isGrantedPermission(permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION)) {
            turnOnGPS();
            turnOnWifi();
        }
    }

    private boolean isGrantedPermission(String permission, int requestCode) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            // If permission is not granted, request permission
            requestPermissions(new String[]{ permission }, requestCode);
            return false;
        }
        return true;
    }

    private void turnOnWifi() {
        // Turn on wifi if it is not enabled
        if (!wifiManager.isWifiEnabled()) { wifiManager.setWifiEnabled(true); }

        // Register receiver
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.startScan();
    }

    private void turnOnGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent turnOnGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(turnOnGPSIntent);
        }
    }

    private void loadView() {
        // Set action bar
        if (getSupportActionBar() != null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); }

        // Get list wifi view
        lstWifi = findViewById(R.id.lstWifi);

        // Get detail button view
        btnDetail = findViewById(R.id.btnDetail);
    }

    private void loadData() {
        requestPeriodicUpdateDataFromFireBase(FIRE_BASE_UPDATE_INTERVAL);

        adapter = new WifiAdapter(DataSource.getCurrentWifiList(), getApplicationContext());

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
    }

    private void requestPeriodicUpdateDataFromFireBase(int interval) {
        /*TODO put user uuid when login*/
        Data userUuid = new Builder().putString("userUuid", "WGjuRx4KwPdaPea9vD0PGWnCcTa2").build();

        // Request update data from FireBase every 12 hours
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest.Builder builder;
        builder = new PeriodicWorkRequest.Builder(UpdateDataFromFireBaseWorker.class, interval, TimeUnit.MINUTES);
        builder
                .addTag(UpdateDataFromFireBaseWorker.TAG)
                .setInputData(userUuid)
                .setConstraints(constraints);
        PeriodicWorkRequest updateDataWork = builder.build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(UpdateDataFromFireBaseWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, updateDataWork);
    }

    private void setEvent() {
        // Button is disable by default
        btnDetail.setEnabled(false);
        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WifiListActivity.this, WifiDetailActivity.class);
                intent.putExtra(EXTRA_WIFI_DATA, adapter.getSelectedPosition());
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION) { wifiManager.startScan(); }
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
