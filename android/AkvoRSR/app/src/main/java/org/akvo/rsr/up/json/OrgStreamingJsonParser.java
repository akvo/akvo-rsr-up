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

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Organisation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
 * http://rsr.akvo.org/rest/v1/typeaheads/organisations/
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


//This is an attempt to radically speed up parsing the very long JSON file
public class OrgStreamingJsonParser {

    protected boolean mSyntaxError = false;
    protected String mServerVersion = "";
    protected SimpleDateFormat mDateOnly;
    protected SimpleDateFormat mDateTime;
    int mItemCount = 0;
    int mItemTotalCount = 0;

    // where to store results
    protected RsrDbAdapter mDba;

    /*
     * constructor
     */
    public OrgStreamingJsonParser(RsrDbAdapter aDba, String serverVersion) {
        mDba = aDba;
        mServerVersion = serverVersion;
    }

    public int getCount() {
        return mItemCount;
    }

    public int getTotalCount() {
        return mItemTotalCount;
    }


    public void parse(String body) throws JsonParseException, IOException  {
        JsonFactory f = new JsonFactory();
        JsonParser parser = f.createParser(body);
 
        // continue parsing the token till the end of input is reached
        while (!parser.isClosed()) {
            // get the token
            JsonToken token = parser.nextToken();
            // if its the last token then we are done
            if (token == null)
                break;
            // we want to look for a field that says results
 
            if (JsonToken.FIELD_NAME.equals(token) && "count".equals(parser.getCurrentName())) {
                token = parser.nextToken();
                mItemTotalCount = parser.getIntValue();
            } else
            if (JsonToken.FIELD_NAME.equals(token) && "results".equals(parser.getCurrentName())) {
                // we are entering the datasets now. The first token should be
                // start of array
                token = parser.nextToken();
                if (!JsonToken.START_ARRAY.equals(token)) {
                    break;// bail out
                }
                //loop over each element of the array
                while (true) {

                    // each element of the array is an org object so the next token should be {
                    token = parser.nextToken();
                    if (!JsonToken.START_OBJECT.equals(token)) {
                        break;
                    }

                    Organisation o = new Organisation(); //start of a new org

                    while (!JsonToken.END_OBJECT.equals(token)) {
                        token = parser.nextToken();
                        if (JsonToken.FIELD_NAME.equals(token) && "id".equals(parser.getCurrentName())) {
                            token = parser.nextToken();
                            o.setId(Integer.toString(parser.getIntValue()));
                        } else if (JsonToken.FIELD_NAME.equals(token) && "name".equals(parser.getCurrentName())) {
                            token = parser.nextToken();
                            o.setName(parser.getText());
                        } else if (JsonToken.FIELD_NAME.equals(token) && "long_name".equals(parser.getCurrentName())) {
                            token = parser.nextToken();
                            o.setLongName(parser.getText());
                        }
                    }
                    mDba.saveOrganisation(o);
                    mItemCount++;
                    
                }
            }
        }
    }
}
