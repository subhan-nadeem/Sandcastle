package com.subhan_nadeem.sandcastle.models.notifications;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Subhan Nadeem on 2017-10-15.
 */

public class NewMessageNotification extends BaseNotification {
    @SerializedName("user")
    private String mUser;
    @SerializedName("message")
    private String mMessage;

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
