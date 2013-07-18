package org.akvo.rsr.android.util;

import org.akvo.rsr.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtil {
	public static void errorAlert(Context ctx, String title, Exception e) {
		/* Display any Error to the GUI. */
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
	    alert.setTitle(title);
	    alert.setMessage(e.toString());
	
	    alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
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
	    alert.setIcon(android.R.drawable.ic_dialog_alert);
	    alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	dialog.cancel();
		    	}
		    });
	    alert.show();
	}
	
	public static void infoAlert(Context ctx, String title, String msg) {
		/* Display an info dialog to the GUI. */
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
	    alert.setTitle(title).setMessage(msg).setIcon(android.R.drawable.ic_dialog_info);
	
	    alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	dialog.cancel();
		    	}
		    });
	    alert.show();
	}
	

}
