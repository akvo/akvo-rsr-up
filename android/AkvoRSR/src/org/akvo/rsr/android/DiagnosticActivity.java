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

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;
import org.akvo.rsr.android.util.FileUtil;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.database.Cursor;

public class DiagnosticActivity extends Activity {

	private TextView txt;
	private Button btnUpdates;
	private Button btnAddUpdate;

	private RsrDbAdapter dba;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//get the look
		setContentView(R.layout.activity_diagnostic);
		//find the fields
		txt = (TextView) findViewById(R.id.text_field);
		//Activate buttons
		btnUpdates = (Button) findViewById(R.id.btn_diag_a);
		btnUpdates.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {//delete image cache files
				clearCache(DiagnosticActivity.this);
			}
		});
		btnAddUpdate = (Button) findViewById(R.id.btn_diag_b);
		btnAddUpdate.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				clearData();
			}
		});
 
		dba = new RsrDbAdapter(this);
	}

	/**
	 * removes all files from the image cache
	 * TODO: move to FileUtil?
	 */
	private void clearCache(Context context ) {
		RsrDbAdapter dba = new RsrDbAdapter(context);
		dba.open();
		dba.clearProjectThumbnailFiles();
		dba.clearUpdateThumbnailFiles();
		dba.close();
		File f = FileUtil.getExternalCacheDir(this);
		File [] files = f.listFiles();
		if (files != null) { //dir might not exist
			long sizeSum = 0;
			for (int i = 0; i < files.length; i++) {
				sizeSum += files[i].length();
				files[i].delete();
			}
			DialogUtil.infoAlert(context, "Cache cleared", files.length + " files deleted (" + sizeSum/(1024*1024) + " MB)");
		}
		clearOldCache();
	}

	/**
	 * removes all files from the old image cache
	 */
	private void clearOldCache() {
		File f = new File(Environment.getExternalStorageDirectory() + ConstantUtil.IMAGECACHE_DIR);
		File [] files = f.listFiles();
		if (files != null) { //dir might not exist
			for (int i = 0; i < files.length; i++) { 
				files[i].delete();
			}
		}
		f = new File(Environment.getExternalStorageDirectory() + ConstantUtil.PHOTO_DIR);
		files = f.listFiles();
		if (files != null) { //dir might not exist
			for (int i = 0; i < files.length; i++) { 
				files[i].delete();
			}
		}
		
	}

	/*
	 * clears the database
	 * TODO: should we clear login credentials, too?
	 */
	private void clearData() {
		dba.clearAllData();
		DialogUtil.infoAlert(this, "Data cleared", "All project and update info deleted");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		dba.open();
		Cursor a = dba.listAllCountries();
		txt.append("\nCountries in db: " + String.valueOf(a.getCount())+"\n");
		while (a.moveToNext())
			txt.append("\n["+a.getString(a.getColumnIndex(RsrDbAdapter.PK_ID_COL))+"] "+a.getString(a.getColumnIndex(RsrDbAdapter.NAME_COL))+" ");
		a.close();
		Cursor c = dba.listAllUpdates();
		txt.append("\nUpdates in db: " + String.valueOf(c.getCount())+"\n");
		while (c.moveToNext())
			txt.append("'"+c.getString(c.getColumnIndex(RsrDbAdapter.PK_ID_COL))+"' for '"+c.getString(c.getColumnIndex(RsrDbAdapter.PROJECT_COL))+"' ");
		c.close();
		Cursor d = dba.listAllUpdatesFor("609");
		txt.append("\nUpdates in db for 609: " + String.valueOf(d.getCount())+"\n");
		d.close();
		
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdAvailSize = (double)stat.getAvailableBlocks()
		                   * (double)stat.getBlockSize();
		//One binary gigabyte equals 1,073,741,824 bytes.
		double gigaAvailable = sdAvailSize / 1073741824;
		txt.append(gigaAvailable + " GB free on card\n");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		dba.close();
	}
	

	@Override
	protected void onDestroy() {
		if (dba != null) {
			dba.close();
		}
		super.onDestroy();
	}

	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 *
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.project_detail, menu);
		return true;
	}


}
