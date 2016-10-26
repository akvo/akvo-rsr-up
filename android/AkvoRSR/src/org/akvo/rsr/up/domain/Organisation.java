/*
 *  Copyright (C) 2012-2015 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.domain;

import java.util.Date;


public class Organisation {
	private String mId;
	private String mName;
    private String mLongName;
    private String mEmail;
    private String mUrl;
    private String mOldType;//Type letter
    private String mNewType;//IATI type integer code?
    private String mPrimaryCountryId;
    private Date mLastModifiedAt;
    private String mLogo;
//    private Set<String>mCountryIds;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getOldType() {
        return mOldType;
    }

    public void setOldType(String t) {
        mOldType = t;
    }

    public String getNewType() {
        return mNewType;
    }

    public void setNewType(String t) {
        mNewType = t;
    }

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getLongName() {
		return mLongName;
	}

	public void setLongName(String name) {
		mLongName = name;
	}

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Date getLastModifiedAt() {
        if (mLastModifiedAt != null) return mLastModifiedAt;
        return new Date(0);
    }

    public void setLastModifiedAt(Date lastMod ) {
        mLastModifiedAt = lastMod;
    }
    
    /*    
    public Set<String> getCountryIds() {
        return mCountryIds;
    }

    public String getCountryIdsString() {
        String projlist = "";
        for (String id : mCountryIds) {
            projlist += id + ",";
        }
        if (projlist.length() > 0)
            projlist = projlist.substring(0, projlist.length()-1);
        return projlist;
    }

    public void setCountryIdsString(String ids) {
        clearCountryIds();
        for (String s:ids.split(",")) {
            addCountryId(s.trim());
        }
    }

    public void addCountryId(String id) {
        mCountryIds.add(id);
    }

    public void clearCountryIds() {
        mCountryIds.clear();
    }
    
*/
    public String getPrimaryCountryId() {
        return mPrimaryCountryId;
    }

    public void setPrimaryCountryId(String id) {
        mPrimaryCountryId=id;
    }

    public String getLogo() {
        return mLogo;
    }

    public void setLogo(String logo) {
        mLogo = logo;
    }

}
