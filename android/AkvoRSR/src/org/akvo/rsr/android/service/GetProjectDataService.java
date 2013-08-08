package org.akvo.rsr.android.service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.akvo.rsr.android.UpdateEditorActivity;
import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.FileUtil;
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
		String errMsg = null;
		try {
			dl.fetchProjectList(this, new URL(SettingsUtil.host(this) +
					                          String.format(ConstantUtil.FETCH_PROJ_URL_PATTERN, SettingsUtil.Read(this, "authorized_orgid"))));
			broadcastProgress(0, 100, 100);//For this phase, only whole operation
			
			//We only get published projects from that URL, so we need to iterate on them and get corresponding updates
			ad.open();
			Cursor c = ad.listAllProjects();
			try {
				int i = 0;
				while (c.moveToNext()) {
					i++;
					dl.fetchUpdateList(	this,
									   	new URL(SettingsUtil.host(this) +
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
		} catch (FileNotFoundException e) {
			Log.e(TAG,"Cannot find:",e);
			errMsg = "Cannot find: "+ e.getMessage();
		} catch (Exception e) {
			Log.e(TAG,"Bad fetch:",e);
			errMsg = "Fetch failed: "+ e;
		}
//		progressBroadcast(1, 0, 0);
		
		try {
			dl.fetchNewThumbnails(this,
					SettingsUtil.host(this),
					FileUtil.getExternalFilesDir(this).toString(),
					new Downloader.ProgressReporter() {
						public void sendUpdate(int sofar, int total) {
							Intent intent = new Intent(ConstantUtil.PROJECTS_PROGRESS_ACTION);
							intent.putExtra(ConstantUtil.PHASE_KEY, 2);
							intent.putExtra(ConstantUtil.SOFAR_KEY, sofar);
							intent.putExtra(ConstantUtil.TOTAL_KEY, total);
						    LocalBroadcastManager.getInstance(myself).sendBroadcast(intent);							
						}
					}
					);
		} catch (MalformedURLException e) {
			Log.e(TAG,"Bad thumbnail URL:",e);
			errMsg = "Thumbnail url problem: "+ e;
		}

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
