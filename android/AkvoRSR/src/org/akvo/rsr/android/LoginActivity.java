package org.akvo.rsr.android;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
