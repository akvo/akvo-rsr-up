/*
 *  Copyright (C) 2012-2015 Stichting Akvo (Akvo Foundation)
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Location;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class FileUtil {

    public static String TAG = "FileUtil";

    /**
     * Reads a file into a byte array
     * 
     * @param file
     * @return the bytes of the file
     */
    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) {
                throw new IOException("File size >= 2 GB");
            }
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    /**
     * Get the external app image directory.
     * 
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir != null) {
                return cacheDir;
            }
        }

        // Before Froyo we need to construct the external cache dir ourselves
        // AND it will not be automatically created
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Get the external app image directory.
     * 
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalPhotoDir(Context context) {
        if (hasExternalCacheDir()) {
            File cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (cacheDir != null) {
                return cacheDir;
            }
        }

        // Before Froyo we need to construct the external files dir ourselves
        // AND it will not be automatically created
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/files/Pictures";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Checks if OS version has built-in external cache dir method.
     */
    public static boolean hasExternalCacheDir() {
        return hasFroyo();
    }

    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
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
     * shrinks an image file (to save upload bandwidth) the quick way, by a
     * power-of-2 integer factor This will lose any EXIF information
     */
    public static boolean shrinkImageFileQuickly(String filename, int maxSize) {
        Bitmap bm = readSubsampledImageFile(filename, maxSize);
        if (bm == null) {
            return false;
        } else {
            // save it back
            try {
                FileOutputStream stream = new FileOutputStream(filename);
                if (bm.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                    stream.close();
                    return true;
                }
                return false;
            } catch (IOException e) {
                Log.e(TAG, "Could not write resized image: ", e);
                return false;
            }
        }
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
        FileOutputStream ostream = new FileOutputStream(filename);
        try {
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        } finally {
            ostream.close();
        }
    }

    public static void rotateImageFileKeepExif(String filename, boolean clockwise) throws IOException {
        ExifInterface exif1 = new ExifInterface(filename);
        rotateImageFile(filename, clockwise);
        exif1.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(ExifInterface.ORIENTATION_NORMAL));
        exif1.saveAttributes();
    }

    /**
     * propagates the EXIF rotation attribute
     * 
     * @param originalImage
     * @param resizedImage
     */
    public static void propagateOrientation(String originalImage, String resizedImage) {
        try {
            ExifInterface exif1 = new ExifInterface(originalImage);
            ExifInterface exif2 = new ExifInterface(resizedImage);

            final String orientation1 = exif1.getAttribute(ExifInterface.TAG_ORIENTATION);
            final String orientation2 = exif2.getAttribute(ExifInterface.TAG_ORIENTATION);

            if (!TextUtils.isEmpty(orientation1) && !orientation1.equals(orientation2)) {
                Log.d(TAG, "Orientation property in EXIF does not match. Overriding it with original value...");
                exif2.setAttribute(ExifInterface.TAG_ORIENTATION, orientation1);
                exif2.saveAttributes();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

    }

    /**
     * propagates the EXIF geolocation
     * 
     * @param originalImage
     * @param resizedImage
     * @return true if original image had a position and it was copied
     */
    public static boolean propagateLocation(String originalImage, String resizedImage) {
        try {
            ExifInterface exif1 = new ExifInterface(originalImage);
            ExifInterface exif2 = new ExifInterface(resizedImage);

            final String lon1 = exif1.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            final String lat1 = exif1.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            // final String alt1 =
            // exif1.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            // more??

            if (!TextUtils.isEmpty(lon1) && !TextUtils.isEmpty(lat1)) {
                Log.d(TAG, "Location defined. Propagating it.");
                exif2.setAttribute(ExifInterface.TAG_GPS_LATITUDE, lat1);
                exif2.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lon1);
                // exif2.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, alt1);
                exif2.saveAttributes();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
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

            // double altitude =
            // exif1.getAttributeDouble(ExifInterface.TAG_GPS_ALTITUDE, -1);
            // int ref =
            // exif1.getAttributeInt(ExifInterface.TAG_GPS_ALTITUDE_REF, -1);

            if (exif1.getLatLong(latlon)) {
                Location loc = new Location();
                loc.setLatitude(Float.toString(latlon[0]));
                loc.setLongitude(Float.toString(latlon[1]));
                // if (altitude >= 0 && ref >= 0) {
                // loc.setElevation(Double.toString(altitude * ((ref == 1) ? -1
                // : 1)));
                // }
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

    
    /**
     * writes to the error log
     * 
     * @param record the string to append to the log
     */
    public static void appendToLog(Context context, String record) {
        try {
            // Open file
            BufferedWriter buf = new BufferedWriter( new FileWriter(getExternalCacheDir(context) + ConstantUtil.LOG_FILE_NAME));
            try {
                buf.append(record);
            } finally {
                buf.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    
    
}
