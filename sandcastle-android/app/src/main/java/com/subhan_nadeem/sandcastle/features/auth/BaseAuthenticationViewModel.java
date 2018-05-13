package com.subhan_nadeem.sandcastle.features.auth;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.res.Resources;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.subhan_nadeem.sandcastle.data.RealmHelper;
import com.subhan_nadeem.sandcastle.models.AuthTokenHolder;
import com.subhan_nadeem.sandcastle.models.responses.APIError;
import com.subhan_nadeem.sandcastle.models.responses.AuthenticateResponse;
import com.subhan_nadeem.sandcastle.network.APIService;
import com.subhan_nadeem.sandcastle.network.APIUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Subhan Nadeem on 2017-10-30.
 * ViewModel that holds common functionality between signup and login viewmodels
 * i.e. username, password fields and errors, loading observable boolean that UI observes in both cases
 */

public abstract class BaseAuthenticationViewModel extends AndroidViewModel {

    private static final int MIN_USERNAME_LENGTH = 4;
    final Resources mResources;
    ObservableField<String> mUsernameError = new ObservableField<>();
    ObservableField<String> mPasswordError = new ObservableField<>();
    ObservableBoolean mLoading = new ObservableBoolean();
    ObservableField<String> mUsername = new ObservableField<>();
    ObservableField<String> mPassword = new ObservableField<>();
    private MutableLiveData<Boolean> mIsLoggedIn = new MutableLiveData<>();

    BaseAuthenticationViewModel(@NonNull Application application) {
        super(application);
        mResources = application.getResources();
    }

    MutableLiveData<Boolean> getIsLoggedIn() {
        return mIsLoggedIn;
    }

    public ObservableField<String> getPassword() {
        return mPassword;
    }

    public void setPassword(ObservableField<String> password) {
        mPassword = password;
    }

    public ObservableField<String> getUsername() {
        return mUsername;
    }

    public void setUsername(ObservableField<String> username) {
        mUsername = username;
    }

    public ObservableBoolean getLoading() {
        return mLoading;
    }

    boolean isValidData() {
        boolean isValid = true;

        if (TextUtils.isEmpty(mUsername.get())
                || mUsername.get().length() < MIN_USERNAME_LENGTH) {
            mUsernameError.set("Username must be at least " + MIN_USERNAME_LENGTH + " characters");
            isValid = false;
        } else {
            mUsernameError.set(null);
        }

        if (TextUtils.isEmpty(mPassword.get())) {
            mPasswordError.set("You must enter a password");
            isValid = false;
        } else {
            mPasswordError.set(null);
        }

        return isValid;
    }


    /**
     * Login is attempted from both the registration and login viewmodels
     */
    void attemptLogin() {

        mLoading.set(true);

        APIService apiService = APIUtils.getAPIService();

        apiService.authenticateUser(mUsername.get(), mPassword.get(),
                FirebaseInstanceId.getInstance().getToken(), android.os.Build.MODEL).enqueue(
                new Callback<AuthenticateResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthenticateResponse> call,
                                           @NonNull Response<AuthenticateResponse> response) {
                        if (response.isSuccessful()) {

                            AuthenticateResponse authenticateResponse = response.body();

                            persistLogin(authenticateResponse);

                            mIsLoggedIn.setValue(true);
                        } else {
                            mLoading.set(false);

                            APIError apiError = APIError.parseError(response.errorBody());
                            mPasswordError.set(apiError.getMessage());
                            mUsernameError.set(apiError.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthenticateResponse> call,
                                          @NonNull Throwable t) {
                        Log.d(getClass().getName(), "Login failure: " + t.toString());
                    }
                });
    }


    /**
     * Persists login information to memory
     * @param authenticateResponse Given authentication response from server
     */
    private void persistLogin(AuthenticateResponse authenticateResponse) {
        RealmHelper repository = new RealmHelper();

        AuthTokenHolder authTokenHolder = new AuthTokenHolder(
                authenticateResponse.getAuthToken(),
                authenticateResponse.getRefreshToken());

        repository.storeTokens(authTokenHolder);

        // Persist user object
        repository.persistUserObject(authenticateResponse.getUser());
    }

    public ObservableField<String> getUsernameError() {
        return mUsernameError;
    }

    public ObservableField<String> getPasswordError() {
        return mPasswordError;
    }

    public abstract void onExecuteButtonClick();

    public abstract String getExecuteButtonText();

    public abstract String getFacebookButtonText();

    public abstract String getLoadingText();

    public abstract String getSwitchText();


}
