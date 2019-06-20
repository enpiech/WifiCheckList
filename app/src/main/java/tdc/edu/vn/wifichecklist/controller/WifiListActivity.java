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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import tdc.edu.vn.wifichecklist.R;
import tdc.edu.vn.wifichecklist.adapter.ItemClickListener;
import tdc.edu.vn.wifichecklist.adapter.WifiAdapter;
import tdc.edu.vn.wifichecklist.dal.DataSource;
import tdc.edu.vn.wifichecklist.model.Wifi;

public class WifiListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static String EXTRA_WIFI_DATA = "EXTRA_WIFI_DATA_ID";

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION = 0;
    private static final int WIFI_SCAN_INTERVAL_SECOND = 15;
    private static final int FIRE_BASE_UPDATE_INTERVAL = 15;

    private ListView lstWifi;
    private WifiAdapter adapter;
    private Button btnDetail;
    private DrawerLayout drawer;

    private WifiManager wifiManager;
    private LocationManager locationManager;

    private String userUid;

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

    private void loadView() {
        // Get list wifi view
        lstWifi = findViewById(R.id.lstWifi);

        // Get detail button view
        btnDetail = findViewById(R.id.btnDetail);

        // Get custom toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get drawer
        drawer = findViewById(R.id.drawer_layout);
        drawer.setStatusBarBackground(R.color.colorPrimary);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadData() {
        DataSource.requestPeriodicUpdateDataFromFireBase(this, FIRE_BASE_UPDATE_INTERVAL);

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

        // Get user uid from FireBase auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null)
        {
            userUid = auth.getCurrentUser().getUid();
        }
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
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_current:
                break;
            case R.id.nav_save:
                DataSource.updateToFirebase(getApplicationContext(), userUid);
                break;
            case R.id.nav_load:
                DataSource.loadFromFirebase(getApplicationContext(), userUid);
                break;
            case R.id.nav_logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                startActivity(new Intent(getApplication(), SignUpActivity.class));
                break;
        }
        return true;
    }
}
