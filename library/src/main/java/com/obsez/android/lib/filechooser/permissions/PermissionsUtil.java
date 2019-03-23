package com.obsez.android.lib.filechooser.permissions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.Random;

public final class PermissionsUtil {
    @SuppressWarnings("unused")
    public static final String TAG = PermissionsUtil.class.getName();

    @FunctionalInterface
    public interface CallBack extends Serializable
    {
        void callBack(final String[] permissions);
    }

    public static void checkPermissions(@NonNull Context context, @Nullable final CallBack onPermissionGranted, @Nullable final CallBack onPermissionDenied, final String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.length == 0) {
            if (onPermissionGranted != null) onPermissionGranted.callBack(permissions);
            return;
        }

        int requestCode = _random.nextInt(1024);
        _callBacks.put(requestCode, new Pair<CallBack, CallBack>(onPermissionGranted, onPermissionDenied));

        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra(PermissionActivity.INTENT_EXTRA_PERMISSIONS, permissions);
        intent.putExtra(PermissionActivity.INTENT_EXTRA_REQUEST_CODE, requestCode);
        context.startActivity(intent);
    }

    private static final SparseArray<Pair<CallBack, CallBack>> _callBacks = new SparseArray<>();
    private static final Random _random = new Random();

    public static Pair<CallBack, CallBack> getCallBacks(final int key) {
        Pair<CallBack, CallBack> p = _callBacks.get(key, new Pair<CallBack, CallBack>(null, null));
        _callBacks.remove(key);
        return p;
    }
}
