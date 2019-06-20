package tdc.edu.vn.wifichecklist.Helper;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import tdc.edu.vn.wifichecklist.model.Wifi;
import tdc.edu.vn.wifichecklist.R;

public class GetWifiDataFromJson {
    public static ArrayList<Wifi> getWifiDataFromJSON(Context context) {
        ArrayList<Wifi> wifiList = new ArrayList<>();

        try {
            String jsonText = readText(context, R.raw.data_wifi);
            JSONArray wifiDataArray = new JSONArray(jsonText);
            for (int i = 0; i < wifiDataArray.length(); ++i) {
                JSONObject wifiData = wifiDataArray.getJSONObject(i);
                String ssid = wifiData.getString(Wifi.SSID_KEY);
                String bssid = wifiData.getString(Wifi.BSSID_KEY);
                String security = wifiData.getString(Wifi.SECURITY_KEY);
                int frequency = wifiData.getInt(Wifi.FREQUENCY_KEY);
                int rssi = wifiData.getInt(Wifi.RSSI_KEY);
                wifiList.add(new Wifi(ssid, bssid, security, frequency, rssi));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  wifiList;
    }

    private static String readText(Context context, int redId) throws IOException {
        InputStream is = context.getResources().openRawResource(redId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while ((s = br.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
    }
}
