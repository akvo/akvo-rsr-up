/*
 *  Copyright (C) 2012-2015 Stichting Akvo (Akvo Foundation)
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

import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.Uploader;

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
			Uploader.sendUpdate(this,
					localUpdateId,
					SettingsUtil.host(this) + ConstantUtil.POST_UPDATE_URL, //+ ConstantUtil.API_KEY_PATTERN,
					SettingsUtil.host(this) + ConstantUtil.VERIFY_UPDATE_PATTERN,
					sendImg,
					user,
					new Downloader.ProgressReporter() {
                        
                        @Override
                        public void sendUpdate(int sofar, int total) {
                            broadcastProgress(0, sofar, total);
                        }
                    });
		} catch (Uploader.FailedPostException e) {
			i.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, e.getMessage());
		}
		catch (Uploader.UnresolvedPostException e) {
			i.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, e.getMessage());
			i.putExtra(ConstantUtil.SERVICE_UNRESOLVED_KEY, true);
		} catch (Exception e) {//TODO: show to user
			Log.e(TAG, "Config problem", e);
		}

		//broadcast completion
	    LocalBroadcastManager.getInstance(this).sendBroadcast(i);

	}

	/**
	 * broadcasts interim progress (primarily back to update editor activity)
	 * @param p Phase, not used here
	 * @param s Progress so far
	 * @param t Target for this phase
	 */
    private void broadcastProgress(int p, int s, int t){
        Intent i1 = new Intent(ConstantUtil.UPDATES_SENDPROGRESS_ACTION);
        i1.putExtra(ConstantUtil.PHASE_KEY, p);
        i1.putExtra(ConstantUtil.SOFAR_KEY, s);
        i1.putExtra(ConstantUtil.TOTAL_KEY, t);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i1);      
    }


}
