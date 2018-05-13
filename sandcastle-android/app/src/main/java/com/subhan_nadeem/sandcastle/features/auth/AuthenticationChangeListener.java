package com.subhan_nadeem.sandcastle.features.auth;

/**
 * Created by Subhan Nadeem on 2017-10-30.
 * Listener that handles changed in the AuthenticationActivity's child fragments
 */

public interface AuthenticationChangeListener {
    void onLoggedIn();
    void onRegistered();
    void onRegisterRequested();
    void onLoginRequested();
}
