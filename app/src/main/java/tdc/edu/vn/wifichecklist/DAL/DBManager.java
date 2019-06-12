package tdc.edu.vn.wifichecklist.DAL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

import tdc.edu.vn.wifichecklist.Model.Wifi;

public class DBManager extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "wifi_data";
    private static final String SSID = "ssid";
    private static final String BSSID = "bssid";
    private static final String SECURITY = "security";
    private static final String PASSWORD = "password";

    private Context mContext;

    public DBManager(Context context) {
        super(context, TABLE_NAME, null, 1);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                SSID + " TEXT primary key, " +
                BSSID + " TEXT, " +
                SECURITY + " TEXT, " +
                PASSWORD + " TEXT)";

        db.execSQL(sqlQuery);
        Toast.makeText(mContext, "Create database success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Toast.makeText(mContext, "Drop success", Toast.LENGTH_SHORT).show();
    }

    public long createWifiData(Wifi wifi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SSID, wifi.getSsid());
        contentValues.put(BSSID, wifi.getBssid());
        contentValues.put(SECURITY, wifi.getSecurity());
        contentValues.put(PASSWORD, wifi.getPassword());

        final long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result;
    }

    public ArrayList<Wifi> selectAllWifiData() {
        ArrayList<Wifi> wifiArrayList = new ArrayList<>();

        final SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, SSID);
        if (cursor.moveToFirst()) {
           do {
               final String ssid = cursor.getString(cursor.getColumnIndex(SSID));
               final String bssid = cursor.getString(cursor.getColumnIndex(BSSID));
               final String security = cursor.getString(cursor.getColumnIndex(SECURITY));
               final String password = cursor.getString(cursor.getColumnIndex(PASSWORD));
               final Wifi cursorWifiData = new Wifi(ssid, bssid, security, -1, -1);
               cursorWifiData.setPassword(password);
               wifiArrayList.add(cursorWifiData);
           } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return wifiArrayList;
    }

    public Wifi selectWifiData(Wifi wifi) {
        final SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, SSID + " = ?", new String[]{ wifi.getSsid() }, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        } else {
            return null;
        }

        final String ssid = cursor.getString(0);
        final String bssid = cursor.getString(1);
        final String security = cursor.getString(2);
        final String password = cursor.getString(3);
        final Wifi cursorWifiData = new Wifi(ssid, bssid, security, -1, -1);
        cursorWifiData.setPassword(password);
        cursor.close();
        db.close();

        return cursorWifiData;
    }

    public int updateWifiData(Wifi wifi) {
        int result;

        final SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BSSID, wifi.getBssid());
        contentValues.put(SECURITY, wifi.getSecurity());
        contentValues.put(PASSWORD, wifi.getPassword());

        result = db.update(TABLE_NAME, contentValues, SSID + " = ? ", new String[]{ wifi.getSsid() });
        db.close();
        return result;
    }

    public void deleteWifiData(Wifi wifi) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, BSSID + " = ? ", new String[]{ wifi.getBssid() });
        db.close();
    }
}
