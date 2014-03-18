/*
 *  Copyright (C) 2012-2013 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo RSR.
 *
 *  Akvo RSR is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
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
	private Set<String> publishedProjects;

	public User() {
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

	public Set<String> getPublishedProjects() {
		return publishedProjects;
	}

	public void addPublishedProject(String id) {
		this.publishedProjects.add(id);
	}

	public void clearPublishedProjects() {
		this.publishedProjects.clear();
	}
}
