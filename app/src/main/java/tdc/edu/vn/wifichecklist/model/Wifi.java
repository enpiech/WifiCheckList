package tdc.edu.vn.wifichecklist.model;

import java.io.Serializable;

public class Wifi implements Serializable {
    public static final String SSID_KEY = "ssid";
    public static final String BSSID_KEY = "bssid";
    public static final String SECURITY_KEY = "security";
    public static final String FREQUENCY_KEY = "frequency";
    public static final String RSSI_KEY = "rssi";
    public static final String PASSWORD_KEY = "password";
    public static final String STATE_KEY = "state";

    private static final String WIFI_FREQUENCY_5GHZ = " [5 GHz] ";
    private static final String WIFI_FREQUENCY_2_4GHZ = " [2.4 GHz] ";

    private String mSsid;
    private String mBssid;
    private String mSecurity;
    private int mFrequency;
    private int mRssi;
    private String mPassword;
    private boolean mIsUpdated;

    public Wifi() { }

    public Wifi(String ssid, String bssid, String security, int frequency, int rssi) {
        this.mSsid = ssid;
        this.mBssid = bssid;
        this.mSecurity = security;
        this.mFrequency = frequency;
        this.mRssi = rssi;
        this.mIsUpdated = false;
    }

    public Wifi(String ssid, String bssid, String security, int frequency, int rssi,
        String password) {
        this.mSsid = ssid;
        this.mBssid = bssid;
        this.mSecurity = security;
        this.mFrequency = frequency;
        this.mRssi = rssi;
        this.mPassword = password;
        this.mIsUpdated = false;
    }

    public Wifi(String ssid, String bssid, String security, int frequency, int rssi,
        String password, boolean isUpdated) {
        this.mSsid = ssid;
        this.mBssid = bssid;
        this.mSecurity = security;
        this.mFrequency = frequency;
        this.mRssi = rssi;
        this.mPassword = password;
        this.mIsUpdated = isUpdated;
    }

    /* GETTER */

    public String getSsid() {
        return mSsid;
    }

    public String getBssid() {
        return mBssid;
    }

    public String getSecurity() {
        return mSecurity;
    }

    public int getFrequency() {
        return mFrequency;
    }

    public int getRssi() {
        return mRssi;
    }

    public String getWifiName() {
        String displayName = mSsid;
        if (mFrequency > 5179 && mFrequency < 5186) {
            displayName += WIFI_FREQUENCY_5GHZ;
        } else {
            displayName += WIFI_FREQUENCY_2_4GHZ;
        }
        return displayName;
    }

    public String getPassword() {
        return mPassword;
    }

    public boolean isUpdated() {
        return mIsUpdated;
    }

    /* SETTER */

    public void setSsid(String ssid) {
        this.mSsid = ssid;
    }

    public void setBssid(String bssid) {
        this.mBssid = bssid;
    }

    public void setSecurity(String mSecurity) {
        this.mSecurity = mSecurity;
    }

    public void setFrequency(int frequency) {
        this.mFrequency = frequency;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public void setIsUpdated(boolean isUpdated) {
        this.mIsUpdated = isUpdated;
    }

    @Override
    public String toString() {
        return "Wifi{" +
                "mSsid='" + mSsid + '\'' +
                ", mBssid='" + mBssid + '\'' +
                ", mSecurity='" + mSecurity + '\'' +
                ", mFrequency=" + mFrequency +
                ", mRssi=" + mRssi +
                ", mPassword='" + mPassword + '\'' +
                ", mIsUpdated=" + mIsUpdated +
                '}';
    }
}
