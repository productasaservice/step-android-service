package com.discover.step.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.StepApplication;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Geri on 2015.01.27..
 */
public class PointsAdapter extends BaseAdapter {

    List<Day> dateList;
    private LayoutInflater mLayoutInflater;

    public PointsAdapter(List<Day> dateList) {
        mLayoutInflater = LayoutInflater.from(StepApplication.getContext());
        this.dateList = dateList;
    }

    @Override
    public int getCount() {
        return dateList.size();
    }

    @Override
    public Day getItem(int position) {
        return dateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item_date_step, null);
            holder = new ViewHolder();
            holder.dateTv = (TextView) view.findViewById(R.id.date_step_dateTv);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Day date = getItem(position);
        holder.dateTv.setText(date.date);

        return view;
    }


    private class ViewHolder {
        public TextView dateTv;
    }
}
