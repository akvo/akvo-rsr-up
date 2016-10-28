/*
 *  Copyright (C) 2016 Stichting Akvo (Akvo Foundation)
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

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.Uploader;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class SubmitEmploymentService extends IntentService {

	private static final String TAG = "SubmitEmploymentService";

	public SubmitEmploymentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        int orgId = Integer.parseInt(intent.getStringExtra(ConstantUtil.ORG_ID_KEY));
        int countryId = intent.getStringExtra(ConstantUtil.COUNTRY_ID_KEY)==null?-1:Integer.parseInt(intent.getStringExtra(ConstantUtil.COUNTRY_ID_KEY));
        String jobTitle = intent.getStringExtra(ConstantUtil.JOB_TITLE_KEY);
		
		Intent i2 = new Intent(ConstantUtil.EMPLOYMENT_SENT_ACTION);

		try {
			Uploader.postEmployment(
			        this,
			        SettingsUtil.host(this)+ConstantUtil.POST_EMPLOYMENT_PATTERN,
                    orgId,
                    countryId,
                    jobTitle,
			        SettingsUtil.getAuthUser(this)
			        );

		}
		catch (Exception e) {
			i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, getResources().getString(R.string.errmsg_emp_application_failed) + e.getMessage());
			Log.e(TAG,"SubmitEmploymentService() error:", e);
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i2);

	}
}
