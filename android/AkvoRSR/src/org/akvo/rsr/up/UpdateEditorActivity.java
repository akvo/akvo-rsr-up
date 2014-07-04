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
 *  See the GNU Affero General Public License included with this program for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.up;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.service.SubmitProjectUpdateService;
import org.akvo.rsr.up.service.VerifyProjectUpdateService;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.MediaStore;

/**
 * implements the page where the user inputs and sends an update
 */

public class UpdateEditorActivity extends ActionBarActivity {

    private final int photoRequest = 777;
    private final int photoPick = 888;
    private String captureFilename = null;
    private boolean warnAboutBigImage = false;
    private boolean shrinkBigImage = true;
    private final int shrinkSize = 1024;
    private User mUser;

    private int nextLocalId; // load from / save to variable store
    private String projectId = null;
    private String updateId = null;
    private Update update = null;
    private boolean editable;
    // UI
    private TextView projTitleLabel;
    private EditText projupdTitleText;
    private EditText projupdDescriptionText;
    private EditText photoCaptionText;
    private EditText photoCreditText;
    private ImageView projupdImage;
    private Button btnSubmit;
    private Button btnDraft;
    private Button btnTakePhoto;
    private Button btnAttachPhoto;
    private Button btnDelPhoto;
    private Button btnRotRightPhoto;
    private View photoAndToolsGroup;
    private View photoAddGroup;
    private View progressGroup;
    private ProgressBar inProgress;

    // Database
    private RsrDbAdapter dba;

    private BroadcastReceiver broadRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = SettingsUtil.getAuthUser(this);
        nextLocalId = SettingsUtil.ReadInt(this, ConstantUtil.LOCAL_ID_KEY, -1);

        // find which update we are editing
        // null means create a new one
        Bundle extras = getIntent().getExtras();
        projectId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
                : null;
        if (projectId == null) {
            DialogUtil.errorAlert(this, R.string.noproj_dialog_title, R.string.noproj_dialog_msg);
        }
        updateId = extras != null ? extras.getString(ConstantUtil.UPDATE_ID_KEY)
                : null;
        if (updateId == null) {
            updateId = savedInstanceState != null ? savedInstanceState
                    .getString(ConstantUtil.UPDATE_ID_KEY) : null;
        }

        // get the look
        setContentView(R.layout.activity_update_editor);
        // find the fields
        progressGroup = findViewById(R.id.sendprogress_group);
        inProgress = (ProgressBar) findViewById(R.id.sendProgressBar);
        projTitleLabel = (TextView) findViewById(R.id.projupd_edit_proj_title);
        projupdTitleText = (EditText) findViewById(R.id.projupd_edit_title);
        projupdDescriptionText = (EditText) findViewById(R.id.projupd_edit_description);
        projupdImage = (ImageView) findViewById(R.id.image_update_detail);
        photoAndToolsGroup = findViewById(R.id.image_with_tools);
        photoAddGroup = findViewById(R.id.photo_buttons);
        photoCaptionText = (EditText) findViewById(R.id.projupd_edit_photo_caption);
        photoCreditText = (EditText) findViewById(R.id.projupd_edit_photo_credit);

