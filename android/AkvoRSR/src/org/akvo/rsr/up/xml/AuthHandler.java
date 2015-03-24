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
 *  See the GNU Affero General Public License included with this program for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.up.xml;

import java.util.Set;

import org.akvo.rsr.up.domain.User;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Example input:
 * 
<credentials>
	<api_key>asdjklfhlasufhkjasdjfnhalkjdnkjsdhfkjsdnkjfnsdfkjhsdkjfs</api_key>
	<user_id>666</user_id>
	<org_id>42</org_id>
	<published_projects>
	<id>42</id>
	<id>4711</id>
	</published_projects>
</credentials>

 */



public class AuthHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_cred = false;
	private boolean in_apikey = false;
	private boolean in_userid = false;
	private boolean in_username = false;
	private boolean in_orgid = false;
	private boolean in_projects = false;
	private boolean in_projid = false;
	private boolean syntaxError = false;
	private User user = null;
	private String val;
	private int level;

	/*
	 * constructor
	 */
	public AuthHandler(){
		super();
	}
	
	// ===========================================================
	// Getters & Setters
	// ===========================================================

	public boolean getError() {
		return syntaxError;
	}

	public String getApiKey() {
		return user.getApiKey();
	}
	
	public Set<String> getPublishedProjects() {
		return user.getPublishedProjects();
	}
	
	public User getUser() {
		return user;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		level = 0;
		user = new User();
	}

	@Override
	public void endDocument() throws SAXException {
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,	String qName, Attributes atts) throws SAXException {
		val = "";
		if (localName.equals("credentials") && level == 0) {
			this.in_cred = true;
		} else if (in_cred && localName.equals("api_key")) {
			this.in_apikey = true;
		} else if (in_cred && localName.equals("username")) {
			this.in_username = true;
		} else if (in_cred && localName.equals("user_id")) {
			this.in_userid = true;
		} else if (in_cred && localName.equals("published_projects")) {
			this.in_projects = true;
		} else if (in_projects && localName.equals("id")) {
			this.in_projid = true;
		} else if (in_cred && localName.equals("org_id")) {
			this.in_orgid = true;
		}
		level++;
	}
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)	throws SAXException {
		level--;
		if (localName.equals("credentials")) {
			this.in_cred = false;
		} else if (localName.equals("api_key")) {
			this.in_apikey = false;
			user.setApiKey(val);
		} else if (localName.equals("username")) {
			this.in_username = false;
			user.setUsername(val);
		} else if (localName.equals("user_id")) {
			this.in_userid = false;
			user.setId(val);
		} else if (localName.equals("published_projects")) {
			this.in_projects = false;
		} else if (in_projid && localName.equals("id")) {
			this.in_projid = false;
			user.addPublishedProject(val);
		} else if (localName.equals("org_id")) {
			this.in_orgid = false;
			user.setOrgId(val);
		}
	}
	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (this.in_apikey
		|| this.in_username
		|| this.in_userid
		|| this.in_orgid
		|| this.in_projid) {
			val += new String(ch, start, length);
		}
    }

}
