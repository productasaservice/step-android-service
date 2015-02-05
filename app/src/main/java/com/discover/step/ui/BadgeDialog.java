package com.discover.step.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.discover.step.R;
import com.discover.step.model.Achievement;

/**
 * Created by Geri on 2015.02.05..
 */
public class BadgeDialog {

    private Dialog mDialog;
    private Activity mActivity;
    private Achievement achievement;

    private TextView mTitleTv, mDescriptionTv, mCloseTv;
    private ImageView mIconIv;

    public BadgeDialog(Activity activity, Achievement achievement) {
        mActivity = activity;
        mDialog = new Dialog(mActivity);
        this.achievement = achievement;

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(LayoutInflater.from(mActivity).inflate(R.layout.view_new_badge_dialog,null));
        mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogNoAnimation;

        mIconIv = (ImageView) mDialog.findViewById(R.id.dialog_iconIv);
        mTitleTv = (TextView) mDialog.findViewById(R.id.dialog_titleTv);
        mDescriptionTv = (TextView) mDialog.findViewById(R.id.dialog_descriptionTv);
        mCloseTv = (TextView) mDialog.findViewById(R.id.dialog_closeTv);

        mTitleTv.setText(achievement.name);
        mDescriptionTv.setText(achievement.message);

        mCloseTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        setPicture(achievement);

        mDialog.show();
    }

    private void setPicture(Achievement a) {
        if (a.name.equalsIgnoreCase("newbie")) {
            mIconIv.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.badge_newbie));
        } else if (a.name.equalsIgnoreCase("adventurer")) {
            mIconIv.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.badge_adventurer));
        } else if (a.name.equalsIgnoreCase("walking dead")) {
            mIconIv.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.badge_walking_dead));
        } else if (a.name.equalsIgnoreCase("superstar")) {
            mIconIv.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.badge_superstar));
        }
    }
}
