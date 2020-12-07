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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;


public class SubmitIpdService extends IntentService {

	private static final String TAG = "SubmitIpdService";

	public SubmitIpdService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int period = Integer.parseInt(intent.getStringExtra(ConstantUtil.PERIOD_ID_KEY));
        String data = intent.getStringExtra(ConstantUtil.DATA_KEY);
        String currentActualValue = intent.getStringExtra(ConstantUtil.CURRENT_ACTUAL_VALUE_KEY);
        boolean relative = intent.getBooleanExtra(ConstantUtil.RELATIVE_DATA_KEY, false);
        String description = intent.getStringExtra(ConstantUtil.DESCRIPTION_KEY);
        String photoFn = intent.getStringExtra(ConstantUtil.PHOTO_FN_KEY);
        String fileFn = intent.getStringExtra(ConstantUtil.FILE_FN_KEY);
		
		Intent i2 = new Intent(ConstantUtil.RESULT_SENT_ACTION);

		try {
			Uploader.sendIndicatorPeriodData(
			        this,
			        ConstantUtil.POST_RESULT_URL,
			        ConstantUtil.IPD_ATTACHMENT_PATTERN,
                    period,
                    data,
                    currentActualValue,
			        relative,
			        description,
			        photoFn,
			        fileFn,
			        SettingsUtil.getAuthUser(this),
			        null);

		}
		catch (Exception e) {
			i2.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, getResources().getString(R.string.errmsg_resultpost_failed) + e.getMessage());
			Log.e(TAG,"SubmitIpdService() error:", e);
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i2);

	}
}
