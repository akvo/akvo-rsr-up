package org.akvo.rsr.android;

import org.akvo.rsr.android.dao.RsrDbAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ProjectListActivity extends Activity {

	RsrDbAdapter ad;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        //Create db
        ad = new RsrDbAdapter(this);
        
		Button refreshButton = (Button) findViewById(R.id.menu_refresh_projects);
		refreshButton.setOnClickListener( new   View.OnClickListener() {
			public void onClick(View view) {
				ad.clearAllData();
				//fetch new data
				//TODO
				//redisplay list
				//TODO
			}
		});
        
        //temporary simple string list
        String[] s = {"FOO","BAR","BAZ"}; 
//        ArrayAdapter adapter = new ArrayAdapter<String>(this, 
//        		android.R.layout.simple_list_item_1, s);
//        setListAdapter(adapter);
    }
    
  

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_project_list, menu);
        return true;
    }
    
    //Project item pushed
    public void ShowProject(View view){
    	//for demo, open the one static project screen
	    Intent intent = new Intent(this, ProjectDetailActivity.class);
	    startActivity(intent);
    }


    
}
