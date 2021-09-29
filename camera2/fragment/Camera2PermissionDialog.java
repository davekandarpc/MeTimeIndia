package com.metime.camera2.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class Camera2PermissionDialog extends DialogFragment {

    public static final String FRAGMENT_DIALOG = "PermissionDialog";

    public static final int REQUEST_VIDEO_PERMISSIONS = 1;
    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private Fragment mParentFragment;
    private String mRationaleMessage;

    public static Camera2PermissionDialog newInstance(Fragment mParentFragment, String mRationaleMessage) {
        Camera2PermissionDialog f = new Camera2PermissionDialog();
        f.mParentFragment = mParentFragment;
        f.mRationaleMessage = mRationaleMessage;
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(mRationaleMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mParentFragment.requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mParentFragment.getActivity().finish();
                            }
                        })
                .create();
    }

}