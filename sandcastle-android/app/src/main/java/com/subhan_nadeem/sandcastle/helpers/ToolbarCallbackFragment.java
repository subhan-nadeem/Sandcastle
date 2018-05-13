package com.subhan_nadeem.sandcastle.helpers;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.subhan_nadeem.sandcastle.features.main.ToolbarChangeListener;

/**
 * Created by Subhan Nadeem on 2017-10-21.
 * ToolbarCallbackFragment that holds common behaviour for most fragments
 *
 * Features:
 * 1. Implements ToolbarChangeListener for all fragments
 */

public class ToolbarCallbackFragment extends Fragment {

    protected ToolbarChangeListener mToolbarChangeListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mToolbarChangeListener = (ToolbarChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ToolbarChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mToolbarChangeListener = null;
    }

}
