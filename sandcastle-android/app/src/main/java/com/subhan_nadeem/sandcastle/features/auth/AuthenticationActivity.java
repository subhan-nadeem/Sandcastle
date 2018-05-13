package com.subhan_nadeem.sandcastle.features.auth;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.subhan_nadeem.sandcastle.App;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.databinding.ActivityAuthenticationBinding;
import com.subhan_nadeem.sandcastle.features.main.MainActivity;

/**
 * Created by Subhan Nadeem on 2017-10-30.
 * Handles login/register authentication fragments
 */

public class AuthenticationActivity extends AppCompatActivity
        implements AuthenticationChangeListener {
    private ActivityAuthenticationBinding mBinding;

    public static Intent getIntent(Context context) {
        Intent i = new Intent(context, AuthenticationActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_authentication);

        initLoginFragment();
    }

    private void initLoginFragment() {
        Fragment loginFragment = LoginFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, loginFragment)
                .commit();
    }

    private void logInApp() {
        App app = (App) getApplication();
        app.initSocket();
        startActivity(MainActivity.getIntent(this));
        finish();
    }

    @Override
    public void onLoggedIn() {
        logInApp();
    }

    @Override
    public void onRegistered() {
        switchToProfileFragment();
    }

    private void switchToProfileFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.fragment_container, ProfilePhotoFragment.newInstance());
        transaction.commit();
    }

    @Override
    public void onRegisterRequested() {
        switchToRegisterFragment();
    }

    private void switchToRegisterFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.fragment_container, SignupFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoginRequested() {
        switchToLoginFragment();
    }

    private void switchToLoginFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.fragment_container, LoginFragment.newInstance());
        transaction.commit();
    }
}
