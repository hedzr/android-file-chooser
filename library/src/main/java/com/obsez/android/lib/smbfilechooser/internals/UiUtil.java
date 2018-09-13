package com.obsez.android.lib.smbfilechooser.internals;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import java.util.List;

public final class UiUtil {

    public static float dip2px(final float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (dipValue * scale + 0.5f);
    }

    public static int px2dip(final int pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    @Nullable public static Drawable resolveFileTypeIcon(@NonNull final Context ctx, @NonNull final Uri fileUri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(fileUri);
        intent.setType(getMimeType(ctx, fileUri));

        final PackageManager pm = ctx.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
        //noinspection LoopStatementThatDoesntLoop
        for (ResolveInfo match : matches) {
            //final CharSequence label = match.loadLabel(pm);
            return match.loadIcon(pm);
        }
        return null; //ContextCompat.getDrawable(ctx, R.drawable.ic_file);
    }

    @Nullable public static String getMimeType(@NonNull final Context ctx, @NonNull final Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = ctx.getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static int getThemeAccentColor(@NonNull final Context context) {
        int colorAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAttr = android.R.attr.colorAccent;
        } else {
            //Get colorAccent defined for AppCompat
            colorAttr = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }

    public static void hideKeyboard(@NonNull final Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it.
        if (view == null) view = new View(activity);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboardFrom(@NonNull final Context context, @NonNull final View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
