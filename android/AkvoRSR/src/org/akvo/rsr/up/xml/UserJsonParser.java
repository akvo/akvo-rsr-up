/*
 *  Copyright (C) 2015 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.xml;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.User;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * http://rsr.akvo.org/rest/v1/user/2/?format=json
 * sample result:
 * 
 {
  "id": 2,
  "first_name": "Thomas",
  "last_name": "Bjelkeman-Pettersson",
  "email": "thomas@akvo.org",
  "organisation": {
    "id": 42,
    "logo": "/media/cache/7f/72/7f722d662fd2c7deb8d9649c7954043d.jpg",
    "long_name": "Akvo Foundation",
    "name": "Akvo",
    "primary_location": {
      "id": 34,
      "latitude": 52.3723,
      "longitude": 4.907987,
      "city": "Amsterdam",
      "state": "Noord-Holland",
      "address_1": "'s-Gravenhekje 1A",
      "address_2": "",
      "postcode": "1011 TG",
      "location_target": {
        "id": 42,
        "created_at": "2008-08-04T22:35:06",
        "last_modified_at": "2015-11-10T10:41:11.950152",
        "name": "Akvo",
        "long_name": "Akvo Foundation",
        "language": "en",
        "organisation_type": "N",
        "new_organisation_type": 22,
        "iati_org_id": "NL-KVK-27327087",
        "logo": "db/org/42/Organisation_42_logo_2014-08-07_15.32.44.jpg",
        "url": "http://www.akvo.org/",
        "facebook": "",
        "twitter": "",
        "linkedin": "",
        "phone": "+31-(0)20-8200175",
        "mobile": "",
        "fax": "",
        "contact_person": "Lissy van Noort",
        "contact_email": "lissy@akvo.org",
        "description": "Akvo develops and runs web and mobile services that are designed to support international development partnership networks. Akvo manages a number of unique and transformative services, backed by a partner support and training team: \r\nAkvo Really Simple Reporting (RSR) makes it easy to bring complex partner networks online and enable field and support teams to share status updates. \r\nAkvo FLOW allows field surveys to be managed and carried out using Android smart phones, with results shared online in dashboards. \r\nAkvo Openaid enables governments and multilateral organisations to present aid spend data online in easy to navigate ways that help them meet transparency obligations.  \r\nAkvo is a non-profit foundation with staff in the Netherlands, Britain, Sweden, Kenya, India and the United States. The Akvo tools are open source, used by over 700 organisations and applied throughout the world for better programming and reporting in areas such as water, sanitation, health, education and economic development.",
        "notes": "Organisation duplicate 2091 deleted",
        "primary_location": 34,
        "can_create_projects": true,
        "content_owner": 42,
        "allow_edit": true,
        "public_iati_file": true,
        "can_become_reporting": true,
        "internal_org_ids": []
      },
      "country": {
        "id": 3,
        "name": "Netherlands",
        "iso_code": "nl",
        "continent": "Europe",
        "continent_code": "eu"
      }
    },
    "absolute_url": "/en/organisation/42/"
  },
  "organisations": [
    {
      "id": 42,
      "logo": "/media/cache/7f/72/7f722d662fd2c7deb8d9649c7954043d.jpg",
      "long_name": "Akvo Foundation",
      "name": "Akvo",
      "primary_location": {
        "id": 34,
        "latitude": 52.3723,
        "longitude": 4.907987,
        "city": "Amsterdam",
        "state": "Noord-Holland",
        "address_1": "'s-Gravenhekje 1A",
        "address_2": "",
        "postcode": "1011 TG",
        "location_target": {
          "id": 42,
          "created_at": "2008-08-04T22:35:06",
          "last_modified_at": "2015-11-10T10:41:11.950152",
          "name": "Akvo",
          "long_name": "Akvo Foundation",
          "language": "en",
          "organisation_type": "N",
          "new_organisation_type": 22,
          "iati_org_id": "NL-KVK-27327087",
          "logo": "db/org/42/Organisation_42_logo_2014-08-07_15.32.44.jpg",
          "url": "http://www.akvo.org/",
          "facebook": "",
          "twitter": "",
          "linkedin": "",
          "phone": "+31-(0)20-8200175",
          "mobile": "",
          "fax": "",
          "contact_person": "Lissy van Noort",
          "contact_email": "lissy@akvo.org",
          "description": "Akvo develops and runs web and mobile services that are designed to support international development partnership networks. Akvo manages a number of unique and transformative services, backed by a partner support and training team: \r\nAkvo Really Simple Reporting (RSR) makes it easy to bring complex partner networks online and enable field and support teams to share status updates. \r\nAkvo FLOW allows field surveys to be managed and carried out using Android smart phones, with results shared online in dashboards. \r\nAkvo Openaid enables governments and multilateral organisations to present aid spend data online in easy to navigate ways that help them meet transparency obligations.  \r\nAkvo is a non-profit foundation with staff in the Netherlands, Britain, Sweden, Kenya, India and the United States. The Akvo tools are open source, used by over 700 organisations and applied throughout the world for better programming and reporting in areas such as water, sanitation, health, education and economic development.",
          "notes": "Organisation duplicate 2091 deleted",
          "primary_location": 34,
          "can_create_projects": true,
          "content_owner": 42,
          "allow_edit": true,
          "public_iati_file": true,
          "can_become_reporting": true,
          "internal_org_ids": []
        },
        "country": {
          "id": 3,
          "name": "Netherlands",
          "iso_code": "nl",
          "continent": "Europe",
          "continent_code": "eu"
        }
      },
      "absolute_url": "/en/organisation/42/"
    }
  ]
}
 ...

 */

public class UserJsonParser extends JsonParser {

    private final String ORG = "organisation";
    /*
     * constructor
     */
    public UserJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mRoot != null) {
    		User u = new User();
    		u.setId(mRoot.getString("id"));
    		u.setFirstname(mRoot.getString("first_name"));
    		u.setLastname(mRoot.getString("last_name"));
    		u.setEmail(mRoot.getString("email"));
    		if (!mRoot.isNull(ORG)) { //organisation is optional until employed
    		    JSONObject org = mRoot.getJSONObject(ORG);
    		    if (org != null) u.setOrgId(org.getString("id"));
    		} 
    		
    		
    		mDba.saveUser(u);
    	}
    }
}
