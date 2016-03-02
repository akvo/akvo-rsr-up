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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.service.SubmitIpdService;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.ThumbnailUtil;

import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ResultEditorActivity extends ActionBarActivity {

    private static final String TAG = "ResultEditorActivity";
    private final int photoRequest = 887;
    private final int photoPick = 889;
    private EditText mDataEdit;
    private EditText mDescriptionEdit;
    private CheckBox mRelativeDataCheckbox;
    private ProgressDialog mProgress = null;
    private BroadcastReceiver rec;
    private int mPeriodId;
    private String mPeriodActualValue;
    private String captureFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //find which period we will be updating
        Bundle extras = getIntent().getExtras();
        mPeriodId = extras != null ? extras.getInt(ConstantUtil.PERIOD_KEY,0) : 0;
        mPeriodActualValue = extras != null ? extras.getString(ConstantUtil.CURRENT_ACTUAL_VALUE_KEY) : null;

        setContentView(R.layout.activity_result_editor);

        mDataEdit = (EditText) findViewById(R.id.edit_data);
        mRelativeDataCheckbox = (CheckBox) findViewById(R.id.cb_relative_data);
        mDescriptionEdit = (EditText) findViewById(R.id.edit_comment);

        final Button button = (Button) findViewById(R.id.btn_send_result);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendIt(v);
            }
        });

        Button btnAttachPhoto = (Button) findViewById(R.id.btn_attach_photo);
        btnAttachPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, photoPick);
            }
        });


        // register a listener for the completion intent
        IntentFilter f = new IntentFilter(ConstantUtil.RESULT_SENT_ACTION);
        rec = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(rec, f);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
        super.onDestroy();
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * starts the submission process
     * 
     * @param view
     */
    public void sendIt(View view) {
        // We must have a connection
        if (!Downloader.haveNetworkConnection(this, false)) {
            // helpful error message, instead of a failure later
            DialogUtil.errorAlert(this, R.string.nonet_dialog_title, R.string.nonet_dialog_msg);
            return;
        }
        // start a "progress" animation
        mProgress = new ProgressDialog(this);
        mProgress.setTitle(R.string.send_progress_title);
        mProgress.setMessage(getResources().getString(R.string.send_progress_msg));
        mProgress.show();

        // start the upload service
        Intent intent = new Intent(this, SubmitIpdService.class);
        intent.putExtra(ConstantUtil.DATA_KEY, mDataEdit.getText().toString());
        intent.putExtra(ConstantUtil.RELATIVE_DATA_KEY, mRelativeDataCheckbox.isChecked());
        intent.putExtra(ConstantUtil.DESCRIPTION_KEY, mDescriptionEdit.getText().toString());
        intent.putExtra(ConstantUtil.PERIOD_KEY, mPeriodId);
        intent.putExtra(ConstantUtil.CURRENT_ACTUAL_VALUE_KEY, mPeriodActualValue);
//        intent.putExtra(ConstantUtil.PHOTO_FN_KEY, "");
//        intent.putExtra(ConstantUtil.FILE_FN_KEY, "");

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
        if (mProgress != null) {
            mProgress.dismiss();
        }

        String err = intent.getStringExtra(ConstantUtil.SERVICE_ERRMSG_KEY);
        if (err == null) {
            String msg = getResources().getString(R.string.msg_update_published);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            finish();//close editor
        } else {
            // stay on this page
            DialogUtil.errorAlert(this, "Error", err);
        }
    }


    //TODO move this to FileUtil
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
//                    projupdImage.setImageResource(R.drawable.thumbnail_error);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //shrink or rotate here?
                
//                update.setThumbnailFilename(captureFilename);
//                update.setThumbnailUrl("dummyUrl"); // absence will be interpreted
                                                    // as unset thumbnail
//                ThumbnailUtil.setPhotoFile(projupdImage, update.getThumbnailUrl(), captureFilename, null, null, false);
                // show result
//                photoLocation = FileUtil.exifLocation(captureFilename);
//                showPhoto(true);
            }
        }
    }
    
            /**
     * Broadcast receiver for receiving status updates from the SubmitIpdService IntentService
     *
     */
    private class ResponseReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private ResponseReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ConstantUtil.RESULT_SENT_ACTION) {
                onSendFinished(intent);
            }
        }
    }

}
