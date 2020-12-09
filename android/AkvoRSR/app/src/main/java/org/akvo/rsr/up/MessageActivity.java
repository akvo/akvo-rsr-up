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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MessageActivity extends BackActivity {

    public static String TAG = "MessageActivity";
	private TextView mTextView;

	private RsrDbAdapter mDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diagnostic);
		mTextView = (TextView) findViewById(R.id.text_field);
		mDb = new RsrDbAdapter(this);
		mDb.open();
	}

	@Override
	protected void onResume() {
		super.onResume();
        mTextView.setText(R.string.messages_subtitle);
        fetchLog();
	}

    /**
     * Displays the error log
     */
    private void fetchLog() {
        try {
            // Open file
            try (BufferedReader buf = new BufferedReader(new FileReader(FileUtil.getExternalCacheDir(this) + ConstantUtil.LOG_FILE_NAME))) {
                String s;
                while ((s = buf.readLine()) != null) {
                    mTextView.append(s);
                }

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
        getMenuInflater().inflate(R.menu.messages, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_clear_messages:
	    //TODO
            return true;
        case R.id.action_mail_messages:
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
