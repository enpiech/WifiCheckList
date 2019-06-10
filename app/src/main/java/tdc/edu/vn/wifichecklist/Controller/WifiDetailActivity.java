package tdc.edu.vn.wifichecklist.Controller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import tdc.edu.vn.wifichecklist.Model.Wifi;
import tdc.edu.vn.wifichecklist.R;

public class WifiDetailActivity extends AppCompatActivity {
    private TextView txtSsid;
    private TextView txtBssid;
    private TextView txtSecurity;
    private TextView txtFrequency;
    private TextView txtRssi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_detail_layout);

        loadView();

        loadData();
    }

    void loadView() {
        // Set action barr
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtSsid = findViewById(R.id.txtSsid);
        txtBssid = findViewById(R.id.txtBssid);
        txtSecurity = findViewById(R.id.txtSecurity);
        txtFrequency = findViewById(R.id.txtFrequency);
        txtRssi = findViewById(R.id.txtRssi);
    }

    void loadData() {
        Wifi wifiData = (Wifi) getIntent().getSerializableExtra("wifiData");

        if (wifiData != null) {
            txtSsid.setText(wifiData.getSsid());
            txtBssid.setText(wifiData.getBssid());
            txtSecurity.setText(wifiData.getSecurity());
            txtFrequency.setText(wifiData.getFrequency() + "");
            txtRssi.setText(wifiData.getRssi() + "");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
