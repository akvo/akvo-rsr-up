/*
 *  Copyright (C) 2015,2020 Stichting Akvo (Akvo Foundation)
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * http://rsr.akvo.org/rest/v1/organisation/?format=xml
 * Example input:
 * 
{
    "count": 2868,
    "next": "http://rsr.akvo.org/rest/v1/organisation/?page=2&format=json",
    "previous": null,
    "results":
    [
        {
            "locations":
            [
                {
                    "id": 1,
                    "latitude": 52.3723,
                    "longitude": 4.907987,
                    "city": "Amsterdam",
                    "state": "Noord-Holland",
                    "address_1": "'s-Gravenhekje 1A",
                    "address_2": "",
                    "postcode": "1011 TG",
                    "location_target": 411,
                    "country": 3
                }
            ],
            "logo": "/media/cache/35/5a/355a75e9d210457a23422c5541054eff.jpg",
            "id": 411,
            "created_at": "2011-09-05T14:11:13",
            "last_modified_at": "2015-02-26T13:56:50.339395",
            "name": "1%Club",
            "long_name": "Stichting 1%CLUB",
            "language": "en",
            "organisation_type": "N",
            "new_organisation_type": 22,
            "iati_org_id": "NL-KVK-34367327",
            "url": "http://www.onepercentclub.com/",
            "facebook": "",
            "twitter": "",
            "linkedin": "",
            "phone": "",
            "mobile": "",
            "fax": "",
            "contact_person": "Bart Lacroix",
            "contact_email": "info@1procentclub.nl",
            "description": "The 1%CLUB Foundation is an initiative of Anna Chojnacka and Bart Lacroix.

"The 1%CLUB is about power and powerlessness. We live in a world where economic means are very unequally divided. At least you can independently make sure that a part of our means ends up where you think it would be most useful!" - Anna Chojnacka.

"Through the website you can choose yourself which projects you want to support, so you know exactly where your 1% is going. The website combines Web 2.0 elements with the rise of people and organisations who want to contribute to development cooperation, and is therefore really in itself a form of International Cooperation 2.0" - Bart Lacroix.

1%Club is a marketplace that connects smart development projects with people, money and knowledge around the world. Based in Amsterdam, it is focused on engaging individuals and groups to commit one per cent of their time and money to international development projects.",
            "notes": "",
            "primary_location": 1,
            "can_create_projects": false,
            "content_owner": null,
            "allow_edit": true,
            "public_iati_file": true,
            "can_become_reporting": false,
            "internal_org_ids":
            [
            ],
            "absolute_url": "/en/organisation/411/"
        },
 ...

 */

public class OrgListJsonParser extends ListJsonParser {

	

    /*
     * constructor
     */
    public OrgListJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mResultsArray != null) {
			//Loop on results
    		for (int ri = 0; ri < mResultsArray.length(); ri++) {
    			JSONObject aResult = mResultsArray.getJSONObject(ri);
    			Organisation o = new Organisation();
    			o.setId(aResult.getString("id"));
                o.setName(aResult.getString("name"));
                o.setLongName(aResult.getString("long_name"));
                o.setOldType(aResult.getString("organisation_type"));
                o.setNewType(aResult.getString("new_organisation_type"));
                o.setLogo(aResult.getString("logo"));
                int primaryLoc = -1;
                try {
                    primaryLoc = aResult.getInt("primary_location");
                }
                catch (JSONException e) {
                    //Eat it
                }
    			//Loop on nested locations
    			JSONArray locationsArray = aResult.getJSONArray("locations"); 
        		for (int ii = 0; ii < locationsArray.length(); ii++) {
        			JSONObject aLocation = locationsArray.getJSONObject(ii);
        			//o.addCountryId(aLocation.getString("country"));
                    if (aLocation.getInt("id") == primaryLoc) {
                        o.setPrimaryCountryId(aLocation.getString("country"));
                    }
        		}
                mDba.saveOrganisation(o);
                mItemCount++;
    		}
    	}
    }
}
