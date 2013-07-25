package org.akvo.rsr.android.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

public class FileUtil {

	public static byte[] readFile(String file) throws IOException {
	        return readFile(new File(file));
	    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
    
/**
* Get the external app image directory.
*
* @param context The context to use
* @return The external cache dir
*/
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir != null) {
                return cacheDir;
            }
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
* Check if OS version has built-in external cache dir method.
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

    /* TODO extend as target version advances
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
*/
}
