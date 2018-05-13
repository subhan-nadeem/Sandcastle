package com.subhan_nadeem.sandcastle.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.subhan_nadeem.sandcastle.models.AuthTokenHolder;
import com.subhan_nadeem.sandcastle.models.User;

import io.realm.Realm;

/**
 * Created by Subhan Nadeem on 2017-10-11.
 * Centralized helper for Realm object transactions
 */

public class RealmHelper {

    private static final String TAG = "RealmHelper";

    public void persistUserObject(final User user) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.copyToRealmOrUpdate(user);
                    Log.d(TAG, "User successfully inserted");
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }


    public boolean isUserLoggedIn() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            final User user = realm.where(User.class).findFirst();

            return user != null;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }


    public void storeTokens(final AuthTokenHolder tokens) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {

                    final AuthTokenHolder tokenHolder = realm.where(AuthTokenHolder.class)
                            .findFirst();

                    // Only one instance of a token holder can exist
                    if (tokenHolder != null) tokenHolder.deleteFromRealm();

                    realm.copyToRealmOrUpdate(tokens);
                    Log.d(TAG, "Tokens successfully inserted");
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }


    /**
     * Retrieves user auth token
     *
     * @return User auth token if one exists, empty string otherwise
     */
    public String getUserAuthToken() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            final AuthTokenHolder authTokenHolder
                    = realm.where(AuthTokenHolder.class).findFirst();

            return authTokenHolder == null || authTokenHolder.getAuthToken() == null ? ""
                    : authTokenHolder.getAuthToken();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    /**
     * Retrieves user auth token
     *
     * @return User auth token if one exists, empty string otherwise
     */
    public String getRefreshToken() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            final AuthTokenHolder authTokenHolder
                    = realm.where(AuthTokenHolder.class).findFirst();

            return authTokenHolder == null || authTokenHolder.getRefreshToken() == null ? ""
                    : authTokenHolder.getRefreshToken();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }


    public AuthTokenHolder getTokenHolder() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            AuthTokenHolder holder = realm.where(AuthTokenHolder.class).findFirst();
            return holder == null ? null : realm.copyFromRealm(holder);
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }


    /**
     * @return True if given ID is the user's ID, false otherwise
     */
    public boolean isUserID(long id) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            final User user = realm.where(User.class).findFirst();

            boolean isSameID = false;

            if (user != null)
                isSameID = user.getId() == id;

            return isSameID;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }


    /**
     * Returns an unmanaged copy of the user object, for reference purposes
     */
    public User getUser() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            final User user = realm.where(User.class).findFirst();

            return user == null ? null : realm.copyFromRealm(user);
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void setUserChatroomID(final long userChatroomID) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    final User user = realm.where(User.class).findFirst();
                    user.setChatroomID(userChatroomID);
                    realm.copyToRealmOrUpdate(user);
                    Log.d(TAG, "User chat room ID successfully updated");
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }
}
