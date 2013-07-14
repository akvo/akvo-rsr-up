/*
 *  Copyright (C) 2012-2013 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo RSR.
 *
 *  Akvo RSR is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.android;

import java.io.File;

import org.akvo.rsr.android.service.SignInService;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;
import org.akvo.rsr.android.util.SettingsUtil;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private static final String TAG = "LoginActivity";
	private EditText usernameEdit;
	private EditText passwordEdit;
	private	ProgressDialog progress = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log.i(TAG, "External storage: mounted ");
			File f = new File (Environment.getExternalStorageDirectory().getPath() + ConstantUtil.PHOTO_DIR);
			if (f.mkdirs() || f.isDirectory() ) {
				Log.i(TAG, "Found/created photo dir " + f.getAbsolutePath());
			} else
				Log.e("LoginActivity", "could not find/create photo dir");
			f = new File (Environment.getExternalStorageDirectory().getPath() + ConstantUtil.IMAGECACHE_DIR);
			if (f.mkdirs() || f.isDirectory()) {
				Log.i(TAG, "Found/created image cache dir " + f.getAbsolutePath());
			} else
				Log.e("LoginActivity", "could not find/create image cache dir");
		} else {
			DialogUtil.errorAlert(this, "No storage available", "Akvo RSR requires a mounted storage card for image files. Mount card and restart app.");
			
		}
		
		setContentView(R.layout.activity_login);

		usernameEdit = (EditText) findViewById(R.id.edit_username);
		passwordEdit = (EditText) findViewById(R.id.edit_password);
        final Button button = (Button) findViewById(R.id.btn_login);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	signIn(v);
            }
        });

        final TextView forgot = (TextView) findViewById(R.id.link_to_forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ConstantUtil.HOST + ConstantUtil.PWD_URL));
                startActivity(myIntent);                
            }
        });

        final TextView about = (TextView) findViewById(R.id.link_to_about);
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showAbout();
            }
        });
        
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (haveCredentials()) { //skip login
		    Intent intent = new Intent(this, ProjectListActivity.class);
		    startActivity(intent);
		} else {
			passwordEdit.setText("");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
            return true;
        case R.id.action_about:
        	showAbout();
            return true;
	    default:
	    	return super.onOptionsItemSelected(item);
	    }

	}

	private void showAbout(){
		Intent i3 = new Intent(this, AboutActivity.class);
		startActivity(i3);
	}
	
    //Sign In button pushed
    public void signIn(View view) {
		//start a "progress" animation
    	progress = new ProgressDialog(this);
		progress.setTitle("Signing in");
		progress.setMessage("Sending login information");
		progress.show();
    	
    	//request API key from server
		//register a listener for the completion intent
		IntentFilter f = new IntentFilter(ConstantUtil.AUTHORIZATION_RESULT_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new ResponseReceiver(),
                f);
		
		Intent intent = new Intent(this, SignInService.class);
		intent.putExtra(ConstantUtil.USERNAME_KEY, usernameEdit.getText().toString());
		intent.putExtra(ConstantUtil.PASSWORD_KEY, passwordEdit.getText().toString());
		getApplicationContext().startService(intent);
		//now we wait for a broadcast...

    }

    
    public static void signOut(Context c) {
    	//destroy credentials
		SettingsUtil.Write(c, "authorized_username", "");
		SettingsUtil.Write(c, "authorized_userid",   "");
		SettingsUtil.Write(c, "authorized_orgid",    "");
		SettingsUtil.Write(c, "authorized_apikey",   "");
    }

    
    public boolean haveCredentials() {
    	String u = SettingsUtil.Read(this, "authorized_username");
		String i = SettingsUtil.Read(this, "authorized_userid");
		String o = SettingsUtil.Read(this, "authorized_orgid");
		String k = SettingsUtil.Read(this, "authorized_apikey");
		return u != null && !u.equals("")
			&& i != null && !i.equals("")
			&& o != null && !o.equals("")
			&& k != null && !k.equals("");
    }

    
    //Auth now done in service
	private void onAuthFinished(Intent i) {
		// Dismiss any in-progress dialog
		if (progress != null)
			progress.dismiss();

		String err = i.getStringExtra(ConstantUtil.SERVICE_ERRMSG_KEY);
		if (err == null) {
			Toast.makeText(getApplicationContext(), "Logged in as " + SettingsUtil.Read(this, "authorized_username"), Toast.LENGTH_SHORT).show();
			//Go to main screen
		    Intent intent = new Intent(this, ProjectListActivity.class);
		    startActivity(intent);
		} else {
			passwordEdit.setText("");
			//Let user keep username
			//stay on this page
			DialogUtil.errorAlert(this, "Error", err);
		}
	}

	//Broadcast receiver for receiving status updates from the IntentService
	private class ResponseReceiver extends BroadcastReceiver {
		// Prevents instantiation
		private ResponseReceiver() {
		}
		// Called when the BroadcastReceiver gets an Intent it's registered to receive
		public void onReceive(Context context, Intent intent) {
			/*
			 * Handle Intents here.
			 */
			if (intent.getAction() == ConstantUtil.AUTHORIZATION_RESULT_ACTION)
				onAuthFinished(intent);
		}
	}

	

}
