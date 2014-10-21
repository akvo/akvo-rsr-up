/*
 *  Copyright (C) 2012-2014 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.util;

import org.akvo.rsr.up.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	    alert.setPositiveButton(R.string.btncaption_ok, new DialogInterface.OnClickListener() {
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
        alert.setPositiveButton(R.string.btncaption_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
                }
            });
        alert.show();
    }
    
    public static void errorAlert(Context ctx, int title, int msg) {
        /* Display any Error to the GUI. */
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setPositiveButton(R.string.btncaption_ok, new DialogInterface.OnClickListener() {
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
    
        alert.setPositiveButton(R.string.btncaption_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
                }
            });
        alert.show();
    }
    
    public static void infoAlert(Context ctx, int title, int msg) {
        /* Display an info dialog to the GUI. */
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setTitle(title).setMessage(msg).setIcon(android.R.drawable.ic_dialog_info);
    
        alert.setPositiveButton(R.string.btncaption_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
                }
            });
        alert.show();
    }
    
    public static void infoAlert(Context ctx, int title, String msg) {
        /* Display an info dialog to the GUI. */
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setTitle(title).setMessage(msg).setIcon(android.R.drawable.ic_dialog_info);
    
        alert.setPositiveButton(R.string.btncaption_ok, new DialogInterface.OnClickListener() {
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
		showTextInputDialog(parentContext,
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
    public static void showConfirmDialog(int titleId, String textId,
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
        builder.setPositiveButton(R.string.btncaption_ok, positiveListener);
        tipText.setBackgroundColor(parentContext.getResources().getColor(R.color.rsr_blue));
        tipText.setPadding(10, 10, 10,10);

        if (includeNegative) {
            builder.setNegativeButton(R.string.btncaption_cancel, negativeListener);
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
     * displays a simple dialog box with a single positive button and an
     * optional (based on a flag) cancel button using the resource id of the
     * string passed in for the title and a string for the text. users can install listeners for
     * both the positive and negative buttons
     * 
     * @param titleId
     * @param text
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
    public static void showConfirmDialog(int titleId, String text,
            Context parentContext, boolean includeNegative,
            final DialogInterface.OnClickListener positiveListener,
            final DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
        TextView tipText = new TextView(parentContext);
        builder.setTitle(titleId);
        tipText.setText(text);
        builder.setView(tipText);
        builder.setPositiveButton(R.string.btncaption_ok, positiveListener);
        tipText.setBackgroundColor(parentContext.getResources().getColor(R.color.rsr_blue));
        tipText.setPadding(10, 10, 10,10);

        if (includeNegative) {
            builder.setNegativeButton(R.string.btncaption_cancel, negativeListener);
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
     * displays a simple dialog box with a single positive button and an
     * optional (based on a flag) cancel button using the resource id of the
     * string passed in for the title and a string for the text. users can install listeners for
     * both the positive and negative buttons
     * 
     * @param titleId
     * @param textId
     * @param detailedMsg
     * @param parentContext
     */
    public static void errorAlertWithDetail(
            final Context parentContext,
            int titleId, int textId,
            final String detailedMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
        builder.setTitle(titleId);
        builder.setMessage(textId);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        //The OK button just dismisses the dialog
        builder.setPositiveButton(R.string.btncaption_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
                }
            });
        //Details button shows the whole truth
        builder.setNegativeButton(R.string.btncaption_details, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                infoAlert(parentContext, R.string.detail_dialog_title, detailedMsg);
                }
            });
    
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
	public static void showTextInputDialog(
	        final Context parentContext,
			int title,
			int text,
			EditText inputView,
			DialogInterface.OnClickListener clickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
		LinearLayout main = new LinearLayout(parentContext);
		main.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		main.setOrientation(LinearLayout.VERTICAL);
		main.setBackgroundColor(parentContext.getResources().getColor(R.color.rsr_blue));
		TextView tipText = new TextView(parentContext);
		builder.setTitle(title);
		tipText.setText(text);
		main.addView(tipText);
		main.addView(inputView);
		builder.setView(main);
		builder.setPositiveButton(R.string.btncaption_ok, clickListener);

		builder.setNegativeButton(R.string.btncaption_cancel,
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
	
	
    /**
     * displays the alert dialog box warning that the GPS receiver is off. If
     * the affirmative button is clicked, the Location Settings panel is
     * launched. If the negative button is clicked, it will just close the
     * dialog
     * 
     * @param parentContext
     */
    public static void showGPSDialog(final Context parentContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parentContext);
        builder.setMessage(R.string.gpsdisabled_dialog_msg)
                .setCancelable(true)
                .setPositiveButton(R.string.btncaption_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                parentContext
                                        .startActivity(new Intent(
                                                "android.settings.LOCATION_SOURCE_SETTINGS"));
                            }
                        })
                .setNegativeButton(R.string.btncaption_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        builder.show();
    }


}
