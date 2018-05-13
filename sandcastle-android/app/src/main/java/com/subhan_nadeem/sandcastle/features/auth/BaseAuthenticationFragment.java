package com.subhan_nadeem.sandcastle.features.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by Subhan Nadeem on 2017-10-31.
 * Contains base functionality for authentication fragments
 * 1. Attach/detach authentication change listener
 */

public abstract class BaseAuthenticationFragment extends Fragment {

    AuthenticationChangeListener mAuthenticationChangeListener;

    @Override
    public void onDetach() {
        super.onDetach();
        mAuthenticationChangeListener = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscribeUI();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAuthenticationChangeListener = (AuthenticationChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AuthenticationChangeListener");
        }
    }

    abstract void subscribeUI();

    public abstract void onSwitchModeClick();
}
