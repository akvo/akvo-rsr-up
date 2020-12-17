/*
 *  Copyright (C) 2016,2020 Stichting Akvo (Akvo Foundation)
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Period;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.ThumbnailUtil;
import org.akvo.rsr.up.worker.SubmitIpdWorker;
import org.jetbrains.annotations.NotNull;

import android.net.Uri;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static org.akvo.rsr.up.util.ConstantUtil.PHOTO_REQUEST;

public class ResultEditorActivity extends BackActivity {

    private static final int PHOTO_PICK = 1112;
    private static final int FILE_PICK = 1113;
    private static final String endash = "\u2013";

    private EditText mDataEdit;
    private EditText mDescriptionEdit;
    private CheckBox mRelativeDataCheckbox;
    private ProgressDialog mProgress = null;
    private View mPhotoTools;
    private View mFileTools;

    private ImageView mPhotoThumbnail;
    private TextView mPhotoInfo;
    private TextView mFileInfo;

    private String mPeriodId;
    private String mPeriodActualValue;
    private String mCaptureFilename;

    private final Navigator navigator = new Navigator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_editor);

        //find which period we will be updating
        if (savedInstanceState != null) {
            mCaptureFilename = savedInstanceState.getString(ConstantUtil.IMAGE_FILENAME_KEY);
            mPeriodId = savedInstanceState.getString(ConstantUtil.PERIOD_ID_KEY);
        } else {
            Bundle extras = getIntent().getExtras();
            mPeriodId = extras != null ? extras.getString(ConstantUtil.PERIOD_ID_KEY) : null;
        }

        final SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        RsrDbAdapter mDba = new RsrDbAdapter(this);
        mDba.open();
        String mPeriodStart = "";
        String mPeriodEnd = "";
        try {
            Period period = mDba.findPeriod(mPeriodId);
            if (period != null) {
                mPeriodActualValue = period.getActualValue();
                mPeriodStart = dateOnly.format(period.getPeriodStart());
                mPeriodEnd = dateOnly.format(period.getPeriodEnd());
            }
        } finally {
            mDba.close();
        }

        TextView periodTitleLabel = (TextView) findViewById(R.id.period_title);
        periodTitleLabel.setText(String.format("Period %s%s%s", mPeriodStart, endash, mPeriodEnd));//TODO: localisation
        mDataEdit = (EditText) findViewById(R.id.edit_data);
        mRelativeDataCheckbox = (CheckBox) findViewById(R.id.cb_relative_data);
        mDescriptionEdit = (EditText) findViewById(R.id.edit_comment);
        mPhotoTools = (View) findViewById(R.id.photo_tools);
        mFileTools = (View) findViewById(R.id.file_tools);
        mPhotoThumbnail = (ImageView) findViewById(R.id.image_ipd);
        mPhotoInfo = (TextView) findViewById(R.id.photo_info);
        mFileInfo = (TextView) findViewById(R.id.file_info);

        final Button button = (Button) findViewById(R.id.btn_send_result);
        button.setOnClickListener(this::sendIt);

        Button btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(view -> {
            // generate unique filename
            mCaptureFilename = FileUtil.generateImageFile("capture", ResultEditorActivity.this);
            navigator.navigateToCamera(mCaptureFilename, ResultEditorActivity.this);
        });

        Button btnAttachPhoto = (Button) findViewById(R.id.btn_attach_photo);
        btnAttachPhoto.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, PHOTO_PICK);
        });

        Button btnAttachFile = (Button) findViewById(R.id.btn_attach_file);
        btnAttachFile.setOnClickListener(view -> {
            Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            filePickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerIntent.setType("*/*");
            startActivityForResult(filePickerIntent, FILE_PICK);
        });
    }

    /**
     * starts the submission process
     */
    public void sendIt(View view) {
        if (!Downloader.haveNetworkConnection(this, false)) {
            DialogUtil.errorAlert(this, R.string.nonet_dialog_title, R.string.nonet_dialog_msg);
            return;
        }
        // start a "progress" animation
        mProgress = new ProgressDialog(this);
        mProgress.setTitle(R.string.send_progress_title);
        mProgress.setMessage(getResources().getString(R.string.send_progress_msg));
        mProgress.show();

        // start the upload service
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        Data.Builder builder = new Data.Builder();
        builder.putString(ConstantUtil.DATA_KEY, mDataEdit.getText().toString());
        builder.putBoolean(ConstantUtil.RELATIVE_DATA_KEY, mRelativeDataCheckbox.isChecked());
        builder.putString(ConstantUtil.DESCRIPTION_KEY, mDescriptionEdit.getText().toString());
        builder.putString(ConstantUtil.PERIOD_ID_KEY, mPeriodId);
        builder.putString(ConstantUtil.CURRENT_ACTUAL_VALUE_KEY, mPeriodActualValue);
        builder.putString(ConstantUtil.PHOTO_FN_KEY, mCaptureFilename);
        OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(SubmitIpdWorker.class)
                        .addTag(SubmitIpdWorker.TAG)
                        .setInputData(builder.build())
                        .build();
        workManager.enqueueUniqueWork(SubmitIpdWorker.TAG, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
        workManager.getWorkInfosByTagLiveData(SubmitIpdWorker.TAG).observe(this, listOfWorkInfos -> {

            // If there are no matching work info, do nothing
            if (listOfWorkInfos == null || listOfWorkInfos.isEmpty()) {
                return;
            }

            // We only care about the first output status.
            WorkInfo workInfo = listOfWorkInfos.get(0);

            boolean finished = workInfo.getState().isFinished();

            if (finished) {
                // Dismiss any in-progress dialog
                String err = workInfo.getOutputData().getString(ConstantUtil.SERVICE_ERRMSG_KEY);
                onSendFinished(err);
            }
        });
    }

    /**
     * completes the sign-in process after network activity is done
     */
    private void onSendFinished(String err) {
        if (mProgress != null) {
            mProgress.dismiss();
        }

        if (err == null) {
            DialogUtil.infoAlert(this, R.string.msg_send_success, R.string.msg_period_data_submitted);
            finish();
        } else {
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

        if (requestCode == PHOTO_REQUEST || requestCode == PHOTO_PICK) {
            if (resultCode == RESULT_CANCELED) {
                mCaptureFilename = null;
                return;
            }
            if (requestCode == PHOTO_PICK) {
                InputStream imageStream;
                try {
                    imageStream = getContentResolver().openInputStream(data.getData());
                    mCaptureFilename = FileUtil.getExternalPhotoDir(this) + File.separator + "pick"
                            + System.nanoTime() + ".jpg";
                    try (OutputStream os = new FileOutputStream(mCaptureFilename)) {
                        copyStream(imageStream, os);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
          ThumbnailUtil.setPhotoFile(mPhotoThumbnail, "dummyUrl", mCaptureFilename, null, null, false);
          // show result
          mPhotoTools.setVisibility(View.VISIBLE);
          //TODO: show size of photo file
          File f = new File(mCaptureFilename);
          if (f.exists() && f.isFile()) {
              mPhotoInfo.setText(String.format("%d kB", f.length() / 1024));
          }
        }
        if (requestCode == FILE_PICK) {
            if (resultCode == RESULT_CANCELED) {
                return;
            }
            Uri u = data.getData();
            if (u.getScheme().equalsIgnoreCase("file")) {
                File f = new File(u.getPath());
                if (f.exists() && f.isFile()) {
                    mFileInfo.setText(f.getName());
                    mFileTools.setVisibility(View.VISIBLE);
                }
            }
            //TODO: change button to remove-attachment
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ConstantUtil.PERIOD_ID_KEY, mPeriodId);
        // In case we are being bumped to make room for the camera app:
        outState.putString(ConstantUtil.IMAGE_FILENAME_KEY, mCaptureFilename);
    }
}
