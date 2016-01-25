package com.staticvillage.marcopolo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.staticvillage.marcopolo.model.DataStruct;
import com.staticvillage.marcopolo.model.MarkerData;
import com.staticvillage.marcopolo.model.PointMarker;
import com.staticvillage.marcopolo.model.Response;
import com.staticvillage.marcopolo.net.FileRequest;
import com.staticvillage.marcopolo.net.VolleyRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EditorActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, DeleteDialogFragment.DeleteDialogListener,
        UploadDialogFragment.UploadDialogListener, MarkerDialogFragment.MarkerDialogListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {
    private static final String REQUEST_TAG = "image_request";
    private static final int MARKER_COLOR = 0x80FF0000;
    private static final int RADIUS = 50;

    private CoordinatorLayout mCoordinatorLayout;
    private ProgressDialog mProgressDialog;
    private FloatingActionButton mFab;
    private GoogleMap mMap;
    private RequestQueue mQueue;
    private LinkedList<Circle> mCircleList;
    private LinkedList<Marker> mMarkerList;
    private LinkedList<PointMarker> mPointMarkerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadDialogFragment fragment = new UploadDialogFragment();
                fragment.show(getFragmentManager(), "upload");
            }
        });
        mFab.hide();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Editor");
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCircleList = new LinkedList<>();
        mMarkerList = new LinkedList<>();
        mPointMarkerList = new LinkedList<>();

        mProgressDialog = ProgressDialog.show(EditorActivity.this, "Load Markers", "Loading...");
        new LoadTask().execute();
    }

    /**
     * handle activity break down
     */
    @Override
    protected void onStop() {
        super.onStop();

        if (mQueue != null)
            mQueue.cancelAll(REQUEST_TAG);
    }

    /**
     * Create toolbar menu options
     * @param menu menu
     * @return handled
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_menu, menu);
        return true;
    }

    /**
     * Handle toolbar menu item click
     * @param item menu item clicked
     * @return handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                DeleteDialogFragment deleteDialog = DeleteDialogFragment.newInstance();
                deleteDialog.show(getFragmentManager(), "delete");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handle orientation changes
     * @param newConfig config
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Google map fragment is ready
     * @param googleMap map fragment
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    /**
     * Handle long press on map
     * @param latLng long press location
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        addMarker(latLng);

        MarkerDialogFragment markerDialogFragment =
                MarkerDialogFragment.newInstance(latLng.latitude, latLng.longitude, RADIUS);
        markerDialogFragment.show(getFragmentManager(), "marker");
    }

    /**
     * Delete all geofence markers
     */
    @Override
    public void onDeleteMarkers() {
        mProgressDialog = ProgressDialog.show(this, "Delete Markers", "Deleting...");
        new DeleteTask().execute();
    }

    /**
     * Cancel new marker addition
     * @param delete delete last marker
     */
    @Override
    public void onMarkerCancel(boolean delete) {
        if(delete) {
            int index = mPointMarkerList.size();
            mCircleList.get(index).remove();
            mMarkerList.get(index).remove();

            mCircleList.remove(index);
            mMarkerList.remove(index);
        }
    }

    /**
     * Add new marker
     * @param pointMarker point marker
     */
    @Override
    public void onMarkerAdd(PointMarker pointMarker) {
        if(pointMarker.getMarkerIndex() == -1) {
            pointMarker.setMarkerIndex(mPointMarkerList.size());
            mPointMarkerList.add(pointMarker);
        }

        pointMarker.save();

        for(String data : pointMarker.getData()) {
            MarkerData markerData = new MarkerData();
            markerData.setData(data);
            markerData.setPointMarkerId(pointMarker.getId());
            markerData.save();
        }

        if(mPointMarkerList.size() >= 1 && !mFab.isShown())
            mFab.show();

        Snackbar.make(mCoordinatorLayout, "Undo Marker", Snackbar.LENGTH_INDEFINITE)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoMarker();
                    }
                }).show();
    }

    /**
     * Launch upload task
     * @param name marker set name
     */
    @Override
    public void onUploadDialogPositiveClick(String name) {
        mProgressDialog = ProgressDialog.show(this, "Uploading Markers", "Uploading...");
        new UploadTask(this, name, mPointMarkerList).execute();
    }

    /**
     * Handle marker select
     * @param marker selected marker
     * @return handled
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        int index = mMarkerList.indexOf(marker);
        PointMarker pointMarker = mPointMarkerList.get(index);

        MarkerDialogFragment markerDialogFragment = MarkerDialogFragment.newInstance(pointMarker);
        markerDialogFragment.show(getFragmentManager(), "marker");
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    /**
     * Handle marker drag
     * @param marker marker
     */
    @Override
    public void onMarkerDrag(Marker marker) {
        int index = mMarkerList.indexOf(marker);
        Circle circle = mCircleList.get(index);
        circle.setCenter(marker.getPosition());
    }

    /**
     * Handle marker drag end
     * @param marker marker
     */
    @Override
    public void onMarkerDragEnd(Marker marker) {
        int index = mMarkerList.indexOf(marker);
        Circle circle = mCircleList.get(index);
        circle.setCenter(marker.getPosition());

        PointMarker pointMarker = mPointMarkerList.get(index);
        pointMarker.setLatitude(marker.getPosition().latitude);
        pointMarker.setLongitude(marker.getPosition().longitude);
        pointMarker.save();
    }

    /**
     * Add marker to map
     * @param latLng marker position
     */
    protected void addMarker(LatLng latLng) {
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(RADIUS)
                .fillColor(MARKER_COLOR));

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));

        mCircleList.add(circle);
        mMarkerList.add(marker);
    }

    /**
     * Undo last added marker
     */
    protected void undoMarker() {
        int index = mPointMarkerList.size() - 1;
        mCircleList.get(index).remove();
        mMarkerList.get(index).remove();

        mCircleList.remove(index);
        mMarkerList.remove(index);
        mPointMarkerList.removeLast();
    }

    /**
     * Upload async task
     */
    private class UploadTask extends AsyncTask<Void, Void, Response> {
        private final Context mmContext;
        private final String mmName;
        private final PointMarker[] mmPointMarkers;
        private final Gson mmGson;

        public UploadTask(Context context, String name, List<PointMarker> pointMarkers) {
            this.mmContext = context;
            this.mmName = name;
            this.mmPointMarkers = pointMarkers.toArray(new PointMarker[]{});
            this.mmGson = new Gson();
        }

        /**
         * Send marker images
         * @param file image file
         * @return request response
         * @throws IOException
         */
        private Response sendImage(File file) throws IOException {
            Log.d("marco_polo", "sending");
            String url = mmContext.getString(R.string.post_simage);

            RequestFuture<String> requestFuture = RequestFuture.newFuture();
            FileRequest request = new FileRequest(url, file, requestFuture, requestFuture);
            request.setTag(REQUEST_TAG);

            mQueue = VolleyRequestQueue.getInstance(mmContext).getRequestQueue();
            mQueue.add(request);

            try {
                String responseJson = requestFuture.get(10, TimeUnit.SECONDS);
                return mmGson.fromJson(responseJson, Response.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Send marker set
         * @return request response
         * @throws IOException
         * @throws JSONException
         */
        private Response sendMarkers() throws IOException, JSONException {
            Log.d("marco_polo", "sending");
            String url = getString(R.string.post_marker);
            String pointMarkerJson = mmGson.toJson(mmPointMarkers);

            DataStruct<String> markerStruct = new DataStruct<>(mmName, pointMarkerJson);
            String json = mmGson.toJson(markerStruct);

            RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                    requestFuture, requestFuture);

            mQueue = VolleyRequestQueue.getInstance(mmContext).getRequestQueue();
            mQueue.add(request);

            try {
                JSONObject responseJson = requestFuture.get(10, TimeUnit.SECONDS);
                return mmGson.fromJson(responseJson.toString(), Response.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Crop image
         * @param bitmap image
         * @return cropped image
         */
        private Bitmap cropBitmap(Bitmap bitmap) {
            Bitmap dstBmp;

            if (bitmap.getWidth() >= bitmap.getHeight()) {
                dstBmp = Bitmap.createBitmap(bitmap, bitmap.getWidth()/2 - bitmap.getHeight()/2,
                        0, bitmap.getHeight(), bitmap.getHeight());
            } else {
                dstBmp = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight()/2 - bitmap.getWidth()/2,
                        bitmap.getWidth(), bitmap.getWidth());
            }

            return Bitmap.createScaledBitmap(dstBmp, 300, 300, false);
        }

        @Override
        protected Response doInBackground(Void... params) {
            for(PointMarker pointMarker : mmPointMarkers) {
                try {
                    if(pointMarker.getType().equals("Image")) {
                        Log.d("marco_polo", "Openning image stream");
                        ArrayList<String> points = new ArrayList<>(pointMarker.getData().size());

                        for(String uri : pointMarker.getData()) {
                            String filePath = Uri.parse(uri).getPath();
                            String name = filePath.substring(filePath.lastIndexOf("/") + 1)
                                    .replace("%3A", "_").replace("%2F", "_");

                            ParcelFileDescriptor parcelFileDescriptor =
                                    mmContext.getContentResolver()
                                            .openFileDescriptor(Uri.parse(uri), "r");

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bitmap = cropBitmap(BitmapFactory
                                    .decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor()));

                            File file = new File(getCacheDir(), name);
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            if(!bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream)) {
                                fileOutputStream.close();
                                return null;
                            }
                            fileOutputStream.close();

                            Response response = sendImage(file);

                            if(response == null || response.getStatus_code() >= 300)
                                return response;

                            points.add(response.getPath());
                        }

                        pointMarker.setData(points);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            try {
                return sendMarkers();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Response response) {
            mProgressDialog.dismiss();

            String message;
            if(response == null)
                message = "Error occured while uploading markers";
            else
                message = response.getMessage();

            Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Load markers from DB async task
     */
    private class LoadTask extends AsyncTask<Void, Void, List<PointMarker>> {
        @Override
        protected List<PointMarker> doInBackground(Void... params) {
            List<PointMarker> pointMarkers = PointMarker.listAll(PointMarker.class);

            for (PointMarker pointMarker : pointMarkers) {
                List<MarkerData> markerDataList = MarkerData.find(MarkerData.class, "point_marker_id=?",
                        String.valueOf(pointMarker.getId()));

                for (MarkerData markerData : markerDataList)
                    pointMarker.addData(markerData.getData());
            }

            return pointMarkers;
        }

        @Override
        protected void onPostExecute(List<PointMarker> pointMarkers) {
            int index = 0;
            for(PointMarker pointMarker : pointMarkers) {
                addMarker(new LatLng(pointMarker.getLatitude(), pointMarker.getLongitude()));
                pointMarker.setMarkerIndex(index++);
                mPointMarkerList.add(pointMarker);
            }

            if(mPointMarkerList.size() > 0)
                mMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(mPointMarkerList.get(0).getLatitude(),
                                mPointMarkerList.get(0).getLongitude())));

            mProgressDialog.dismiss();

            if(mPointMarkerList.size() >= 1 && !mFab.isShown())
                mFab.show();

            Snackbar.make(mCoordinatorLayout, "Loaded Markers", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Delete marker set async task
     */
    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            PointMarker.deleteAll(PointMarker.class);
            MarkerData.deleteAll(MarkerData.class);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mMap.clear();
            mPointMarkerList.clear();
            mProgressDialog.dismiss();

            mFab.hide();
            Snackbar.make(mCoordinatorLayout, "Deleted Markers", Snackbar.LENGTH_SHORT).show();
        }
    }
}
