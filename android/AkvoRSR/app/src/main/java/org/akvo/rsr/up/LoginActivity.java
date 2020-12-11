/*
 *  Copyright (C) 2012-2017,2020 Stichting Akvo (Akvo Foundation)
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

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.worker.SignInWorker;

import java.io.File;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText usernameEdit;
    private EditText passwordEdit;
    private ProgressDialog progress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.i(TAG, "External storage: mounted ");
            File f = FileUtil.getExternalCacheDir(this);
            Log.i(TAG, "External cache folder: " + f.getAbsolutePath());
            if (!f.isDirectory()) {
                Log.w(TAG, " must create it");
                if (!f.mkdirs()) {
                    Log.e(TAG, "Failed to create cache folder");
                }
            }
            f = FileUtil.getExternalPhotoDir(this);
            Log.i(TAG, "External photo folder: " + f.getAbsolutePath());
            if (!f.isDirectory()) {
                Log.w(TAG, " must create it");
                if (!f.mkdirs()) {
                    Log.e(TAG, "Failed to create photo folder");
                }
            }

        } else {
            DialogUtil.errorAlert(this, R.string.nocard_dialog_title, R.string.nocard_dialog_msg);
        }

        if (SettingsUtil.host(this).equalsIgnoreCase(ConstantUtil.OLD_HOST)) {
            SettingsUtil.setHost(this, ""); //clear it so new default will be used
        }

        usernameEdit = (EditText) findViewById(R.id.edit_username);
        passwordEdit = (EditText) findViewById(R.id.edit_password);

        final Button button = (Button) findViewById(R.id.btn_login);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn(v);
            }
        });

        final TextView forgot = (TextView) findViewById(R.id.link_to_forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SettingsUtil.host(LoginActivity.this) + ConstantUtil.PWD_URL));
                startActivity(myIntent);
            }
        });

        final TextView settings = (TextView) findViewById(R.id.link_to_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
            }
        });

        final TextView about = (TextView) findViewById(R.id.link_to_about);
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AboutActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SettingsUtil.haveCredentials(this)) { // skip login
            Intent intent = new Intent(this, ProjectListActivity.class);
            startActivity(intent);
            finish();
        } else {
            passwordEdit.setText("");
        }

        if (BuildConfig.DEBUG) {
            usernameEdit.setText(BuildConfig.TEST_USER);
            passwordEdit.setText(BuildConfig.TEST_PASSWORD);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case R.id.action_about:
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * starts the sign in process
     */
    public void signIn(View view) {
        // We must have a connection
        if (!Downloader.haveNetworkConnection(this, false)) {
            // helpful error message, instead of a failure later
            DialogUtil.errorAlert(this, R.string.nonet_dialog_title, R.string.nonet_dialog_msg);
            return;
        }
        // start a "progress" animation
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.login_progress_title);
        progress.setMessage(getResources().getString(R.string.login_progress_msg));
        progress.show();

        // request API key from server
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        OneTimeWorkRequest signInRequest =
                new OneTimeWorkRequest.Builder(SignInWorker.class)
                        .addTag(SignInWorker.TAG)
                        .setInputData(createInputDataForUri(usernameEdit.getText().toString(), passwordEdit.getText().toString()))
                        .build();
        workManager.enqueueUniqueWork(SignInWorker.TAG, ExistingWorkPolicy.REPLACE, signInRequest);
        workManager.getWorkInfosByTagLiveData(SignInWorker.TAG).observe(this, listOfWorkInfos -> {

            // If there are no matching work info, do nothing
            if (listOfWorkInfos == null || listOfWorkInfos.isEmpty()) {
                return;
            }

            // We only care about the first output status.
            WorkInfo workInfo = listOfWorkInfos.get(0);

            boolean finished = workInfo.getState().isFinished();
            if (finished) {
                // Dismiss any in-progress dialog
                if (progress != null) {
                    progress.dismiss();
                }

                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    String msg = getResources().getString(R.string.msg_logged_in_as_template, SettingsUtil.Read(this, "authorized_username"));
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    // Go to main screen
                    Intent mainIntent = new Intent(this, ProjectListActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    passwordEdit.setText("");
                    // Let user keep username
                    // stay on this page
                    String err = workInfo.getOutputData().getString(ConstantUtil.SERVICE_ERRMSG_KEY);
                    if (!TextUtils.isEmpty(err)) {
                        DialogUtil.errorAlert(this, "Error", err);
                    }
                }
            }
        });
    }

    private Data createInputDataForUri(String login, String password) {
        Data.Builder builder = new Data.Builder();
        if (login != null) {
            builder.putString(ConstantUtil.USERNAME_KEY, login);
        }

        if (password != null) {
            builder.putString(ConstantUtil.PASSWORD_KEY, password);
        }
        return builder.build();
    }
}
