package com.subhan_nadeem.sandcastle.models.responses;

import com.google.gson.annotations.SerializedName;
import com.subhan_nadeem.sandcastle.models.Message;

public class NewMessageResponse {
    @SerializedName("message")
    private Message mMessage;

    public Message getMessage() {
        return mMessage;
    }

    public void setMessage(Message message) {
        mMessage = message;
    }
}
