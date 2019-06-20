package tdc.edu.vn.wifichecklist.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import tdc.edu.vn.wifichecklist.model.Wifi;

public class SQLiteDBManager extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "wifi_data";
    private static final int TABLE_VERSION = 1;

    public SQLiteDBManager(Context context) {
        super(context, TABLE_NAME, null, TABLE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlQuery = "CREATE TABLE " + TABLE_NAME + "(" +
                Wifi.SSID_KEY + " TEXT primary key," +
                Wifi.BSSID_KEY + " TEXT NOT NULL," +
                Wifi.SECURITY_KEY + " TEXT NOT NULL," +
                Wifi.FREQUENCY_KEY + " INTEGER NOT NULL," +
                Wifi.PASSWORD_KEY + " TEXT NOT NULL," +
                Wifi.STATE_KEY + " INTEGER NOT NULL)";

        db.execSQL(sqlQuery);

        Log.d("SQLite", "Create database success");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

        Log.d("SQLite", "Drop table success");
    }

    // Return effected row in SQLite
    // If return -1, there is an error when create new record
    public long createWifiData(Wifi wifi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Wifi.SSID_KEY, wifi.getSsid());
        contentValues.put(Wifi.BSSID_KEY, wifi.getBssid());
        contentValues.put(Wifi.SECURITY_KEY, wifi.getSecurity());
        contentValues.put(Wifi.FREQUENCY_KEY, wifi.getFrequency());
        contentValues.put(Wifi.PASSWORD_KEY, wifi.getPassword());
        contentValues.put(Wifi.STATE_KEY, wifi.isUpdated());

        final long result = db.insert(TABLE_NAME, null, contentValues);

        db.close();

        return result;
    }

    public ArrayList<Wifi> selectAllWifiData() {
        ArrayList<Wifi> wifiArrayList = new ArrayList<>();
        final SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, Wifi.SSID_KEY);

        Log.d("Sqlite", cursor.getColumnName(0) + "");

        if (cursor.moveToFirst()) {
           do {
               final String ssid = cursor.getString(cursor.getColumnIndex(Wifi.SSID_KEY));
               final String bssid = cursor.getString(cursor.getColumnIndex(Wifi.BSSID_KEY));
               final String security = cursor.getString(cursor.getColumnIndex(Wifi.SECURITY_KEY));
               final int frequency = cursor.getInt(cursor.getColumnIndex(Wifi.FREQUENCY_KEY));
               final String password = cursor.getString(cursor.getColumnIndex(Wifi.PASSWORD_KEY));
               final boolean state = (cursor.getInt(cursor.getColumnIndex(Wifi.STATE_KEY)) == 1);
               final Wifi cursorWifiData = new Wifi(ssid, bssid, security, frequency, -1, password, state);

               wifiArrayList.add(cursorWifiData);
           } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return wifiArrayList;
    }

    // Return selected wifi, null if not found
    public Wifi selectWifiData(Wifi wifi) {
        final SQLiteDatabase db = this.getReadableDatabase();
        Wifi cursorWifiData;

        Cursor cursor = db.query(TABLE_NAME, null, Wifi.BSSID_KEY + " = ?", new String[]{ wifi.getBssid() }, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            final String ssid = cursor.getString(cursor.getColumnIndex(Wifi.SSID_KEY));
            final String bssid = cursor.getString(cursor.getColumnIndex(Wifi.BSSID_KEY));
            final String security = cursor.getString(cursor.getColumnIndex(Wifi.SECURITY_KEY));
            final int frequency = cursor.getInt(cursor.getColumnIndex(Wifi.FREQUENCY_KEY));
            final String password = cursor.getString(cursor.getColumnIndex(Wifi.PASSWORD_KEY));
            final boolean state = (cursor.getInt(cursor.getColumnIndex(Wifi.STATE_KEY)) == 1);
            cursorWifiData = new Wifi(ssid, bssid, security, frequency, -1, password, state);

            cursor.close();
        } else {
            Log.d("SQLite", "No record found");
            cursorWifiData = null;
        }

        db.close();

        return cursorWifiData;
    }

    // Update wifi data, return effected row, -1 if error
    public int updateWifiData(Wifi wifi) {
        final SQLiteDatabase db = this.getWritableDatabase();

        final ContentValues contentValues = new ContentValues();
        contentValues.put(Wifi.BSSID_KEY, wifi.getBssid());
        contentValues.put(Wifi.SECURITY_KEY, wifi.getSecurity());
        contentValues.put(Wifi.FREQUENCY_KEY, wifi.getFrequency());
        contentValues.put(Wifi.PASSWORD_KEY, wifi.getPassword());
        contentValues.put(Wifi.STATE_KEY, wifi.isUpdated());

        final int result = db.update(TABLE_NAME, contentValues, Wifi.BSSID_KEY + " = ? ", new String[]{ wifi.getBssid() });

        db.close();

        return result;
    }

    public int deleteWifiData(Wifi wifi) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final int result = db.delete(TABLE_NAME, Wifi.BSSID_KEY + " = ? ", new String[]{ wifi.getBssid() });

        db.close();

        return result;
    }
}
