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
 *  See the GNU Affero General Public License included with this program for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.up;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.FileUtil;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MessageActivity extends ActionBarActivity {

    public static String TAG = "MessageActivity";

	private TextView mTextView;
//	private Button mBtnUpdates;
//	private Button mBtnClearDb;

	private RsrDbAdapter mDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//get the look
		setContentView(R.layout.activity_diagnostic);
		//find the fields
		mTextView = (TextView) findViewById(R.id.text_field);
		//Activate buttons
/*
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
 */
		mDb = new RsrDbAdapter(this);
		mDb.open();
	}

	
	@Override
	protected void onResume() {
		super.onResume();
        mTextView.setText("Messages, newest last:");
        fetchLog();
	}

	
    /**
     * returns the error log
     * 
     * @param record the string to append to the log
     */
    private void fetchLog() {
        try {
            // Open file
            BufferedReader buf = new BufferedReader( new FileReader(FileUtil.getExternalCacheDir(this) + ConstantUtil.LOG_FILE_NAME));
            try {
                String s;
                while ((s = buf.readLine()) != null) {
                    mTextView.append(s);
                }
                
            } finally {
                buf.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

	@Override
	protected void onDestroy() {
		if (mDb != null) {
			mDb.close();
		}
		super.onDestroy();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.diagnostics, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*
        case R.id.action_clear_diagnostics:
            return true;
        case R.id.action_settings:
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
            */
        default:
            return super.onOptionsItemSelected(item);
        }

    }

}
