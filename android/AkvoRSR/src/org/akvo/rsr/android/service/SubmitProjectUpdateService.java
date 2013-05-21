package org.akvo.rsr.android.service;

import java.net.URL;

import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;
import org.akvo.rsr.android.xml.Downloader;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;



public class SubmitProjectUpdateService extends IntentService {

	private static final String TAG = "SubmitProjectDataService";

	public SubmitProjectUpdateService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Downloader dl = new Downloader();
		try {
			dl.SendUnsentUpdates(this, new URL(ConstantUtil.HOST + ConstantUtil.POST_UPDATE_URL + ConstantUtil.TEST_API_KEY));
		} catch (Exception e) {
			Log.e(TAG,"Error making post URL",e);
		}

		//broadcast completion
		Intent i = new Intent(ConstantUtil.UPDATES_SENT_ACTION);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);

	}
}
