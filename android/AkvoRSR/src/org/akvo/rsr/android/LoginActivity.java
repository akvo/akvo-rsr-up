package org.akvo.rsr.android;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends Activity {

	private static final String imageCache = "/akvorsr/imagecache/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		File f = new File (Environment.getExternalStorageDirectory().getPath() + imageCache);
//		File f = new File (imageCache);
		if (!f.mkdirs())
			Log.e("LoginActivity", "could not create cache dir");
		
		
		setContentView(R.layout.activity_login);
		
        final Button button = (Button) findViewById(R.id.btnLogin);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	SignIn(v);
            }
        });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

    //Sign In button pushed
    public void SignIn(View view){
	    Intent intent = new Intent(this, ProjectListActivity.class);
	    startActivity(intent);
    }
}
