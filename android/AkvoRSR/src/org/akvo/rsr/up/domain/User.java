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

import java.util.HashSet;
import java.util.Set;


public class User {
	private String mId;
	private String mUsername;
	private String mFirstname;
	private String mLastname;
	private String mEmail;
	private String mApiKey;
	private String mLegacyOrgId; //for other users, until we start handling them having more than one org
    private Set<String> mOrgIds; //for the logged-in user
    private Set<String> mPublishedProjIds;

	public User() {
        mOrgIds = new HashSet<String>(2);
        mPublishedProjIds = new HashSet<String>(10);
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getOrgId() {
		return mLegacyOrgId;
	}

	public void setOrgId(String id) {
		mLegacyOrgId = id;
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String name) {
		mUsername = name;
	}

	public String getFirstname() {
		return mFirstname;
	}

	public void setFirstname(String name) {
		mFirstname = name;
	}

	public String getLastname() {
		return mLastname;
	}

	public void setLastname(String name) {
		mLastname = name;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		mEmail = email;
	}

	public String getApiKey() {
		return mApiKey;
	}

	public void setApiKey(String summary) {
		mApiKey = summary;
	}

    public Set<String> getPublishedProjIds() {
        return mPublishedProjIds;
    }

    public String getPublishedProjIdsString() {
        String projlist = "";
        for (String id : mPublishedProjIds) {
            projlist += id + ",";
        }
        if (projlist.length() > 0)
            projlist = projlist.substring(0, projlist.length()-1);
        return projlist;
    }

    public void addPublishedProjId(String id) {
        mPublishedProjIds.add(id);
    }

    public void clearPublishedProjIds() {
        mPublishedProjIds.clear();
    }
    
    public Set<String> getOrgIds() {
        return mOrgIds;
    }
    
    public String getOrgIdsString() {
        String orglist = "";
        for (String id : mOrgIds) {
            orglist += id + ",";
        }
        if (orglist.length() > 0)
            orglist=orglist.substring(0, orglist.length()-1);
        return orglist;
    }

    public void addOrgId(String id) {
        mOrgIds.add(id);
    }

    public void clearOrgIds() {
        mOrgIds.clear();
    }
}
