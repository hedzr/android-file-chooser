package com.obsez.android.lib.smbfilechooser.internals;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.Spanned;

import java.io.File;
import java.text.DecimalFormat;

import jcifs.smb.SmbFile;

/**
 * Created by coco on 6/7/15. Edited by Guiorgy on 10/09/18.
 */
public class FileUtil {

    @NonNull public static String getExtension(@Nullable final File file) {
        if (file == null) {
            return "";
        }

        int dot = file.getName().lastIndexOf(".");
        if (dot >= 0) {
            return file.getName().substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    @NonNull public static String getExtension(@Nullable final SmbFile file) {
        if (file == null) {
            return "";
        }

        int dot = file.getName().lastIndexOf(".");
        if (dot >= 0) {
            return file.getName().substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    @NonNull public static String getExtensionWithoutDot(@NonNull final File file) {
        String ext = getExtension(file);
        if (ext.length() == 0) {
            return ext;
        }
        return ext.substring(1);
    }

    @NonNull public static String getExtensionWithoutDot(@NonNull final SmbFile file) {
        String ext = getExtension(file);
        if (ext.length() == 0) {
            return ext;
        }
        return ext.substring(1);
    }

    @NonNull public static String getReadableFileSize(final long size) {
        final int BYTES_IN_KILOBYTES = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KILOBYTES = " KB";
        final String MEGABYTES = " MB";
        final String GIGABYTES = " GB";
        float fileSize = 0;
        String suffix = KILOBYTES;

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = size / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    suffix = GIGABYTES;
                } else {
                    suffix = MEGABYTES;
                }
            }
        }
        return String.valueOf(dec.format(fileSize) + suffix);
    }


    public static class NewFolderFilter implements InputFilter{
        private final int maxLength;
        private final String regex;

        public NewFolderFilter() {
            this.maxLength = 255;
            this.regex = "[<>|\\\\:&;#\n\r\t?*~\0-\37]";
        }

        public NewFolderFilter(final int max) {
            this.maxLength = max;
            this.regex = "[<>|\\\\:&;#\n\r\t?*~\0-\37]";
        }

        public NewFolderFilter(@NonNull final String regex) {
            this.maxLength = 255;
            this.regex = regex;
        }

        public NewFolderFilter(final int max, @NonNull final String regex) {
            this.maxLength = max;
            this.regex = regex;
        }

        @Override
        public CharSequence filter(final CharSequence source, final int start, final int end, final Spanned dest, final int dstart, final int dend){
            String filtered;

            int keep = maxLength - (dest.length() - (dend - dstart));
            if (keep <= 0) {
                filtered = "";
            } else if (keep >= end - start) {
                filtered = source.subSequence(start, end).toString();
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
                filtered = source.subSequence(start, keep).toString();
            }

            return filtered.replaceAll(regex, "");
        }
    }

    public static abstract class LightContextWrapper{
        final private Context context;

        public LightContextWrapper(@NonNull final Context context){
            this.context = context;
        }

        @NonNull public Context getBaseContext() {
            return context;
        }

        @NonNull public Resources getResources() {
            return context.getResources();
        }
    }
}
