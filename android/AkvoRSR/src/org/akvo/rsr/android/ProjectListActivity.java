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

import java.net.MalformedURLException;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.service.GetProjectDataService;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.view.adapter.ProjectListCursorAdapter;
import org.akvo.rsr.android.xml.Downloader;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ListActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

public class ProjectListActivity extends ListActivity {



	private static final String TAG = "ProjectListActivity";

	private RsrDbAdapter ad;
	private Cursor dataCursor;
	private TextView projCountLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_list);

		projCountLabel = (TextView) findViewById(R.id.projcountlabel);

		Button refreshButton = (Button) findViewById(R.id.button_refresh_projects);		
		refreshButton.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				ad.clearAllData();
				//fetch new data
				startGetProjectsService();
				//redisplay list
				getData();
			}
		});
 
        //Create db
        ad = new RsrDbAdapter(this);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_project_list, menu);
//		Button btnDiag = (Button) findViewById(R.menu.menu_diagnostics
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_diagnostics:
				Intent i = new Intent(this, DiagnosticActivity.class);
				startActivity(i);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }

	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (dataCursor != null) {
			dataCursor.close();
		}	
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ad.open();
		getData();
	}
	
	
	@Override
	protected void onDestroy() {
		if (dataCursor != null) {
			try {
				dataCursor.close();
			} catch (Exception e) {

			}
		}
		if (ad != null) {
			ad.close();
		}
		super.onDestroy();
	}



	/**
	 * show all the projects in the database
	 */
	private void getData() {
		try {
			if (dataCursor != null) {
				dataCursor.close();
			}
		} catch(Exception e) {
			Log.w(TAG, "Could not close old cursor before reloading list",e);
		}
		dataCursor = ad.listAllProjects();
		//Show count
		projCountLabel.setText(Integer.valueOf(dataCursor.getCount()).toString());
		//Populate list view
		ProjectListCursorAdapter projects = new ProjectListCursorAdapter(this, dataCursor);
		setListAdapter(projects);

	}

	/**
	 * when a list item is clicked, get the id of the selected
	 * item and open one-project activity.
	 */
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);

		Intent i = new Intent(view.getContext(), ProjectDetailActivity.class);
		i.putExtra(ConstantUtil.PROJECT_ID_KEY, ((Long) view.getTag(R.id.project_id_tag)).toString());
		startActivity(i);
	}

	
	/*
	 * Start the service fetching new project data
	 */
	private void startGetProjectsService() {
		//TODO start a real service, register a listener for a completion intent
//		Intent i = new Intent(this, GetProjectDataService.class);
//		i.putExtra(SERVER_KEY, "http://test.akvo.org");
//		i.putExtra(URL_KEY, "/api/v1/project/?format=xml"); //get first 20 by default
//		getApplicationContext().startService(i);
		//meanwhile:
		Downloader dl = new Downloader();
		//TODO THIS MIGHT HANG, no timeout defined...
		dl.FetchProjectList(this,ConstantUtil.HOST,ConstantUtil.FETCH_PROJ_URL);//Akvo projs
		//We only get published projects from that URL, so we need to iterate on them and get corresponding updates
		Cursor c=ad.listAllProjects();
		while (c.moveToNext()) {
			dl.FetchUpdateList(this,
								ConstantUtil.HOST,
								"/api/v1/project_update/?format=xml&limit=0&project="+
								c.getString(c.getColumnIndex(RsrDbAdapter.PK_ID_COL))
								);
		}
		c.close();
		try {
			dl.FetchNewThumbnails(this, ConstantUtil.HOST, Environment.getExternalStorageDirectory().getPath() + ConstantUtil.IMAGECACHE_DIR);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}



}
