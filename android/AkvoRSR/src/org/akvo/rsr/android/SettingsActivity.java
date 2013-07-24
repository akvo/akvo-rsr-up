package org.akvo.rsr.android;

import java.net.MalformedURLException;
import java.net.URL;

import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;
import org.akvo.rsr.android.util.SettingsUtil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	final String TAG = "SettingsActivity";

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
						//one line only
						inputView.setSingleLine();
						inputView.setText("http://");//seed input field TODO: change to https when we have that?
						inputView.setSelection(7);
						DialogUtil.showTextInputDialog(
								SettingsActivity.this,
								R.string.host_dialog_title,
								R.string.host_dialog_text,
								inputView,
								new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(
											DialogInterface dialog,
											int which) {
										//String s = StringUtil.ControlToSPace(inputView.getText().toString());
										String s = inputView.getText().toString();
										try {
											//make into valid "protocol://host[:port]" URL
											URL u = new URL(s);
											s = u.getProtocol()+"://"+u.getHost();
											if (u.getPort() >= 0)
												s += ":" + u.getPort();
											customPref.setSummary(s);
											SettingsUtil.Write(SettingsActivity.this,
													ConstantUtil.HOST_SETTING_KEY,
													s);
											
										} catch (MalformedURLException e) {
											DialogUtil.showConfirmDialog(R.string.error_title,
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
