package org.akvo.rsr.android;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.service.GetProjecDataService;
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

public class UpdateListActivity extends ListActivity {


	private static final String TAG = "UpdateListActivity";

	private RsrDbAdapter ad;
	private Cursor dataCursor;
	private TextView updateCountLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_list);

		updateCountLabel = (TextView) findViewById(R.id.updatecountlabel);
 
        //Create db
        ad = new RsrDbAdapter(this);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_project_list, menu);
		return true;
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
		dataCursor = ad.findAllUpdates();
		//Show count
		updateCountLabel.setText(Integer.valueOf(dataCursor.getCount()).toString());
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
		i.putExtra(ConstantUtil.UPDATE_ID_KEY, ((Long) view.getTag(R.id.update_id_tag)).toString());
		startActivity(i);
	}

	
	/*
	 * Start the service fetching new update data
	 */
	private void startGetProjectsService() {
		//TODO start a real service, register a listener for a completion intent
//		Intent i = new Intent(this, GetProjecDataService.class);
//		i.putExtra(SERVER_KEY, "http://test.akvo.org");
//		i.putExtra(URL_KEY, "/api/v1/project/?format=xml"); //get first 20 by default
//		getApplicationContext().startService(i);
		//meanwhile:
		Downloader dl = new Downloader();
		//TODO THIS MIGHT HANG, no timeout defined...
		dl.FetchUpdateList(this,"http://test.akvo.org","/api/v1/update/?format=xml&partnerships__organisation=42");//Akvo updates
		dl.FetchNewThumbnails(this, "http://test.akvo.org", Environment.getExternalStorageDirectory()+"/"+ConstantUtil.IMAGECACHE_DIR);	
	}



}
