package com.subhan_nadeem.sandcastle.models.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Subhan Nadeem on 2017-11-09.
 * Response received when refreshing user refresh token
 */

public class RefreshTokenResponse {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("auth_token")
    @Expose
    private String authToken;
    @SerializedName("refresh_token")
    @Expose
    private String refreshToken;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

