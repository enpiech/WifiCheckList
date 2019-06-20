package tdc.edu.vn.wifichecklist.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import tdc.edu.vn.wifichecklist.dal.DataSource;
import tdc.edu.vn.wifichecklist.dal.SQLiteDBManager;
import tdc.edu.vn.wifichecklist.model.Wifi;

public class UpdateDataFromFireBaseWorker extends Worker {
    public static final String TAG = "update_data_from_fire_base";
    private static final String USER_NODE = "user";
    private static final String WIFI_NODE = "wifi";

    private DatabaseReference mDatabase;

    public UpdateDataFromFireBaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String userUuid = getInputData().getString("userUuid");

        if (userUuid != null) { updateUserKnownWifiList(userUuid); }

        return Result.success();
    }

    private void updateUserKnownWifiList(final String userUuid) {
        final SQLiteDBManager db = new SQLiteDBManager(getApplicationContext());
        final ArrayList<Wifi> wifiList = db.selectAllWifiData();

        if (wifiList.size() == 0) { return; }

        Log.d("Worker", "Wifi count: " + wifiList.size());
        Log.d("Worker", "User uuid: " + userUuid);

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
    }

    private ArrayList<String> getUserKnownWifiList(final String userUuid) {
        final ArrayList<String> wifiList = new ArrayList<>();

        mDatabase.child(USER_NODE).child(userUuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot data : dataSnapshot.getChildren()) {
                    final String ssid = data.getValue(String.class);
                    wifiList.add(ssid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return wifiList;
    }

    private void getWifiData(final String wifiSsid) {
        mDatabase.child(WIFI_NODE).child(wifiSsid).addListenerForSingleValueEvent(
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
