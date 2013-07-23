package org.akvo.rsr.android.util;

import org.akvo.rsr.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	
	/**
	 * shows an authentication dialog that asks for the administrator passcode
	 * 
	 * @param parentContext
	 * @param listener
	 */
	public static void showAdminAuthDialog(final Context parentContext,
			final AdminAuthDialogListener listener) {
		final EditText input = new EditText(parentContext);
		input.setSingleLine();
		ShowTextInputDialog(parentContext,
				R.string.authtitle,
				R.string.authtext,
				input,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String val = input.getText().toString();
						if (ConstantUtil.ADMIN_AUTH_CODE.equals(val)) {
							listener.onAuthenticated();
							if (dialog != null) {
								dialog.dismiss();
							}
						} else {
							 showConfirmDialog(R.string.authfailed, R.string.invalidpassword, parentContext);
							if (dialog != null) {
								dialog.dismiss();
							}
						}
					}
				});
	}
	

	/**
	 * displays a simple dialog box with only a single, positive button using
	 * the resource ids of the strings passed in for the title and text.
	 * 
	 * @param titleId
	 * @param textId
	 * @param parentContext
	 */
	public static void showConfirmDialog(int titleId, int textId,
			Context parentContext) {
		showConfirmDialog(titleId, textId, parentContext, false,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (dialog != null) {
							dialog.cancel();
						}
					}
				});
	}

	/**
	 * displays a simple dialog box with a single positive button and an
	 * optional (based on a flag) cancel button using the resource ids of the
	 * strings passed in for the title and text.
	 * 
	 * @param titleId
	 * @param textId
	 * @param parentContext
	 */
	public static void showConfirmDialog(int titleId, int textId,
			Context parentContext, boolean includeNegative,
			DialogInterface.OnClickListener listener) {
		showConfirmDialog(titleId, textId, parentContext, includeNegative,
				listener, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				});
	}


	/**
	 * displays a simple dialog box with a single positive button and an
	 * optional (based on a flag) cancel button using the resource ids of the
	 * strings passed in for the title and text. users can install listeners for
	 * both the positive and negative buttons
	 * 
	 * @param titleId
	 * @param textId
	 * @param parentContext
	 * @param includeNegative
	 * @param positiveListener
	 *            - if includeNegative is false, this will also be bound to the
	 *            cancel handler
	 * @param negativeListener
	 *            - only used if includeNegative is true - if the negative
	 *            listener is non-null, it will also be bound to the cancel
	 *            listener so pressing back to dismiss the dialog will have the
	 *            same effect as clicking the negative button.
	 */
	public static void showConfirmDialog(int titleId, int textId,
			Context parentContext, boolean includeNegative,
			final DialogInterface.OnClickListener positiveListener,
			final DialogInterface.OnClickListener negativeListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
		TextView tipText = new TextView(parentContext);
		builder.setTitle(titleId);
		tipText.setText(textId);
		builder.setView(tipText);
		builder.setPositiveButton(R.string.okbutton, positiveListener);
		if (includeNegative) {
			builder.setNegativeButton(R.string.cancelbutton, negativeListener);
			if (negativeListener != null) {
				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						negativeListener.onClick(dialog, -1);
					}
				});
			}
		} else if (positiveListener != null) {
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					positiveListener.onClick(dialog, -1);
				}
			});
		}

		builder.show();
	}


	/**
	 * shows a dialog that prompts the user to enter a single text value as
	 * input
	 * 
	 * @param parentContext
	 * @param title
	 * @param text
	 * @param clickListener
	 */
	public static void ShowTextInputDialog(final Context parentContext,
			int title, int text, EditText inputView,
			DialogInterface.OnClickListener clickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
		LinearLayout main = new LinearLayout(parentContext);
		main.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		main.setOrientation(LinearLayout.VERTICAL);
		TextView tipText = new TextView(parentContext);
		builder.setTitle(title);
		tipText.setText(text);
		main.addView(tipText);
		main.addView(inputView);
		builder.setView(main);
		builder.setPositiveButton(R.string.okbutton, clickListener);

		builder.setNegativeButton(R.string.cancelbutton,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				});

		builder.show();
	}
	/**
	 * interface that should be implemented by uses of the AdminAuthDialog to be
	 * notified when authorization is successful
	 * 
	 * 
	 * 
	 */
	public interface AdminAuthDialogListener {
		void onAuthenticated();
	}
}
