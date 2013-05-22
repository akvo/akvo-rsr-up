package org.akvo.rsr.android.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.xml.Downloader;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GetProjectDataService extends IntentService {
	
	private static final String TAG = "GetProjectDataService";

	public GetProjectDataService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO get the URL?
		RsrDbAdapter ad = new RsrDbAdapter(this);
		Downloader dl = new Downloader();
		try {
			dl.FetchProjectList(this,new URL(ConstantUtil.HOST+ConstantUtil.FETCH_PROJ_URL));//Akvo projs
			
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
		try {
			dl.FetchNewThumbnails(this, ConstantUtil.HOST, Environment.getExternalStorageDirectory().getPath() + ConstantUtil.IMAGECACHE_DIR);
		} catch (MalformedURLException e) {
			Log.e(TAG,"Bad thumbnail URL:",e);
		}

		//broadcast completion
		Intent i = new Intent(ConstantUtil.PROJECTS_FETCHED_ACTION);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);

	}
}
