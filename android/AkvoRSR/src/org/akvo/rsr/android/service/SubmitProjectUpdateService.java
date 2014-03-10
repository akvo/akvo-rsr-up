package org.akvo.rsr.android.service;

import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;

import org.akvo.rsr.android.domain.User;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.SettingsUtil;
import org.akvo.rsr.android.xml.Downloader;
import org.akvo.rsr.android.xml.Downloader.PostFailedException;
import org.akvo.rsr.android.xml.Downloader.PostUnresolvedException;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class SubmitProjectUpdateService extends IntentService {

	private static final String TAG = "SubmitProjectUpdateService";

	public SubmitProjectUpdateService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String localUpdateId = intent.getStringExtra(ConstantUtil.UPDATE_ID_KEY);

		boolean sendImg = SettingsUtil.ReadBoolean(this, ConstantUtil.SEND_IMG_SETTING_KEY, true);
		
		User user = SettingsUtil.getAuthUser(this);
		
		Intent i = new Intent(ConstantUtil.UPDATES_SENT_ACTION);

		try {
			Downloader.sendUpdate(this,
					localUpdateId,
					SettingsUtil.host(this) + ConstantUtil.POST_UPDATE_URL + ConstantUtil.API_KEY_PATTERN,
					SettingsUtil.host(this) + ConstantUtil.VERIFY_UPDATE_PATTERN,
					sendImg,
					user);
		} catch (PostFailedException e) {
			i.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, e.getMessage());
		}
		catch (PostUnresolvedException e) {
			i.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, e.getMessage());
			i.putExtra(ConstantUtil.SERVICE_UNRESOLVED_KEY, true);
		} catch (Exception e) {
			Log.e(TAG, "Config problem", e);
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);

	}
}
