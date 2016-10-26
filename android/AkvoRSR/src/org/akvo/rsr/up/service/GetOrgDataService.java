/*
 *  Copyright (C) 2015 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GetOrgDataService extends IntentService {

    private static final String TAG = "GetOrgDataService";
    private static boolean mRunning = false;
    private static final boolean mFetchEmployments = true;
    private static final boolean mFetchOrgs = true;
    private static final boolean mFetchCountries = true;

    public GetOrgDataService() {
        super(TAG);
    }

	
    public static boolean isRunning(Context context) {
	    return mRunning;
    }
	

    /**
     * Fetch all organisations data from server.
     * 
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        mRunning = true;
        RsrDbAdapter ad = new RsrDbAdapter(this);
        Downloader dl = new Downloader();
        String errMsg = null;
        final boolean fetchImages = !SettingsUtil.ReadBoolean(this, "setting_delay_image_fetch", false);
        final boolean brief = true;//TODO put the brief/full flag in the intent
        final String host = SettingsUtil.host(this);
        final Long start = System.currentTimeMillis();
        
        ad.open();
        try {
            if (mFetchOrgs) {
                // Fetch org data.
                try {
                    if (brief) {
                        dl.fetchTypeaheadOrgList(
                                this,
                                ad,
                                new URL(host + ConstantUtil.FETCH_ORGS_TYPEAHEAD_URL),
                                new Downloader.ProgressReporter() {
                                    public void sendUpdate(int sofar, int total) {
                                        Intent intent = new Intent(ConstantUtil.ORGS_PROGRESS_ACTION);
                                        intent.putExtra(ConstantUtil.PHASE_KEY, 0);
                                        intent.putExtra(ConstantUtil.SOFAR_KEY, sofar);
                                        intent.putExtra(ConstantUtil.TOTAL_KEY, total);
                                        LocalBroadcastManager.getInstance(GetOrgDataService.this)
                                                .sendBroadcast(intent);
                                    }
                                }
                        );
                    } else {
                        dl.fetchOrgListRestApiPaged(
                                this,
                                ad,
                                new URL(host + ConstantUtil.FETCH_ORGS_URL),
                                new Downloader.ProgressReporter() {
                                    public void sendUpdate(int sofar, int total) {
                                        Intent intent = new Intent(ConstantUtil.ORGS_PROGRESS_ACTION);
                                        intent.putExtra(ConstantUtil.PHASE_KEY, 0);
                                        intent.putExtra(ConstantUtil.SOFAR_KEY, sofar);
                                        intent.putExtra(ConstantUtil.TOTAL_KEY, total);
                                        LocalBroadcastManager.getInstance(GetOrgDataService.this)
                                                .sendBroadcast(intent);
                                    }
                                }
                        );
                    }
                    //TODO need a way to get this called by the paged fetch: broadcastProgress(0, j, dl.???);
                } catch (Exception e) { // probably network reasons
                    Log.e(TAG, "Bad organisation fetch:", e);
                    errMsg = getResources().getString(R.string.errmsg_org_fetch_failed) + e.getMessage();
                }
            }

            if (mFetchEmployments) {
                // Fetch emp data.
                try {
                    dl.fetchEmploymentListPaged(
                            this,
                            ad,
                            new URL(host + String.format(ConstantUtil.FETCH_EMPLOYMENTS_URL_PATTERN,SettingsUtil.getAuthUser(this).getId())),
                            new Downloader.ProgressReporter() {
                                public void sendUpdate(int sofar, int total) {
                                    Intent intent = new Intent(ConstantUtil.ORGS_PROGRESS_ACTION);
                                    intent.putExtra(ConstantUtil.PHASE_KEY, 0);
                                    intent.putExtra(ConstantUtil.SOFAR_KEY, sofar);
                                    intent.putExtra(ConstantUtil.TOTAL_KEY, total);
                                    LocalBroadcastManager.getInstance(GetOrgDataService.this)
                                            .sendBroadcast(intent);
                                }
                            }
                    );
                    //TODO need a way to get this called by the paged fetch: broadcastProgress(0, j, dl.???);
                } catch (Exception e) { // probably network reasons
                    Log.e(TAG, "Bad employment fetch:", e);
                    errMsg = getResources().getString(R.string.errmsg_emp_fetch_failed) + e.getMessage();
                }
            }
            broadcastProgress(0, 100, 100);
            
            try {
            if (mFetchCountries && ad.getCountryCount() == 0) { // rarely changes, so only fetch countries if we never did that
                dl.fetchCountryListRestApiPaged(this, ad, new URL(SettingsUtil.host(this) +
                        String.format(ConstantUtil.FETCH_COUNTRIES_URL)));
            }
            } catch (Exception e) { // probably network reasons
                Log.e(TAG, "Bad organisation fetch:", e);
                errMsg = getResources().getString(R.string.errmsg_org_fetch_failed) + e.getMessage();
            }

            broadcastProgress(1, 100, 100);

            //logos?
            if (fetchImages) {
                try {
                    dl.fetchMissingThumbnails(this,
                            host,
                            FileUtil.getExternalCacheDir(this).toString(),
                            new Downloader.ProgressReporter() {
                                public void sendUpdate(int sofar, int total) {
                                    Intent intent = new Intent(ConstantUtil.ORGS_PROGRESS_ACTION);
                                    intent.putExtra(ConstantUtil.PHASE_KEY, 2);
                                    intent.putExtra(ConstantUtil.SOFAR_KEY, sofar);
                                    intent.putExtra(ConstantUtil.TOTAL_KEY, total);
                                    LocalBroadcastManager.getInstance(GetOrgDataService.this)
                                            .sendBroadcast(intent);
                                }
                            }
                            );
                } catch (MalformedURLException e) {
                    Log.e(TAG, "Bad thumbnail URL:", e);
                    errMsg = "Thumbnail url problem: " + e;
                }
            }
        } finally {
            if (ad != null)
                ad.close();
        }
        
        Long end = System.currentTimeMillis();
        Log.i(TAG, "Fetch complete in: "+ (end-start)/1000.0);
        
        mRunning = false;

        // broadcast completion
        Intent intent2 = new Intent(ConstantUtil.ORGS_FETCHED_ACTION);
        if (errMsg != null)
            intent2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, errMsg);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);

    }

    private void broadcastProgress(int p, int s, int t) {
        Intent i1 = new Intent(ConstantUtil.ORGS_PROGRESS_ACTION);
        i1.putExtra(ConstantUtil.PHASE_KEY, p);
        i1.putExtra(ConstantUtil.SOFAR_KEY, s);
        i1.putExtra(ConstantUtil.TOTAL_KEY, t);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i1);
    }

}
