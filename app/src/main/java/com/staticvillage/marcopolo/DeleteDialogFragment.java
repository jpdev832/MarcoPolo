package com.staticvillage.marcopolo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by joelparrish.
 */
public class DeleteDialogFragment extends DialogFragment {
    /**
     * Create new instance of fragment
     * @return fragment
     */
    public static DeleteDialogFragment newInstance() {
        return new DeleteDialogFragment();
    }

    /**
     * Delete Dialog Listener
     */
    public interface DeleteDialogListener {
        void onDeleteMarkers();
    }

    private DeleteDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DeleteDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Markers?")
                .setMessage("You will lose all markers!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDeleteMarkers();
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
