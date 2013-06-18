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
import org.akvo.rsr.android.domain.Project;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.SettingsUtil;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ProjectDetailActivity extends Activity {

	private String projId = null;
	private TextView projTitleLabel;
	private TextView projLocationText;
	private TextView projSummaryText;
	private TextView publishedCountView;
	private TextView unsynchCountView;
	private TextView draftCountView;
	private ImageView projImage;
	private Button btnUpdates;
	private Button btnAddUpdate;
	private boolean debug;

	private RsrDbAdapter dba;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		debug = SettingsUtil.ReadBoolean(this, "setting_debug", false);

		//find which project we will be showing
		Bundle extras = getIntent().getExtras();
		projId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
				: null;
		if (projId == null) {
			projId = savedInstanceState != null ? savedInstanceState
					.getString(ConstantUtil.PROJECT_ID_KEY) : null;
		}

		//get the look
		setContentView(R.layout.activity_project_detail);
		//find the fields
		projTitleLabel		= (TextView) findViewById(R.id.text_proj_detail_title);
//		projLocationText 	= (TextView) findViewById(R.id.text_proj_location);
		projSummaryText		= (TextView) findViewById(R.id.text_proj_summary);
		projImage 			= (ImageView) findViewById(R.id.image_proj_detail);
		publishedCountView 	= (TextView) findViewById(R.id.text_proj_detail_published_count);
		unsynchCountView 	= (TextView) findViewById(R.id.text_proj_detail_unsynchronized_count);
		draftCountView 		= (TextView) findViewById(R.id.text_proj_detail_draft_count);

		//Activate buttons
		btnUpdates = (Button) findViewById(R.id.btn_view_updates);
		btnUpdates.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), UpdateListActivity.class);
				i.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
				startActivity(i);
			}
		});
		btnAddUpdate = (Button) findViewById(R.id.btn_add_update);
		btnAddUpdate.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), UpdateEditActivity.class);
				i.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
				startActivity(i);
			}
		});
 

		
		dba = new RsrDbAdapter(this);
		
		// Show the Up button in the action bar.
		//		setupActionBar();
	}

	@Override
	protected void onResume() {
		super.onResume();
		dba.open();
		Project project = dba.findProject(projId);
		
		if (debug) {
			projTitleLabel.setText("["+ projId+"] "+project.getTitle());
		} else {
			projTitleLabel.setText(project.getTitle());
		}
//		projLocationText.setText(project.getLocation()); //not sure what this is supposed to match in XML
		projSummaryText.setText(project.getSummary());
		
		dba.open();
		int [] stateCounts = {0,0,0};
		try {
			stateCounts = dba.countAllUpdatesFor(projId);
		} finally {
			dba.close();	
		}
		Resources res = getResources();
		publishedCountView.setText(Integer.toString(stateCounts[2]) + res.getString(R.string.count_published));
		unsynchCountView.setText(Integer.toString(stateCounts[1]) + res.getString(R.string.count_unsent));
		draftCountView.setText(Integer.toString(stateCounts[0]) + res.getString(R.string.count_draft));

		
		//Find file containing thumbnail
		String fn = project.getThumbnailFilename();
		File f;
		if (fn != null && (f = new File(fn)) != null && f.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
			if (bm != null) {
				projImage.setImageBitmap(bm);
			}
		} else {
			//Fall back to generic logo
			projImage.setImageResource(R.drawable.ic_launcher);
		}

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
