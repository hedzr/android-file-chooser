package com.obsez.android.lib.filechooser.permissions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = PermissionActivity.class.getName();

    private String[] toArray(final List<String> list) {
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String[] permissions = intent.getStringArrayExtra(INTENT_EXTRA_PERMISSIONS);
        if (permissions.length == 0) finish();
        _requestCode = intent.getIntExtra(INTENT_EXTRA_REQUEST_CODE, -1);
        if (_requestCode == -1) finish();
        _permissionListener = PermissionsUtil.getPermissionListener(_requestCode);

        for (String permission : permissions) {
            if (permission == null || permission.isEmpty()) {
                throw new RuntimeException("permission can't be null or empty");
            }
            if (ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED) {
                _permissions_granted.add(permission);
            } else {
                _permissions_denied.add(permission);
            }
        }

        if (_permissions_denied.isEmpty()) {
            if (_permissions_granted.isEmpty()) {
                throw new RuntimeException("there are no permissions");
            } else {
                if (_permissionListener != null) _permissionListener.onPermissionGranted(toArray(_permissions_granted));
                finish();
            }
        } else {
            ActivityCompat.requestPermissions(this, toArray(_permissions_denied), _requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != _requestCode) {
            finish();
        }
        _permissions_denied.clear();
        for (int i = permissions.length - 1; i >= 0; --i) {
            if (grantResults[i] == PERMISSION_GRANTED) {
                _permissions_granted.add(permissions[i]);
            } else {
                _permissions_denied.add(permissions[i]);
            }
        }
        if (_permissions_denied.isEmpty()) {
            if (_permissions_granted.isEmpty()) {
                throw new RuntimeException("there are no permissions");
            } else {
                if (_permissionListener != null) _permissionListener.onPermissionGranted(toArray(_permissions_granted));
                finish();
            }
        } else {
            if (_permissionListener != null) _permissionListener.onPermissionDenied(toArray(_permissions_denied));
            finish();
        }
    }

    @Nullable
    private PermissionsUtil.OnPermissionListener _permissionListener;
    public int _requestCode;

    private List<String> _permissions_granted = new ArrayList<>();
    private List<String> _permissions_denied = new ArrayList<>();

    public static final String INTENT_EXTRA_PERMISSIONS = "PERMISSIONS";
    public static final String INTENT_EXTRA_REQUEST_CODE = "REQUEST_CODE";
}
