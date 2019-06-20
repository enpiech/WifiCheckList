package tdc.edu.vn.wifichecklist.dal;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.Data.Builder;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import tdc.edu.vn.wifichecklist.dal.manager.SQLiteDBManager;
import tdc.edu.vn.wifichecklist.model.Wifi;
import tdc.edu.vn.wifichecklist.worker.UpdateDataFromFireBaseWorker;

public class DataSource {
    public static final String USER_NODE = "user";
    public static final String WIFI_NODE = "wifi";

    private static ArrayList<Wifi> sWifiList = new ArrayList<>();

    public static ArrayList<Wifi> getCurrentWifiList() {
        return sWifiList;
    }

    public static void addWifi(Wifi wifi) {
        sWifiList.add(wifi);
    }

    public static void clear() {
        sWifiList.clear();
    }

    public static void forgetWifi(Context context, Wifi wifi) {
        if (sWifiList.remove(wifi)) {
            SQLiteDBManager database = new SQLiteDBManager(context);
            if (database.deleteWifiData(wifi) != -1) {
                Log.d("Detail", "Forget wifi: " + wifi.toString());
            }
        }
    }

    public static void createWifiData(Context context, Wifi wifi) {
        wifi.setIsUpdated(true);

        SQLiteDBManager dbManager = new SQLiteDBManager(context);
        if (dbManager.createWifiData(wifi) == -1) {
            Toast.makeText(context, "Created failed", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Detail", "Create wifi: " + wifi.toString());
        }
    }

    public static void updateWifiData(Context context, Wifi wifi) {
        wifi.setIsUpdated(true);

        SQLiteDBManager dbManager = new SQLiteDBManager(context);
        if (dbManager.updateWifiData(wifi) == -1) {
            Toast.makeText(context, "Update data failed", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Detail", "Update wifi: " + wifi.toString());
        }
    }

    public static void requestPeriodicUpdateDataFromFireBase(Context context, int interval) {
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

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UpdateDataFromFireBaseWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, updateDataWork);
    }

    public static void updateToFirebase(Context context, String userUuid) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final SQLiteDBManager db = new SQLiteDBManager(context);
        final ArrayList<Wifi> wifiList = db.selectAllWifiData();

        Log.d("DataSource", "Wifi count: " + wifiList.size());
        Log.d("DataSource", "User uuid: " + userUuid);

        if (wifiList.size() == 0) { return; }

        for (int i = 0; i < wifiList.size(); ++i) {
            Wifi wifi = wifiList.get(i);
            // Only update if current network had changed
            if (wifi.isUpdated()) {
                mDatabase
                        .child(USER_NODE)
                        .child(userUuid)
                        .child(i + "")
                        .setValue(wifi.getSsid());

                mDatabase
                        .child(WIFI_NODE)
                        .child(wifi.getSsid())
                        .child(Wifi.BSSID_KEY)
                        .setValue(wifi.getBssid());
                mDatabase
                        .child(WIFI_NODE)
                        .child(wifi.getSsid())
                        .child(Wifi.FREQUENCY_KEY)
                        .setValue(wifi.getFrequency());

                mDatabase
                        .child(WIFI_NODE)
                        .child(wifi.getSsid())
                        .child(Wifi.SECURITY_KEY)
                        .setValue(wifi.getSecurity());

                mDatabase
                        .child(WIFI_NODE)
                        .child(wifi.getSsid())
                        .child(Wifi.PASSWORD_KEY)
                        .setValue(wifi.getPassword());
            }
        }
        Log.d("DataSource", "Update data to FireBase");
    }

    // Clear data then load data form firebase
    public static void loadFromFirebase(Context context, String userUuid) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final SQLiteDBManager db = new SQLiteDBManager(context);
        sWifiList.clear();
        final ArrayList<String> wifiIdList = getUserKnownWifiList(userUuid);
        for (String id : wifiIdList) {
            getWifiData(id);
        }

        Log.d("DataSource", "Load data from FireBase" + sWifiList.size());
    }

    private static ArrayList<String> getUserKnownWifiList(final String userUuid) {
        final ArrayList<String> wifiIdList = new ArrayList<>();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(DataSource.USER_NODE).child(userUuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot data : dataSnapshot.getChildren()) {
                    final String ssid = data.getValue(String.class);
                    wifiIdList.add(ssid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return wifiIdList;
    }

    private static void getWifiData(final String wifiSsid) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(DataSource.WIFI_NODE).child(wifiSsid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Wifi wifi = dataSnapshot.getValue(Wifi.class);
                        if (wifi != null) wifi.setSsid(wifiSsid);

                        DataSource.addWifi(wifi);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
