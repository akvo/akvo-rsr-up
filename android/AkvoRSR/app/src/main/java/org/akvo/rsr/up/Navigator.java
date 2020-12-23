package org.akvo.rsr.up;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.akvo.rsr.up.util.ConstantUtil;

import java.io.File;

class Navigator {

    private static final String TAG = "Navigator";

    void navigateToCamera(String filename, AppCompatActivity activity) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri = FileProvider.getUriForFile(activity, ConstantUtil.FILE_PROVIDER_AUTHORITY,
                    new File(filename));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            activity.startActivityForResult(intent, ConstantUtil.PHOTO_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error while taking picture", e);
            Toast.makeText(activity.getApplicationContext(), R.string.take_photo_error, Toast.LENGTH_LONG).show();
        }
    }
}
