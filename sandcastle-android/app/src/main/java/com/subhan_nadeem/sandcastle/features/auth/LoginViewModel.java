package com.subhan_nadeem.sandcastle.features.auth;

import android.app.Application;

import com.subhan_nadeem.sandcastle.R;

/**
 * Created by Subhan Nadeem on 2017-10-11.
 * ViewModel pertaining to LoginActivity
 */

@SuppressWarnings("ConstantConditions")
public class LoginViewModel extends BaseAuthenticationViewModel {

    private static final String TAG = "LoginViewModel";

    public LoginViewModel(Application application) {
        super(application);
    }

    @Override
    public void onExecuteButtonClick() {
        if (isValidData()) {
            attemptLogin();
        }
    }

    @Override
    public String getExecuteButtonText() {
        return mResources.getString(R.string.login);
    }

    @Override
    public String getFacebookButtonText() {
        return mResources.getString(R.string.log_in_with_facebook);
    }

    @Override
    public String getLoadingText() {
        return mResources.getString(R.string.authenticating);
    }

    @Override
    public String getSwitchText() {
        return mResources.getString(R.string.switch_to_register_text);
    }


}
