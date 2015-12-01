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
import org.akvo.rsr.up.domain.Country;
import org.json.JSONException;

/*
 * http://rsr.akvo.org/rest/v1/country/3/?format=json
 * sample result:
 * 
 
    {
        "id": 3,
        "name": "Netherlands",
        "iso_code": "nl",
        "continent": "Europe",
        "continent_code": "eu"
    }

 */

public class CountryJsonParser extends JsonParser {

    /*
     * constructor
     */
    public CountryJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mRoot != null) {
    		Country c = new Country();
    		c.setId(mRoot.getString("id"));
    		c.setName(mRoot.getString("name"));
    		c.setIsoCode(mRoot.getString("iso_code"));
    		c.setContinent(mRoot.getString("continent"));
    		
    		mDba.saveCountry(c);
    	}
    }
}
