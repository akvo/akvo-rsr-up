/*
 *  Copyright (C) 2015-2016 Stichting Akvo (Akvo Foundation)
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
import org.akvo.rsr.up.service.GetOrgDataService;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.viewadapter.OrgListCursorAdapter;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

public class OrgListActivity extends AppCompatActivity {


	private static final String TAG = "OrgListActivity";

	private RsrDbAdapter ad;
	private Cursor dataCursor;
    private TextView projCountLabel;
    private EditText searchField;
	private LinearLayout inProgress;
	private ProgressBar inProgress1;
	private ListView mList;
    private TextView mEmptyText;
    private TextView mFirstTimeText;
    private TextView mUnemployedText;
	private BroadcastReceiver broadRec;
    private Button searchButton;

    private boolean mEmployed; //False if user is not employed with any organisation
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    //employment can for now only change at login, so assign it for life of activity
	    mEmployed = !SettingsUtil.getAuthUser(this).getOrgIds().isEmpty();
	    
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_org_list);

        projCountLabel = (TextView) findViewById(R.id.orgcountlabel);
		inProgress = (LinearLayout) findViewById(R.id.orglistprogress);
		inProgress1 = (ProgressBar) findViewById(R.id.progressBar1);

        mList = (ListView) findViewById(R.id.list_orgs);
        mEmptyText = (TextView) findViewById(R.id.list_empty_text);
        mFirstTimeText = (TextView) findViewById(R.id.first_time_text);
        mUnemployedText = (TextView) findViewById(R.id.unemployed_text);
        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
// NYI               Intent i = new Intent(view.getContext(), OrgDetailActivity.class);
//                i.putExtra(ConstantUtil.PROJECT_ID_KEY, ((Long) view.getTag(R.id.project_id_tag)).toString());
//                startActivity(i); 
            }
        });
        
        
        searchButton = (Button) findViewById(R.id.btn_projsearch);        
        searchButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                //toggle visibility. When invisible, clear search string
                if (searchField.getVisibility() == View.VISIBLE){
                    searchField.setVisibility(View.GONE);
                    searchField.setText("");
                    // show magnifying glass
                    searchButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_search,0,0,0);
                    getData();
                } else {
                    searchField.setVisibility(View.VISIBLE);
                    //show X instead of magnifying glass
                    searchButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_close_normal,0,0,0);
                    searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
//                    searchField.requestFocus();
//                    showSoftKeyBoard(searchField);
                    }
            }
        });
 
        searchField = (EditText) findViewById(R.id.txt_projsearch);
        searchField.setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                    hideSoftKeyBoard();
                    // update list with new search string
                    getData();
                    return true;
                }
            }
        );

        //Create db
        ad = new RsrDbAdapter(this);
        ad.open();

		//register a listener for completion broadcasts
		IntentFilter f = new IntentFilter(ConstantUtil.ORGS_FETCHED_ACTION);
		f.addAction(ConstantUtil.ORGS_PROGRESS_ACTION);
		broadRec = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadRec, f);
        
        //in case we came back here during a refresh
        if (GetOrgDataService.isRunning(this)) {
            inProgress.setVisibility(View.VISIBLE);
        }
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
            startGetOrgsService();
            return true;
        case R.id.menu_settings:
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        case R.id.menu_messages:
            Intent i2 = new Intent(this, MessageActivity.class);
            startActivity(i2);
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

		int count;
		if (searchString == null || searchString.length() == 0) {
		    dataCursor = ad.listAllOrgs();
	        //Show count
		    count = dataCursor.getCount();
	        projCountLabel.setText(Integer.valueOf(count).toString());
		} else {
            dataCursor = ad.listAllOrgsMatching(searchString);		    
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
            } else
            if (searchString == null || searchString.length() == 0) { //must be empty DB
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
		OrgListCursorAdapter projects = new OrgListCursorAdapter(this, dataCursor);
		mList.setAdapter(projects);
	}

		
	/**
	 * starts the service fetching new project data
	 */
	private void startGetOrgsService() {
        if (GetOrgDataService.isRunning(this)) { //TODO should disable menu choice instead
            return; //only one at a time
        }
        
		//TODO: disable menu choice
		//start a service		
		Intent i = new Intent(this, GetOrgDataService.class);
		getApplicationContext().startService(i);
		
		//start progress animation
		inProgress.setVisibility(View.VISIBLE);
		inProgress1.setProgress(0);
	}

	
	/**
	 * handles result of refresh service
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
			DialogUtil.errorAlertWithDetail(this, R.string.errmsg_com_failure, R.string.msg_check_network, err);
		}

		//Refresh the list
		getData();
	}


	/**
	 * updates the progress bars as fetch progresses
	 * @param phase
	 * @param done
	 * @param total
	 */
	private void onFetchProgress(int done, int total) {
	    inProgress1.setIndeterminate(false);
	    inProgress1.setMax(total);
	    inProgress1.setProgress(done);
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
			if (intent.getAction() == ConstantUtil.ORGS_FETCHED_ACTION)
				onFetchFinished(intent);
			else if (intent.getAction() == ConstantUtil.ORGS_PROGRESS_ACTION)
				onFetchProgress(intent.getExtras().getInt(ConstantUtil.SOFAR_KEY, 0),
						        intent.getExtras().getInt(ConstantUtil.TOTAL_KEY, 100));
		}
	}
}
