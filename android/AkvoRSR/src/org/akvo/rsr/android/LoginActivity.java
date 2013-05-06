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
import java.net.URL;

import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private static final String TAG = "LoginActivity";
	private static final String imageFolder1 = "/akvorsr/photos";
	private static final String imageCache2 = "/akvorsr/imagecache";
	private String rsrApiKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log.i(TAG, "External storage: mounted ");
			File f = new File (Environment.getExternalStorageDirectory().getPath() + imageFolder1);
			if (f.mkdirs() || f.isDirectory() ) {
				Log.i(TAG, "Found/created photo dir "+f.getAbsolutePath());
			} else
				Log.e("LoginActivity", "could not find/create photo dir");
			f = new File (Environment.getExternalStorageDirectory().getPath() + imageCache2);
			if (f.mkdirs() || f.isDirectory()) {
				Log.i(TAG, "Found/created image cache dir "+f.getAbsolutePath());
			} else
				Log.e("LoginActivity", "could not find/create image cache dir");
		} else {
			DialogUtil.errorAlert(this, "No storage available", "Akvo RSR requires a mounted storage card for image files. Mount card and restart app.");
			
		}
		
		setContentView(R.layout.activity_login);
		
        final Button button = (Button) findViewById(R.id.btnLogin);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	SignIn(v);
            }
        });

        final TextView forgot = (TextView) findViewById(R.id.link_to_forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ConstantUtil.HOST+ConstantUtil.PWD_URL));
                startActivity(myIntent);                
            }
        });

        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

    //Sign In button pushed
    public void SignIn(View view) {
    	//TODO request API key from server
    	rsrApiKey = ConstantUtil.TEST_API_KEY;
	    Intent intent = new Intent(this, ProjectListActivity.class);
	    startActivity(intent);
    }
}
