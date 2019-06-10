package tdc.edu.vn.wifichecklist.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import tdc.edu.vn.wifichecklist.Model.Wifi;
import tdc.edu.vn.wifichecklist.R;

public class WifiAdapter extends ArrayAdapter<Wifi> {
    private ArrayList<Wifi> dataSet;
    private Context mContext;
    private int selectedPosition = -1;
    private ItemClickListener itemClickListener;

    private static class ViewHolder {
        RadioButton radWifiItem;
        TextView txtWifiItemName;
        ImageView imgWifiItem;
    }

    public WifiAdapter(ArrayList<Wifi> data, Context context) {
        super(context, R.layout.wifi_list_item, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Wifi data = getItem(position);

        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.wifi_list_item, parent, false);
            viewHolder.radWifiItem = convertView.findViewById(R.id.radWifiItem);
            viewHolder.txtWifiItemName = convertView.findViewById(R.id.txtWifiItemName);
            viewHolder.imgWifiItem = convertView.findViewById(R.id.imgWifiItem);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.radWifiItem.setChecked(position == selectedPosition);
        viewHolder.txtWifiItemName.setText(data.getWifiName());

        viewHolder.radWifiItem.setTag(position);
        viewHolder.txtWifiItemName.setTag(position);

        final int imageId;
        if (data.getRssi() > -60) {
            imageId = R.mipmap.ic_wifi_lv4;
        } else if (data.getRssi() < -61 && data.getRssi() > -70) {
            imageId = R.mipmap.ic_wifi_lv3;
        } else if (data.getRssi() < -71 && data.getRssi() > -80) {
            imageId = R.mipmap.ic_wifi_lv2;
        } else {
            imageId = R.mipmap.ic_wifi_lv1;
        }

        viewHolder.imgWifiItem.setImageResource(imageId);

        viewHolder.radWifiItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemChanged(view);
            }
        });

        viewHolder.txtWifiItemName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemChanged(view);
            }
        });

        return result;
    }

    private void itemChanged(View v) {
        if (selectedPosition == (int) v.getTag()) {
            selectedPosition = -1;
        } else {
            selectedPosition = (int) v.getTag();
        }

        itemClickListener.onItemClick(selectedPosition);

        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
