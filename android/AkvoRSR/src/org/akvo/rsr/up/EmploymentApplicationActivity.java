/*
 *  Copyright (C) 2016 Stichting Akvo (Akvo Foundation)
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

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Country;
import org.akvo.rsr.up.domain.Organisation;
import org.akvo.rsr.up.service.GetOrgDataService;
import org.akvo.rsr.up.service.SubmitEmploymentService;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.SettingsUtil;

import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author stellan
 *
 * Displays a list of current employments ("connections"), approved and non-approved, and a form that lets user add more.
 * Organisation and country input fields use completion.
 */
public class EmploymentApplicationActivity extends BackActivity implements OnItemSelectedListener,OnItemClickListener {

    private static final String TAG = "EmploymentApplicationActivity";
    
    private ScrollView mScroll;
    private TextView mList;
    private AutoCompleteTextView mOrganisationEdit;
    private AutoCompleteTextView mCountryEdit;
    private TextView mSelectedOrg;
    private EditText mJobTitle;
    
    private LinearLayout mProgress;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mBroadRec;
    private Button mApplyButton;

    private RsrDbAdapter mDba;
    private String mSelectedOrgId;

    private static String[] orgNames = new String[1]; 
    private static String[] countryNames = new String[1]; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_employment_application);

        //UI
        mScroll = (ScrollView) findViewById(R.id.scroll);
        mList = (TextView) findViewById(R.id.employment_list);
        mOrganisationEdit = (AutoCompleteTextView) findViewById(R.id.org_name);
        mOrganisationEdit.setThreshold(1);
        mOrganisationEdit.setOnItemSelectedListener(this);
        mOrganisationEdit.setOnItemClickListener(this);

        mCountryEdit = (AutoCompleteTextView) findViewById(R.id.country_name);
        mCountryEdit.setThreshold(1);

        mSelectedOrg = (TextView) findViewById(R.id.selected_org);
        mJobTitle = (EditText) findViewById(R.id.job_title);

        mProgress = (LinearLayout) findViewById(R.id.application_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        
        mApplyButton = (Button) findViewById(R.id.btn_send_result);
        mApplyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendIt(v);
            }
        });
        mApplyButton.setEnabled(false); //until something is selected

        // register a listener for completion intents
        IntentFilter filter = new IntentFilter(ConstantUtil.EMPLOYMENT_SENT_ACTION);
        filter.addAction(ConstantUtil.ORGS_FETCHED_ACTION);
        filter.addAction(ConstantUtil.ORGS_PROGRESS_ACTION);
        mBroadRec = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadRec, filter);
        
        //Create db
        mDba = new RsrDbAdapter(this);

        getData();
        getOrgs();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadRec);
        super.onDestroy();
    }

    
    private void getOrgs() {
        mDba.open();
        orgNames = mDba.getOrgNameList().toArray(orgNames);
        countryNames = mDba.getCountryNameList().toArray(countryNames);
        mDba.close();
        //Make adapters for completion
        ArrayAdapter<String> orgAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                orgNames);
        mOrganisationEdit.setAdapter(orgAdapter);        
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                countryNames);
        mCountryEdit.setAdapter(countryAdapter);        
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.org_list, menu);
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
         default:
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * show list of all employments for this user
     */
    private void getData() {
        //fetch data
        mDba.open();
        Cursor dataCursor = mDba.listAllEmploymentsForUser(SettingsUtil.getAuthUser(this).getId());
        mDba.close();
        String list = "";
        if (dataCursor.getCount() > 0) {
            final int org_name_col = dataCursor.getColumnIndex("name"); //from join
            final int approved_col = dataCursor.getColumnIndex(RsrDbAdapter.APPROVED_COL);
            final int groupname_col = dataCursor.getColumnIndex(RsrDbAdapter.GROUP_NAME_COL);
            while (dataCursor.moveToNext()) {  //must be grouped on result and indicator
                String org_name = dataCursor.getString(org_name_col);
                if (dataCursor.getInt(approved_col)==0) {
                    org_name += " - pending";//TODO localization
                } else {
                    org_name += " (" + dataCursor.getString(groupname_col) + ")";
                }
                list += org_name + "\n";
            }
        }
        dataCursor.close();
        mList.setText(list);
    }   
        
    
    
    /**
     * starts the service fetching new project data
     */
    private void startGetOrgsService() {
        if (GetOrgDataService.isRunning(this)) { //TODO should disable menu choice instead
            return; //only one at a time
        }
        
        //start a service       
        Intent i = new Intent(this, GetOrgDataService.class);
        //brief info fetch is the default
        getApplicationContext().startService(i);
        
        //start progress animation
        mProgress.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
    }

    
    /**
     * handles result of refresh service
     * @param intent
     */
    private void onFetchFinished(Intent intent) {
        // Hide in-progress indicators
        mProgress.setVisibility(View.GONE);
        
        String err = intent.getStringExtra(ConstantUtil.SERVICE_ERRMSG_KEY);
        if (err == null) {
            Toast.makeText(getApplicationContext(), R.string.msg_fetch_complete, Toast.LENGTH_SHORT).show();
        } else {
            //show a dialog instead
            DialogUtil.errorAlertWithDetail(this, R.string.errmsg_com_failure, R.string.msg_check_network, err);
        }

        //Refresh the lists
        getOrgs();
        getData();
    }


    /**
     * updates the progress bars as fetch progresses
     * @param phase
     * @param done
     * @param total
     */
    private void onFetchProgress(int done, int total) {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(total);
        mProgressBar.setProgress(done);
        }

    
    
    /**
     * starts the submission process
     * 
     * @param view
     */
    private void sendIt(View view) {
        // We must have a connection
        if (!Downloader.haveNetworkConnection(this, false)) {
            // helpful error message, instead of a failure later
            DialogUtil.errorAlert(this, R.string.nonet_dialog_title, R.string.nonet_dialog_msg);
            return;
        }
        //Look up country id
        mDba.open();
        Country c = mDba.findCountryByName(mCountryEdit.getText().toString()); 
        mDba.close();
        
        //start progress animation
        mProgress.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);

        // start the upload service
        Intent intent = new Intent(this, SubmitEmploymentService.class);
        intent.putExtra(ConstantUtil.ORG_ID_KEY, mSelectedOrgId);
        if (c != null) intent.putExtra(ConstantUtil.COUNTRY_ID_KEY, c.getId());
        String jt = mJobTitle.getText().toString();
        if (jt != null) intent.putExtra(ConstantUtil.JOB_TITLE_KEY, jt);
        getApplicationContext().startService(intent);
        // now we wait for a broadcast...
    }

    
    /**
     * completes the sign-in process after network activity is done
     * 
     * @param intent
     */
    private void onSendFinished(Intent intent) {
        // Dismiss any in-progress dialog
        mProgress.setVisibility(View.GONE);

        String err = intent.getStringExtra(ConstantUtil.SERVICE_ERRMSG_KEY);
        if (err == null) {
            getData(); //show the new employment record
            findViewById(R.id.mainLayout).requestFocus(); //clear focus from form fields
            mScroll.scrollTo(0, 0); //Scroll up top to show new entry in list
            String msg = getResources().getString(R.string.msg_emp_applied);
            DialogUtil.infoAlert(this, R.string.msg_send_success, R.string.msg_emp_applied);
//            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        } else {
            // stay on this page
            DialogUtil.errorAlert(this, "Error", err);
        }
    }


    /**
     * Broadcast receiver for receiving status updates from any IntentService
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
            else if (intent.getAction() == ConstantUtil.EMPLOYMENT_SENT_ACTION) {
                onSendFinished(intent);
            }
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSelectedOrg.setText("["+position+"]"); 
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mSelectedOrg.setText("-----"); 
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = (String)parent.getItemAtPosition(position); //COULD BE COUNTRY NAME
        mDba.open();
        Organisation o = mDba.findOrgByName(name); 
        mDba.close();
        if (o != null) {
            mSelectedOrg.setText(o.getLongName());
            mSelectedOrgId = o.getId();
            mApplyButton.setEnabled(true);
        } else {
            mApplyButton.setEnabled(false);
        }
    }

}
