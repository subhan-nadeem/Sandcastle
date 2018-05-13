package com.subhan_nadeem.sandcastle.features.auth;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.databinding.FragmentSignupBinding;

/**
 * Created by Subhan Nadeem on 2017-10-31.
 * Signup view
 */

public class SignupFragment extends BaseAuthenticationFragment {

    private FragmentSignupBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false);

        mBinding.setView(this);

        mBinding.setViewModel(ViewModelProviders.of(this).get(SignupViewModel.class));

        mBinding.loginButtons.authenticateText
                .setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));

        return mBinding.getRoot();
    }

    @Override
    void subscribeUI() {
        SignupViewModel viewModel = ViewModelProviders.of(this)
                .get(SignupViewModel.class);

        viewModel.getIsLoggedIn().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isLoggedIn) {
                if (isLoggedIn) {
                    mAuthenticationChangeListener.onRegistered();
                }
            }
        });
    }

    @Override
    public void onSwitchModeClick() {
        mAuthenticationChangeListener.onLoginRequested();
    }

    public static Fragment newInstance() {
        return new SignupFragment();
    }
}
