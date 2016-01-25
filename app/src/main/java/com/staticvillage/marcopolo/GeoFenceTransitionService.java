package com.staticvillage.marcopolo;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by joelparrish on 12/23/15.
 */
public class GeoFenceTransitionService extends IntentService {
    public static final String BROADCAST_ACTION = "com.staticvillage.marcopolo.BROADCAST";
    public static final String BROADCAST_EXTRA_ID = "com.staticvillage.marcopolo.EXTRA_ID";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
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

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d("marco_polo", "geofence enter");
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

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
            // Log the error.
            Log.e("marco_polo", "invalid transition type");
        }
    }
}
