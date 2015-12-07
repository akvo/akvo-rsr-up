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

package org.akvo.rsr.up.json;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.json.JSONArray;
import org.json.JSONException;



public class ListJsonParser extends JsonParser{

    // ===========================================================
    // Fields
    // ===========================================================


	int mItemCount = 0;
	int mItemTotalCount = 0;
    private String mNextUrl = "";
    protected JSONArray mResultsArray;

    /**
     * constructor
     * aDba: an open database adapter
     * serverVersion: From http header, in case there is ever a need for version-specific parsing
     */
    public ListJsonParser(RsrDbAdapter aDba, String serverVersion) {
    	super(aDba, serverVersion);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public int getCount() {
        return mItemCount;
    }

    public int getTotalCount() {
        return mItemTotalCount;
    }

    public String getNextUrl() {
        return mNextUrl;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    public void parse(String body) throws JSONException {
    	super.parse(body);
    	mItemCount= 0;
        
        mNextUrl = mRoot.getString("next");
        mItemTotalCount = mRoot.getInt("count");
        mResultsArray = mRoot.getJSONArray("results");
    }
    
    
}
