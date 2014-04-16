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

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.content.res.Resources;

public class ProjectDetailActivity extends Activity {

	private String projId = null;
	private Project project = null;
	private TextView projTitleLabel;
	private TextView projSummaryText;
	private TextView projLocationText;
	private TextView publishedCountView;
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
		projLocationText 	= (TextView) findViewById(R.id.text_proj_location);
		projSummaryText		= (TextView) findViewById(R.id.text_proj_summary);
		projImage 			= (ImageView) findViewById(R.id.image_proj_detail);
		publishedCountView 	= (TextView) findViewById(R.id.text_proj_detail_published_count);
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
				Intent i = new Intent(view.getContext(), UpdateEditorActivity.class);
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
		try{
    		project = dba.findProject(projId);
    		if (project == null) { //DB may have been cleared
    		    return;
    		}
    		if (debug) {
    			projTitleLabel.setText("["+ projId + "] "+project.getTitle());
    		} else {
    			projTitleLabel.setText(project.getTitle());
    		}
    
    		String loc = "";
    		if (project.getCity() != null && project.getCity().length() > 0) {
    			loc += project.getCity() + ", ";
    		}
    		if (project.getState() != null && project.getState().length() > 0) {
    			loc += project.getState() + ", ";			
    		}
    		if (project.getCountry() != null && project.getCountry().length() > 0) {
    			loc += project.getCountry() + ", ";			
    		}
    		if (loc.length() > 1) {
    			loc = loc.substring(0, loc.length()-2);
    		}
    
    		//TODO check against 0,0 too?
    		if (project.getLatitude() != null &&
    			project.getLongitude() != null ) {
    			loc += "\nLatitude " + project.getLatitude() +
    			        " Longitude " + project.getLongitude();
    			projLocationText.setOnClickListener(
    				new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						launchLatLonIntent();
    					}
    				});
    		} else {
    			projLocationText.setOnClickListener(null);
    		}
    
    		projLocationText.setText(loc);
    		projSummaryText.setText(project.getSummary());
    		
    		int [] stateCounts = {0,0,0};
    		stateCounts = dba.countAllUpdatesFor(projId);
    		Resources res = getResources();
    		publishedCountView.setText(Integer.toString(stateCounts[2]) + res.getString(R.string.count_published));
    		draftCountView.setText(Integer.toString(stateCounts[0]) + res.getString(R.string.count_draft));
    
    		FileUtil.setPhotoFile(projImage,project.getThumbnailUrl(), project.getThumbnailFilename(), projId, null);

		} finally {
            dba.close();    
        }

	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	

	@Override
	protected void onDestroy() {
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
	            return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }

	}
	
	private void launchLatLonIntent() {
		if (project != null &&
		    project.getLatitude() != null &&
			project.getLongitude() != null ) {
			Uri uri = Uri.parse("geo:" + project.getLatitude() +
			"," + project.getLongitude()); //Possibly add "?zoom=z"
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			
		}
		
	}

}
