/*
 *  Copyright (C) 2015-2016,2020 Stichting Akvo (Akvo Foundation)
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
import org.akvo.rsr.up.util.Downloader.ProgressReporter;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * http://rsr.akvo.org/rest/v1/organisation/?format=xml
 * Example input:
 * 
{
    "count": 2868, 
    "results": [
        {
            "id": 2550, 
            "name": "(HCT)", 
            "long_name": "Strenghtening integrated delivery of HIV/AIDS Services"
        }, 
        {
            "id": 411, 
            "name": "1%Club", 
            "long_name": "Stichting 1%CLUB"
        }, 
 ...

 */

public class OrgTypeaheadJsonParser extends ListJsonParser {

    /*
     * constructor
     */
    public OrgTypeaheadJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    public void parse(String body, ProgressReporter prog) throws JSONException {

    	super.parse(body);

    	prog.sendUpdate(0, mItemTotalCount);
    	if (mResultsArray != null) {
			//Loop on results
    		for (int ri = 0; ri < mResultsArray.length(); ri++) {
    			JSONObject aResult = mResultsArray.getJSONObject(ri);
    			Organisation o = new Organisation();
    			o.setId(aResult.getString("id"));
                o.setName(aResult.getString("name"));
                o.setLongName(aResult.getString("long_name"));
                mDba.saveMinimalOrganisation(o);
                mItemCount++;
                prog.sendUpdate(mItemCount, mItemTotalCount);
    		}
    	}
    }
}
