package com.discover.step.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.discover.step.R;

import java.util.List;

/**
 * Created by Geri on 2015.02.18..
 */
public class BaseSpinnerAdapter extends ArrayAdapter<String> {

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<String> items;

    public BaseSpinnerAdapter(Context context, List<String> objects) {
        super(context, 0, objects);
        items = objects;
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    private static class ViewHolder {
        TextView titleTv;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomDropDownView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.spinner_base_item, null);

            holder = new ViewHolder();
            holder.titleTv = (TextView) convertView.findViewById(R.id.spinner_itme_titleTv);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String title = this.getItem(position);

        holder.titleTv.setText(title);

        return convertView;
    }

    public View getCustomDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.spinner_base_list_item, null);

            holder = new ViewHolder();
            holder.titleTv = (TextView) convertView.findViewById(R.id.spinner_itme_titleTv);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String title = this.getItem(position);

        holder.titleTv.setText(title);

        return convertView;
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    public int getPosition(String item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(item)) {
                return i;
            }
        }
        return -1;
    }
}

