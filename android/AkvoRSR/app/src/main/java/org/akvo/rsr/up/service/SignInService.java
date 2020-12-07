/*
 *  Copyright (C) 2012-2016 Stichting Akvo (Akvo Foundation)
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
import org.akvo.rsr.up.util.Uploader;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
			User user = Uploader.authorize(new URL(SettingsUtil.host(this) + ConstantUtil.AUTH_URL),
									 		 username,
									 		 password);
			if (user != null) {
				//Yes!
				SettingsUtil.signIn(this, user);
				
				//use project list to set projects visible
				RsrDbAdapter dba = new RsrDbAdapter(this);
				dba.open();
				dba.setVisibleProjects(user.getPublishedProjIds());
				//detailed employment list is short and will be useful on early logins
				new Downloader().fetchEmploymentListPaged(
                            this,
                            dba,
                            new URL(SettingsUtil.host(this) + String.format(ConstantUtil.FETCH_EMPLOYMENTS_URL_PATTERN,SettingsUtil.getAuthUser(this).getId())),
                            null
                    );
				//TODO maybe fetch countries and (minimal)organisations too (if never done before)
                dba.close();
				
			}
			else {
				i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, getResources().getString(R.string.errmsg_signin_denied));
				SettingsUtil.signOut(this);
			}
		}
		catch (Exception e) {
			i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, getResources().getString(R.string.errmsg_signin_failed) + e.getMessage());
			Log.e(TAG,"SignInService() error:", e);
            SettingsUtil.signOut(this); //all fetches have to succeed for a login, to avoid weird states
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i2);

	}
}
