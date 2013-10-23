package org.akvo.rsr.android.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.widget.ImageView;

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

	//Show a thumbnail from a filename 
	//TODO show different fallback images depending on problem
    // 0 Image!
	// 1 No image set
	// 2 Image not loaded (setting)
	// 3 Image load failed
	// 4 Image loaded, but unreadable
	// 5 Image loaded, but cleared from cache
	public static void setPhotoFile(ImageView imgView, String fn) {
		//Handle taken photo
		if (fn != null && new File(fn).exists()) {
			//DialogUtil.infoAlert(this, "Photo returned", "Got a photo");			
			//make thumbnail and show it on page
			//shrink to save memory
			BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(fn, o);
	        // The new size we want to scale to
	        final int REQUIRED_SIZE = 140;

	        // Find the correct scale value. It should be a power of 2.
	        int width_tmp = o.outWidth, height_tmp = o.outHeight;
	        int scale = 1;
	        while (true) {
	            if (width_tmp / 2 < REQUIRED_SIZE
	               || height_tmp / 2 < REQUIRED_SIZE) {
	                break;
	            }
	            width_tmp /= 2;
	            height_tmp /= 2;
	            scale *= 2;
	        }

	        // Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;			
			
			Bitmap bm = BitmapFactory.decodeFile(fn,o2);
			if (bm != null) {
				imgView.setImageBitmap(bm);
			}
		}

	}
	

}
