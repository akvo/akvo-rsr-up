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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class JsonParser {

    // ===========================================================
    // Fields
    // ===========================================================


	JSONObject mRoot;
	protected boolean mSyntaxError = false;
    protected String mServerVersion = "";
    protected SimpleDateFormat dateOnly;
    protected SimpleDateFormat dateTime;

    // where to store results
    protected RsrDbAdapter mDba;

    /**
     * constructor
     * aDba: an open database adapter
     * serverVersion: From http header, in case there ever needs to be version-specific parsing
     */
    public JsonParser(RsrDbAdapter aDba, String serverVersion) {
        mDba = aDba;
        mServerVersion = serverVersion;
    	//prepare for date parsing
    	dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    	dateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
    	dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    	dateOnly.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected Date dateOrNull(String s) {
    	try {
    		return dateOnly.parse(s);
		} catch (ParseException e) {
	    	return null;
		}
    }
    
    protected Date dateTimeOrNull(String s) {
    	try {
    		return dateTime.parse(s);
		} catch (ParseException e) {
	    	return null;
		}
    }
    
    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public boolean getError() {
        return mSyntaxError;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    public void parse(String body) throws JSONException {

    	mRoot = new JSONObject(body);
    }
    
    
}
