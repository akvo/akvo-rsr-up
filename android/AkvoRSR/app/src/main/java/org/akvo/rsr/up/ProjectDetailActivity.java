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

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.service.GetProjectDataService;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.ThumbnailUtil;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

public class ProjectDetailActivity extends AppCompatActivity {

	private String projId = null;
	private Project project = null;
	private TextView projTitleLabel;
	private TextView projSummaryText;
	private TextView projLocationText;
	private TextView publishedCountView;
    private TextView draftCountView;
    private TextView resultCountView;
	private ImageView projImage;
    private Button btnUpdates;
	private Button btnAddUpdate;
    private Button btnResults;
    private Button btnRefresh;
    private LinearLayout inProgress;
    private ProgressBar mInProgressBar;
    private TextView mInProgressWhat;
	private boolean mDebug;

	private RsrDbAdapter mDba;
    private BroadcastReceiver mBroadRec;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDebug = SettingsUtil.ReadBoolean(this, "setting_debug", false);

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
        draftCountView      = (TextView) findViewById(R.id.text_proj_detail_draft_count);
        resultCountView     = (TextView) findViewById(R.id.text_proj_detail_result_count);
        inProgress          = (LinearLayout) findViewById(R.id.proj_detail_progress);
        mInProgressBar      = (ProgressBar) findViewById(R.id.progress_bar);
        mInProgressWhat     = (TextView) findViewById(R.id.progress_title);

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
		
        //Results
        btnResults = (Button) findViewById(R.id.btn_view_results);
        btnResults.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ResultListActivity.class);
                i.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
                startActivity(i);
            }
        });

        //Refresh for this project only (speeds up things if very many projects)
        btnRefresh = (Button) findViewById(R.id.btn_refresh_proj);
        btnRefresh.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                startGetProjectsService();
            }
        });

		
		mDba = new RsrDbAdapter(this);
		
		// Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        //register a listener for completion broadcasts
        IntentFilter f = new IntentFilter(ConstantUtil.PROJECTS_FETCHED_ACTION);
        f.addAction(ConstantUtil.PROJECTS_PROGRESS_ACTION);
        mBroadRec = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadRec, f);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mDba.open();
		try{
    		project = mDba.findProject(projId);
    		if (project == null) { //DB may have been cleared
    		    return;
    		}
    		if (mDebug) {
    			projTitleLabel.setText("[" + projId + "] " + project.getTitle());
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
    		stateCounts = mDba.countAllUpdatesFor(projId);
    		Resources res = getResources();
    		publishedCountView.setText(Integer.toString(stateCounts[2]) + res.getString(R.string.count_published));
            draftCountView.setText(Integer.toString(stateCounts[0]) + res.getString(R.string.count_draft));
            draftCountView.setVisibility(stateCounts[0] > 0 ? View.VISIBLE : View.GONE);

            int rc = mDba.countResultsFor(projId);
            int ic = mDba.countIndicatorsFor(projId);
            resultCountView.setText(Integer.toString(rc) + res.getString(R.string.count_results) + ", " + Integer.toString(ic) + res.getString(R.string.count_indicators));
            resultCountView.setVisibility(rc > 0 ? View.VISIBLE : View.GONE);
            btnResults.setEnabled(rc > 0);
    
    		ThumbnailUtil.setPhotoFile(projImage,project.getThumbnailUrl(), project.getThumbnailFilename(), projId, null, true);

		} finally {
            mDba.close();    
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ConstantUtil.PROJECT_ID_KEY, projId);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.project_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
            case android.R.id.home:
                finish();
//              NavUtils.navigateUpFromSameTask(this);
                return true;
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
    /**
     * starts the service fetching new project data
     */
    private void startGetProjectsService() {
        if (GetProjectDataService.isRunning(this)) { //unlikely as button should be disabled
            return; //only one at a time
        }
        //disable button
        btnRefresh.setEnabled(false);
        //start a service       
        Intent i = new Intent(this, GetProjectDataService.class);
        i.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
        getApplicationContext().startService(i);
        
        //start progress animation
        inProgress.setVisibility(View.VISIBLE);
        mInProgressBar.setProgress(0);
        mInProgressWhat.setText("=====");
    }

    
    /**
     * handles result of refresh service
     * @param intent
     */
    private void onFetchFinished(Intent intent) {
        // Hide in-progress indicators
        inProgress.setVisibility(View.GONE);
        //Re-enable button
        btnRefresh.setEnabled(true);
        
        String err = intent.getStringExtra(ConstantUtil.SERVICE_ERRMSG_KEY);
        if (err == null) {
            Toast.makeText(getApplicationContext(), R.string.msg_fetch_complete, Toast.LENGTH_SHORT).show();
        } else {
            //show a dialog instead
            DialogUtil.errorAlertWithDetail(this, R.string.errmsg_com_failure, R.string.msg_check_network, err);
        }

        //TODO:Refresh the page?
//        getData();
    }
    
    /**
     * updates the progress bar etc as fetch progresses
     * @param phase
     * @param done
     * @param total
     */
    private void onFetchProgress(int phase, int done, int total) {
        mInProgressBar.setMax(total);
        mInProgressBar.setProgress(done);
        switch (phase) {
            case  0:mInProgressWhat.setText("Projects");break;
            case  1:mInProgressWhat.setText("Updates");break;
            case  2:mInProgressWhat.setText("Photos");break;
            default:mInProgressWhat.setText("???");break;
        }
    }
    
    /**
     * receives status updates from any IntentService
     *
     */
    private class ResponseReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private ResponseReceiver() {
        }
        
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ConstantUtil.PROJECTS_FETCHED_ACTION)
                onFetchFinished(intent);
            else if (intent.getAction() == ConstantUtil.PROJECTS_PROGRESS_ACTION)
                onFetchProgress(intent.getExtras().getInt(ConstantUtil.PHASE_KEY, 0),
                                intent.getExtras().getInt(ConstantUtil.SOFAR_KEY, 0),
                                intent.getExtras().getInt(ConstantUtil.TOTAL_KEY, 100));
        }
    }


}
