package org.akvo.rsr.android.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.SettingsUtil;
import org.akvo.rsr.android.xml.Downloader;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GetProjectDataService extends IntentService {
	
	private static final String TAG = "GetProjectDataService";
	private final GetProjectDataService myself = this;

	public GetProjectDataService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO get the URL?
		RsrDbAdapter ad = new RsrDbAdapter(this);
		Downloader dl = new Downloader();
		try {
			dl.FetchProjectList(this, new URL(ConstantUtil.HOST+String.format(ConstantUtil.FETCH_PROJ_URL,SettingsUtil.Read(this, "authorized_orgid"))));
			
			//We only get published projects from that URL, so we need to iterate on them and get corresponding updates
			ad.open();
			Cursor c = ad.listAllProjects();
			try {
			while (c.moveToNext()) {
				dl.FetchUpdateList(	this,
								   	new URL(ConstantUtil.HOST+
									"/api/v1/project_update/?format=xml&limit=0&project=" + //TODO move to constants
									c.getString(c.getColumnIndex(RsrDbAdapter.PK_ID_COL)))
									);
			}
			}
			finally {
				if (c != null)
					c.close();
			}
		} catch (Exception e) {
			Log.e(TAG,"Bad fetch:",e);
		}
		//broadcast completion
		Intent i = new Intent(ConstantUtil.PROJECTS_PROGRESS_ACTION);
		i.putExtra(ConstantUtil.SOFAR_KEY, 0);
		i.putExtra(ConstantUtil.TOTAL_KEY, 2);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
		try {
			dl.FetchNewThumbnails(this,
					ConstantUtil.HOST,
					Environment.getExternalStorageDirectory().getPath() + ConstantUtil.IMAGECACHE_DIR,
					new Downloader.ProgressReporter() {
						public void sendUpdate(int sofar, int total) {
							Intent i = new Intent(ConstantUtil.PROJECTS_PROGRESS_ACTION);
							i.putExtra(ConstantUtil.SOFAR_KEY, sofar);
							i.putExtra(ConstantUtil.TOTAL_KEY, total);
						    LocalBroadcastManager.getInstance(myself).sendBroadcast(i);							
						}
					}
					);
		} catch (MalformedURLException e) {
			Log.e(TAG,"Bad thumbnail URL:",e);
		}

		//broadcast completion
		Intent i2 = new Intent(ConstantUtil.PROJECTS_FETCHED_ACTION);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i2);

	}
}
