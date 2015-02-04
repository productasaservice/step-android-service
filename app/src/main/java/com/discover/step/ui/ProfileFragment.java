package com.discover.step.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.Session;
import com.discover.step.adapters.PointsAdapter;
import com.discover.step.async.SafeAsyncTask;
import com.discover.step.bc.ServerConnector;
import com.discover.step.bl.AchievementManager;
import com.discover.step.bl.PrefManager;
import com.discover.step.bl.StepManager;
import com.discover.step.bl.UserManager;
import com.discover.step.helper.BitmapHelper;
import com.discover.step.helper.StepHelper;
import com.discover.step.model.Achievement;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Geri on 2015.01.27..
 */
public class ProfileFragment extends Fragment {

    private ImageView profileIv, mNewiebleIv, mAdvanturerIv, mWalkingDeadIv, mSuperstarIv;
    private TextView mNameTv, mStepCountTv, mStepennyCountTv;
    private ListView mLastStepPointsLv;
    private PointsAdapter mAdapter;
    private View view;

    private ProgressDialog mProgressDialog;

    private User currentUser;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivity.isOptionsMenuEnabled = false;
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile,container,false);

        openAnimation();

        profileIv = (ImageView) view.findViewById(R.id.profile_pictureIv);
        mNewiebleIv = (ImageView) view.findViewById(R.id.profile_badge_newbieIv);
        mAdvanturerIv = (ImageView) view.findViewById(R.id.profile_badge_adventurerIv);
        mWalkingDeadIv = (ImageView) view.findViewById(R.id.profile_badge_walingdeadIv);
        mSuperstarIv = (ImageView) view.findViewById(R.id.profile_badge_superstarIv);

        mNameTv = (TextView) view.findViewById(R.id.profile_nameTv);
        mStepCountTv = (TextView) view.findViewById(R.id.profile_stepsCountTv);
        mStepennyCountTv = (TextView) view.findViewById(R.id.profile_stepennyCountTv);

        mLastStepPointsLv = (ListView) view.findViewById(R.id.profile_last_stepsLv);

        //Update screen data.
        updateScreenData();

        return view;
    }

    /**
     * Load last step points.
     */
    private class LoadLastStepPointsTask extends SafeAsyncTask<Void,Void,List<Day>> {

        @Override
        protected List<Day> doWorkInBackground(Void... params) throws Exception {
            return StepManager.getInstance().getStepDays();
        }

        @Override
        protected void onSuccess(List<Day> days) {
            super.onSuccess(days);

                mAdapter = new PointsAdapter(days);
                mLastStepPointsLv.setAdapter(mAdapter);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        StepHelper.setListViewHeightBasedOnChildren(mAdapter,mLastStepPointsLv);
                    }
                },300);

                mLastStepPointsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MainActivity.showMarkersOnMap(mAdapter.getItem(position));
                        ((MainActivity)getActivity()).onBackPressed();
                    }
                });

        }

        private boolean isContaining(List<String> list, String value) {
            for (String s : list) {
                if (s.equalsIgnoreCase(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Update screen data.
     */
    public void updateScreenData() {
        openAnimation();
        //redraw options menu.
        MainActivity.isOptionsMenuEnabled = false;
        getActivity().supportInvalidateOptionsMenu();

        //get current authenticated user.
        currentUser = UserManager.getInstance().getAuthenticatedUser();

        //set user name.
        mNameTv.setText(currentUser.first_name + " " + currentUser.last_name);

        //set user profile picture.
        ImageLoader.getInstance().loadImage(currentUser.picture_url,new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (loadedImage != null) {
                    profileIv.setImageBitmap(BitmapHelper.getCenterChorpedRoundedBitmap(loadedImage,89));
                }
            }
        });

        //Initialize user step points.
        mStepCountTv.setText(PrefManager.getInstance().getUserStepCount(currentUser.social_id) + "");

        //Check achievements.
        List<Achievement> achievements = AchievementManager.getInstance().getAchievements();
        for (Achievement a : achievements) {
            if (AchievementManager.getInstance().isStepBasedAchievementCompleted(a)) {
                if (a.name.equalsIgnoreCase("newbie")) {
                    mNewiebleIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.badge_newbie));
                } else if (a.name.equalsIgnoreCase("adventurer")) {
                    mAdvanturerIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.badge_adventurer));
                } else if (a.name.equalsIgnoreCase("walking dead")) {
                    mWalkingDeadIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.badge_walking_dead));
                } else if (a.name.equalsIgnoreCase("superstar")) {
                    mSuperstarIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.badge_superstar));
                }
            }
        }

        //List last step points.
        new LoadLastStepPointsTask().execute();
    }

    public void openAnimation() {
        if (view != null) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
            anim.setDuration(500);
            anim.setInterpolator(new DecelerateInterpolator());
            view.startAnimation(anim);
        }
    }

    public void closeAnimation(final OnAnimEndListener listener) {
        if (view != null) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_and_hide);
            anim.setDuration(500);
            anim.setInterpolator(new AccelerateInterpolator());
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (listener != null)
                        listener.onReady();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            view.startAnimation(anim);
        }
    }

    public interface OnAnimEndListener {
        public void onReady();
    }

}
