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

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.TextView;

import org.akvo.rsr.up.dao.RsrDbAdapter;

import java.util.List;

public class DiagnosticActivity extends BackActivity {

	private TextView mTextView;

    private RsrDbAdapter mDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diagnostic);
		//find the fields
		mTextView = (TextView) findViewById(R.id.text_field);
		//Activate buttons
		mDb = new RsrDbAdapter(this);
		mDb.open();
	}

	
	@Override
	protected void onResume() {
		super.onResume();

        Cursor users = mDb.listAllUsers();
        mTextView.append("\n\nUsers in db: " + users.getCount());
        while (users.moveToNext())
            mTextView.append("\n[" + users.getString(users.getColumnIndex(RsrDbAdapter.PK_ID_COL)) +
                             "] "  + users.getString(users.getColumnIndex(RsrDbAdapter.FIRST_NAME_COL)) +
                             " "   + users.getString(users.getColumnIndex(RsrDbAdapter.LAST_NAME_COL)));
        users.close();

        List<String>u = mDb.getMissingUsersList();
        mTextView.append("\n\nMissing users in db: " + u.size());
        for (String id:u) {
            mTextView.append("\n["+id+"] ");
        }

        Cursor orgs = mDb.listAllOrgs();
        mTextView.append("\n\nOrgs in db: " + orgs.getCount());
        while (orgs.moveToNext())
            mTextView.append("\n["+orgs.getString(orgs.getColumnIndex(RsrDbAdapter.PK_ID_COL))+"] "+orgs.getString(orgs.getColumnIndex(RsrDbAdapter.NAME_COL))+" ");
        orgs.close();

        List<String>o = mDb.getMissingOrgsList();
        mTextView.append("\n\nMissing orgs in db: " + o.size());
        for (String id:o) {
            mTextView.append("\n["+id+"] ");
        }
        Cursor b = mDb.listAllProjects();
        mTextView.append("\n\nProjects in db: " + b.getCount());
        b.close();
        Cursor b2 = mDb.listVisibleProjects();
        mTextView.append("\n\nVisible projects in db: " + b2.getCount());
        b2.close();
        Cursor c = mDb.listAllUpdates();
        mTextView.append("\n\nUpdates in db: " + c.getCount());
        c.close();
        Cursor a = mDb.listAllCountries();
        mTextView.append("\n\nCountries in db: " + a.getCount());
        while (a.moveToNext())
            mTextView.append("\n[" + a.getString(a.getColumnIndex(RsrDbAdapter.PK_ID_COL))+"] " + a.getString(a.getColumnIndex(RsrDbAdapter.NAME_COL)) + " ");
        a.close();

        mTextView.append("\n\nResults in db: " + mDb.countResults());

        mTextView.append("\n\nIndicators in db: " + mDb.countIndicators());

        mTextView.append("\n\nPeriods in db: " + mDb.countPeriods());

		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdAvailSize = (double)stat.getAvailableBlocks()
		                   * (double)stat.getBlockSize();
		//One binary gigabyte equals 1,073,741,824 bytes.
		double gigaAvailable = sdAvailSize / 1073741824;
        mTextView.append("\n\n" + gigaAvailable + " GiB free on card\n");
	}
		

	@Override
	protected void onDestroy() {
		if (mDb != null) {
			mDb.close();
		}
		super.onDestroy();
	}
}
