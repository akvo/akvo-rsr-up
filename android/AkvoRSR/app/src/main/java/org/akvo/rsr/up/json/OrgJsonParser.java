/*
 *  Copyright (C) 2015-2017,2020 Stichting Akvo (Akvo Foundation)
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
 * http://rsr.akvo.org/rest/v1/organisation/42/?format=json
 * sample input:
 * 

    {
        "locations":
        [
            {
                "id": 34,
                "latitude": 52.3723,
                "longitude": 4.907987,
                "city": "Amsterdam",
                "state": "Noord-Holland",
                "address_1": "'s-Gravenhekje 1A",
                "address_2": "",
                "postcode": "1011 TG",
                "location_target": 42,
                "country": 3
            }
        ],
        "logo": "/media/cache/7f/72/7f722d662fd2c7deb8d9649c7954043d.jpg",
        "id": 42,
        "created_at": "2008-08-04T22:35:06",
        "last_modified_at": "2015-11-10T10:41:11.950152",
        "name": "Akvo",
        "long_name": "Akvo Foundation",
        "language": "en",
        "organisation_type": "N",
        "new_organisation_type": 22,
        "iati_org_id": "NL-KVK-27327087",
        "url": "http://www.akvo.org/",
        "facebook": "",
        "twitter": "",
        "linkedin": "",
        "phone": "+31-(0)20-8200175",
        "mobile": "",
        "fax": "",
        "contact_person": "Lissy van Noort",
        "contact_email": "lissy@akvo.org",
        "description": "Akvo develops and runs web and mobile services that are designed to support international development partnership networks. Akvo manages a number of unique and transformative services, backed by a partner support and training team: 
    Akvo Really Simple Reporting (RSR) makes it easy to bring complex partner networks online and enable field and support teams to share status updates. 
    Akvo FLOW allows field surveys to be managed and carried out using Android smart phones, with results shared online in dashboards. 
    Akvo Openaid enables governments and multilateral organisations to present aid spend data online in easy to navigate ways that help them meet transparency obligations.  
    Akvo is a non-profit foundation with staff in the Netherlands, Britain, Sweden, Kenya, India and the United States. The Akvo tools are open source, used by over 700 organisations and applied throughout the world for better programming and reporting in areas such as water, sanitation, health, education and economic development.",
        "notes": "Organisation duplicate 2091 deleted",
        "primary_location": 34,
        "can_create_projects": true,
        "content_owner": 42,
        "allow_edit": true,
        "public_iati_file": true,
        "can_become_reporting": true,
        "internal_org_ids":
        [
        ],
        "absolute_url": "/en/organisation/42/"
    }


 */

public class OrgJsonParser extends BaseJsonParser {

    /*
     * constructor
     */
    public OrgJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mRoot != null) {
    		Organisation org = new Organisation();
    		org.setId(mRoot.getString("id"));
    		org.setName(mRoot.getString("name"));
    		org.setLongName(mRoot.getString("long_name"));
    		org.setEmail(mRoot.getString("contact_email"));
            org.setOldType(mRoot.getString("organisation_type"));
            org.setNewType(mRoot.getString("new_organisation_type"));
            if (!mRoot.isNull("primary_location")) {
                int primaryLoc = mRoot.getInt("primary_location");
                //Loop on nested locations to find the primary
                JSONArray locationsArray = mRoot.getJSONArray("locations"); 
                for (int i = 0; i < locationsArray.length(); i++) {
                    JSONObject aLocation = locationsArray.getJSONObject(i);
                    //o.addCountryId(aLocation.getString("country"));
                    if (mRoot.getInt("id") == primaryLoc) {
                        org.setPrimaryCountryId(aLocation.getString("country"));
                    }
                }
            }
    		
    		mDba.saveOrganisation(org);
    	}
    }
}
