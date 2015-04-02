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
	private String id;
	private String username;
	private String firstname;
	private String lastname;
	private String email;
	private String apiKey;
	private String orgId;
    private Set<String> mOrgIds;
    private Set<String> publishedProjects;

	public User() {
        mOrgIds = new HashSet<String>(2);
        publishedProjects = new HashSet<String>(10);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String id) {
		this.orgId = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String name) {
		this.username = name;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String name) {
		this.firstname = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String name) {
		this.lastname = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String summary) {
		this.apiKey = summary;
	}

    public Set<String> getPublishedProjIds() {
        return publishedProjects;
    }

    public String getPublishedProjIdsString() {
        String projlist = "";
        for (String id : publishedProjects) {
            projlist += id + ",";
        }
        if (projlist.length() > 0)
            projlist = projlist.substring(0, projlist.length()-1);
        return projlist;
    }

    public void addPublishedProjId(String id) {
        this.publishedProjects.add(id);
    }

    public void clearPublishedProjIds() {
        this.publishedProjects.clear();
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
        this.mOrgIds.add(id);
    }

    public void clearOrgIds() {
        this.mOrgIds.clear();
    }
}
