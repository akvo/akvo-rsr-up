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
import org.akvo.rsr.up.domain.Employment;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * /rest/v1/employment/?format=json&user=42
 * sample result:
 * 
    {
        "count": 1,
        "next": null,
        "previous": null,
        "results":
        [
            {
                "organisation_name": "Bangladesh Association for Social Awareness",
                "country_name": null,
                "group_name": "Users",
                "id": 17,
                "organisation": 19,
                "user": 42,
                "group": 2,
                "is_approved": true,
                "country": null,
                "job_title": ""
            }
        ]
    }


 */

public class EmploymentListJsonParser extends ListJsonParser {

    /*
     * constructor
     */
    public EmploymentListJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    public void parseOneItem(JSONObject aResult) throws JSONException {
        Employment e = new Employment();
        e.setId(aResult.getString("id"));
        e.setUserId(aResult.getString("user"));
        e.setOrganisationId(aResult.getString("organisation"));
        e.setGroupId(aResult.getString("group")); //defaults to 2/Users
        e.setCountryId(aResult.getString("country"));
        e.setJobTitle(aResult.getString("job_title"));
        e.setGroupName(aResult.getString("group_name"));
        e.setApproved(aResult.getBoolean("is_approved"));
        mDba.saveEmployment(e);        
    }
    
    public void parseOneItem(String s) throws JSONException {
        parseOneItem(new JSONObject(s));
    }
        @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

        if (mResultsArray != null) {
            //Loop on results
            for (int ri = 0; ri < mResultsArray.length(); ri++) {
                parseOneItem(mResultsArray.getJSONObject(ri));
                mItemCount++;
            }
        }
    }
}
