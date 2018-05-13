package com.subhan_nadeem.sandcastle.features.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.databinding.FragmentLoginBinding;

/**
 * Created by Subhan Nadeem on 2017-10-30.
 * Login view
 */

public class LoginFragment extends BaseAuthenticationFragment {

    private FragmentLoginBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        mBinding.setView(this);

        mBinding.setViewModel(ViewModelProviders.of(this).get(LoginViewModel.class));

        mBinding.loginButtons.authenticateText
                .setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));

        return mBinding.getRoot();
    }


    @Override
    void subscribeUI() {
        LoginViewModel viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        viewModel.getIsLoggedIn().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isLoggedIn) {
                if (isLoggedIn) {
                    animateDone();
                }
            }
        });
    }

    @Override
    public void onSwitchModeClick() {
        mAuthenticationChangeListener.onRegisterRequested();
    }


    private void animateDone() {
        mBinding.loginButtons.authenticateText.clearAnimation();
        mBinding.loginButtons.authenticateText.setVisibility(View.GONE);
        Animation slideOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_out);
        slideOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBinding.loginButtons.loadingLottie.setVisibility(View.GONE);

                mBinding.loginButtons.doneLottie.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mAuthenticationChangeListener.onLoggedIn();
                    }
                });
                mBinding.loginButtons.doneLottie.playAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mBinding.loginButtons.loadingLottie.setAnimation(slideOutAnim);
    }


    public static Fragment newInstance() {
        return new LoginFragment();
    }
}
