package org.akvo.rsr.up.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.Downloader;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.Uploader;

import java.util.Timer;
import java.util.TimerTask;


public class VerifyProjectUpdateService extends Service {

	private static final String TAG = "VerifyProjectUpdate";
    private static Timer timer;
    private static final long INITIAL_DELAY_MS = 60000; //one minute
    private static final long INTERVAL_MS = 300000; //five minutes

    /**
     * life cycle method for the service. This is called by the system when the
     * service is started. It will schedule a timerTask that will periodically
     * check the current location and send it to the server
     */
    public int onStartCommand(final Intent intent, int flags, int startid) {
        // Safe to lazy initialize the static field, since this method
        // will always be called in the Main Thread
		//Log.d(TAG, "Starting verification service");
        if (timer == null) {
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    //only try if we have network connection
                    if (Downloader.haveNetworkConnection(VerifyProjectUpdateService.this, false)) {
                        verifyIt();
                    }
                }
            }, INITIAL_DELAY_MS, INTERVAL_MS);
        }
        return Service.START_STICKY;
    }

	
	void verifyIt() {
		//Must not do network IO on main thread
		new Thread(new Runnable() {
			public void run() {
				//Log.v(TAG, "Starting a verify pass");
				Intent i = new Intent(ConstantUtil.UPDATES_VERIFIED_ACTION);

				try {
                    Context context = VerifyProjectUpdateService.this;                      
					int unresolveds = Uploader.verifyUpdates(context, SettingsUtil.host(VerifyProjectUpdateService.this) + ConstantUtil.VERIFY_UPDATE_PATTERN);
					if (unresolveds == 0) { //mission accomplished
						Log.i(TAG, "Every update verified");
						
						NotificationHelper helper = new NotificationHelper();
						helper.createNotificationChannel(context);
						helper.displayNotification(context);

				        if (timer != null) {
				            timer.cancel();
				        }
				        //stop the service
				        stopSelf();
					} else {
						Log.i(TAG, "Still unverified:" + unresolveds);
					}

                } catch (Uploader.FailedPostException e) {
                   Log.e(TAG, "Update error", e);
                } catch (Exception e) {
					Log.e(TAG, "Update error", e);
                    i.putExtra(ConstantUtil.SERVICE_ERRMSG_KEY, e.getMessage());
				}
				
			}
		}).start();
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
