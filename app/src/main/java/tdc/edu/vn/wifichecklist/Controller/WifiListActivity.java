package tdc.edu.vn.wifichecklist.Controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import tdc.edu.vn.wifichecklist.Adapter.ItemClickListener;
import tdc.edu.vn.wifichecklist.Adapter.WifiAdapter;
import tdc.edu.vn.wifichecklist.Helper.GetWifiDataFromJson;
import tdc.edu.vn.wifichecklist.Model.Wifi;
import tdc.edu.vn.wifichecklist.R;

public class WifiListActivity extends AppCompatActivity {
    private ListView lstWifi;
    private ArrayList<Wifi> wifiList;
    private static WifiAdapter adapter;
    private Button btnDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list_layout);

        loadView();

        loadData();
    }

    private void loadView() {
        // Set action barr
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get list wifi view
        lstWifi = findViewById(R.id.lstWifi);

        // Get detail button view
        btnDetail = findViewById(R.id.btnDetail);
    }

    private void loadData(){
        // Get wifi list
        wifiList = GetWifiDataFromJson.getWifiDataFromJSON(this);
        adapter = new WifiAdapter(wifiList, getApplicationContext());
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

        // Button is disable by default
        btnDetail.setEnabled(false);
        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WifiListActivity.this, WifiDetailActivity.class);
                intent.putExtra("wifiData", adapter.getItem(adapter.getSelectedPosition()));
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }
}
