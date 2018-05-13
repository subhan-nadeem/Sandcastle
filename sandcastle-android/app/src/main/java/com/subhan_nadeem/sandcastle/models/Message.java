package com.subhan_nadeem.sandcastle.models;

import android.databinding.ObservableBoolean;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.subhan_nadeem.sandcastle.data.RealmHelper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.UUID;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

@SuppressWarnings("unused")
public class Message {

    @SerializedName("chat_room_id")
    private Long mChatRoomId;
    @SerializedName("created_at")
    private String mCreatedAt;
    @SerializedName("message")
    private String mMessage;
    @SerializedName("message_id")
    private Long mMessageId;
    @SerializedName("room_name")
    private String mRoomName;
    @SerializedName("room_created_at")
    private String mRoomCreatedAt;
    @SerializedName("user_id")
    private Long mUserId;
    @SerializedName("username")
    private String mUsername;
    @SerializedName("avatar_url")
    private String mAvatarURL;
    // Socket event listener awaiting confirmation of message upload
    private String EVENT_UPLOAD_CONFIRMATION;
    // Message is considered uploaded by default, and this is set to false when
    // waiting for server upload confirmation
    private transient ObservableBoolean mIsUploaded;

    /**
     * For use when sending message from client side and awaiting confirmation
     *
     * @param createdAt Time message was sent at
     * @param message   Message content
     */
    public Message(String createdAt, String message) {
        User user = new RealmHelper().getUser();
        mCreatedAt = createdAt;
        mMessage = message;
        mUserId = user.getId();
        mUsername = user.getName();
        mIsUploaded = new ObservableBoolean(true);
        EVENT_UPLOAD_CONFIRMATION = "message_uploaded_" + UUID.randomUUID().toString();
    }

    public String getAvatarURL() {
        return mAvatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        mAvatarURL = avatarURL;
    }

    /**
     * Returns message's specific socket event (unique)
     */
    public String getUploadConfirmationEvent() {
        return EVENT_UPLOAD_CONFIRMATION;
    }

    public ObservableBoolean isUploaded() {
        if (mIsUploaded == null) mIsUploaded = new ObservableBoolean(true);
        return mIsUploaded;
    }

    /**
     * Called when message is sent to server
     */
    public void setUploadConfirmationListener(final Socket socket) {
        mIsUploaded.set(false);
        socket.on(EVENT_UPLOAD_CONFIRMATION, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                boolean isUploaded = (boolean) args[0];
                mIsUploaded.set(isUploaded);
                if (isUploaded)
                    socket.off(EVENT_UPLOAD_CONFIRMATION);
                else
                    Log.d(getClass().getName(), "Error uploading message");
            }
        });
    }

    /**
     * Returns properly formatted timestamp
     * e.g. "12:01AM"
     */
    public String getTimestamp() {
        DateTime dateTime = new DateTime(mCreatedAt);
        DateTimeFormatter timestampFormat = DateTimeFormat.forPattern("h:mma");
        return timestampFormat.print(dateTime);
    }

    /**
     * Determines whether or not the message is sent by the logged in user
     *
     * @return True if message is sent by self, false otherwise
     */
    public boolean isSelf() {
        return new RealmHelper().isUserID(mUserId);
    }

    public Long getChatRoomId() {
        return mChatRoomId;
    }

    public void setChatRoomId(Long chatRoomId) {
        mChatRoomId = chatRoomId;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        mCreatedAt = createdAt;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public Long getMessageId() {
        return mMessageId;
    }

    public void setMessageId(Long messageId) {
        mMessageId = messageId;
    }

    public Object getRoomName() {
        return mRoomName;
    }

    public void setRoomName(String roomName) {
        mRoomName = roomName;
    }

    public String getRoomCreatedAt() {
        return mRoomCreatedAt;
    }

    public void setRoomCreatedAt(String roomCreatedAt) {
        mRoomCreatedAt = roomCreatedAt;
    }

    public Long getUserId() {
        return mUserId;
    }

    public void setUserId(Long userId) {
        mUserId = userId;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

}
