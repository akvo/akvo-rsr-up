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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.Project;
import org.akvo.rsr.android.domain.Update;
import org.akvo.rsr.android.domain.User;
import org.akvo.rsr.android.service.SubmitProjectUpdateService;
import org.akvo.rsr.android.service.VerifyProjectUpdateService;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.DialogUtil;
import org.akvo.rsr.android.util.FileUtil;
import org.akvo.rsr.android.util.SettingsUtil;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.MediaStore;

/**
 * implements the page where the user inputs and sends an update
 */

public class UpdateEditorActivity extends Activity {

    private final int photoRequest = 777;
    private final int photoPick = 888;
    private String captureFilename = null;
    private boolean warnAboutBigImage = false;
    private boolean shrinkBigImage = true;
    private final int shrinkSize = 1024;
    private User user;

    private int nextLocalId; // load from / save to variable store
    private String projectId = null;
    private String updateId = null;
    private Update update = null;
    private boolean editable;
    // UI
    private TextView projTitleLabel;
    private EditText projupdTitleText;
    private EditText projupdDescriptionText;
    private ImageView projupdImage;
    private Button btnSubmit;
    private Button btnDraft;
    private Button btnTakePhoto;
    private Button btnAttachPhoto;
    private Button btnDelPhoto;
    private View photoAndDeleteGroup;
    private View photoAddGroup;
    ProgressDialog progress;

    // Database
    private RsrDbAdapter dba;

    private BroadcastReceiver broadRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = SettingsUtil.getAuthUser(this);
        nextLocalId = SettingsUtil.ReadInt(this, ConstantUtil.LOCAL_ID_KEY, -1);

        // find which update we are editing
        // null means create a new one
        Bundle extras = getIntent().getExtras();
        projectId = extras != null ? extras.getString(ConstantUtil.PROJECT_ID_KEY)
                : null;
        if (projectId == null) {
            DialogUtil.errorAlert(this, "No project id", "Caller did not specify a project");
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
        projTitleLabel = (TextView) findViewById(R.id.projupd_edit_proj_title);
        projupdTitleText = (EditText) findViewById(R.id.edit_projupd_title);
        projupdDescriptionText = (EditText) findViewById(R.id.edit_projupd_description);
        projupdImage = (ImageView) findViewById(R.id.image_update_detail);
        photoAndDeleteGroup = findViewById(R.id.image_with_delete);
        photoAddGroup = findViewById(R.id.photo_buttons);

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
                // TODO: delete image file if it was take through this app?
                // Hide them
                photoAndDeleteGroup.setVisibility(View.GONE);
                photoAddGroup.setVisibility(View.VISIBLE);
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
            update.setUserId(user.getId());
            update.setDate(new Date());
            editable = true;
        } else {
            update = dba.findUpdate(updateId);
            if (update == null) {
                DialogUtil.errorAlert(this, "Update missing", "Cannot open update " + updateId);
            } else {
                // populate fields
                editable = update.getDraft(); // This should always be true with
                                              // the current UI flow - we go to
                                              // UpdateDetailActivity if it is
                                              // sent
                projupdTitleText.setText(update.getTitle());
                projupdDescriptionText.setText(update.getText());

                // show preexisting image
                if (update.getThumbnailFilename() != null) {
                    // btnTakePhoto.setText(R.string.btncaption_rephoto);
                    FileUtil.setPhotoFile(projupdImage, update.getThumbnailUrl(),
                            update.getThumbnailFilename(), updateId, null);
                    photoAndDeleteGroup.setVisibility(View.VISIBLE);
                    photoAddGroup.setVisibility(View.GONE);
                }

            }
        }

