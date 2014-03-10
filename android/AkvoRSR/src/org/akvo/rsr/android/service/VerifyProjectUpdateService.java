package org.akvo.rsr.android.service;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.SettingsUtil;
import org.akvo.rsr.android.xml.Downloader;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class VerifyProjectUpdateService extends Service {

	private static final String TAG = "VerifyProjectUpdateService";
    private static Timer timer;
//    private static final long INITIAL_DELAY_MS = 60000; //one minute
//    private static final long INTERVAL_MS = 300000; //five minutes

    private static final long INITIAL_DELAY_MS = 6000;
    private static final long INTERVAL_MS = 30000;


    /**
     * life cycle method for the service. This is called by the system when the
     * service is started. It will schedule a timerTask that will periodically
     * check the current location and send it to the server
     */
    public int onStartCommand(final Intent intent, int flags, int startid) {
        // Safe to lazy initialize the static field, since this method
        // will always be called in the Main Thread
        if (timer == null) {
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                	verifyIt();
                }
            }, INITIAL_DELAY_MS, INTERVAL_MS);
        }
        return Service.START_STICKY;
    }

	
	void verifyIt() {
		//Must not do network IO on main thread
		new Thread(new Runnable() {
			public void run() {

				Intent i = new Intent(ConstantUtil.UPDATES_VERIFIED_ACTION);

				try {
					int unresolveds = Downloader.verifyUpdates(VerifyProjectUpdateService.this, SettingsUtil.host(VerifyProjectUpdateService.this) + ConstantUtil.VERIFY_UPDATE_PATTERN);
					if (unresolveds == 0) { //mission accomplished
						//TODO:broadcast completion?
					    //LocalBroadcastManager.getInstance(VerifyProjectUpdateService.this).sendBroadcast(i);
						//TODO:notify user?
						
				        if (timer != null) {
				            timer.cancel();
				        }
				        //stop the service
				        stopSelf();
					}

				} catch (Exception e) {
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
