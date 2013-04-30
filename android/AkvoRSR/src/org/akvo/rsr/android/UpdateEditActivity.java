/*
 *  Copyright (C) 2012-2013 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.android;

import java.io.Closeable;
import java.io.File;
import java.util.Currency;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.Project;
import org.akvo.rsr.android.domain.Update;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;

public class UpdateEditActivity extends Activity {
	
	private final int photoRequest = 777;
	private  String captureFilename = null;
	
	private int nextLocalId = -1; //TODO load from /save to variable store
	private String projectId = null;
	private String updateId = null;
	private Update update = null;
	//UI
	private TextView projTitleLabel;
	private EditText projupdTitleText;
	private EditText projupdDescriptionText;
	private ImageView projImage;
	private Button btnSubmit;
	private Button btnDraft;
	private Button btnPhoto;
	//Database
	private RsrDbAdapter dba;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//find which update we are editing
		//null means create a new one
		Bundle extras = getIntent().getExtras();
		projectId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
				: null;
		if (projectId == null) {
			DialogUtil.errorAlert(this,"","");
		}
		updateId = extras != null ? extras.getString(ConstantUtil.UPDATE_ID_KEY)
				: null;
		if (updateId == null) {
			updateId = savedInstanceState != null ? savedInstanceState
					.getString(ConstantUtil.UPDATE_ID_KEY) : null;
		}
		
		
		//get the look
		setContentView(R.layout.activity_edit_update);
		//find the fields
		projTitleLabel = (TextView) findViewById(R.id.projupd_edit_proj_title);
		projupdTitleText = (EditText) findViewById(R.id.edit_projupd_title);
		projupdDescriptionText = (EditText) findViewById(R.id.edit_projupd_description);
		projImage = (ImageView) findViewById(R.id.image_proj_detail);

		//Activate buttons
		btnSubmit = (Button) findViewById(R.id.btn_send_update);
		btnSubmit.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				sendUpdate();
			}
		});
		
		btnDraft = (Button) findViewById(R.id.btn_save_draft);
		btnDraft.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				saveAsDraft();
			}
		});
		
		btnPhoto = (Button) findViewById(R.id.btn_take_photo);
		btnPhoto.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
			    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    // generate unique filename
			    captureFilename = Environment.getExternalStorageDirectory() + ConstantUtil.PHOTO_DIR + "capture" + System.nanoTime() + ".jpg";
			    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(captureFilename)));
			    startActivityForResult(takePictureIntent, photoRequest);
			}
		});
		
		dba = new RsrDbAdapter(this);
		dba.open();
		
		if (updateId == null) { //create new
			update = new Update();
		} else {
			update = dba.findUpdate(updateId);
			if (updateId == null) {
				//TODO raise error
			}
			
			//TODO populate fields
		}
		
		// Show the Up button in the action bar.
		//		setupActionBar();
	}

	/*
	 * (non-Javadoc)
	 * Get notification of photo taken
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == photoRequest) {
			if (resultCode == RESULT_CANCELED) {
				return;
			}
			//Handle taken photo
			if (new File(captureFilename).exists()) {
				update.setThumbnailFilename(captureFilename);
				DialogUtil.infoAlert(this, "Photo returned", "Got a photo");
			}
			//TODO take thumbnail in intent and show it on form
		}
	}

	/*
	 * Save current update
	 */
	private void saveAsDraft() {
		update.setDraft(true);
		update.setTitle(projupdTitleText.getText().toString());
		update.setText(projupdDescriptionText.getText().toString());
		//MUST have project and a local update id
		update.setProjectId(projectId);
		update.setId(Integer.toString(nextLocalId));
		nextLocalId--;
		dba.saveUpdate(update);
		DialogUtil.errorAlert(this, "Update saved as draft", "You can edit and submit it later");
		finish();
	}

	/*
	 * Save current update
	 */
	private void sendUpdate() {
		update.setUnsent(true);
		update.setTitle(projupdTitleText.getText().toString());
		update.setText(projupdDescriptionText.getText().toString());
		dba.saveUpdate(update);
		//TODO start synch service
		finish();
		
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		dba.open();
		if (projectId != null) {
			Project project = dba.findProject(projectId);
			projTitleLabel.setText(project.getTitle());
		} else {
			projTitleLabel.setText("<NO PROJECT ID>");
		}
/*		
		//Find file containing thumbnail		
		File f = new File(project.getThumbnailFilename());
		if (f.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
			if (bm != null)
				projImage.setImageBitmap(bm);
		} else {
			//Fall back to generic logo
			projImage.setImageResource(R.drawable.ic_launcher);
		}
		*/

	}
	
	@Override
	protected void onPause() {
		super.onPause();
		dba.close();
	}
	

	@Override
	protected void onDestroy() {
		if (dba != null) {
			dba.close();
		}
		super.onDestroy();
	}

	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 *
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.project_detail, menu);
		return true;
	}


}
