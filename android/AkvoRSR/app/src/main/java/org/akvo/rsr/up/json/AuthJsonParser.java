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
import org.akvo.rsr.up.domain.Organisation;
import org.akvo.rsr.up.domain.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * http://rsr.akvo.org/auth/?format=json
 * sample input:
 * 
{
    "username": "teststellan",
    "user_id": 1465,
    "organisations": [411, 411, 361],
    "allow_edit_projects": [4412, 4241, 467, 293],
    "api_key": "fe30377723549e09e0707b4d3518fcb5977f7e09",
    "published_projects": [4412, 4241, 2036, 2035, 2034, 2033, 2031, 2029, 2024, 2022, 2021, 1336, 1117, 1114, 852, 677, 659, 467, 293, 284, 275, 138, 54]
    }

 */

public class AuthJsonParser extends BaseJsonParser {

    User mUser;
    /*
     * constructor
     */
    public AuthJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    //Unusually, this method does not save to the DB
    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mRoot != null) {
    		mUser = new User();
            mUser.setId(mRoot.getString("user_id"));
            mUser.setUsername(mRoot.getString("username"));
    		mUser.setApiKey(mRoot.getString("api_key"));
            //Loop on nested orgs
            JSONArray locationsArray = mRoot.getJSONArray("organisations"); 
            for (int i = 0; i < locationsArray.length(); i++) {
                mUser.addOrgId(Integer.toString(locationsArray.getInt(i)));
            }
            //Loop on nested published projects
            JSONArray pubProjArray = mRoot.getJSONArray("published_projects"); 
            for (int i = 0; i < pubProjArray.length(); i++) {
                mUser.addPublishedProjId(Integer.toString(pubProjArray.getInt(i)));//int or long?
            }
            //Loop on nested editable projects
            JSONArray editProjArray = mRoot.getJSONArray("allow_edit_projects"); 
            for (int i = 0; i < editProjArray.length(); i++) {
                mUser.addEditProjId(Integer.toString(editProjArray.getInt(i)));//int or long?
            }
    		
    	}
    }
    
    public User getUser() {
        return mUser;
    }
}
