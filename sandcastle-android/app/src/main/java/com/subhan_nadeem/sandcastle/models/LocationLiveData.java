package com.subhan_nadeem.sandcastle.models;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

/**
 * Created by Subhan Nadeem on 2017-10-11.
 * Abstracts location checking
 */

@SuppressWarnings("MissingPermission")
public class LocationLiveData extends LiveData<Location> {

    private final static int LOCATION_REQUEST_INTERVAL_SECONDS = 30;
    private final static int MIN_DISPLACEMENT_REQUEST_METRES = 0;
    private final String TAG = this.getClass().getName();

    private final Context context;

    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            setValue(locationResult.getLastLocation());
        }
    };

    public LocationLiveData(Context context) {
        this.context = context;
    }

    @Override
    protected void onActive() {
        super.onActive();
        FusedLocationProviderClient locationProviderClient = getFusedLocationProviderClient();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(LOCATION_REQUEST_INTERVAL_SECONDS));
        locationRequest.setSmallestDisplacement(MIN_DISPLACEMENT_REQUEST_METRES);
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        Log.d(TAG, "Getting location...");
    }

    @NonNull
    private FusedLocationProviderClient getFusedLocationProviderClient() {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }
        return fusedLocationProviderClient;
    }

    @Override
    protected void onInactive() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}