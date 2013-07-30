package org.akvo.rsr.android.service;

import org.akvo.rsr.android.domain.User;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.SettingsUtil;
import org.akvo.rsr.android.xml.Downloader;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


public class SubmitProjectUpdateService extends IntentService {

	private static final String TAG = "SubmitProjectUpdateService";

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
		
		Intent i = new Intent(ConstantUtil.UPDATES_SENT_ACTION);

		Downloader dl = new Downloader();
		try {
			dl.sendUnsentUpdates(this, SettingsUtil.host(this) + ConstantUtil.POST_UPDATE_URL + ConstantUtil.API_KEY_PATTERN, sendImg, user);
		} catch (Exception e) {
			i.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, e.getMessage());
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);

	}
}
