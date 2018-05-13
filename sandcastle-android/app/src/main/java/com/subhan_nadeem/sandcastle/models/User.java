package com.subhan_nadeem.sandcastle.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

@SuppressWarnings("unused")
public class User extends RealmObject {

    @SerializedName("user_id")
    @PrimaryKey
    @Required
    private Long mId;
    @SerializedName("name")
    @Required
    private String mName;
    @SerializedName("chat_room_id")
    private Long mChatroomID;
    @SerializedName("avatar_url")
    private String mAvatarURL;

    public String getAvatarURL() {
        return mAvatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        mAvatarURL = avatarURL;
    }

    public Long getChatroomID() {
        return mChatroomID;
    }

    public void setChatroomID(Long chatroomID) {
        mChatroomID = chatroomID;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
