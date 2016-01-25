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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
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
import com.staticvillage.marcopolo.net.VolleyRequestQueue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private ImageTask mImageTask;
    private Handler mHandler;
    private Runnable mRunnable;
    private RequestQueue mQueue;
    private int mCurrentId;
    private int mZoom;
    private int mTilt;
    private int mImageDelay;
    private int mUrlDelay;
    private int mGeoFenceEnterDelay;
    private int mGeoFenceInterval;
    private int mGeoFenceFastInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence);

        mPoloView = (PoloView) findViewById(R.id.imgMarker);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPointMarkerList       = new LinkedList<>();
        mGeoFences             = new LinkedList<>();
        mHandler               = new Handler();
        mDrawer                = new PoloDrawer(this);
        mBroadcastIntentFilter = new IntentFilter(GeoFenceTransitionService.BROADCAST_ACTION);
        mZoom                  = getResources().getInteger(R.integer.geofence_zoom_level);
        mTilt                  = getResources().getInteger(R.integer.geofence_tilt_level);
        mImageDelay            = getResources().getInteger(R.integer.image_delay);
        mUrlDelay              = getResources().getInteger(R.integer.url_delay);
        mGeoFenceEnterDelay    = getResources().getInteger(R.integer.geofence_enter_delay);
        mGeoFenceInterval      = getResources().getInteger(R.integer.geofence_interval);
        mGeoFenceFastInterval  = getResources().getInteger(R.integer.geofence_fast_interval);

        mPoloView.setDrawer(mDrawer);

        Log.d("marco_polo", getIntent().getData().toString());
        createLocationRequest();
    }

    /**
     * Connect to services
     */
    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
        mReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mBroadcastIntentFilter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Remove callbacks
     */
    @Override
    protected void onPause() {
        super.onPause();

        if(mRunnable != null)
            mHandler.removeCallbacks(mRunnable);

        if(mImageTask != null) {
            mImageTask.cancel(true);
        }
    }

    /**
     * Handle activity break down
     */
    @Override
    protected void onStop() {
        super.onStop();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mGoogleApiClient.disconnect();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    /**
     * Handle orientation changes
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Google map service ready
     * @param googleMap map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(false);
    }

    /**
     * Google API client connected
     * @param bundle bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        mProgressDialog = ProgressDialog.show(this, "Download Marker", "Downloading Markers...");
        new DownloadTask(this, getIntent().getData().toString()).execute();

        LocationServices
                .FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Google API client connection suspended
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Google API client connection failed
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Geofence register result
     * @param status status
     */
    @Override
    public void onResult(Status status) {
        if(!status.isSuccess())
            Toast.makeText(this, "Failed to initialize geofences", Toast.LENGTH_SHORT).show();
    }

    /**
     * Geofence triggered
     * @param id marker id of triggered event
     */
    @Override
    public void onGeoFence(int id) {
        if(++id >= mPointMarkerList.size())
            return;

        mCurrentId = id;
        PointMarker pointMarker = mPointMarkerList.get(id);
        addMarker(pointMarker, pointMarker.getMessage());

        if(mImageTask != null)
            mImageTask.cancel(true);

        if(pointMarker.getType().equals("URL")) {
            final String uri = pointMarker.getData().get(0);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Intent launcher = new Intent( Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(launcher);
                }
            };

            mHandler.postDelayed(mRunnable, mUrlDelay);
            return;
        }

        mImageTask = new ImageTask(pointMarker.getData());
        mImageTask.execute();
    }

    /**
     * User location updated
     * @param location new location
     */
    @Override
    public void onLocationChanged(Location location) {
        updatePosition(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    /**
     * Create Geofence Location Request
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mGeoFenceInterval);
        mLocationRequest.setFastestInterval(mGeoFenceFastInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Get pending intent to handle geofence triggers
     * @return pending intent
     */
    protected PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeoFenceTransitionService.class);
        mGeofencePendingIntent = PendingIntent
                .getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * Create geofence from point marker
     * @param pointMarker point marker
     * @return Geofence
     */
    protected Geofence getGeoFence(PointMarker pointMarker) {
        return new Geofence.Builder()
                .setRequestId(String.valueOf(pointMarker.getMarkerIndex()))
                .setCircularRegion(pointMarker.getLatitude(), pointMarker.getLongitude(),
                        pointMarker.getRadius())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    /**
     * Get geofence request
     * @return geofence request
     */
    protected GeofencingRequest getGeoFencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFences);
        return builder.build();
    }

    /**
     * Add geofences to monitor
     */
    protected void addGeoFences() {
        if(mGeoFences.size() > 0) {
            LocationServices
                    .GeofencingApi
                    .addGeofences(mGoogleApiClient,
                            getGeoFencingRequest(),
                            getGeofencePendingIntent())
                    .setResultCallback(this);
        }
    }

    /**
     * Load image for point marker
     * @param url image url
     */
    protected void loadImage(String url) {
        Glide.with(this)
                .load(url)
                .asBitmap()
                .dontAnimate()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if(mCurrentId != 0)
                            mDrawer.setMoment(resource);
                    }
                });
    }

    /**
     * Update map information based on markers and user locations
     * @param userLatLng user location
     */
    protected void updatePosition(LatLng userLatLng) {
        if(mMarker == null)
            return;

        if(mPolyLine != null)
            mPolyLine.remove();

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(userLatLng)
                .add(mMarker.getPosition());

        mPolyLine = mMap.addPolyline(polylineOptions);

        double bearing = SphericalUtil.computeHeading(userLatLng, mMarker.getPosition());
        mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(userLatLng)
                        .bearing((float) bearing)
                        .zoom(mZoom)
                        .tilt(mTilt)
                        .build()));
    }

    /**
     * Add new marker to map
     * @param pointMarker point marker
     * @param message marker message
     */
    protected void addMarker(PointMarker pointMarker, String message) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng markerLatLng = new LatLng(pointMarker.getLatitude(), pointMarker.getLongitude());
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(markerLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.polo_marker)));

        updatePosition(userLatLng);

        mDrawer.setMessage(message);
    }

    /**
     * Async task for downloading marker set file
     */
    private class DownloadTask extends AsyncTask<Void, Void, List<PointMarker>> {
        private final Context mmContext;
        private final String mmMarkerUrl;
        private final Gson mmGson;

        public DownloadTask(Context context, String url) {
            this.mmContext = context;
            this.mmMarkerUrl = url;
            this.mmGson = new Gson();
        }

        @Override
        protected List<PointMarker> doInBackground(Void... params) {
            Log.d("marco_polo", "retrieving markers");

            RequestFuture<String> requestFuture = RequestFuture.newFuture();
            StringRequest request = new StringRequest(Request.Method.GET, mmMarkerUrl, requestFuture,
                    requestFuture);

            mQueue = VolleyRequestQueue.getInstance(mmContext).getRequestQueue();
            mQueue.add(request);

            try {
                String responseJson = requestFuture.get(10, TimeUnit.SECONDS);
                List<PointMarker> response = mmGson.fromJson(responseJson,
                        new TypeToken<List<PointMarker>>(){}.getType());
                return response;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<PointMarker> pointMarkerList) {
            mProgressDialog.dismiss();

            if(pointMarkerList == null || pointMarkerList.size() == 0) {
                finish();
            }
            mPointMarkerList = pointMarkerList;

            if(mPointMarkerList.size() > 0) {
                for(PointMarker m : mPointMarkerList) {
                    mGeoFences.add(getGeoFence(m));
                }

                addGeoFences();
                addMarker(mPointMarkerList.get(0), mPointMarkerList.get(0).getMessage());
            }
        }
    }

    /**
     * Sequentially display marker images with delay
     */
    private class ImageTask extends AsyncTask<Void, String, Void> {
        private final List<String> mmImages;

        public ImageTask(List<String> images) {
            mmImages = images;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for(String image : mmImages) {
                publishProgress(image);
                try {
                    Thread.sleep(mImageDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            loadImage(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mImageTask = null;
        }
    }

    /**
     * Receive geofence trigger broadcasts
     */
    private class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("marco_polo", "broadcast received");
            final String id = intent.getStringExtra(GeoFenceTransitionService.BROADCAST_EXTRA_ID);

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    onGeoFence(Integer.parseInt(id));
                }
            };

            mHandler.postDelayed(mRunnable, mGeoFenceEnterDelay);
        }
    }
}
