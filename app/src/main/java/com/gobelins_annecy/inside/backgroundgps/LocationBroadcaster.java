package com.gobelins_annecy.inside.backgroundgps;

/**
 * Created by vague on 20/02/2016.
 */
interface LocationBroadcaster {
    void onLocationChanged(double lat, double lng);
}
