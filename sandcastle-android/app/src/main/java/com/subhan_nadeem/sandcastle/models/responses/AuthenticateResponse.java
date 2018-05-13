package com.subhan_nadeem.sandcastle.models.responses;

import com.google.gson.annotations.SerializedName;
import com.subhan_nadeem.sandcastle.models.User;

@SuppressWarnings("unused")
public class AuthenticateResponse extends BaseResponse {

    @SerializedName("auth_token")
    private String mAuthToken;
    @SerializedName("refresh_token")
    private String mRefreshToken;
    @SerializedName("user")
    private User mUser;

    public String getAuthToken() {
        return mAuthToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        mRefreshToken = refreshToken;
    }

    public void setAuthToken(String authToken) {
        mAuthToken = authToken;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

}
