package com.gobelins_annecy.inside.backgroundgps;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        ResultCallback<LocationSettingsResult> {

    private LocationManager locationManager;
    // Interface for callback
    private LocationBroadcaster locationBroadcaster;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Request class
    private LocationRequest mLocationRequest;
    // API for location
    private GoogleApiClient mGoogleApiClient;
    // Debug
    private static final String TAG = "LocationService";
    PendingResult<LocationSettingsResult> result;

    public LocationService() {
    }

    /**
     * Run API Location
     */

    @Override
    public void onCreate() {
        super.onCreate();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();

        Log.d(TAG, "Service started");
    }

    /**
     * Success API connection callback
     * @param connectionHint
     */

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Service connected");
        checkIfDeviceReady();
        createLocationRequest();
        checkIfDeviceReady();
    }

    /**
     * Fail API connection callback
     * @param connectionResult
     */

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed connection to service : " + connectionResult);
    }

    /**
     * Suspended API connection callback
     * @param i
     */

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended : " + i);
    }

    /**
     * Check if location is enabled
     */

    protected void checkIfDeviceReady(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(this);
    }

    /**
     * Callback for checking location enabling
     * @param result
     */

    public void onResult(LocationSettingsResult result) {
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.d(TAG, "Go ahead");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Log.d(TAG, "Turn on your GPS Please");
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
                Log.d(TAG, "Setting unaivailable");
                break;
        }
    }


    /**
     * Set a location request
     */

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Request location update
     */

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Stop location updates
     */

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Listener for location change
     * @param location
     */

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        locationBroadcaster.onLocationChanged(location.getLatitude(), location.getLongitude());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class used for the client Binder.
     */

    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    /**
     * Set callbacks from parent activity
     * @param locationBroadcaster
     */

    public void setCallbacks(LocationBroadcaster locationBroadcaster) {
        this.locationBroadcaster = locationBroadcaster;
    }
}
