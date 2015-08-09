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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ThumbnailUtil {

    public static String TAG = "ThumbnailUtil";

    private static Hashtable<String, Bitmap> cache;

    public void initCache() {
        cache = new Hashtable<String, Bitmap>(10);
    }

    /**
     * Shows a thumbnail from a URL and a filename
     * 
     * @param imgView where to show it
     * @param url where to fetch it from
     * @param fn filename in the image cache
     * @param projectId non-null if this is a Project
     * @param updateId non/null if this is an Update show different fallback
     *            images depending on case: 0 Image good and shown 1 No image
     *            set 2 Image not loaded (setting) 3 Image load failed
     *            (currently treated as 2, would need to remember fetch sts) 4
     *            Image loaded, but unreadable 5 Image loaded, but cleared from
     *            cache (should be treated as 2)
     */
    public static void setPhotoFile(
            ImageView imgView,
            String url,
            String fn,
            String projectId,
            String updateId,
            boolean enableExpand) {

        if (url == null) { // not set
            imgView.setImageResource(R.drawable.thumbnail_noimage);
        } else if (fn == null) { // Not fetched
            imgView.setImageResource(R.drawable.thumbnail_load);
            // set tags so we will know what to load on a click
            if (projectId != null || updateId != null) {
                imgView.setTag(R.id.thumbnail_url_tag, url);
                imgView.setTag(R.id.thumbnail_expandable_tag, new Boolean(enableExpand));
                // remember so we can update db when clicked
                imgView.setTag(R.id.project_id_tag, projectId);
                imgView.setTag(R.id.update_id_tag, updateId);
                // make it clickable
                imgView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DoLateIconLoad((ImageView) v);
                    }
                });
            }

        } else { // in cache directory, try to display it
            File f = new File(fn);
            if (!f.exists()) { // cache corruption
                imgView.setImageResource(R.drawable.thumbnail_error);
                enableExpand = false;
            } else { // have file
                // DialogUtil.infoAlert(this, "Photo returned", "Got a photo");
                // make thumbnail and show it on page
                // shrink to save memory
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(fn, o);
                // The new size we want to scale to
                final int REQUIRED_SIZE = 320; // 640/2
                // int portSize = imgView.getWidth(); //sometimes returns 0
                // if (imgView.getHeight() > portSize) portSize =
                // imgView.getHeight();
                // Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = subsamplingFactor(o, REQUIRED_SIZE);
                // o2.inDensity = DisplayMetrics.DENSITY_MEDIUM;
                o2.inScaled = true;

                Bitmap bm = BitmapFactory.decodeFile(fn, o2);
                if (bm == null) {
                    imgView.setImageResource(R.drawable.thumbnail_error);
                } else {
                    imgView.setImageBitmap(bm);
                }
            }
            // now fetched, (maybe late)
            // clean out hints from this ImageView
            imgView.setOnClickListener(null);
            imgView.setClickable(false);
            imgView.setTag(R.id.thumbnail_url_tag, null);
            imgView.setTag(R.id.thumbnail_expandable_tag, null);
            imgView.setTag(R.id.project_id_tag, null);
            imgView.setTag(R.id.update_id_tag, null);
            if (projectId != null && updateId != null) { // late
                // TODO: how to update the list cursor content?
                // Otherwise we need a URL-filename lookaside list
            }

            imgView.setTag(R.id.thumbnail_fn_tag, fn);
            if (enableExpand) {
                // make it clickable
                imgView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFullImageDefaultViewer((ImageView) v);
                    }
                });
            }
        }
    }

    /**
     * returns a power-of-two subsampling factor
     * 
     * @param o
     * @param maxSize
     * @return
     */
    private static int subsamplingFactor(BitmapFactory.Options o, int maxSize) {
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
     * fetches and displays a delayed-load icon when clicked
     */
    static void DoLateIconLoad(final ImageView iv) {
        // fetch *must* happen in another thread than main on Android API 11 and
        // later
        new Thread(new Runnable() {
            public void run() {

                try {
                    final String url = (String) iv.getTag(R.id.thumbnail_url_tag);
                    final String pid = (String) iv.getTag(R.id.project_id_tag);
                    final String uid = (String) iv.getTag(R.id.update_id_tag);
                    final Boolean enableExpansion = (Boolean) iv.getTag(R.id.thumbnail_expandable_tag);

                    URL curl = new URL(SettingsUtil.host(iv.getContext()));
                    String directory = FileUtil.getExternalCacheDir(iv.getContext()).toString();

                    if (url == null || (pid == null && uid == null)) {
                        Log.w(TAG, "Insufficient data for late load ");
                    } else {
                        RsrDbAdapter dba = new RsrDbAdapter(iv.getContext());
                        dba.open();
                        final String fn;
                        if (pid != null) {
                            fn = Downloader.httpGetToNewFile(new URL(curl, url), directory, "prj" + pid + "_");
                            dba.updateProjectThumbnailFile(pid, fn);
                        } else if (uid != null) {
                            fn = Downloader.httpGetToNewFile(new URL(curl, url), directory, "upd" + uid + "_");
                            dba.updateUpdateThumbnailFile(uid, fn);
                        } else {
                            fn = null;
                        }
                        dba.close();
                        // if (fn != null) { //remember fn for use before list
                        // cursor updates
                        // iv.setTag(R.id.thumbnail_fn_tag, fn);
                        // }

                        // post UI work back to main thread
                        iv.post(new Runnable() {
                            public void run() {
                                setPhotoFile(iv, url, fn, null, null, enableExpansion!= null && enableExpansion.booleanValue());
                                // nulls prevent infinite recursion
                            }
                        });

                    }
                } catch (Exception e) {
                    iv.post(new Runnable() {
                        public void run() {
                            iv.setImageResource(R.drawable.thumbnail_error); // boo!
                        }
                    });
                    // DialogUtil.errorAlert(ctx,
                    // "Error fetching proj image from URL " + url, e);
                    Log.e(TAG, "DoLateIconLoad Error", e);
                }
            }
        }).start();

    }

    /**
     * ask OS to show the full-size image when clicked - probably handled by
     * Gallery app
     */
    static void showFullImageDefaultViewer(ImageView iv) {
        final String fn = (String) iv.getTag(R.id.thumbnail_fn_tag);

        // Launch default viewer for the file
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri hacked_uri = Uri.parse("file://" + fn);
        intent.setDataAndType(hacked_uri, "image/*");
        iv.getContext().startActivity(intent);

    }

}
