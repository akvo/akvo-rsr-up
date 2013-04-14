package org.akvo.rsr.android;

import org.akvo.rsr.android.dao.RsrDbAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class ProjectListActivity extends Activity {

	RsrDbAdapter ad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_project_list);
        //Create db
        ad = new RsrDbAdapter(this);
        
		Button refreshButton = (Button) findViewById(R.id.menu_refresh_projects);
		refreshButton.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				ad.clearAllData();
				//fetch new data
				//TODO
				//redisplay list
				//TODO
			}
		});
 
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.project_list, menu);
		return true;
	}


}
