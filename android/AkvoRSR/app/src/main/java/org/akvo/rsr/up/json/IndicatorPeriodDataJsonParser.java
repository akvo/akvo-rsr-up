/*
 *  Copyright (C) 2016,2020 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.json;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.IndicatorPeriodData;
import org.json.JSONException;

/*
 * http://rsr.akvo.org/rest/v1/indicator_period_data/nnn/?format=json
 *  or returned when posting to
 * http://rsr.akvo.org/rest/v1/indicator_period_data
 * 
 * sample:
 * 

    {
            "user_details": {
                "id": 1589, 
                "first_name": "Kasper", 
                "last_name": "Brandt", 
                "approved_organisations": []
            }, 
            "status_display": "Draft", 
            "photo_url": "", 
            "file_url": "", 
            "id": 12, 
            "created_at": "2016-02-10T12:44:34.785015", 
            "last_modified_at": "2016-02-10T12:44:34.785073", 
            "period": 8156, 
            "user": 1589, 
            "relative_data": true, 
            "data": "25.00", 
            "period_actual_value": "", 
            "status": "D", 
            "text": "", 
            "photo": "", 
            "file": "", 
            "update_method": "W"
        }


 */

public class IndicatorPeriodDataJsonParser extends BaseJsonParser {

    IndicatorPeriodData ipd = null;

    /*
     * constructor
     */
    public IndicatorPeriodDataJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mRoot != null) {
    		ipd = new IndicatorPeriodData();
    		ipd.setId(mRoot.getString("id"));
            ipd.setData(mRoot.getString("data"));
            ipd.setRelativeData(mRoot.getBoolean("relative_data"));
            ipd.setStatus(mRoot.getString("status"));
            ipd.setDescription(mRoot.getString("text"));
            ipd.setUserId(mRoot.getString("user"));
            ipd.setPeriodId(mRoot.getString("period"));
            ipd.setPhotoUrl(mRoot.getString("photo_url"));
            ipd.setFileUrl(mRoot.getString("file_url"));
    		
    		mDba.saveIpd(ipd);
    	}
    }
    
    public IndicatorPeriodData getPeriodData() {
        return ipd;
    }
}
