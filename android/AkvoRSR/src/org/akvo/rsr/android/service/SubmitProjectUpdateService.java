package org.akvo.rsr.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/*
 * What to submit:
 * 
<object>
<update_method>W</update_method>
<project>/api/v1/project/277/</project>
<user>/api/v1/project/1/</user>
<title>The Rain in Spain...</title>
<photo_location>E</photo_location>
<text>
The rain in Spain stays mainly in the plain!

By George, she's got it! By George, she's got it!
</text>
</object>

 * To URL:
/api/v1/project_update/?format=xml&api_key=62a101a36893397300cbf62fbbf0debaa2818496&username=gabriel

 * As:  
application/xml
    
  */

public class SubmitProjectUpdateService extends Service {
    public SubmitProjectUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
