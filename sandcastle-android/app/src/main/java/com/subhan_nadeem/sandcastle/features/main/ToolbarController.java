package com.subhan_nadeem.sandcastle.features.main;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.subhan_nadeem.sandcastle.models.ToolbarOptions;

/**
 * Created by Subhan Nadeem on 2017-10-21.
 * Handles toolbar business logic
 */

class ToolbarController {
    final private static int TOOLBAR_ANIMATION_DURATION_MS = 500;
    final private AppCompatActivity mActivity;
    final private AppBarLayout mAppbarLayout;
    final private View mCoordinatorChild;

    ToolbarController(AppCompatActivity activity, AppBarLayout appbarLayout, Toolbar toolbar,
                      View coordinatorChild) {
        mActivity = activity;
        mAppbarLayout = appbarLayout;
        mCoordinatorChild = coordinatorChild;
        initToolbar(toolbar);
    }

    private void initToolbar(Toolbar toolbar) {
        mActivity.setSupportActionBar(toolbar);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void animateAppBarMargins(final int newMarginPX) {
        final CoordinatorLayout.LayoutParams params
                = (CoordinatorLayout.LayoutParams)
                mAppbarLayout.getLayoutParams();

        final int oldMargin = params.bottomMargin;

        // No need to animate if same values
        if (newMarginPX == oldMargin) return;

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                int currentMargin;
                if (newMarginPX > oldMargin)
                    currentMargin = (int) (newMarginPX * interpolatedTime);
                else
                    currentMargin = (int) (oldMargin - (oldMargin - newMarginPX) * interpolatedTime);

                params.setMargins(currentMargin, currentMargin, currentMargin, currentMargin);

                mAppbarLayout.setLayoutParams(params);
            }
        };
        a.setDuration(TOOLBAR_ANIMATION_DURATION_MS); // in ms
        mAppbarLayout.startAnimation(a);
    }

    private void showTitle(String roomName) {
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        mActivity.getSupportActionBar().setTitle(roomName);
    }
    private void setToolbarScrollingBehaviour() {
        CoordinatorLayout.LayoutParams params
                = (CoordinatorLayout.LayoutParams) mCoordinatorChild.getLayoutParams();

        params.setBehavior(new AppBarLayout.ScrollingViewBehavior());

        mCoordinatorChild.setLayoutParams(params);
    }

    private void removeToolbarBehaviour() {
        CoordinatorLayout.LayoutParams params
                = (CoordinatorLayout.LayoutParams) mCoordinatorChild.getLayoutParams();

        params.setBehavior(null);

        mCoordinatorChild.setLayoutParams(params);
    }

    void changeToolbar(ToolbarOptions options) {
        if (options.getMargins() != null)
            animateAppBarMargins(options.getMargins());

        if (options.getTitle() != null)
            showTitle(options.getTitle());
        else
            hideTitle();

        if (options.getSubtitle() != null)
            showSubtitle(options.getSubtitle());
        else
            showSubtitle(null);
    }

    private void showSubtitle(String subtitle) {
        mActivity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void hideTitle() {
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}
