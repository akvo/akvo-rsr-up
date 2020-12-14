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
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.ThumbnailUtil;
import org.akvo.rsr.up.worker.GetProjectDataWorker;
import org.jetbrains.annotations.NotNull;

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
	private Button btnResults;
    private Button btnRefresh;
    private LinearLayout inProgress;
    private ProgressBar mInProgressBar;
    private TextView mInProgressWhat;
	private boolean mDebug;

	private RsrDbAdapter mDba;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_detail);

		mDebug = SettingsUtil.ReadBoolean(this, "setting_debug", false);

		//find which project we will be showing
		Bundle extras = getIntent().getExtras();
		projId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
				: null;
		if (projId == null) {
			projId = savedInstanceState != null ? savedInstanceState
					.getString(ConstantUtil.PROJECT_ID_KEY) : null;
		}

		projTitleLabel = (TextView) findViewById(R.id.text_proj_detail_title);
		projLocationText = (TextView) findViewById(R.id.text_proj_location);
		projSummaryText = (TextView) findViewById(R.id.text_proj_summary);
		projImage = (ImageView) findViewById(R.id.image_proj_detail);
		publishedCountView = (TextView) findViewById(R.id.text_proj_detail_published_count);
		draftCountView = (TextView) findViewById(R.id.text_proj_detail_draft_count);
		resultCountView = (TextView) findViewById(R.id.text_proj_detail_result_count);
		inProgress = (LinearLayout) findViewById(R.id.proj_detail_progress);
		mInProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mInProgressWhat = (TextView) findViewById(R.id.progress_title);

		Button btnUpdates = (Button) findViewById(R.id.btn_view_updates);
		btnUpdates.setOnClickListener(view -> {
			Intent i = new Intent(view.getContext(), UpdateListActivity.class);
			i.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
			startActivity(i);
		});
		Button btnAddUpdate = (Button) findViewById(R.id.btn_add_update);
		btnAddUpdate.setOnClickListener(view -> {
			Intent i = new Intent(view.getContext(), UpdateEditorActivity.class);
			i.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
			startActivity(i);
		});
		
        //Results
        btnResults = (Button) findViewById(R.id.btn_view_results);
        btnResults.setOnClickListener(view -> {
			Intent i = new Intent(view.getContext(), ResultListActivity.class);
			i.putExtra(ConstantUtil.PROJECT_ID_KEY, projId);
			startActivity(i);
		});

        //Refresh for this project only (speeds up things if very many projects)
        btnRefresh = (Button) findViewById(R.id.btn_refresh_proj);
        btnRefresh.setOnClickListener(view -> startGetProjectsService());

		
		mDba = new RsrDbAdapter(this);
		
		// Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mDba.open();
		try {
    		project = mDba.findProject(projId);
    		if (project == null) { //DB may have been cleared
    		    return;
    		}
    		if (mDebug) {
    			projTitleLabel.setText(String.format("[%s] %s", projId, project.getTitle()));
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
    
    		if (project.getLatitude() != null &&
    			project.getLongitude() != null ) {
    			loc += "\nLatitude " + project.getLatitude() +
    			        " Longitude " + project.getLongitude();
    			projLocationText.setOnClickListener(v -> launchLatLonIntent());
    		} else {
    			projLocationText.setOnClickListener(null);
    		}
    
    		projLocationText.setText(loc);
    		projSummaryText.setText(project.getSummary());
    		
    		int [] stateCounts;
    		stateCounts = mDba.countAllUpdatesFor(projId);
    		Resources res = getResources();
    		publishedCountView.setText(String.format("%d%s", stateCounts[2], res.getString(R.string.count_published)));
            draftCountView.setText(String.format("%d%s", stateCounts[0], res.getString(R.string.count_draft)));
            draftCountView.setVisibility(stateCounts[0] > 0 ? View.VISIBLE : View.GONE);

            int rc = mDba.countResultsFor(projId);
            int ic = mDba.countIndicatorsFor(projId);
            resultCountView.setText(String.format("%d%s, %d%s", rc, res.getString(R.string.count_results), ic, res.getString(R.string.count_indicators)));
            resultCountView.setVisibility(rc > 0 ? View.VISIBLE : View.GONE);
            btnResults.setEnabled(rc > 0);
    
    		ThumbnailUtil.setPhotoFile(projImage,project.getThumbnailUrl(), project.getThumbnailFilename(), projId, null, true);

		} finally {
            mDba.close();    
        }
	}
	
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
		outState.putString(ConstantUtil.PROJECT_ID_KEY, projId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.project_detail, menu);
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
        //disable button
        btnRefresh.setEnabled(false);
        inProgress.setVisibility(View.VISIBLE);
        mInProgressBar.setProgress(0);
        mInProgressWhat.setText("=====");

		WorkManager workManager = WorkManager.getInstance(getApplicationContext());
		Data.Builder builder = new Data.Builder();
		builder.putString(ConstantUtil.PROJECT_ID_KEY, projId);
		OneTimeWorkRequest oneTimeWorkRequest =
				new OneTimeWorkRequest.Builder(GetProjectDataWorker.class)
						.addTag(GetProjectDataWorker.TAG)
						.setInputData(builder.build())
						.build();
		workManager.enqueueUniqueWork(GetProjectDataWorker.TAG, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);

		workManager.getWorkInfosForUniqueWorkLiveData(GetProjectDataWorker.TAG).observe(this, listOfWorkInfos -> {
			// If there are no matching work info, do nothing
			if (listOfWorkInfos == null || listOfWorkInfos.isEmpty()) {
				return;
			}

			// We only care about the first output status.
			WorkInfo workInfo = listOfWorkInfos.get(0);
			boolean finished = workInfo.getState().isFinished();

			if (finished) {
					String err = workInfo.getOutputData().getString(ConstantUtil.SERVICE_ERRMSG_KEY);
					onFetchFinished(err);
			}
		});

		workManager.getWorkInfosByTagLiveData(GetProjectDataWorker.TAG).observe(this, listOfWorkInfos -> {
			// If there are no matching work info, do nothing
			if (listOfWorkInfos == null || listOfWorkInfos.isEmpty()) {
				return;
			}

			// We only care about the first output status.
			WorkInfo workInfo = listOfWorkInfos.get(0);

			if (WorkInfo.State.RUNNING.equals(workInfo.getState())) {
				int phase = workInfo.getProgress().getInt(ConstantUtil.PHASE_KEY, 0);
				int sofar = workInfo.getProgress().getInt(ConstantUtil.SOFAR_KEY, 0);
				int total = workInfo.getProgress().getInt(ConstantUtil.TOTAL_KEY, 100);
				onFetchProgress(phase, sofar, total);
			}
		});
	}

    /**
     * handles result of refresh service
     */
    private void onFetchFinished(String err) {
        // Hide in-progress indicators
        inProgress.setVisibility(View.GONE);
        //Re-enable button
        btnRefresh.setEnabled(true);

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
     */
    private void onFetchProgress(int phase, int done, int total) {
        mInProgressBar.setMax(total);
        mInProgressBar.setProgress(done);
		switch (phase) {
			case 0:
				mInProgressWhat.setText(R.string.progress_projects);
				break;
			case 1:
				mInProgressWhat.setText(R.string.progress_updates);
				break;
			case 2:
				mInProgressWhat.setText(R.string.progress_photos);
				break;
			default:
				mInProgressWhat.setText("???");
				break;
		}
    }
}
