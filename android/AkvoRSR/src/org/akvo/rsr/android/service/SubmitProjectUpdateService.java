package org.akvo.rsr.android.service;

import java.net.URL;

import org.akvo.rsr.android.domain.User;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;
import org.akvo.rsr.android.util.SettingsUtil;
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
		boolean sendImg = SettingsUtil.ReadBoolean(this, "setting_send_images", true);
		
		User user = new User();
		user.setUsername(SettingsUtil.Read(this, "authorized_username"));
		user.setId(SettingsUtil.Read(this, "authorized_userid"));
		user.setOrgId(SettingsUtil.Read(this, "authorized_orgid"));
		user.setApiKey(SettingsUtil.Read(this, "authorized_apikey"));
		
		Downloader dl = new Downloader();
		dl.SendUnsentUpdates(this, ConstantUtil.HOST + ConstantUtil.POST_UPDATE_URL + ConstantUtil.API_KEY_PATTERN, sendImg, user);

		//broadcast completion
		Intent i = new Intent(ConstantUtil.UPDATES_SENT_ACTION);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);

	}
}
