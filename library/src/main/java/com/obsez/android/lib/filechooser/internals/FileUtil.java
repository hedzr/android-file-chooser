package com.obsez.android.lib.filechooser.internals;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by coco on 6/7/15.
 */
public class FileUtil {


    public static String getExtension(File file) {
        if (file == null) {
            return null;
        }

        int dot = file.getName().lastIndexOf(".");
        if (dot >= 0) {
            return file.getName().substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    public static String getExtensionWithoutDot(File file) {
        String ext = getExtension(file);
        if (ext.length() == 0) {
            return ext;
        }
        return ext.substring(1);
    }

    public static String getReadableFileSize(long size) {
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

}
