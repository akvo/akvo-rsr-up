/*
 *  Copyright (C) 2012-2017 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up;

import java.net.MalformedURLException;
import java.net.URL;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
    final String TAG = "SettingsActivity";
    final String HOST_SEED = "https://";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);
		final Preference customPref = (Preference) findPreference(ConstantUtil.HOST_SETTING_KEY);
        customPref.setPersistent(false);
        customPref.setSummary(SettingsUtil.host(this));
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	
			@Override
			public boolean onPreferenceClick(Preference pref) {
				DialogUtil.showAdminAuthDialog(SettingsActivity.this,
				new DialogUtil.AdminAuthDialogListener() {
					
					@Override
					public void onAuthenticated() {
						final EditText inputView = new EditText(SettingsActivity.this);
						inputView.setText(HOST_SEED); //seed the input field 
						inputView.setSelection(HOST_SEED.length());
						DialogUtil.showTextInputDialog(
								SettingsActivity.this,
								R.string.host_dialog_title,
								R.string.host_dialog_msg,
								inputView,
								new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(
											DialogInterface dialog,
											int which) {
										String s = inputView.getText().toString();
										try {
											//make into valid "protocol://host[:port]" URL
											URL u = new URL(s);
											if (u.getHost().contains(" ")) {//common spelling "correction" problem
											    throw new MalformedURLException();
											}
											s = u.getProtocol() + "://" + u.getHost();
											if (u.getPort() >= 0)
												s += ":" + u.getPort();
											//save to preferences
											customPref.setSummary(s);
											SettingsUtil.setHost(SettingsActivity.this,s);
											//clear local database to prevent db mixups
                                            SettingsUtil.WriteLong(SettingsActivity.this, ConstantUtil.FETCH_TIME_KEY, 0L); //forget time of last fetch
											FileUtil.clearCache(SettingsActivity.this, false);
											RsrDbAdapter mDb = new RsrDbAdapter(SettingsActivity.this);
											mDb.open();
									        mDb.clearAllData(); //will confuse open activities
									        mDb.close();
									        if (SettingsUtil.haveCredentials(SettingsActivity.this)) { //if logged in
    									        //Go back to proj list closing all other activities
    									        Intent intent = new Intent(getApplicationContext(), ProjectListActivity.class);
    									        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    									        startActivity(intent);
									        }

											
										} catch (MalformedURLException e) {
											DialogUtil.showConfirmDialog(R.string.error_dialog_title,
																		 R.string.errmsg_bad_url,
																		 SettingsActivity.this);
												if (dialog != null) {
													dialog.dismiss();
												}

										}
										
									}
								});
					}
				});

//				Log.i(TAG,"Click!");
				return true;
			}
		});
        //Ensure user remember version for the feedback form
		String version = "0.0";
		try {
		    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}

        
        Preference feedbackPref = (Preference) findPreference("feedback_form");
		feedbackPref.setPersistent(false);
		feedbackPref.setTitle(getResources().getString(R.string.label_setting_feedback_version,version));

		final Preference ccPref = (Preference) findPreference("clear_cache");
        ccPref.setPersistent(false);
        ccPref.setSummary(getResources().getString(R.string.label_clearcache_freespace,
                        FileUtil.countCacheMB(this)));
        ccPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        	public boolean onPreferenceClick(Preference preference) {
        		FileUtil.clearCache(SettingsActivity.this, true);
                ccPref.setSummary(getResources().getString(R.string.label_clearcache_freespace,
                        FileUtil.countCacheMB(SettingsActivity.this)));
        		return true;
        	}	
        });
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        case R.id.action_diagnostics:
			Intent i3 = new Intent(this, DiagnosticActivity.class);
			startActivity(i3);
            return true;
	    default:
	    	return super.onOptionsItemSelected(item);
	    }

	}


}
