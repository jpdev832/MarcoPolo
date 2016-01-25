package com.staticvillage.marcopolo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Created by joelparrish on 12/30/15.
 */
public class UploadDialogFragment extends DialogFragment {
    /**
     * Upload Dialog Listener
     */
    public interface UploadDialogListener {
        public void onUploadDialogPositiveClick(String name);
    }

    private UploadDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (UploadDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.marker_upload, null);
        final EditText txtName = (EditText) view.findViewById(R.id.txtName);

        builder.setView(view)
                .setTitle("Upload Markers")
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onUploadDialogPositiveClick(txtName.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
