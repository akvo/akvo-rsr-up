/*
 *  Copyright (C) 2012-2014 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo RSR.
 *
 *  Akvo RSR is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included with this program for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.up.service;

import java.net.URL;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.SettingsUtil;

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
				i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, getResources().getString(R.string.errmsg_signin_denied));
				SettingsUtil.signOut(this);
			}
		}
		catch (Exception e) {
			i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, getResources().getString(R.string.errmsg_signin_failed) + e.getMessage());
			Log.e(TAG,"SignIn() error:",e);
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i2);

	}
}
