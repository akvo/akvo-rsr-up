/*
 *  Copyright (C) 2012-2015 Stichting Akvo (Akvo Foundation)
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.viewadapter.ResultListCursorAdapter;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.Cursor;

public class ResultListActivity extends ActionBarActivity {


	private static final String TAG = "ResultListActivity";

	private RsrDbAdapter mDba;
	private Cursor dataCursor;
	private TextView mProjectTitleLabel;
	private TextView mResultCountLabel;
    private ListView mList;
    private String mProjId;
    private boolean mDebug;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDebug = SettingsUtil.ReadBoolean(this, "setting_debug", false);

		//find which project we will be showing results for
		Bundle extras = getIntent().getExtras();
		mProjId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
				: null;
		if (mProjId == null) {
			mProjId = savedInstanceState != null ? savedInstanceState
					.getString(ConstantUtil.PROJECT_ID_KEY) : null;
		}

		setContentView(R.layout.activity_result_list);

		mProjectTitleLabel = (TextView) findViewById(R.id.rlisttitlelabel);
		mResultCountLabel = (TextView) findViewById(R.id.resultcountlabel);
        mList = (ListView) findViewById(R.id.list_results);
/*
        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(view.getContext(), UpdateDetailActivity.class);
                i.putExtra(ConstantUtil.UPDATE_ID_KEY, ((Long) view.getTag(R.id.update_id_tag)).toString());
                i.putExtra(ConstantUtil.PROJECT_ID_KEY, ((Long) view.getTag(R.id.project_id_tag)).toString());
                startActivity(i);
            }
        });
        

		View listFooter = getLayoutInflater().inflate(R.layout.update_list_footer, null, false);
		//if the button were not the outermost view we would need to find it to set onClick
		Button addButton = (Button) listFooter.findViewById(R.id.btn_add_update2);
		addButton.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				startEditorNew();
			}
		});
		
		mList.addFooterView(listFooter);
  */      
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        //Create db

		mDba = new RsrDbAdapter(this);
        mDba.open();
	}

	private void startEditorNew() {
		Intent i3 = new Intent(this, UpdateEditorActivity.class);
		i3.putExtra(ConstantUtil.PROJECT_ID_KEY, mProjId);
		startActivity(i3);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            //NavUtils.navigateUpFromSameTask(this);
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

			}
		}
		if (mDba != null) {
			mDba.close();
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
			Log.w(TAG, "Could not close old cursor before reloading list",e);
		}

		//Show title
		Project p = mDba.findProject(mProjId);
		mProjectTitleLabel.setText(p.getTitle());
		//fetch data
		dataCursor = mDba.listResultsIndicatorsPeriodsFor(mProjId);

		//Populate list view
//		mList.setAdapter(new ResultListCursorAdapter(this, dataCursor));

        final int res_pk = dataCursor.getColumnIndex("result_id");
        final int ind_pk = dataCursor.getColumnIndex("indicator_id");
        final int per_pk = dataCursor.getColumnIndex("period_id");
        final int res_title = dataCursor.getColumnIndex("result_title");
        final int ind_title = dataCursor.getColumnIndex("indicator_title");
        final int per_start = dataCursor.getColumnIndex("period_start");
        final int per_end = dataCursor.getColumnIndex("period_end");
        final int actual_value = dataCursor.getColumnIndex("actual_value");
        final int target_value = dataCursor.getColumnIndex("target_value");
		ArrayList<String> list = new ArrayList<String>();
		int last_res = -1, last_ind = -1, resultCounter = 0, indicatorCounter = 0;
		
	    final SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		
		while (dataCursor.moveToNext()) {  //should be grouped on result and indicator
            int res = dataCursor.getInt(res_pk);
            int ind = dataCursor.getInt(ind_pk);
            int per = dataCursor.getInt(per_pk);
            if (res != last_res) {
                resultCounter++;
                String rt = dataCursor.getString(res_title);
                list.add(new String("Result [" + res + "]: " + rt));
                last_res = res;
                last_ind = -1;
            }
            
            if (ind > 0 ) { // ==0 if no indicators
                indicatorCounter++;
                String it = dataCursor.getString(ind_title);
                list.add(new String("    Indicator [" + ind + "]: " + it));                                
                last_ind = ind;
            }
            
            if (per > 0) {  // ==0 if no periods
                String av = dataCursor.getString(actual_value);
                String tv = dataCursor.getString(target_value);
                String ps = "";
                if (!dataCursor.isNull(per_start)) {
                    Date d = new Date(dataCursor.getInt(per_start)*1000L);
                    ps = dateOnly.format(d);
                }
                String pe = "";
                if (!dataCursor.isNull(per_end)) {
                    Date d = new Date(dataCursor.getInt(per_end)*1000L);
                    ps = dateOnly.format(d);
                }
                if (mDebug) {
                    list.add(new String("        Period [" + per + "] " + ps + "--" + pe + " : " + av + "/" + tv ));
                } else {
                    list.add(new String("        " + ps + "--" + pe + " : " + av + "/" + tv ));
                }
            }
		}

		//Show count
        mResultCountLabel.setText(resultCounter + ", " + indicatorCounter);

        mList.setAdapter(new ArrayAdapter<String>(this,R.layout.result_list_item,R.id.result_item_test,list));
	}	

}
