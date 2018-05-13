package com.subhan_nadeem.sandcastle.features.auth;

import android.app.Application;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.models.responses.APIError;
import com.subhan_nadeem.sandcastle.network.APIService;
import com.subhan_nadeem.sandcastle.network.APIUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Subhan Nadeem on 2017-10-30.
 * ViewModel pertaining to signup view
 */

public class SignupViewModel extends BaseAuthenticationViewModel {

    private ObservableField<String> mPasswordReenter = new ObservableField<>();

    public ObservableField<String> getPasswordReenter() {
        return mPasswordReenter;
    }

    public void setPasswordReenter(ObservableField<String> passwordReenter) {
        mPasswordReenter = passwordReenter;
    }

    SignupViewModel(@NonNull Application application) {
        super(application);
    }

    private void registerUser() {

        APIService apiService = APIUtils.getAPIService();

        mLoading.set(true);
        apiService.registerUser(mUsername.get(), mPassword.get()).enqueue(
                new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        mLoading.set(false);
                        if (response.isSuccessful()) {
                            Log.d(getClass().getName(), response.body().toString());
                            attemptLogin();
                        } else {
                            APIError apiError = APIError.parseError(response.errorBody());
                            if (apiError.getMessage().equals("User already exists")) {
                                mUsernameError.set(apiError.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onExecuteButtonClick() {
        if (isValidData()) registerUser();
    }

    @Override
    boolean isValidData() {
        if (!super.isValidData()) return false;

        if (TextUtils.isEmpty(mPasswordReenter.get())
                || !mPasswordReenter.get().equals(mPassword.get())) {
            mPasswordError.set("Your entered passwords do not match");
            return false;
        }

        // Data is valid
        mPasswordError.set(null);
        mUsernameError.set(null);
        return true;
    }


    @Override
    public String getExecuteButtonText() {
        return mResources.getString(R.string.register);
    }

    @Override
    public String getFacebookButtonText() {
        return mResources.getString(R.string.register_with_facebook);
    }

    @Override
    public String getLoadingText() {
        return mResources.getString(R.string.registering);
    }

    @Override
    public String getSwitchText() {
        return mResources.getString(R.string.switch_to_login_text);
    }
}
