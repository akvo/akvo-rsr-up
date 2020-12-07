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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Organisation;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.ThumbnailUtil;

public class UpdateDetailActivity extends AppCompatActivity {
	
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
    private TextView projupdPhotoCredit;
    private TextView projupdPhotoCaption;
	private TextView synchFlag;
	private TextView projupdLocationText;
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
			DialogUtil.errorAlert(this,R.string.noid_dialog_title, R.string.noid_dialog_msg);
		}
		
		
		//get the look
		setContentView(R.layout.activity_update_detail);
		//find the fields
		projTitleLabel = (TextView) findViewById(R.id.projupd_edit_proj_title);
		projupdTitleText = (TextView) findViewById(R.id.projupd_detail_title);
        projupdDescriptionText = (TextView) findViewById(R.id.projupd_detail_descr);
        projupdPhotoCaption = (TextView) findViewById(R.id.projupd_detail_photo_caption);
        projupdPhotoCredit = (TextView) findViewById(R.id.projupd_detail_photo_credit);
        projupdImage = (ImageView) findViewById(R.id.image_update_detail);
        projupdUser = (TextView) findViewById(R.id.projupd_detail_user);
        projupdLocationText = (TextView) findViewById(R.id.text_projupd_location);
        synchFlag = (TextView) findViewById(R.id.projupd_detail_synchronising);

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
		try {
    		Project project = dba.findProject(projectId);
    		projTitleLabel.setText(project.getTitle());
    
    		update = dba.findUpdate(updateId);
    		if (update == null) {
    			DialogUtil.errorAlert(this, R.string.noupd_dialog_title ,R.string.noupd_dialog_msg);
    		} else {
    			//populate fields
    			synching = update.getUnsent();
    			editable = update.getDraft() && !synching;
    			projupdTitleText.setText(update.getTitle());	
    			projupdDescriptionText.setText(update.getText());
                projupdPhotoCaption.setText(update.getPhotoCaption());
                if (update.getPhotoCredit() != null && update.getPhotoCredit().length() > 0) {
                    projupdPhotoCredit.setText(getResources().getString(R.string.label_photo_credit, update.getPhotoCredit()));                    
                }
    			User author = dba.findUser(update.getUserId());
    			Organisation org = null;
    			String sig = "";
    			if (author != null) {
    			    sig += author.getFirstname() + " " + author.getLastname();
    			    if (author.getOrgId() != null) org = dba.findOrganisation(author.getOrgId());
    	            if (org != null) sig += ", " + org.getName();
    			}
    			if (author == null || debug) {
    				sig += "[" + update.getUserId() + "]";
    			}
    		    projupdUser.setText(sig);
    		    ThumbnailUtil.setPhotoFile(projupdImage,update.getThumbnailUrl(),update.getThumbnailFilename(), null, updateId, true);
    		    
    		    String loc = "";
	            if (update.getCity() != null && update.getCity().length() > 0) {
	                loc += update.getCity() + ", ";
	            }
	            if (update.getState() != null && update.getState().length() > 0) {
	                loc += update.getState() + ", ";           
	            }
	            if (update.getCountry() != null && update.getCountry().length() > 0) {
	                loc += update.getCountry() + ", ";         
	            }
	            if (loc.length() > 1) {
	                loc = loc.substring(0, loc.length()-2);
	            }
	    
	            //TODO check against 0,0 too?
	            //TODO string constant!
	            if (update.validLatLon()) {
	                loc += "\nLatitude " + update.getLatitude() +
 	                        " Longitude " + update.getLongitude();
	                projupdLocationText.setOnClickListener(
	                    new OnClickListener() {
	                        @Override
	                        public void onClick(View v) {
	                            launchLatLonIntent();
	                        }
	                    });
	            } else {
	                projupdLocationText.setOnClickListener(null);
	            }
	    
	            projupdLocationText.setText(loc);

    		}
		}
		finally {
		    dba.close();
		}
		
		btnEdit.setEnabled(editable);
		btnEdit.setVisibility(editable?View.VISIBLE:View.GONE);
		synchFlag.setVisibility(synching?View.VISIBLE:View.GONE);
		
		// Show the Up button in the action bar.
		//		setupActionBar();
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.update_detail, menu);
        return true;
    }

    /**
     * disables/enables delete button to match update status
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem del = menu.findItem(R.id.action_delete_update);
        del.setEnabled(update.getUnsent() || update.getDraft());
        return true;
    }

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
            case R.id.action_delete_update:
                if (update.getUnsent() || update.getDraft()) {
                    dba.open();
                    dba.deleteUpdate(update.getId());
                    dba.close();
                    update = null;
                    finish();
                }
                return true;
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

	private void launchLatLonIntent() {
        if (update != null && update.validLatLon() ) {
            Uri uri = Uri.parse("geo:" + update.getLatitude() + "," + update.getLongitude()); //Possibly add "?zoom=z"
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
	}
}
