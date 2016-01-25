package com.staticvillage.marcopolo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.staticvillage.marcopolo.model.DataStruct;
import com.staticvillage.marcopolo.model.PointMarker;
import com.staticvillage.marcopolo.model.Response;
import com.staticvillage.marcopolo.utils.ImageHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class EditorActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener, DeleteDialogFragment.DeleteDialogListener,
        UploadDialogFragment.UploadDialogListener, MarkerDialogFragment.MarkerDialogListener{
    private static final int MARKER_COLOR = 0x80FF0000;
    private static final int RADIUS = 50;

    private CoordinatorLayout mCoordinatorLayout;
    private ProgressDialog mProgressDialog;
    private FloatingActionButton mFab;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Location mLastLocation;
    private Circle mLastMarker;
    private LinkedList<PointMarker> mPointMarkerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mPointMarkerList = new LinkedList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Editor");
        setSupportActionBar(toolbar);

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadDialogFragment fragment = new UploadDialogFragment();
                fragment.show(getFragmentManager(), "upload");
            }
        });
        mFab.hide();

        mProgressDialog = ProgressDialog.show(EditorActivity.this, "Load Markers", "Loading...");
        new LoadTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                DeleteDialogFragment deleteDialog = new DeleteDialogFragment();
                deleteDialog.show(getFragmentManager(), "delete");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mMap != null) {
            if(mPointMarkerList.size() > 0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(mPointMarkerList.get(0).getLatitude(),
                                mPointMarkerList.get(0).getLongitude())));
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLastLocation.getLatitude(),
                                mLastLocation.getLongitude()), 16));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect services", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mLastMarker = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(RADIUS)
                .fillColor(MARKER_COLOR));

        MarkerDialogFragment markerDialogFragment = new MarkerDialogFragment();
        markerDialogFragment.setLatLng(latLng);
        markerDialogFragment.setRadius(RADIUS);
        markerDialogFragment.show(getFragmentManager(), "marker");
    }

    @Override
    public void onDeleteMarkers() {
        mProgressDialog = ProgressDialog.show(this, "Delete Markers", "Deleting...");
        new DeleteTask().execute();
    }

    @Override
    public void onMarkerCancel() {
        mLastMarker.remove();
    }

    @Override
    public void onMarkerAdd(PointMarker pointMarker) {
        pointMarker.setMarkerIndex(mPointMarkerList.size());
        pointMarker.save();
        mPointMarkerList.add(pointMarker);

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

    @Override
    public void onUploadDialogPositiveClick(String name) {
        mProgressDialog = ProgressDialog.show(this, "Uploading Markers", "Uploading...");
        new UploadTask(this, name, mPointMarkerList).execute();
    }

    protected void undoMarker() {
        mLastMarker.remove();
        mPointMarkerList.removeLast();
    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        private final Context mmContext;
        private final String mmName;
        private final List<PointMarker> mmPointMarkers;
        private final Gson mmGson;

        public UploadTask(Context context, String name, List<PointMarker> pointMarkers) {
            this.mmContext = context;
            this.mmName = name;
            this.mmPointMarkers = pointMarkers;
            this.mmGson = new Gson();
        }

        private String getRealPathFromURI(Context context, Uri uri){
            String filePath = "";
            String wholeID  = DocumentsContract.getDocumentId(uri);
            String id       = wholeID.split(":")[1];
            String[] column = { MediaStore.Images.Media.DATA };
            String sel      = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{ id }, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        }

        private PointMarker sendImage(PointMarker pointMarker, String name, byte[] byteArray) throws IOException {
            Log.d("marco_polo", "sending");
            URL url = new URL(getString(R.string.post_simage));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataStruct<String> dataStruct =
                    new DataStruct<String>(name, Base64.encodeToString(byteArray, Base64.DEFAULT));

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(mmGson.toJson(dataStruct));
            writer.flush();

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
                Response response = mmGson.fromJson(sb.toString(), Response.class);
                pointMarker.setData(response.getPath());
                Log.d("marco_polo", sb.toString());
            }else{
                pointMarker.setData("");
                Log.d("marco_polo", "failed");
            }

            return pointMarker;
        }

        private void sendMarkers() throws IOException {
            Log.d("marco_polo", "sending");
            URL url = new URL(getString(R.string.post_marker));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            String json = mmGson.toJson(mmPointMarkers);
            Log.d("marco_polo", json);
            DataStruct<String> markerStruct = new DataStruct<>(mmName, json);

            Log.d("marco_polo", mmGson.toJson(markerStruct));

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(mmGson.toJson(markerStruct));
            writer.flush();

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
                Response response = mmGson.fromJson(sb.toString(), Response.class);
                Log.d("marco_polo", sb.toString());
            }else{
                Log.d("marco_polo", "failed");
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            long timestamp = new Date().getTime();

            for(PointMarker pointMarker : mmPointMarkers) {
                try {
                    if(pointMarker.getType().equals("Image")) {
                        Log.d("marco_polo", "Openning image stream");

                        String name = pointMarker.getData()
                                .substring(pointMarker.getData().lastIndexOf("/") + 1)
                                .replace("%3A", "_");

                        String filepath = getRealPathFromURI(mmContext, Uri.parse(pointMarker.getData()));
                        Bitmap bitmap = ImageHelper.decodeBitmap(mmContext, filepath, 360, 203);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte[] byteArray = stream.toByteArray();

                        sendImage(pointMarker, name, byteArray);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                sendMarkers();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            Snackbar.make(mCoordinatorLayout, "Saved Markers", Snackbar.LENGTH_SHORT).show();
        }
    }

    private class LoadTask extends AsyncTask<Void, Void, List<PointMarker>> {
        @Override
        protected List<PointMarker> doInBackground(Void... params) {
            return PointMarker.listAll(PointMarker.class);
        }

        @Override
        protected void onPostExecute(List<PointMarker> pointMarkers) {
            int index = 0;
            for(PointMarker pointMarker : pointMarkers) {
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(pointMarker.getLatitude(), pointMarker.getLongitude()))
                        .radius(RADIUS)
                        .fillColor(MARKER_COLOR));

                pointMarker.setMarkerIndex(index++);
                mPointMarkerList.add(pointMarker);
            }

            if(mPointMarkerList.size() > 0 && mLastLocation != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(mPointMarkerList.get(0).getLatitude(),
                                mPointMarkerList.get(0).getLongitude())));

            Snackbar.make(mCoordinatorLayout, "Loaded Markers", Snackbar.LENGTH_SHORT).show();
            mProgressDialog.dismiss();

            if(mPointMarkerList.size() >= 1 && !mFab.isShown())
                mFab.show();
        }
    }

    private class DeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            PointMarker.deleteAll(PointMarker.class);
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
