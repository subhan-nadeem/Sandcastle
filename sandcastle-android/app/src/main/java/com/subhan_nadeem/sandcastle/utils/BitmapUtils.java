package com.subhan_nadeem.sandcastle.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.subhan_nadeem.sandcastle.R;

/**
 * Created by Subhan Nadeem on 2017-10-12.
 * Utilities that help manage bitmaps
 */

public class BitmapUtils {

    public static Bitmap getScaledPinBitmap(Resources resources, int drawable) {
        final int HEIGHT = resources.getDimensionPixelSize(R.dimen.marker_height); //215;
        final int WIDTH = resources.getDimensionPixelSize(R.dimen.marker_width);
        BitmapDrawable bitmapdraw = (BitmapDrawable) resources.getDrawable(drawable);
        Bitmap b = bitmapdraw.getBitmap();
        return Bitmap.createScaledBitmap(b, WIDTH, HEIGHT, false);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
