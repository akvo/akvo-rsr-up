package org.akvo.rsr.android.service;

import java.net.MalformedURLException;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.xml.Downloader;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class GetProjectDataService extends IntentService {
	
	private final String TAG = "GetProjectDataService";

	public GetProjectDataService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO get the URL?
		RsrDbAdapter ad = new RsrDbAdapter(this);
		Downloader dl = new Downloader();
		//TODO THIS MIGHT HANG, no timeout defined...
		dl.FetchProjectList(this,ConstantUtil.HOST,ConstantUtil.FETCH_PROJ_URL);//Akvo projs
		//We only get published projects from that URL, so we need to iterate on them and get corresponding updates
		ad.open();
		Cursor c = ad.listAllProjects();
		while (c.moveToNext()) {
			dl.FetchUpdateList(this,
								ConstantUtil.HOST,
								"/api/v1/project_update/?format=xml&limit=0&project=" + //TODO move to constants
								c.getString(c.getColumnIndex(RsrDbAdapter.PK_ID_COL))
								);
		}
		c.close();
		try {
			dl.FetchNewThumbnails(this, ConstantUtil.HOST, Environment.getExternalStorageDirectory().getPath() + ConstantUtil.IMAGECACHE_DIR);
		} catch (MalformedURLException e) {
			Log.e(TAG,"Bad URL:",e);
		}

		//TODO broadcast completion
		//TODO send a notification?
		
	}
}
