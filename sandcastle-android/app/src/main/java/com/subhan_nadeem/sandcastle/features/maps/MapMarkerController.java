package com.subhan_nadeem.sandcastle.features.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.models.ChatRoom;
import com.subhan_nadeem.sandcastle.models.MapMarkerComponent;

import java.util.HashMap;
import java.util.List;

import static com.subhan_nadeem.sandcastle.utils.BitmapUtils.getScaledPinBitmap;

/**
 * Created by Subhan Nadeem on 2017-10-28.
 * Handles drawing/memory management of map markers
 */

class MapMarkerController {

    // Map that enables marker UI components

    // to be retrieved by their ID, for use for onclicklistener
    private HashMap<String, MapMarkerComponent> mMarkersMap = new HashMap<>();

    // Lets chatrooms be retrieved by their ID
    // Used to see if updated chatrooms already exist on the map
    private HashMap<Long, ChatRoom> mChatRoomMap = new HashMap<>();
    private final Bitmap mMarkerBitmap; // Hold in memory to avoid slowing down UI
    private final Context mContext;

    void setMap(GoogleMap map) {
        mMap = map;
    }

    private GoogleMap mMap;

    MapMarkerController(Context context) {
        mContext = context;
        mMarkerBitmap = getScaledPinBitmap(mContext.getResources(), R.drawable.map_pin_blue);
    }


    HashMap<String, MapMarkerComponent> getMarkersMap() {
        return mMarkersMap;
    }

    /**
     * Adds marker of the corresponding chatroom to map and persists it in hashmap
     */
    private void addChatroomToUI(ChatRoom room) {
        if (mMap == null) return;

        LatLng markerLatLng = new LatLng(room.getLat(), room.getLng());

        Marker marker = mMap.addMarker(
                new MarkerOptions().position(markerLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(mMarkerBitmap)));

        marker.setTitle(room.getName());

        marker.setSnippet(String.valueOf(room.getUsers().size()));

        room.setMarker(marker);

        Circle markerCircle = drawMarkerCircle(markerLatLng);

        MapMarkerComponent component = new MapMarkerComponent(marker, markerCircle, room);

        mMarkersMap.put(marker.getId(), component);

        mChatRoomMap.put(room.getChatRoomId(), room);
    }

    private Circle drawMarkerCircle(LatLng position) {
        double radiusInMeters = MapsViewModel.CHATROOM_RANGE_METRES;
        int strokeColor = ContextCompat.getColor(mContext, R.color.mapCircleOutline);

        int shadeColor = ContextCompat.getColor(mContext, R.color.mapCircleFill);
        CircleOptions circleOptions = new CircleOptions().center(position)
                .radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        return mMap.addCircle(circleOptions);
    }


    /**
     * Checks to see if the new list of chatrooms has any chatrooms not currently on the map
     * and if there are any rooms on the map that are no longer nearby
     *
     * @param chatRooms Updated list of chatrooms
     */
    void reconcileMapMarkers(@Nullable List<ChatRoom> chatRooms) {
        if (chatRooms == null) return;

        // Add any new chat rooms to current UI
        for (ChatRoom room : chatRooms) {
            if (mChatRoomMap.get(room.getChatRoomId()) == null)
                addChatroomToUI(room);
        }

        // Ensure all chatrooms shown on map are the same as chatrooms
        for (ChatRoom chatRoom : mChatRoomMap.values()) {
            if (!chatRooms.contains(chatRoom)) {

                // Given chatroom is not in list of new chatrooms, so remove from map
                Marker marker = chatRoom.getMarker();
                if (marker != null) {
                    mMarkersMap.get(marker.getId()).getCircle().remove();
                    mMarkersMap.remove(marker.getId());
                    marker.remove();
                }
                mChatRoomMap.remove(chatRoom.getChatRoomId());
            }
        }
    }
}
