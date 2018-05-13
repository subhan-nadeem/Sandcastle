package com.subhan_nadeem.sandcastle.models;

import com.google.android.gms.maps.model.Marker;
import com.google.gson.annotations.SerializedName;

import java.lang.ref.WeakReference;
import java.util.List;

@SuppressWarnings("unused")
public class ChatRoom {

    @SerializedName("chat_room_id")
    private Long mChatRoomId;
    @SerializedName("created_at")
    private String mCreatedAt;
    @SerializedName("distance_metres")
    private Double mDistanceMetres;
    @SerializedName("lat")
    private Double mLat;
    @SerializedName("lng")
    private Double mLng;
    @SerializedName("name")
    private String mName;
    @SerializedName("users")
    private List<User> mUsers;

    // Holds weak reference to marker
    private transient WeakReference<Marker> mMarker = new WeakReference<>(null);

    public List<User> getUsers() {
        return mUsers;
    }

    public void setUsers(List<User> users) {
        mUsers = users;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj.getClass() == this.getClass()
                && ((ChatRoom) obj).getChatRoomId().longValue() == getChatRoomId();
    }

    public Marker getMarker() {
        return mMarker.get();
    }

    public void setMarker(Marker marker) {
        mMarker = new WeakReference<>(marker);
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

    public Double getDistanceMetres() {
        return mDistanceMetres;
    }

    public void setDistanceMetres(Double distanceMetres) {
        mDistanceMetres = distanceMetres;
    }

    public Double getLat() {
        return mLat;
    }

    public void setLat(Double lat) {
        mLat = lat;
    }

    public Double getLng() {
        return mLng;
    }

    public void setLng(Double lng) {
        mLng = lng;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
