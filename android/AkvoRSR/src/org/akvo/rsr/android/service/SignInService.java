package org.akvo.rsr.android.service;

import java.net.URL;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.User;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.SettingsUtil;
import org.akvo.rsr.android.xml.Downloader;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


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

		try {
			User user = Downloader.authorize(new URL(SettingsUtil.host(this) + ConstantUtil.AUTH_URL),
									 		 username,
									 		 password);
			if (user != null) {
				//Yes!
				SettingsUtil.signIn(this, user);
				
				//use project list to set projects visible
				RsrDbAdapter dba = new RsrDbAdapter(this);
				dba.open();
				dba.setVisibleProjects(user.getPublishedProjects());
				dba.close();

			}
			else {
				i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, "Wrong password and/or username");
				SettingsUtil.signOut(this);
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
