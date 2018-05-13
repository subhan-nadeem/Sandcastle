package com.subhan_nadeem.sandcastle.features.maps;

import android.databinding.ObservableField;
import android.util.Log;

import com.google.gson.JsonObject;
import com.subhan_nadeem.sandcastle.models.BaseSocketController;

import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Subhan Nadeem on 2017-10-27.
 * Controls socket IO interactions with map
 */

class MapsSocketController extends BaseSocketController {

    private static final String EVENT_LOCATION_UPDATE = "locationUpdate";
    private static final String KEY_NEARBY_USERS_UPDATE = "nearbyUsersUpdate";
    private static final String STATE_MAP = "stateMap";
    private final OnNearbyUsersUpdateListener mListener;

    /**
     * Will receive a JSON obj of format:
     * {"1": "user", "2": "user2"}
     */
    private final Emitter.Listener nearbyUsersUpdateListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject userObj = (JSONObject) args[0];

            Log.d(getClass().getName(), "Received nearby users:\n" + userObj.toString());
            mListener.onNearbyUsersUpdated(userObj.length());
        }
    };

    MapsSocketController(ObservableField<Socket> socket, OnNearbyUsersUpdateListener listener) {
        super(socket, STATE_MAP);
        mListener = listener;
    }

    @Override
    protected void onResumeChild() {
        mSocket.get().on(KEY_NEARBY_USERS_UPDATE, nearbyUsersUpdateListener);
    }

    @Override
    protected void onPauseChild() {
        mSocket.get().off(KEY_NEARBY_USERS_UPDATE);
    }

    void emitLocation(double latitude, double longitude) {
        if (mSocket.get() == null) return;

        String KEY_LAT = "lat";
        String KEY_LNG = "lng";

        JsonObject locationObj = new JsonObject();

        locationObj.addProperty(KEY_LAT, latitude);
        locationObj.addProperty(KEY_LNG, longitude);

        mSocket.get().emit(EVENT_LOCATION_UPDATE, locationObj.toString());
    }
}
