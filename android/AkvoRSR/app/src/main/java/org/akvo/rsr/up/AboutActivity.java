/*
 *  Copyright (C) 2012-2015,2020 Stichting Akvo (Akvo Foundation)
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

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		//plug in the version name from the manifest
		String version;
		try {
		    version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "<not found>";
		}
		TextView tv = (TextView) findViewById(R.id.version_name);
		if (tv != null)
			tv.setText(version);
		
        final ImageButton btnUpdates = (ImageButton) findViewById(R.id.btn_akvo_link);
        btnUpdates.setOnClickListener(v -> {
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.akvo.org/"));
			startActivity(myIntent);
		});

        final TextView licenseLink = (TextView) findViewById(R.id.link_to_license);
        licenseLink.setOnClickListener(v -> startActivity(new Intent(AboutActivity.this, LicenseActivity.class)));
	}
}
