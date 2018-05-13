package com.subhan_nadeem.sandcastle.models.notifications;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Subhan Nadeem on 2017-10-15.
 */

public class BaseNotification {

    @SerializedName("notification_id")
    private Long notificationID;

    public Long getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(Long notificationID) {
        this.notificationID = notificationID;
    }
}
