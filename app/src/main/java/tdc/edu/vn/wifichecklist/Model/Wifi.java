package tdc.edu.vn.wifichecklist.Model;

import java.io.Serializable;

public class Wifi implements Serializable {
    public static final String SSID_KEY = "ssid";
    public static final String BSSID_KEY = "bssid";
    public static final String SECURITY_KEY = "security";
    public static final String FREQUENCY_KEY = "frequency";
    public static final String RSSI_KEY = "rssi";

    private static final String WIFI_FREQUENCY_5GHZ = " [5 GHz] ";
    private static final String WIFI_FREQUENCY_2_4GHZ = " [2.4 GHz] ";

    private String ssid;
    private String bssid;
    private String security;
    private int frequency;
    private int rssi;
    private String password;

    public Wifi(String ssid, String bssid, String security, int frequency, int rssi) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.security = security;
        this.frequency = frequency;
        this.rssi = rssi;
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSecurity() {
        return security;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getRssi() {
        return rssi;
    }

    public String getWifiName() {
        String displayName = ssid;
        if (frequency > 5179 && frequency < 5186) {
            displayName += WIFI_FREQUENCY_5GHZ;
        } else {
            displayName += WIFI_FREQUENCY_2_4GHZ;
        }
        return displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
