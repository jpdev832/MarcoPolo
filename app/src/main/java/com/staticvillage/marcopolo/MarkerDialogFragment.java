package com.staticvillage.marcopolo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;
import com.staticvillage.marcopolo.model.PointMarker;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by joelparrish on 1/1/16.
 */
public class MarkerDialogFragment extends DialogFragment {
    protected static final String ARG_LATITUDE = "latitiude";
    protected static final String ARG_LONGITUDE = "longitude";
    protected static final String ARG_RADIUS = "radius";
    protected static final String ARG_POINT_MARKER = "point_marker";

    /**
     * Create new marker dialog instance
     * @param latitude marker latitude
     * @param longitude marker longitude
     * @param radius geofence radius
     * @return MarkerDialogFragment
     */
    public static MarkerDialogFragment newInstance(double latitude, double longitude, int radius) {
        MarkerDialogFragment fragment = new MarkerDialogFragment();

        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        args.putInt(ARG_RADIUS, radius);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create new marker dialog instance
     * @param pointMarker point marker
     * @return MarkerDialogFragment
     */
    public static MarkerDialogFragment newInstance(PointMarker pointMarker) {
        MarkerDialogFragment fragment = new MarkerDialogFragment();

        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, pointMarker.getLatitude());
        args.putDouble(ARG_LONGITUDE, pointMarker.getLongitude());
        args.putInt(ARG_RADIUS, pointMarker.getRadius());
        args.putParcelable(ARG_POINT_MARKER, pointMarker);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Marker Dialog Listener
     */
    public interface MarkerDialogListener {
        void onMarkerAdd(PointMarker pointMarker);
        void onMarkerCancel(boolean delete);
    }

    private MarkerDialogListener mListener;
    private LatLng mLatLng;
    private Spinner mLstType;
    private EditText mTxtData;
    private EditText mTxtMessage;
    private List<String> mDataItems;
    private PointMarker mPointMarker;
    private int mRadius;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MarkerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        if(args.containsKey(ARG_POINT_MARKER)) {
            mPointMarker = args.getParcelable(ARG_POINT_MARKER);
            mLatLng = new LatLng(mPointMarker.getLatitude(), mPointMarker.getLongitude());
            mRadius = mPointMarker.getRadius();
        } else {
            mLatLng = new LatLng(args.getDouble(ARG_LATITUDE), args.getDouble(ARG_LONGITUDE));
            mRadius = args.getInt(ARG_RADIUS);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.marker_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        View view = getActivity().getLayoutInflater().inflate(R.layout.marker_add, null);

        mLstType = (Spinner) view.findViewById(R.id.lstType);
        mTxtData = (EditText) view.findViewById(R.id.txtData);
        Button btnLink = (Button) view.findViewById(R.id.btnImage);
        btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLstType.getSelectedItem().toString().equals("Image")) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType("image/*");
                    startActivityForResult(intent, 135642);
                }
            }
        });
        mTxtMessage = (EditText) view.findViewById(R.id.txtMessage);

        mLstType.setAdapter(adapter);
        mDataItems = new LinkedList<>();

        if(mPointMarker != null) {
            mLstType.setSelection((mPointMarker.getType().equals("Image")) ? 1 : 0);
            mTxtData.setText("Images selsected: " + mPointMarker.getData().size());
            mTxtMessage.setText(mPointMarker.getMessage());
        }

        //TODO - integrate plan URL option
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Add Marker")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mPointMarker == null)
                            mPointMarker = new PointMarker();
                        mPointMarker.setTimestamp(new Date().getTime());
                        mPointMarker.setLatitude(mLatLng.latitude);
                        mPointMarker.setLongitude(mLatLng.longitude);
                        mPointMarker.setType(mLstType.getSelectedItem().toString());
                        mPointMarker.setData(mDataItems);
                        mPointMarker.setRadius(mRadius);
                        mPointMarker.setMessage(mTxtMessage.getText().toString());

                        mListener.onMarkerAdd(mPointMarker);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onMarkerCancel((mPointMarker == null) ? true : false);
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 135642:
                if(resultCode == Activity.RESULT_OK){
                    if(data.getData() != null) {
                        mDataItems.add(data.getData().toString());
                    } else {
                        ClipData clipData = data.getClipData();
                        Log.d("marco_polo", "Images selsected: " + clipData.getItemCount());
                        mTxtData.setText("Images selsected: " + clipData.getItemCount());

                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            mDataItems.add(clipData.getItemAt(i).getUri().toString());
                        }
                    }
                }
        }
    }
}
