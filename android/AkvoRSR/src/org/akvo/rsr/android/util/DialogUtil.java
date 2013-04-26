package org.akvo.rsr.android.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtil {
	public static void errorAlert(Context ctx, Exception e) {
		/* Display any Error to the GUI. */
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
	    alert.setTitle("Error fetching project list");
	    alert.setMessage(e.toString());
	
	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	dialog.cancel();
		    	}
		    });
	    alert.show();
	}

	public static void errorAlert(Context ctx, String title, String msg) {
		/* Display any Error to the GUI. */
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
	    alert.setTitle(title);
	    alert.setMessage(msg);
	
	    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	dialog.cancel();
		    	}
		    });
	    alert.show();
	}
	

}
