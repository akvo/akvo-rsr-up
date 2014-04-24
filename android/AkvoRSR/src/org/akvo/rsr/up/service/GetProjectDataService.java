package org.akvo.rsr.up.service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GetProjectDataService extends IntentService {
	
	private static final String TAG = "GetProjectDataService";
    private static boolean mRunning = false;
    private static final boolean mFetchUsers = true;
    private static final boolean mFetchCountries = true;
    private static final boolean mFetchUpdates = true;

	public GetProjectDataService() {
		super(TAG);
	}
	
	public static boolean isRunning(Context context) {
	    return mRunning;
	    /* this solution uses an interface documented as intended for debug use
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GetProjectDataService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
        */
    }
	
	
	@Override
	protected void onHandleIntent(Intent intent) {

	    mRunning = true;
	    RsrDbAdapter ad = new RsrDbAdapter(this);
		Downloader dl = new Downloader();
		String errMsg = null;
		boolean noimages = SettingsUtil.ReadBoolean(this, "setting_delay_image_fetch", false);
		String host = SettingsUtil.host(this);

        ad.open();
        try {
            try {
			dl.fetchProjectList(this,
			        new URL(SettingsUtil.host(this) +
			                String.format(ConstantUtil.FETCH_PROJ_URL_PATTERN,
			                        SettingsUtil.Read(this, "authorized_orgid"))));
			broadcastProgress(0, 50, 100);
            if (mFetchCountries) {
			//TODO: rarely changes, so only fetch countries if we never did that
                dl.fetchCountryList(this, new URL(SettingsUtil.host(this) +
                        String.format(ConstantUtil.FETCH_COUNTRIES_URL)));
            }
			broadcastProgress(0, 100, 100);
			
	        if (mFetchUpdates) {
    			//We only get published projects from that URL,
    			// so we need to iterate on them and get corresponding updates
    			Cursor c = ad.listAllProjects();
    			try {
    				int i = 0;
    				while (c.moveToNext()) {
    					i++;
    					dl.fetchUpdateList(	this,
    									   	new URL(host +
    										"/api/v1/project_update/?format=xml&limit=0&project=" + //TODO move to constants
    										c.getString(c.getColumnIndex(RsrDbAdapter.PK_ID_COL)))
    										);
    					broadcastProgress(1, i, c.getCount());					
    					}
    				}
    			finally {
    				if (c != null)
    					c.close();
    			}
	        }
    			
		} catch (FileNotFoundException e) {
			Log.e(TAG,"Cannot find:",e);
			errMsg = "Cannot find: "+ e.getMessage();
		} catch (Exception e) {
			Log.e(TAG,"Bad fetch:",e);
			errMsg = "Update Fetch failed: "+ e;
		}
		
		if (mFetchUsers) {
			//Fetch user data for the authors of the updates.
			//This API requires authorization
			User user = SettingsUtil.getAuthUser(this);
			Cursor cursor = ad.listMissingUsers();
            if (cursor != null) {
    			int j = 0;
    			int col = cursor.getColumnIndex(RsrDbAdapter.USER_COL);
    			String key = String.format(Locale.US, ConstantUtil.API_KEY_PATTERN, user.getApiKey(), user.getUsername());
    			while (cursor.moveToNext()) {
    				try { 
    					dl.fetchUser(this,
    								 new URL(host +
    										 String.format(Locale.US, ConstantUtil.FETCH_USER_URL_PATTERN, cursor.getString(col)) +
    										 key),
    								 cursor.getString(col)
    								);
    					j++;
    					}
    				catch (FileNotFoundException e) { //possibly because user is no longer active
    					Log.w(TAG,"Cannot find:" + cursor.getString(col));
    //					errMsg = "Cannot find: "+ e.getMessage(); //not serious
    				} catch (Exception e) { //probably network reasons
    					Log.e(TAG,"Bad fetch:",e);
    					errMsg = "Fetch failed: "+ e;
    				}
    			}
				cursor.close();
			}
			//Log.i(TAG,"Fetched users: " + j);

		}

		broadcastProgress(1, 100, 100);					
			
					
		if (!noimages) {
			try {
				dl.fetchNewThumbnails(this,
						host,
						FileUtil.getExternalCacheDir(this).toString(),
						new Downloader.ProgressReporter() {
							public void sendUpdate(int sofar, int total) {
								Intent intent = new Intent(ConstantUtil.PROJECTS_PROGRESS_ACTION);
								intent.putExtra(ConstantUtil.PHASE_KEY, 2);
								intent.putExtra(ConstantUtil.SOFAR_KEY, sofar);
								intent.putExtra(ConstantUtil.TOTAL_KEY, total);
							    LocalBroadcastManager.getInstance(GetProjectDataService.this).sendBroadcast(intent);							
							}
						}
						);
			} catch (MalformedURLException e) {
				Log.e(TAG,"Bad thumbnail URL:",e);
				errMsg = "Thumbnail url problem: "+ e;
			}
		}
        }
        finally {
            if (ad != null)
                ad.close();
        }

        mRunning = false;

		//broadcast completion
		Intent intent2 = new Intent(ConstantUtil.PROJECTS_FETCHED_ACTION);
		if (errMsg != null)
			intent2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, errMsg);

	    LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);

	}

	
	private void broadcastProgress(int p, int s, int t){
		Intent i1 = new Intent(ConstantUtil.PROJECTS_PROGRESS_ACTION);
		i1.putExtra(ConstantUtil.PHASE_KEY, p);
		i1.putExtra(ConstantUtil.SOFAR_KEY, s);
		i1.putExtra(ConstantUtil.TOTAL_KEY, t);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i1);		
	}

	
}
