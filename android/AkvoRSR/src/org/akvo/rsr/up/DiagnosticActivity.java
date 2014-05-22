/*
 *  Copyright (C) 2012-2014 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up;

import java.io.File;
import java.util.List;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.FileUtil;

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

	private TextView mTextView;
	private Button mBtnUpdates;
	private Button mBtnClearDb;

	private RsrDbAdapter mDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//get the look
		setContentView(R.layout.activity_diagnostic);
		//find the fields
		mTextView = (TextView) findViewById(R.id.text_field);
		//Activate buttons
		mBtnUpdates = (Button) findViewById(R.id.btn_diag_a);
		mBtnUpdates.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {//delete image cache files
				clearCache(DiagnosticActivity.this);
			}
		});
		mBtnClearDb = (Button) findViewById(R.id.btn_diag_b);
		mBtnClearDb.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				clearData();
			}
		});
 
		mDb = new RsrDbAdapter(this);
	}

	/**
	 * removes all files from the image cache
	 */
	private void clearCache(Context context ) {
		FileUtil.clearCache(context, false);
		//clearOldCache();
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
		mDb.clearAllData();
		DialogUtil.infoAlert(this, "Data cleared", "All project and update info deleted");
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		List<String>u = mDb.getMissingUsersList();
        mTextView.append("\nMissing users in db: " + String.valueOf(u.size())+"\n");
        for (String id:u) {
            mTextView.append("\n["+id+"] ");
        }
        List<String>o = mDb.getMissingOrgsList();
        mTextView.append("\nMissing orgs in db: " + String.valueOf(u.size())+"\n");
        for (String id:o) {
            mTextView.append("\n["+id+"] ");
        }
		Cursor a = mDb.listAllCountries();
		mTextView.append("\nCountries in db: " + String.valueOf(a.getCount())+"\n");
		while (a.moveToNext())
			mTextView.append("\n["+a.getString(a.getColumnIndex(RsrDbAdapter.PK_ID_COL))+"] "+a.getString(a.getColumnIndex(RsrDbAdapter.NAME_COL))+" ");
		a.close();
//		Cursor c = mDb.listAllUpdates();
//		mTextView.append("\nUpdates in db: " + String.valueOf(c.getCount())+"\n");
//		while (c.moveToNext())
//			mTextView.append("'"+c.getString(c.getColumnIndex(RsrDbAdapter.PK_ID_COL))+"' for '"+c.getString(c.getColumnIndex(RsrDbAdapter.PROJECT_COL))+"' ");
//		c.close();
//		Cursor d = mDb.listAllUpdatesFor("609");
//		mTextView.append("\nUpdates in db for 609: " + String.valueOf(d.getCount())+"\n");
//		d.close();
		
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdAvailSize = (double)stat.getAvailableBlocks()
		                   * (double)stat.getBlockSize();
		//One binary gigabyte equals 1,073,741,824 bytes.
		double gigaAvailable = sdAvailSize / 1073741824;
		mTextView.append(gigaAvailable + " GB free on card\n");
	}
		

	@Override
	protected void onDestroy() {
		if (mDb != null) {
			mDb.close();
		}
		super.onDestroy();
	}

	

}
