package com.obsez.android.lib.filechooser.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import kotlin.jvm.functions.Function0;

/**
 * https://developer.android.com/topic/performance/graphics/manage-memory
 * https://developer.android.com/topic/performance/graphics/load-bitmap
 */
public class BitmapUtil {

    public static void setBitmapTo(@NonNull ImageView iv, Bitmap bitmap) {
        BitmapDrawable bd = (BitmapDrawable) iv.getDrawable();
        if (bd != null) {
            Bitmap bm = bd.getBitmap();
            if (bm != null) {
                bm.recycle();
            }
        }
        iv.setImageBitmap(bitmap);
    }

    /**
     * @param path   bitmap file path
     * @param width  the expect width
     * @param height the expect image height, 0 means kept ratio
     */
    public static Bitmap decodeBitmap(@NonNull String path, int width, int height) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        Bitmap bmp;

        op.inJustDecodeBounds = true;
        bmp = BitmapFactory.decodeFile(path, op);

        int wRatio = (int) Math.ceil((double) op.outWidth / width);
        int hRatio = height == 0 ? wRatio : (int) Math.ceil((double) op.outHeight / height);

        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }
        op.inJustDecodeBounds = false;

        bmp = BitmapFactory.decodeFile(path, op);
        return bmp;
    }

    /**
     * @param is     the stream
     * @param width  the expect image width
     * @param height the expect image height, 0 means kept ratio
     */
    public static Bitmap decodeBitmap(@NonNull Function0<InputStream> is, int width, int height) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        Bitmap bmp;

        InputStream inputStream = is.invoke();
        op.inJustDecodeBounds = true;
        bmp = BitmapFactory.decodeStream(inputStream, null, op);

        int wRatio = (int) Math.ceil((double) op.outWidth / width);
        int hRatio = height == 0 ? wRatio : (int) Math.ceil((double) op.outHeight / height);

        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStream = is.invoke();

        op.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeStream(inputStream, null, op);

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    /**
     * @param path the image path
     */
    public static Bitmap decodeBitmap(String path) {
        return decodeBitmapAuto(path, -1, 128 * 128);
    }

    public static Bitmap decodeBitmapAuto(String path, int minSideLength, int maxNumOfPixels) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);

        opts.inSampleSize = computeSampleSize(opts, minSideLength, maxNumOfPixels);

        opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, opts);
    }

    public static Bitmap decodeBitmap(@NonNull Context c, @NonNull Uri uri, int width, int height) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            bitmap = decodeBitmap(() -> {
                try {
                    return c.getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }, width, height);
            if (bitmap == null) {

                //MediaStore.Images.Thumbnails.MINI_KIND,  // 512 x 384
                //MediaStore.Images.Thumbnails.MICRO_KIND, // 96 x 96
                bitmap = decodeBitmapAuto(() -> {
                    try {
                        return c.getContentResolver().openInputStream(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                }, -1, 512 * 384);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap decodeBitmap(@NonNull Function0<InputStream> is) {
        return decodeBitmapAuto(is, -1, 128 * 128);
    }

    public static Bitmap decodeBitmapAuto(@NonNull Function0<InputStream> is, int minSideLength,
        int maxNumOfPixels) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

        //try {
        //    is.mark(is.available());
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        InputStream inputStream = is.invoke();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, opts);

        opts.inSampleSize = computeSampleSize(opts, minSideLength, maxNumOfPixels);

        //try {
        //    is.reset();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        try {
            inputStream.close();
            inputStream = is.invoke();

            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(inputStream, null, opts);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get bitmap with lowest memory requests
     *
     * @param context normal [Context]
     * @param resId   raw resource id
     */
    public static Bitmap decodeBitmap(@NonNull Context context, @RawRes int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;

        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * @param context normal [Context]
     * @param resId   raw resource id
     */
    public static Bitmap decodeBitmap(@NonNull Context context, @RawRes int resId, int width, int height) {
        InputStream is = context.getResources().openRawResource(resId);

        BitmapFactory.Options op = new BitmapFactory.Options();

        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeStream(is, null, op);

        int wRatio = (int) Math.ceil((double) op.outWidth / width);
        int hRatio = height == 0 ? wRatio : (int) Math.ceil((double) op.outHeight / height);

        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }

        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        is = context.getResources().openRawResource(resId);

        try {
            op.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(is, null, op);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param context          normal [Context]
     * @param fileNameInAssets the asset pathname
     * @param width            image width
     * @param height           the expect image height, 0 means kept ratio
     * @return Bitmap
     */
    public static Bitmap decodeBitmap(@NonNull Context context, String fileNameInAssets, int width, int height)
        throws IOException {

        InputStream is = context.getAssets().open(fileNameInAssets);
        BitmapFactory.Options op = new BitmapFactory.Options();

        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeStream(is, null, op);

        int wRatio = (int) Math.ceil((double) op.outWidth / width);
        int hRatio = height == 0 ? wRatio : (int) Math.ceil((double) op.outHeight / height);

        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }

        is.close();

        is = context.getAssets().open(fileNameInAssets);
        op.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, op);
    }

    /**
     * capture a View as bitmap, you should always run in a non-main thread
     *
     * @return the captured bitmap of view
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();

        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * capture a View as bitmap with canvas tool, you should always run in a non-main thread
     *
     * @param view the source view
     * @return the captured bitmap of view
     */
    public static Bitmap convertViewToBitmapByCanvas(View view) {
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            // does not have background drawable, then draw white background on
            // the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        return returnedBitmap;
    }

    /**
     * to capture the view as a bitmap without compress
     *
     * @param view         the source view
     * @param bitmapWidth  the expect width
     * @param bitmapHeight the expect height
     * @return the captured bitmap
     */
    public static Bitmap convertViewToBitmap(View view, int bitmapWidth, int bitmapHeight) {
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }


    /**
     * @param options        the source [BitmapFactory.Options] and the output inSampleSize
     * @param minSideLength  the prefer side length
     * @param maxNumOfPixels the prefer pixels number
     * @return the prefer inSampleSize inside options
     */
    static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {

        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;

        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
        int maxNumOfPixels) {

        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
            (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));

        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength),
            Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if (maxNumOfPixels == -1 && minSideLength == -1) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}

