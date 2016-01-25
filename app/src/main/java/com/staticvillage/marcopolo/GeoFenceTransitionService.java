package com.staticvillage.marcopolo;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by joelparrish
 */
public class GeoFenceTransitionService extends IntentService {
    public static final String BROADCAST_ACTION = "com.staticvillage.marcopolo.BROADCAST";
    public static final String BROADCAST_EXTRA_ID = "com.staticvillage.marcopolo.EXTRA_ID";

    /**
     * Creates an IntentService
     */
    public GeoFenceTransitionService() {
        super("geo_fence_marco_polo");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e("marco_polo", "geofencing even error");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d("marco_polo", "geofence enter");

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            //We only want the initial triggered geofence
            if(triggeringGeofences.size() > 0) {
                Log.d("marco_polo", "triggers!");
                Geofence geoFence = triggeringGeofences.get(0);

                Intent localIntent = new Intent(BROADCAST_ACTION);
                localIntent.putExtra(BROADCAST_EXTRA_ID, geoFence.getRequestId());
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            }else {
                Log.d("marco_polo", "No triggers :(");
            }
        } else {
            Log.e("marco_polo", "invalid transition type");
        }
    }
}
