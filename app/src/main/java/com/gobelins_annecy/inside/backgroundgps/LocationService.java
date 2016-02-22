package com.gobelins_annecy.inside.backgroundgps;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class LocationService extends Service {

    private LocationManager locationManager;
    // Interface for callback
    private LocationBroadcaster locationBroadcaster;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private static final String TAG = "LocationService";

    public LocationService() {
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
     * Run android location
     */

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service created");

        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000, 0, listener);

        Log.d(TAG, "Service started");
    }

    /**
     * Listener for android location
     */

    private LocationListener listener = new LocationListener() {


        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "New position : " + location.getLatitude() + " - " + location.getLongitude());
            Log.d(TAG, "onLocationChanged");
            locationBroadcaster.onLocationChanged(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "Status changed");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "Provider enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "Provider disabled");
        }
    };

    /**
     * Set callbacks from parent activity
     * @param locationBroadcaster
     */

    public void setCallbacks(LocationBroadcaster locationBroadcaster) {
        this.locationBroadcaster = locationBroadcaster;
    }
}
