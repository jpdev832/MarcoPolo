package com.staticvillage.marcopolo;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;
import com.staticvillage.marcopolo.model.PointMarker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class GeoFenceActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>, GeoFenceListener, LocationListener {
    private List<PointMarker> mPointMarkerList;
    private List<Geofence> mGeoFences;
    private PoloView mPoloView;
    private GoogleMap mMap;
    private ProgressDialog mProgressDialog;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private IntentFilter mBroadcastIntentFilter;
    private ResponseReceiver mReceiver;
    private Polyline mPolyLine;
    private Marker mMarker;
    private LocationRequest mLocationRequest;
    private PoloDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mDrawer = new PoloDrawer(this);

        mPoloView = (PoloView) findViewById(R.id.imgMarker);
        mPoloView.setDrawer(mDrawer);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPointMarkerList = new LinkedList<>();
        mGeoFences = new LinkedList<>();
        mBroadcastIntentFilter = new IntentFilter(GeoFenceTransitionService.BROADCAST_ACTION);

        Log.d("marco_polo", getIntent().getData().toString());
        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
        mReceiver = new ResponseReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mBroadcastIntentFilter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mGoogleApiClient.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(false);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(mPointMarkerList.size() < 1) {
            mProgressDialog = ProgressDialog.show(this, "Download Marker", "Downloading Markers...");
            new DownloadTask(getIntent().getData().toString()).execute();
        }

        LocationServices
                .FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(Status status) {
//        if(!status.isSuccess())
//            Toast.makeText(this, "Geo Fence failed", Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(this, "geo fence added!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGeoFence(int id) {
        if(++id >= mPointMarkerList.size())
            return;

        PointMarker pointMarker = mPointMarkerList.get(id);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng markerLatLng = new LatLng(pointMarker.getLatitude(), pointMarker.getLongitude());
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(markerLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.polo_marker)));

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(userLatLng)
                .add(markerLatLng);

        mPolyLine = mMap.addPolyline(polylineOptions);

        double bearing = SphericalUtil.computeHeading(userLatLng, markerLatLng);
        mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(markerLatLng)
                        .bearing((float) bearing)
                        .zoom(18)
                        .tilt(80)
                        .build()));

        loadImage(pointMarker.getData());
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mPolyLine != null) {
            mPolyLine.remove();

            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(new LatLng(location.getLatitude(), location.getLongitude()))
                    .add(mMarker.getPosition());

            mPolyLine = mMap.addPolyline(polylineOptions);

            double bearing = SphericalUtil.computeHeading(userLatLng, mMarker.getPosition());
            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(userLatLng)
                            .bearing((float) bearing)
                            .zoom(20)
                            .tilt(80)
                            .build()));
        }
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeoFenceTransitionService.class);
        mGeofencePendingIntent = PendingIntent
                .getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    protected Geofence getGeoFence(PointMarker pointMarker) {
        return new Geofence.Builder()
                .setRequestId(String.valueOf(pointMarker.getMarkerIndex()))
                .setCircularRegion(pointMarker.getLatitude(), pointMarker.getLongitude(),
                        pointMarker.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    protected GeofencingRequest getGeoFencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFences);
        return builder.build();
    }

    private void addGeoFences() {
        if(mGeoFences.size() > 0) {
            LocationServices
                    .GeofencingApi
                    .addGeofences(mGoogleApiClient,
                            getGeoFencingRequest(),
                            getGeofencePendingIntent())
                    .setResultCallback(this);
        }
    }

    private void loadImage(String url) {
        Glide.with(this)
                .load(url)
                .asBitmap()
                .dontAnimate()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        mDrawer.setBitmap(resource);
                    }
                });
    }

    private class DownloadTask extends AsyncTask<Void, Void, List<PointMarker>> {
        private final String markerUrl;
        private final Gson mmGson;

        public DownloadTask(String url) {
            this.markerUrl = url;
            this.mmGson = new Gson();
        }

        private List<PointMarker> retrieveMarkers() {
            Log.d("marco_polo", "retrieving markers");

            try {
                URL url = new URL(markerUrl);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setDoInput(true);

                StringBuilder sb = new StringBuilder();
                int httpResult = connection.getResponseCode();
                if(httpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(),"utf-8"));

                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    br.close();
                    List<PointMarker> response = mmGson.fromJson(sb.toString(),
                            new TypeToken<List<PointMarker>>(){}.getType());
                    Log.d("marco_polo", sb.toString());

                    return response;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected List<PointMarker> doInBackground(Void... params) {
            return retrieveMarkers();
        }

        @Override
        protected void onPostExecute(List<PointMarker> pointMarkerList) {
            mProgressDialog.dismiss();

            if(pointMarkerList == null || pointMarkerList.size() == 0) {
//                Toast.makeText(getBaseContext(), "Error Occurred!", Toast.LENGTH_SHORT).show();
                finish();
            }

            mPointMarkerList = pointMarkerList;

            com.google.android.gms.maps.model.Marker marker = null;
            if(mPointMarkerList.size() > 0) {
                for(PointMarker m : mPointMarkerList) {
                    mGeoFences.add(getGeoFence(m));
                }

                onGeoFence(-1);
                addGeoFences();
            }
        }
    }

    private class ResponseReceiver extends BroadcastReceiver {
        private GeoFenceListener mmListener;

        public ResponseReceiver(GeoFenceListener listener) {
            mmListener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("marco_polo", "broadcast received");
            String id = intent.getStringExtra(GeoFenceTransitionService.BROADCAST_EXTRA_ID);
            mmListener.onGeoFence(Integer.parseInt(id));
        }
    }
}
