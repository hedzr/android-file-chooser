package com.obsez.android.lib.filechooser.internals;

import android.text.InputFilter;
import android.text.Spanned;

import java.io.File;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static class NewFolderFilter implements InputFilter {
        private final int maxLength;
        private final Pattern pattern;

        public NewFolderFilter() {
            this.maxLength = 255;
            this.pattern = Pattern.compile("^[<>|\\\\:&;#\n\r\t?*~\0-\37]");
        }

        public NewFolderFilter(int maxLength) {
            this.maxLength = maxLength;
            this.pattern = Pattern.compile("^[<>|\\\\:&;#\n\r\t?*~\0-\37]");
        }

        public NewFolderFilter(String pattern) {
            this.maxLength = 255;
            this.pattern = Pattern.compile(pattern);
        }

        public NewFolderFilter(int maxLength, String pattern) {
            this.maxLength = maxLength;
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend){
            Matcher matcher = pattern.matcher(source);
            if (matcher.matches()) {
                return "";
            }

            int keep = maxLength - (dest.length() - (dend - dstart));
            if (keep <= 0) {
                return  "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
                return source.subSequence(start, keep).toString();
            }
        }
    }
}
