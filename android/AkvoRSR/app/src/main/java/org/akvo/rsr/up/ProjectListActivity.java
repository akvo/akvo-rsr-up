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
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.viewadapter.ProjectListCursorAdapter;
import org.akvo.rsr.up.worker.GetProjectDataWorker;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.database.Cursor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;

public class ProjectListActivity extends AppCompatActivity {

	private static final String TAG = "ProjectListActivity";

	private RsrDbAdapter ad;
	private Cursor dataCursor;
	private TextView projCountLabel;
	private EditText searchField;
	private LinearLayout inProgress;
	private ProgressBar inProgress1;
	private ProgressBar inProgress2;
	private ProgressBar inProgress3;
	private ListView mList;
	private TextView mEmptyText;
	private TextView mFirstTimeText;
	private TextView mUnemployedText;
	private Button searchButton;

	private boolean isBeingCreated = true;

	private boolean mEmployed; //False if user is not employed with any organisation

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_list);
		//employment can for now only change at login, so assign it for life of activity
		mEmployed = !SettingsUtil.getAuthUser(this).getOrgIds().isEmpty();

		projCountLabel = (TextView) findViewById(R.id.projcountlabel);
		inProgress = (LinearLayout) findViewById(R.id.projlistprogress);
		inProgress1 = (ProgressBar) findViewById(R.id.progressBar1);
		inProgress2 = (ProgressBar) findViewById(R.id.progressBar2);
		inProgress3 = (ProgressBar) findViewById(R.id.progressBar3);

		mList = (ListView) findViewById(R.id.list_projects);
		mEmptyText = (TextView) findViewById(R.id.list_empty_text);
		mFirstTimeText = (TextView) findViewById(R.id.first_time_text);
		mUnemployedText = (TextView) findViewById(R.id.unemployed_text);
		mList.setOnItemClickListener((OnItemClickListener) (parent, view, position, id) -> {
			Intent i = new Intent(view.getContext(), ProjectDetailActivity.class);
			i.putExtra(ConstantUtil.PROJECT_ID_KEY, ((Long) view.getTag(R.id.project_id_tag)).toString());
			startActivity(i);
		});

		searchButton = (Button) findViewById(R.id.btn_projsearch);
		searchButton.setOnClickListener(view -> {
			//toggle visibility. When invisible, clear search string
			if (searchField.getVisibility() == View.VISIBLE) {
				searchField.setVisibility(View.GONE);
				searchField.setText("");
				// show magnifying glass
				searchButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_search, 0, 0, 0);
				getData();
			} else {
				searchField.setVisibility(View.VISIBLE);
				//show X instead of magnifying glass
				searchButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_close_normal, 0, 0, 0);
				searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
				searchField.requestFocus();
				showSoftKeyBoard(searchField);
			}
		});

		searchField = (EditText) findViewById(R.id.txt_projsearch);
		searchField.setOnEditorActionListener((v, actionId, event) -> {
					hideSoftKeyBoard();
					// update list with new search string
					getData();
					return true;
				}
		);

		//Create db
		ad = new RsrDbAdapter(this);
		Log.d(TAG, "Opening DB during create");
		ad.open();

		setUpWorkManagerListeners();
	}

	private void setUpWorkManagerListeners() {
		WorkManager workManager = WorkManager.getInstance(getApplicationContext());
		workManager.getWorkInfosForUniqueWorkLiveData(GetProjectDataWorker.TAG).removeObservers(this);
		LiveData<List<WorkInfo>> uniqueWorkLiveData = workManager.getWorkInfosForUniqueWorkLiveData(GetProjectDataWorker.TAG);
		uniqueWorkLiveData.observe(this, listOfWorkInfos -> {
			// If there are no matching work info, do nothing
			if (listOfWorkInfos == null || listOfWorkInfos.isEmpty()) {
				return;
			}

			// We only care about the first output status.
			WorkInfo workInfo = listOfWorkInfos.get(0);
			boolean finished = workInfo.getState().isFinished();

			if (finished) {
				//if the task was finished before activity started, we do not need to do anything
				if (!isBeingCreated) {
					// Dismiss any in-progress dialog
					String err = workInfo.getOutputData().getString(ConstantUtil.SERVICE_ERRMSG_KEY);
					onFetchFinished(err);
				}
			} else {
				inProgress.setVisibility(View.VISIBLE);
			}
		});

		uniqueWorkLiveData.observe(this, listOfWorkInfos -> {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (SettingsUtil.ReadBoolean(this, "setting_debug", false)) {
			getMenuInflater().inflate(R.menu.project_list_debug, menu); //with debug items
		} else {
			getMenuInflater().inflate(R.menu.project_list, menu); //vanilla version
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				startGetProjectsService();
				return true;
			case R.id.menu_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.menu_messages:
				startActivity(new Intent(this, MessageActivity.class));
				return true;
			case R.id.menu_connect:
				startActivity(new Intent(this, EmploymentApplicationActivity.class));
				return true;
			case R.id.menu_organisations:
				startActivity(new Intent(this, OrgListActivity.class));
				return true;
			case R.id.menu_diagnostics:
				startActivity(new Intent(this, DiagnosticActivity.class));
				return true;
			case R.id.menu_logout:
				SettingsUtil.signOut(this);
				//Fire up the login screen
				Intent mainIntent = new Intent(this, LoginActivity.class);
				startActivity(mainIntent);
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getData();
		isBeingCreated = false;
	}

	@Override
	protected void onDestroy() {
		if (dataCursor != null) {
			Log.d(TAG, "Closing cursor during destroy");
			dataCursor.close();
		}
		if (ad != null) {
			Log.d(TAG, "Closing DB during destroy");
			ad.close();
		}
		super.onDestroy();
	}

	private void hideSoftKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		if (imm.isAcceptingText()) { // verify if the soft keyboard is open
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private void showSoftKeyBoard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.showSoftInput(v, 0);
	}

	/**
	 * shows all projects visible to this user.
	 * Assumes DB is open
	 */
	private void getData() {
		try {
			if (dataCursor != null) {
				Log.d(TAG, "Closing cursor");
				dataCursor.close();
			}
		} catch (Exception e) {
			Log.w(TAG, "Could not close old cursor before reloading list", e);
		}
		String searchString = searchField.getText().toString();

		int count;
		if (searchString == null || searchString.length() == 0) {
			dataCursor = ad.listVisibleProjectsWithCountry();
			//Show count
			count = dataCursor.getCount();
			projCountLabel.setText(Integer.valueOf(count).toString());
		} else {
			dataCursor = ad.listVisibleProjectsWithCountryMatching(searchString);
			//Show count
			count = dataCursor.getCount();
			projCountLabel.setText("(" + Integer.valueOf(count).toString() + ")");
		}
		if (count == 0) { //no records, but why?
			mList.setVisibility(View.GONE);
			if (!mEmployed) {
				mEmptyText.setVisibility(View.GONE);
				mFirstTimeText.setVisibility(View.GONE);
				mUnemployedText.setVisibility(View.VISIBLE);
			} else if (searchString == null || searchString.length() == 0) { //must be empty DB
				mEmptyText.setVisibility(View.GONE);
				mFirstTimeText.setVisibility(View.VISIBLE);
				mUnemployedText.setVisibility(View.GONE);
			} else { //too filtered
				mEmptyText.setVisibility(View.VISIBLE);
				mFirstTimeText.setVisibility(View.GONE);
				mUnemployedText.setVisibility(View.GONE);
			}
		} else {
			mList.setVisibility(View.VISIBLE);
			mEmptyText.setVisibility(View.GONE);
			mFirstTimeText.setVisibility(View.GONE);
			mUnemployedText.setVisibility(View.GONE);
		}
		//Populate list view
		ProjectListCursorAdapter projects = new ProjectListCursorAdapter(this, dataCursor);
		mList.setAdapter(projects);
	}

	/**
	 * starts the service fetching new project data
	 */
	private void startGetProjectsService() {
		if (!mEmployed) { //TODO should disable menu choice instead
			return; //fetch would fail
		}
        
		//start progress animation
		inProgress.setVisibility(View.VISIBLE);
		inProgress1.setProgress(0);
		inProgress2.setProgress(0);
		inProgress3.setProgress(0);

		WorkManager workManager = WorkManager.getInstance(getApplicationContext());
		Data.Builder builder = new Data.Builder();
		OneTimeWorkRequest oneTimeWorkRequest =
				new OneTimeWorkRequest.Builder(GetProjectDataWorker.class)
						.addTag(GetProjectDataWorker.TAG)
						.setInputData(builder.build())
						.build();
		workManager.enqueueUniqueWork(GetProjectDataWorker.TAG, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
	}

	/**
	 * handles result of refresh service
	 */
	private void onFetchFinished(String err) {
		// Hide in-progress indicators
		inProgress.setVisibility(View.GONE);

		if (err == null) {
			Toast.makeText(getApplicationContext(), R.string.msg_fetch_complete, Toast.LENGTH_SHORT).show();
		} else {
			DialogUtil.errorAlertWithDetail(this, R.string.errmsg_com_failure, R.string.msg_check_network, err);
		}

		//Refresh the list
		getData();
	}

	/**
	 * updates the progress bars as fetch progresses
	 */
	private void onFetchProgress(int phase, int done, int total) {
		if (phase == 0) {
			inProgress1.setIndeterminate(false);
			inProgress1.setMax(total);
			inProgress1.setProgress(done);
		}
		if (phase == 1) {
			inProgress1.setMax(100);
			inProgress1.setProgress(100);
			inProgress2.setMax(total);
			inProgress2.setProgress(done);
		}
		if (phase == 2) {
			inProgress2.setMax(100);
			inProgress2.setProgress(100);
			inProgress3.setMax(total);
			inProgress3.setProgress(done);
		}
		getData();
	}
}
