package tdc.edu.vn.wifichecklist.dal;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import tdc.edu.vn.wifichecklist.model.Wifi;

public class DataSource {
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

    public static ArrayList<Wifi> getKnownWifi(Context context) {
        SQLiteDBManager db = new SQLiteDBManager(context);

        return db.selectAllWifiData();
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
}
