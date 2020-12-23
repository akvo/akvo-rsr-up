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
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.viewadapter.UpdateListCursorAdapter;

public class UpdateListActivity extends AppCompatActivity {

	private static final String TAG = "UpdateListActivity";

	private RsrDbAdapter ad;
	private Cursor dataCursor;
	private TextView projectTitleLabel;
	private TextView updateCountLabel;
    private ListView mList;
	private String projId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_list);

		//find which project we will be showing updates for
		Bundle extras = getIntent().getExtras();
		projId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
				: null;
		if (projId == null) {
			projId = savedInstanceState != null ? savedInstanceState
					.getString(ConstantUtil.PROJECT_ID_KEY) : null;
		}


		projectTitleLabel = (TextView) findViewById(R.id.ulisttitlelabel);
		updateCountLabel = (TextView) findViewById(R.id.updatecountlabel);
        mList = (ListView) findViewById(R.id.list_updates);
        mList.setOnItemClickListener((OnItemClickListener) (parent, view, position, id) -> {
			Intent i = new Intent(view.getContext(), UpdateDetailActivity.class);
			i.putExtra(ConstantUtil.UPDATE_ID_KEY, ((Long) view.getTag(R.id.update_id_tag)).toString());
			i.putExtra(ConstantUtil.PROJECT_ID_KEY, ((Long) view.getTag(R.id.project_id_tag)).toString());
			startActivity(i);
		});
        
		View listFooter = getLayoutInflater().inflate(R.layout.update_list_footer, mList, false);
		Button addButton = (Button) listFooter.findViewById(R.id.btn_add_update2);
		addButton.setOnClickListener(view -> startEditorNew());
		
		mList.addFooterView(listFooter);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		ad = new RsrDbAdapter(this);
        ad.open();
	}

	private void startEditorNew() {
		Intent i3 = new Intent(this, UpdateEditorActivity.class);
		i3.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
		startActivity(i3);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.update_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
    	    case R.id.action_settings:
    			Intent intent = new Intent(this, SettingsActivity.class);
    			startActivity(intent);
                return true;
            case R.id.action_add_update:
            	startEditorNew();
            	return true;
    	    default:
    	        return super.onOptionsItemSelected(item);
	    }

	}
	
	@Override
	public void onResume() {
		super.onResume();
		getData();
	}
	
	@Override
	protected void onDestroy() {
		if (dataCursor != null) {
			try {
				dataCursor.close();
			} catch (Exception e) {
				Log.w(TAG, "Error closing cursor");
			}
		}
		if (ad != null) {
			ad.close();
		}
		super.onDestroy();
	}

	/**
	 * show count and list of all the updates in the database for this project
	 */
	private void getData() {
		try {
			if (dataCursor != null) {
				dataCursor.close();
			}
		} catch(Exception e) {
			Log.w(TAG, "Could not close old cursor before reloading list", e);
		}

		Project p = ad.findProject(projId);
		if (p != null) {
			projectTitleLabel.setText(p.getTitle());
		} //TODO: what if project is null?
		dataCursor = ad.listAllUpdatesNewestFirstFor(projId);
		updateCountLabel.setText(Integer.valueOf(dataCursor.getCount()).toString());
		UpdateListCursorAdapter updates = new UpdateListCursorAdapter(this, dataCursor);
		mList.setAdapter(updates);
	}
}
