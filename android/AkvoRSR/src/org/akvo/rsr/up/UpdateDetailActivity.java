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
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

public class UpdateDetailActivity extends Activity {
	
	private String projectId = null;
	private String updateId = null;
	private Update update = null;
	private boolean editable;
	private boolean synching;
	private boolean debug;
	//UI
	private TextView projTitleLabel;
	private TextView projupdTitleText;
	private TextView projupdDescriptionText;
	private TextView projupdUser;
	private TextView synchFlag;
	private ImageView projupdImage;
	private Button btnEdit;
	//Database
	private RsrDbAdapter dba;
	
	//TODO: may want to have an onResume where we check if an upload is ongoing,
	// and if so display a progress indicator
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		debug = SettingsUtil.ReadBoolean(this, "setting_debug", false);

		//find which update we are showing
		//TODO, use uuid as update id? Would let us stick around while an update is posted (and changes id)
        projectId = getIntent().getStringExtra(ConstantUtil.PROJECT_ID_KEY);
        updateId = getIntent().getStringExtra(ConstantUtil.UPDATE_ID_KEY);
		if (projectId == null || updateId == null) {
			DialogUtil.errorAlert(this,"No project/update id", "Caller did not specify a project and update");
		}
		
		
		//get the look
		setContentView(R.layout.activity_update_detail);
		//find the fields
		projTitleLabel = (TextView) findViewById(R.id.projupd_edit_proj_title);
		projupdTitleText = (TextView) findViewById(R.id.projupd_detail_title);
		projupdDescriptionText = (TextView) findViewById(R.id.projupd_detail_descr);
		projupdImage = (ImageView) findViewById(R.id.image_update_detail);
		projupdUser = (TextView) findViewById(R.id.projupd_detail_user);
		synchFlag= (TextView) findViewById(R.id.projupd_detail_synchronising);

		//Activate buttons
				
		btnEdit = (Button) findViewById(R.id.btn_edit_update);
		btnEdit.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), UpdateEditorActivity.class);
				i.putExtra(ConstantUtil.PROJECT_ID_KEY, projectId);
				i.putExtra(ConstantUtil.UPDATE_ID_KEY, updateId);
				startActivity(i);
				finishThisActivity();//close this, as ID will change if update is published -- WINDOW LEAK!
			}
		});

		
		dba = new RsrDbAdapter(this);
		dba.open();

		Project project = dba.findProject(projectId);
		projTitleLabel.setText(project.getTitle());

		update = dba.findUpdate(updateId);
		if (update == null) {
			DialogUtil.errorAlert(this, "Update missing", "Cannot open for review, update " + updateId);
		} else {
			//populate fields
			synching = update.getUnsent();
			editable = update.getDraft() && !synching;
			projupdTitleText.setText(update.getTitle());	
			projupdDescriptionText.setText(update.getText());
			User author = dba.findUser(update.getUserId());
			if (author != null) {
				if (debug) {
					projupdUser.setText(author.getFirstname() + " " + author.getLastname() + "[" + update.getUserId() + "]");
				} else {
					projupdUser.setText(author.getFirstname() + " " + author.getLastname());
				}				
			} else {
				projupdUser.setText("[" + update.getUserId() + "]");
			}
			//show preexisting image
			if (update.getThumbnailFilename() != null) {
				FileUtil.setPhotoFile(projupdImage,update.getThumbnailUrl(),update.getThumbnailFilename(), null, updateId);
			}

		}
		dba.close();
		
		btnEdit.setEnabled(editable);
		btnEdit.setVisibility(editable?View.VISIBLE:View.GONE);
		synchFlag.setVisibility(synching?View.VISIBLE:View.GONE);
		
		// Show the Up button in the action bar.
		//		setupActionBar();
	}
	

	@Override
	protected void onDestroy() {
		if (dba != null) {
			dba.close();
		}
		super.onDestroy();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
        case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }

	}
	
	public void finishThisActivity() {
		finish();
	}

}
