package com.discover.step.ui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.discover.step.R;
import com.discover.step.model.Achievement;
import com.discover.step.model.Challenge;

/**
 * Created by Geri on 2015.03.07..
 */
public class ChallengeDialog {

    private Dialog mDialog;
    private Activity mActivity;
    private Challenge challenge;

    private TextView mTitleTv, mDescriptionTv, mAcceptTv, mCloseTv;

    public ChallengeDialog(Activity activity, Challenge challenge) {
        mActivity = activity;
        mDialog = new Dialog(mActivity);
        this.challenge = challenge;

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(LayoutInflater.from(mActivity).inflate(R.layout.view_new_challenge_request_dialog,null));
        mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogNoAnimation;

        mTitleTv = (TextView) mDialog.findViewById(R.id.dialog_titleTv);
        mDescriptionTv = (TextView) mDialog.findViewById(R.id.dialog_descriptionTv);
        mCloseTv = (TextView) mDialog.findViewById(R.id.dialog_rejectTv);
        mAcceptTv = (TextView) mDialog.findViewById(R.id.dialog_acceptTv);

        mTitleTv.setText(challenge.title);
        String message = "You have received a new challenge request with message:<br>" + challenge.message + "<br><br>" + "Bet: " + challenge.bet;

        mDescriptionTv.setText(Html.fromHtml(message));

        mCloseTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.show();
    }

    public void setOnAcceptButtonListener(final View.OnClickListener listener) {
        mAcceptTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onClick(v);
            }
        });
    }
}