        // Activate buttons
        btnSubmit = (Button) findViewById(R.id.btn_send_update);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendUpdate();
            }
        });

        btnDraft = (Button) findViewById(R.id.btn_save_draft);
        btnDraft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveAsDraft(true);
            }
        });

        btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // generate unique filename
                captureFilename = FileUtil.getExternalPhotoDir(UpdateEditorActivity.this)
                        + File.separator + "capture" + System.nanoTime() + ".jpg";
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(captureFilename)));
                startActivityForResult(takePictureIntent, photoRequest);
            }
        });

        btnAttachPhoto = (Button) findViewById(R.id.btn_attach_photo);
        btnAttachPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, photoPick);
            }
        });

        btnDelPhoto = (Button) findViewById(R.id.btn_delete_photo);
        btnDelPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Forget image
                update.setThumbnailFilename(null);
                // TODO: delete image file if it was taken through this app?
                // Hide photo w tools
                showPhoto(false);
            }
        });

        btnRotRightPhoto = (Button) findViewById(R.id.btn_rotate_photo_r);
        btnRotRightPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Rotate image right
                rotatePhoto(true);
            }
        });

        dba = new RsrDbAdapter(this);
        dba.open();

        Project project = dba.findProject(projectId);
        projTitleLabel.setText(project.getTitle());

        if (updateId == null) { // create new
            update = new Update();
            update.setUuid(UUID.randomUUID().toString()); // should do sth
                                                          // better, especially
                                                          // if MAC address is
                                                          // avaliable
            /*
             * WifiManager wifiManager = (WifiManager)
             * getSystemService(Context.WIFI_SERVICE); WifiInfo wInfo =
             * wifiManager.getConnectionInfo(); String macAddress =
             * wInfo.getMacAddress(); if (macAddress == null) txt_View.append(
             * "MAC Address : " + macAddress + "\n" ); else txt_View.append(
             * "MAC Address : " + macAddress + "\n" ); }
             */
            update.setUserId(mUser.getId());
            update.setDate(new Date());
            editable = true;
        } else {
            update = dba.findUpdate(updateId);
            if (update == null) {
                DialogUtil.errorAlert(this, R.string.noupd_dialog_title, R.string.noupd2_dialog_msg);
            } else {
                // populate fields
                editable = update.getDraft(); // This should always be true with
                                              // the current UI flow - we go to
                                              // UpdateDetailActivity if it is
                                              // sent
                projupdTitleText.setText(update.getTitle());
                projupdDescriptionText.setText(update.getText());
                photoCaptionText.setText(update.getPhotoCaption());
                photoCreditText.setText(update.getPhotoCredit());

                // show preexisting image
                if (update.getThumbnailFilename() != null) {
                    // btnTakePhoto.setText(R.string.btncaption_rephoto);
                    FileUtil.setPhotoFile(projupdImage, update.getThumbnailUrl(),
                            update.getThumbnailFilename(), null, null);
                    showPhoto(true);
                }
            }
        }

        // register a listener for a completion and progress intents
        broadRec = new ResponseReceiver();
        IntentFilter f = new IntentFilter(ConstantUtil.UPDATES_SENT_ACTION);
        f.addAction(ConstantUtil.UPDATES_SENDPROGRESS_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadRec, f);

        enableChanges(editable);
        btnDraft.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnSubmit.setVisibility(editable ? View.VISIBLE : View.GONE);
        // btnTakePhoto.setVisibility(editable?View.VISIBLE:View.GONE);

        // Show the Up button in the action bar.
        // setupActionBar();
    }

    private void showPhoto(boolean show) {
        if (show) {
            photoAndToolsGroup.setVisibility(View.VISIBLE);
            photoAddGroup.setVisibility(View.GONE);
        } else {
            photoAndToolsGroup.setVisibility(View.GONE);
            photoAddGroup.setVisibility(View.VISIBLE);
        }
    }

    
    private void rotatePhoto(boolean clockwise) {
        try {
            FileUtil.rotateImageFile(update.getThumbnailFilename(), clockwise);
        }
        catch (IOException e) {
            DialogUtil.errorAlert(this, R.string.norot_dialog_title, R.string.norot_dialog_msg);
            return;
        }
        catch (OutOfMemoryError e) {
            DialogUtil.errorAlert(this, R.string.norot_dialog_title2, R.string.norot_dialog_msg);
            return;
        }
        FileUtil.setPhotoFile(projupdImage, update.getThumbnailUrl(), update.getThumbnailFilename(), null, null);
    }
    
    /**
     * sets and clears enabled for all elements.
     */
    private void enableChanges(boolean enabled) {
        projupdTitleText.setEnabled(enabled);
        projupdDescriptionText.setEnabled(enabled);
        btnDraft.setEnabled(enabled);
        btnSubmit.setEnabled(enabled);
        btnTakePhoto.setEnabled(enabled);        
        btnDelPhoto.setEnabled(enabled);                
    }
    
    
    private static final int IO_BUFFER_SIZE = 4 * 1024;

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    
    /**
     * gets notification of photo taken or picked
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle photo taken by camera app
        if (requestCode == photoRequest || requestCode == photoPick) {
            if (resultCode == RESULT_CANCELED) {
                return;
            }
            boolean camera = true;
            
            // Handle picked photo
            if (requestCode == photoPick) {
                camera = false;
                if (resultCode == RESULT_CANCELED) {
                    return;
                }
                // data.getData is a content: URI. Need to copy the content to a
                // file, so we can resize and rotate in place
                InputStream imageStream;
                try {
                    imageStream = getContentResolver().openInputStream(data.getData());
                    captureFilename = FileUtil.getExternalPhotoDir(this) + File.separator + "pick"
                            + System.nanoTime() + ".jpg";
                    OutputStream os = new FileOutputStream(captureFilename);
                    try {
                        copyStream(imageStream, os);
                    }
                    finally {
                        os.close();
                    }
                } catch (FileNotFoundException e) {
                    projupdImage.setImageResource(R.drawable.thumbnail_error);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } 
            if (warnAboutBigImage) {
                // check if file too large - if so, show error dialog and return
                File sizeTest = new File(captureFilename);
                if (sizeTest.length() > ConstantUtil.MAX_IMAGE_UPLOAD_SIZE) {
                    DialogUtil.errorAlert(this, R.string.warnbig_dialog_title,
                            camera ? R.string.warnbig_dialog_msg1 : R.string.warnbig_dialog_msg2);
                    return;
                }
            }
            if (shrinkBigImage) {
                // make long edge 1024 px
                if (!FileUtil.shrinkImageFileExactly(captureFilename, shrinkSize, false)) { 
                    DialogUtil.errorAlert(this, R.string.shrinkbig_dialog_title,
                            R.string.shrinkbig_dialog_msg);
                }
            }
            update.setThumbnailFilename(captureFilename);
            update.setThumbnailUrl("dummyUrl"); // absence will be interpreted
                                                // as unset thumbnail
            FileUtil.setPhotoFile(projupdImage, update.getThumbnailUrl(), captureFilename, null,
                    null);
            // show result
            showPhoto(true);
        }
    }
    

    /**
     * fetches text field data from form to object
     */
    private void fetchFields(){
        update.setTitle(projupdTitleText.getText().toString());
        update.setText(projupdDescriptionText.getText().toString());        
        update.setPhotoCaption(photoCaptionText.getText().toString());        
        update.setPhotoCredit(photoCreditText.getText().toString());        
    }
    
    /**
     * Saves current update as draft, if it has a title and this is done by the
     * user
     */
    private void saveAsDraft(boolean interactive) {
        if (untitled()) {
            if (interactive) {
                // Tell user why not
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, R.string.errmsg_empty_title,
                        Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else {
                projupdTitleText.setText("?"); // must have something.
                //In retrospect, we should have allowed nulls in this column and just disallowed posting
            }
        }
        update.setDraft(true);
        update.setUnsent(false);
        fetchFields();
        // update.setDate(new Date()); //should have date from when it was created
        if (update.getId() == null) {// new
            // MUST have project and a local update id
            update.setProjectId(projectId);
            update.setId(Integer.toString(nextLocalId));
            nextLocalId--;
            SettingsUtil.WriteInt(this, ConstantUtil.LOCAL_ID_KEY, nextLocalId);
        }
        dba.saveUpdate(update, true);
        if (interactive) {
            // Tell user what happened
            // TODO: use a confirm dialog instead
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.msg_success_drafted, Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    /**
     * sends current update
     */
    private void sendUpdate() {
        if (untitled()) {
            return;
        }
        if (!Downloader.haveNetworkConnection(this, false)) {
            // helpful error message, instead of a failure later
            DialogUtil.errorAlert(this, R.string.nonet_dialog_title, R.string.nonet_dialog_msg);
            return;
        }

        update.setUnsent(true);
        update.setDraft(false);
        fetchFields();
        update.setProjectId(projectId);
        // update.setDate(new Date()); //keep date from when it was created
        if (update.getId() == null) {// new
            update.setId(Integer.toString(nextLocalId));
            nextLocalId--;
            SettingsUtil.WriteInt(this, ConstantUtil.LOCAL_ID_KEY, nextLocalId);
        }
        dba.saveUpdate(update, true);

        // start upload service
        Intent i = new Intent(this, SubmitProjectUpdateService.class);
        i.putExtra(ConstantUtil.UPDATE_ID_KEY, update.getId());
        getApplicationContext().startService(i);

        // Disable UI during send to avoid confusion
        enableChanges(false);
        // Show the "progress" animation
        progressGroup.setVisibility(View.VISIBLE);

        // Now we wait...

    }

    /** checks if user has set an update title */
    private boolean untitled() {
        return (projupdTitleText.getText().toString().trim().length() == 0);
    }

    /** opens database when Activity resumes */
    @Override
    protected void onResume() {
        super.onResume();
        /*
         * if (projectId != null) { Project project =
         * dba.findProject(projectId);
         * projTitleLabel.setText(project.getTitle()); } else {
         * projTitleLabel.setText("<NO PROJECT ID>"); }
         */

    }

    /** handles result of send attempt */
    private void onSendFinished(Intent i) {
        //Hide progressbar
        progressGroup.setVisibility(View.GONE);

        String err = i.getStringExtra(ConstantUtil.SERVICE_ERRMSG_KEY);
        boolean unresolved = i.getBooleanExtra(ConstantUtil.SERVICE_UNRESOLVED_KEY, false);
        int msgTitle, msgText;
        if (err == null) {
            msgTitle = R.string.msg_update_published;
            msgText = R.string.msg_update_success;
        } else {
            if (unresolved) {
                // Update still has unsent flag set, start service for
                // background synchronisation
                getApplicationContext().startService(
                        new Intent(this, VerifyProjectUpdateService.class));
                msgTitle = R.string.msg_network_problem;
                msgText = R.string.msg_synchronising;
            } else { // was saved as draft
                msgTitle = R.string.msg_network_problem;
                msgText = R.string.msg_update_drafted;
            }
        }
        // display result dialog
        DialogUtil.showConfirmDialog(msgTitle,
                msgText,
                this,
                false,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                            // Return to project or update list
                            finish();
                        }
                    }
                });
    }


    /** closes database when activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /** saves update being worked on before we leave the activity */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // pick up the saved draft if we get restarted
        saveAsDraft(false);
        // in case this created the update in the DB
        outState.putString(ConstantUtil.UPDATE_ID_KEY, update.getId());
        super.onSaveInstanceState(outState);
    }

    /** cleans up */
    @Override
    protected void onDestroy() {
        if (dba != null) {
            dba.close();
        }
        if (broadRec != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadRec);
        }
        super.onDestroy();
    }

    /**
     * Sets up the {@link android.app.ActionBar}, if the API is available.
     * 
     * @TargetApi(Build.VERSION_CODES.HONEYCOMB) private void setupActionBar() {
     *                                           if (Build.VERSION.SDK_INT >=
     *                                           Build.VERSION_CODES.HONEYCOMB)
     *                                           {
     *                                           getActionBar().setDisplayHomeAsUpEnabled
     *                                           (true); } }
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.update_editor, menu);
        return true;
    }

    /**
     * Handles context menu buttons being pushed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_update:
                if (update.getUnsent() || update.getDraft()) {
                    // Verify?
                    dba.deleteUpdate(update.getId());
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
    /**
     * updates the progress bars
     * @param phase
     * @param done
     * @param total
     */
    private void onFetchProgress(int phase, int done, int total) {
        inProgress.setIndeterminate(false);
        inProgress.setProgress(done);
        inProgress.setMax(total);
    }

    /**
     * receives status updates from an IntentService
     *
     */
    private class ResponseReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private ResponseReceiver() {
        }
        
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ConstantUtil.UPDATES_SENT_ACTION) {
                onSendFinished(intent);
            } else if (intent.getAction() == ConstantUtil.UPDATES_SENDPROGRESS_ACTION) {
                onFetchProgress(intent.getExtras().getInt(ConstantUtil.PHASE_KEY, 0),
                                intent.getExtras().getInt(ConstantUtil.SOFAR_KEY, 0),
                                intent.getExtras().getInt(ConstantUtil.TOTAL_KEY, 100));
            }
        }
    }



}
