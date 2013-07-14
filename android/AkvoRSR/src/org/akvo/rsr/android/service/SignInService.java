package org.akvo.rsr.android.service;

import java.net.URL;

import org.akvo.rsr.android.LoginActivity;
import org.akvo.rsr.android.domain.User;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.SettingsUtil;
import org.akvo.rsr.android.xml.Downloader;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


public class SignInService extends IntentService {

	private static final String TAG = "SignInService";

	public SignInService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String username = intent.getStringExtra(ConstantUtil.USERNAME_KEY);
		String password = intent.getStringExtra(ConstantUtil.PASSWORD_KEY);
		
		Intent i2 = new Intent(ConstantUtil.AUTHORIZATION_RESULT_ACTION);

		Downloader dl = new Downloader();
		User user = new User();
		try {
			if (dl.authorize(new URL(ConstantUtil.HOST + ConstantUtil.AUTH_URL),
							username,
							password,
							user)) {
				//Yes!
				SettingsUtil.Write(this, "authorized_username", user.getUsername());
				SettingsUtil.Write(this, "authorized_userid",   user.getId());
				SettingsUtil.Write(this, "authorized_orgid",    user.getOrgId());
				SettingsUtil.Write(this, "authorized_apikey",   user.getApiKey());
				}
			else {
				i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, "Wrong password and/or username");
				LoginActivity.signOut(this);
			}
		}
		catch (Exception e) {
			i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, "Unable to authorize: " + e.getMessage());
			Log.e(TAG,"SignIn() error:",e);
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i2);

	}
}
