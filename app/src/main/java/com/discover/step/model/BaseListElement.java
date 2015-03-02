package com.discover.step.model;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.BaseAdapter;

/**
 * Created by Geri on 2015.03.01..
 */
public abstract class BaseListElement {

    private Drawable icon;
    private String text1;
    private String text2;

    private int requestCode;

    private BaseAdapter adapter;

    public BaseListElement(Drawable icon, String text1, String text2, int requestCode) {
        super();
        this.icon = icon;
        this.text1 = text1;
        this.text2 = text2;
        this.requestCode = requestCode;
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    public void setText1(String text1) {
        this.text1 = text1;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setText2(String text1) {
        this.text2 = text2;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    protected abstract View.OnClickListener getOnClickListener();
}
