package com.subhan_nadeem.sandcastle.features.maps;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.subhan_nadeem.sandcastle.R;

/**
 * Created by Subhan Nadeem on 2017-10-13.
 * Adapter that creates custom map marker info window
 */

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mView;

    InfoWindowAdapter(LayoutInflater inflater) {
        mView = inflater.inflate(R.layout.info_marker, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView titleTextview = mView.findViewById(R.id.info_room_name);
        titleTextview.setText(marker.getTitle());

        TextView numUsers = mView.findViewById(R.id.info_num_users);

        numUsers.setText(marker.getSnippet());
        return mView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Leave null
        return null;
    }
}
