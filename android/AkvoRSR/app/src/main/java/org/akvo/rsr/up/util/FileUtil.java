/*
 *  Copyright (C) 2012-2015,2020 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo RSR.
 *
 *  Akvo RSR is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included with this program for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.up.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Location;
import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class FileUtil {

    public static String TAG = "FileUtil";

    /**
     * Get the external app image directory.
     * 
     * @param context The context to use
     * @return The external cache dir
     */
    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }

    /**
     * Get the external app image directory.
     * 
     * @param context The context to use
     * @return The external cache dir
     */
    public static File getExternalPhotoDir(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    @NotNull
    public static String generateImageFile(String name, Context context) {
        return getExternalPhotoDir(context) + File.separator + name + System.nanoTime() + ".jpg";
    }

    /**
     * returns a power-of-two subsampling factor
     * 
     * @param o
     * @param maxSize
     * @return
     */
    public static int subsamplingFactor(BitmapFactory.Options o, int maxSize) {
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp <= maxSize &&
                    height_tmp <= maxSize) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        return scale;
    }

    /**
     * always reads an image file into a bitmap where long edge is no larger
     * than given size
     * 
     * @param filename
     * @param maxSize
     * @return
     */
    public static Bitmap readSubsampledImageFile(String filename, int maxSize) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, o);
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        if (width_tmp < 0 || height_tmp < 0)
            return null;

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = subsamplingFactor(o, maxSize);
        Log.v(TAG, "Shrinking image by a factor of " + o2.inSampleSize);
        return BitmapFactory.decodeFile(filename, o2);
    }

    /**
     * shrinks an image file so long edge becomes exactly maxSize pixels. If
     * already smaller, do nothing unless flag is set. This will lose any EXIF
     * information if it shrinks. Rotation will be normalized (if library is
     * well-written)
     */
    public static boolean shrinkImageFileExactly(String filename, int maxSize, boolean alwaysRewrite) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, o);
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        if (width_tmp < 0 || height_tmp < 0)
            return false; // unreadable
        if (!alwaysRewrite && width_tmp <= maxSize && height_tmp <= maxSize)
            return true; // already good
        // Have to read and shrink it
        // Subsample it first to save memory if it is huge.
        Bitmap bm = readSubsampledImageFile(filename, maxSize * 2); // could
                                                                    // throw
                                                                    // OutOfMemory
        if (bm == null)
            return false; // unreadable

        float width = bm.getWidth(), height = bm.getHeight();
        float xFactor;
        if (width > height) {
            xFactor = maxSize / width;
        } else {
            xFactor = maxSize / height;
        }
        if (xFactor > 1.0f) {
            xFactor = 1.0f; // never enlarge
        }

        int nHeight = (int) (xFactor * height); // preserve aspect ratio
        int nWidth = (int) (xFactor * width);

        Bitmap bm2 = Bitmap.createScaledBitmap(bm, nWidth, nHeight, true);
        File file = new File(filename);
        try {
            FileOutputStream ostream = new FileOutputStream(file);
            bm2.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
        } catch (Exception e) {
            Log.e(TAG, "Could not write resized image: ", e);
            return false;
        }
        return true;
    }

    /**
     * propagates the EXIF geolocation
     * 
     * @param filename
     * @param maxSize
     */
    public static boolean shrinkImageFileExactlyKeepExif(String filename, int maxSize) {
        try {
            ExifInterface exif1 = new ExifInterface(filename);

            if (!shrinkImageFileExactly(filename, maxSize, false))
                return false;
            // TODO: test this
            exif1.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(ExifInterface.ORIENTATION_NORMAL));

            exif1.saveAttributes();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * counts size of all files in the image cache
     */
    public static long countCacheMB(Context context) {
        File f = getExternalCacheDir(context);
        File[] files = f.listFiles();
        long sizeSum = 0;
        if (files != null) { // dir might not exist
            for (int i = 0; i < files.length; i++) {
                sizeSum += files[i].length();
            }
        }
        return sizeSum / (1024 * 1024);
    }

    /**
     * remove all files in the image cache
     */
    public static void clearCache(Context context, boolean showSavings) {
        RsrDbAdapter dba = new RsrDbAdapter(context);
        dba.open();
        dba.clearProjectThumbnailFiles();
        dba.clearUpdateMediaFiles();
        dba.close();
        File f = getExternalCacheDir(context);
        File[] files = f.listFiles();
        if (files != null) { // dir might not exist
            long sizeSum = 0;
            for (int i = 0; i < files.length; i++) {
                sizeSum += files[i].length();
                files[i].delete();
            }
            if (showSavings) {
                Resources res = context.getResources();
                DialogUtil.infoAlert(context, res.getString(R.string.cleared_dialog_title),
                        res.getString(R.string.cleared_dialog_msg, // use
                                                                   // positional
                                                                   // notation
                                files.length, sizeSum / (1024 * 1024)));
            }
        }
    }

    /**
     * rotates the image in a file 90 degrees
     * 
     * @param filename
     * @param clockwise
     * @return
     * @throws IOException
     */
    public static void rotateImageFile(String filename, boolean clockwise) throws IOException {
        // Read image file
        BitmapFactory.Options o = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(filename, o);
        // Rotate it
        Matrix matrix = new Matrix();
        matrix.postRotate(clockwise ? 90 : -90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        // write image file
        try (FileOutputStream ostream = new FileOutputStream(filename)) {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        }
    }

    public static void rotateImageFileKeepExif(String filename, boolean clockwise) throws IOException {
        ExifInterface exif1 = new ExifInterface(filename);
        rotateImageFile(filename, clockwise);
        exif1.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(ExifInterface.ORIENTATION_NORMAL));
        exif1.saveAttributes();
    }

    /**
     * reads the EXIF geolocation
     * 
     * @param originalImage
     * @return location object or null
     */
    public static Location exifLocation(String originalImage) {
        try {
            ExifInterface exif1 = new ExifInterface(originalImage);
            float[] latlon = new float[2];

            if (exif1.getLatLong(latlon)) {
                Location loc = new Location();
                loc.setLatitude(Float.toString(latlon[0]));
                loc.setLongitude(Float.toString(latlon[1]));
                return loc;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * removes the EXIF geolocation, if any
     * 
     * @param fn
     */
    public static void removeExifLocation(String fn) {
        try {
            ExifInterface exif1 = new ExifInterface(fn);
            final String lon1 = exif1.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            final String lat1 = exif1.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if (!TextUtils.isEmpty(lon1) && !TextUtils.isEmpty(lat1)) { // has
                                                                        // location
                exif1.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "0/1,0/1,0/1");
                exif1.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "0/1,0/1,0/1");
                // exif1.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
                exif1.saveAttributes();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
