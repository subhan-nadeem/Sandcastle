package com.subhan_nadeem.sandcastle.models;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

import java.lang.ref.WeakReference;

/**
 * Created by Subhan Nadeem on 2017-10-13.
 * Holds marker and its circle UI components for combined reference
 */

public class MapMarkerComponent {
    private WeakReference<Marker> mMarker = new WeakReference<>(null);
    private WeakReference<Circle> mCircle = new WeakReference<>(null);
    private ChatRoom mChatRoom;

    public MapMarkerComponent(Marker marker, Circle circle, ChatRoom room) {
        mMarker = new WeakReference<>(marker);
        mCircle = new WeakReference<>(circle);
        mChatRoom = room;
    }

    public ChatRoom getChatRoom() {
        return mChatRoom;
    }

    public Marker getMarker() {
        return mMarker.get();
    }

    public void setMarker(Marker marker) {
        mMarker = new WeakReference<>(marker);
    }

    public Circle getCircle() {
        return mCircle.get();
    }

    public void setCircle(Circle circle) {
        mCircle = new WeakReference<>(circle);
    }
}
