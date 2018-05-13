package com.subhan_nadeem.sandcastle.helpers;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Subhan Nadeem on 2017-10-02.
 * Behaviour that enables custom views to move upwards in response
 * to snackbars etc.
 */

public class MoveUpwardBehaviour extends CoordinatorLayout.Behavior<View> {

    public MoveUpwardBehaviour() {
        super();
    }

    public MoveUpwardBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}