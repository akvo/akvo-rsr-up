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
import org.akvo.rsr.up.domain.Country;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * http://rsr.akvo.org/rest/v1/country/3/?format=json
 * sample result:
 * 
 
{
    "count": 134,
    "next": null,
    "previous": null,
    "results":
    [
        {
            "id": 3,
            "name": "Netherlands",
            "iso_code": "nl",
            "continent": "Europe",
            "continent_code": "eu"
        },
...

 */

public class CountryListJsonParser extends ListJsonParser {

    /*
     * constructor
     */
    public CountryListJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

        if (mResultsArray != null) {
            //Loop on results
            for (int ri = 0; ri < mResultsArray.length(); ri++) {
                JSONObject aResult = mResultsArray.getJSONObject(ri);
                Country c = new Country();
                c.setId(aResult.getString("id"));
                c.setName(aResult.getString("name"));
                c.setIsoCode(aResult.getString("iso_code"));
                c.setContinent(aResult.getString("continent"));
                mDba.saveCountry(c);
                mItemCount++;
            }
        }
    }
}
