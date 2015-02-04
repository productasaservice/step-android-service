package com.discover.step.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.discover.step.R;

/**
 * Created by Geri on 2015.02.04..
 */
public class ShowFunctionDialog {

    private Dialog mDialog;
    private Activity mActivity;

    public ShowFunctionDialog(Activity activity) {
        mActivity = activity;
        mDialog = new Dialog(mActivity);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(LayoutInflater.from(mActivity).inflate(R.layout.view_show_func_dialog,null));
        mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogNoAnimation;
        (mDialog.findViewById(R.id.map_informationRl)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.show();
    }
}
