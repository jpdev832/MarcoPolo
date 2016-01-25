package com.staticvillage.marcopolo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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

/**
 * Created by joelparrish on 1/1/16.
 */
public class MarkerDialogFragment extends DialogFragment {
    /**
     * Marker Dialog Listener
     */
    public interface MarkerDialogListener {
        public void onMarkerAdd(PointMarker pointMarker);
        public void onMarkerCancel();
    }

    private MarkerDialogListener mListener;
    private LatLng mLatLng;
    private Spinner mLstType;
    private EditText mTxtData;
    private int mRadius;

    /**
     * Set Marker center point
     * @param latLng center point
     */
    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    /**
     * Set Marker trigger radius
     * @param radius trigger radius from center point
     */
    public void setRadius(int radius) {
        mRadius = radius;
    }

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
                    intent.setType("image/*");
                    startActivityForResult(intent, 135642);
                }
            }
        });

        mLstType.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Add Marker")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PointMarker m = new PointMarker();
                        m.setTimestamp(new Date().getTime());
                        m.setLatitude(mLatLng.latitude);
                        m.setLongitude(mLatLng.longitude);
                        m.setType(mLstType.getSelectedItem().toString());
                        m.setData(mTxtData.getText().toString());
                        m.setRadius(mRadius);

                        mListener.onMarkerAdd(m);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onMarkerCancel();
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
                    Log.d("marco_polo", data.getData().toString());
                    mTxtData.setText(data.getData().toString());
                }
        }
    }
}