        // register a listener for a completion intent
        broadRec = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadRec,
                new IntentFilter(ConstantUtil.UPDATES_SENT_ACTION));

        projupdTitleText.setEnabled(editable);
        projupdDescriptionText.setEnabled(editable);
        btnDraft.setEnabled(editable);
        btnSubmit.setEnabled(editable);
        btnTakePhoto.setEnabled(editable);
        btnDraft.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnSubmit.setVisibility(editable ? View.VISIBLE : View.GONE);
        // btnTakePhoto.setVisibility(editable?View.VISIBLE:View.GONE);

        // Show the Up button in the action bar.
        // setupActionBar();
    }

    private static final int IO_BUFFER_SIZE = 4 * 1024;

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    /*
     * (non-Javadoc) Get notification of photo taken or picked
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle photo taken by camera app
        if (requestCode == photoRequest) {
            if (resultCode == RESULT_CANCELED) {
                return;
            }
            if (warnAboutBigImage) {
                // check if file too large - if so, show error dialog and return
                File sizeTest = new File(captureFilename);
                if (sizeTest.length() > ConstantUtil.MAX_IMAGE_UPLOAD_SIZE) {
                    DialogUtil.errorAlert(this, "Photo file too big",
                            "Set your camera to a lower resolution");
                    return;
                }
            }
            if (shrinkBigImage) {
                // make long edge 1024 px or smaller
                if (!FileUtil.shrinkImageFileExactly(captureFilename, shrinkSize)) {
                    DialogUtil.errorAlert(this, "Could not shrink photo",
                            "Original was too big to send.");
                }
            }
            update.setThumbnailFilename(captureFilename);
            update.setThumbnailUrl("dummyUrl"); // absence will be interpreted
                                                // as unset thumbnail
            FileUtil.setPhotoFile(projupdImage, update.getThumbnailUrl(), captureFilename, null,
                    null);
            // show result
            photoAndDeleteGroup.setVisibility(View.VISIBLE);
            photoAddGroup.setVisibility(View.GONE);
        }

        // Handle picked photo
        if (requestCode == photoPick) {
            if (resultCode == RESULT_CANCELED) {
                return;
            }
            // data.getData is a content: URI. Need to copy the content to a
            // file.
            InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(data.getData());
                captureFilename = FileUtil.getExternalPhotoDir(this) + File.separator + "pick"
                        + System.nanoTime() + ".jpg";
                OutputStream os = new FileOutputStream(captureFilename);
                copyStream(imageStream, os);
                os.close();
                if (warnAboutBigImage) {
                    // check if file too large - if so, show error dialog and
                    // return
                    File sizeTest = new File(captureFilename);
                    if (sizeTest.length() > ConstantUtil.MAX_IMAGE_UPLOAD_SIZE) {
                        sizeTest.delete(); // save the space
                        DialogUtil.errorAlert(this, "Photo file too big", "Pick a smaller photo");
                        return;
                    }
                }
                if (shrinkBigImage) {
                    // make long edge 1024 px or smaller
                    // since this is a copy we can resize it in place
                    if (!FileUtil.shrinkImageFileExactly(captureFilename, shrinkSize)) {
                        DialogUtil.errorAlert(this, "Could not shrink photo",
                                "Original was too big to send.");
                    }
                }
                // store it and show it
                update.setThumbnailFilename(captureFilename);
                update.setThumbnailUrl("dummyUrl");
                FileUtil.setPhotoFile(projupdImage, update.getThumbnailUrl(), captureFilename,
                        null, null);
                photoAndDeleteGroup.setVisibility(View.VISIBLE);
                photoAddGroup.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                projupdImage.setImageResource(R.drawable.thumbnail_error);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * checks connectivity. TODO: move to util class
     */
    private boolean haveNetworkConnection(Context context, boolean wifiOnly) {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo[] infoArr = connMgr.getAllNetworkInfo();
            if (infoArr != null) {
                for (int i = 0; i < infoArr.length; i++) {
                    if (!wifiOnly) {
                        // if we don't care what KIND of
                        // connection we have, just that there is one
                        if (NetworkInfo.State.CONNECTED == infoArr[i].getState()) {
                            return true;
                        }
                    } else {
                        // if we only want to use wifi, we need to check the
                        // type
                        if (infoArr[i].getType() == ConnectivityManager.TYPE_WIFI
                                && NetworkInfo.State.CONNECTED == infoArr[i].getState()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
                projupdTitleText.setText("?"); // must have something
            }
        }
        update.setDraft(true);
        update.setUnsent(false);
        update.setTitle(projupdTitleText.getText().toString());
        update.setText(projupdDescriptionText.getText().toString());
        // update.setDate(new Date()); //should have date from when it was
        // created
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
            CharSequence text = "Draft saved successfully"; // TODO string
                                                            // resource
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
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
        if (!haveNetworkConnection(this, false)) {
            // TODO helpful error message, and return
            // Toast.makeText(getApplicationContext(), "No network connection!",
            // Toast.LENGTH_SHORT).show();

            // return
        }

        update.setUnsent(true);
        update.setDraft(false);
        update.setTitle(projupdTitleText.getText().toString());
        update.setText(projupdDescriptionText.getText().toString());
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

        // start a "progress" animation
        // TODO: a real filling progress bar?
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.send_dialog_title);
        progress.setMessage(getResources().getString(R.string.send_dialog_msg));
        progress.show();
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
        dba.open();
        /*
         * if (projectId != null) { Project project =
         * dba.findProject(projectId);
         * projTitleLabel.setText(project.getTitle()); } else {
         * projTitleLabel.setText("<NO PROJECT ID>"); }
         */

    }

    /** handles result of send attempt */
    private void onSendFinished(Intent i) {
        // Dismiss any in-progress dialog
        if (progress != null)
            progress.dismiss();
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

    /** receives status updates broadcast from the IntentService */
    private class ResponseReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private ResponseReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to
        // receive
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ConstantUtil.UPDATES_SENT_ACTION)
                onSendFinished(intent);
        }
    }

    /** closes database when activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        dba.close();
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

}
