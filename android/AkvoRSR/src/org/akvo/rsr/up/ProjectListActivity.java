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
import org.akvo.rsr.up.service.GetProjectDataService;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.viewadapter.ProjectListCursorAdapter;

import android.os.Bundle;
import android.app.ListActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

public class ProjectListActivity extends ListActivity {


	private static final String TAG = "ProjectListActivity";

	private RsrDbAdapter ad;
	private Cursor dataCursor;
    private TextView projCountLabel;
    private EditText searchField;
	private LinearLayout inProgress;
	private ProgressBar inProgress1;
	private ProgressBar inProgress2;
	private ProgressBar inProgress3;
	private BroadcastReceiver broadRec;
    private Button searchButton;
    private Button refreshButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_list);

        projCountLabel = (TextView) findViewById(R.id.projcountlabel);
		inProgress = (LinearLayout) findViewById(R.id.projlistprogress);
		inProgress1 = (ProgressBar) findViewById(R.id.progressBar1);
		inProgress2 = (ProgressBar) findViewById(R.id.progressBar2);
		inProgress3 = (ProgressBar) findViewById(R.id.progressBar3);

        searchButton = (Button) findViewById(R.id.btn_projsearch);        
        searchButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                //toggle visibility. When invisible, clear search string
                if (searchField.getVisibility() == View.VISIBLE){
                    searchField.setVisibility(View.GONE);
                    searchField.setText("");
                    getData();
                } else {
                    searchField.setVisibility(View.VISIBLE);
                    //TODO set focus
                    
                }
            }
        });
 
        searchField = (EditText) findViewById(R.id.txt_projsearch);
        searchField.setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    hideSoftKeyBoard();
                    // update list with new search string
                    getData();
                    return true;
                }
            }
        );

        
        refreshButton = (Button) findViewById(R.id.btn_refresh_projects);        
        refreshButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                //fetch new data
                startGetProjectsService();
            }
        });
 
        //Create db
        ad = new RsrDbAdapter(this);
        Log.d(TAG, "Opening DB during create");
        ad.open();

		//register a listener for completion broadcasts
		IntentFilter f = new IntentFilter(ConstantUtil.PROJECTS_FETCHED_ACTION);
		f.addAction(ConstantUtil.PROJECTS_PROGRESS_ACTION);
		broadRec = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadRec, f);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.project_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
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
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getData();
	}
	
	
	@Override
	protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadRec);
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

	    if(imm.isAcceptingText()) { // verify if the soft keyboard is open                      
	        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	    }
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
		} catch(Exception e) {
			Log.w(TAG, "Could not close old cursor before reloading list", e);
		}
		String searchString = searchField.getText().toString();

		if (searchString == null || searchString.length() == 0) {
		    dataCursor = ad.listVisibleProjectsWithCountry();
	        //Show count
	        projCountLabel.setText(Integer.valueOf(dataCursor.getCount()).toString());
		} else {
            dataCursor = ad.listVisibleProjectsWithCountryMatching(searchString);		    
            //Show count
            projCountLabel.setText("(" + Integer.valueOf(dataCursor.getCount()).toString() + ")");
		}
		//Populate list view
		ProjectListCursorAdapter projects = new ProjectListCursorAdapter(this, dataCursor);
		setListAdapter(projects);
	}

	
	/**
	 *  gets the id of the clicked list item and opens the one-project activity.
	 */
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);

		Intent i = new Intent(view.getContext(), ProjectDetailActivity.class);
		i.putExtra(ConstantUtil.PROJECT_ID_KEY, ((Long) view.getTag(R.id.project_id_tag)).toString());
		startActivity(i);
	}

	
	/**
	 * starts the service fetching new project data
	 */
	private void startGetProjectsService() {
		//disable refresh button
		refreshButton.setEnabled(false);
		//start a service
		
		Intent i = new Intent(this, GetProjectDataService.class);
		getApplicationContext().startService(i);
		
		//start progress animation
		inProgress.setVisibility(View.VISIBLE);
		inProgress1.setProgress(0);
		inProgress2.setProgress(0);
		inProgress3.setProgress(0);
	}

	
	/**
	 * handles result of server login attempt
	 * @param intent
	 */
	private void onFetchFinished(Intent intent) {
		// Hide in-progress indicators
		inProgress.setVisibility(View.GONE);
		
		String err = intent.getStringExtra(ConstantUtil.SERVICE_ERRMSG_KEY);
		if (err == null) {
			Toast.makeText(getApplicationContext(), R.string.msg_fetch_complete, Toast.LENGTH_SHORT).show();
		} else {
			//show a dialog instead
			DialogUtil.errorAlert(this, "Error", err);
		}

		//re-enable the refresh button
		refreshButton.setEnabled(true);		
		//Refresh the list
		getData();
	}


	/**
	 * updates the progress bars
	 * @param phase
	 * @param done
	 * @param total
	 */
	private void onFetchProgress(int phase, int done, int total) {
		if (phase == 0) {
			inProgress1.setIndeterminate(false);
            inProgress1.setMax(total);
			inProgress1.setProgress(done);
			}
		if (phase == 1) {
            inProgress1.setMax(100);
			inProgress1.setProgress(100);//make it look good, just in case...
            inProgress2.setMax(total);
			inProgress2.setProgress(done);
			}
		if (phase == 2) {
            inProgress2.setMax(100);
			inProgress2.setProgress(100);//make it look good, just in case...
            inProgress3.setMax(total);
			inProgress3.setProgress(done);
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
