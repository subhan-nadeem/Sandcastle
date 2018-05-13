package com.subhan_nadeem.sandcastle.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Subhan Nadeem on 2017-11-09.
 * Convenience class holding auth and refresh tokens of client
 */

public class AuthTokenHolder extends RealmObject {

    @PrimaryKey
    @Required
    private String authToken;
    @Required
    private String refreshToken;

    public AuthTokenHolder() {
    }

    public AuthTokenHolder(String authToken, String refreshToken) {
        this.authToken = authToken;
        this.refreshToken = refreshToken;
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
