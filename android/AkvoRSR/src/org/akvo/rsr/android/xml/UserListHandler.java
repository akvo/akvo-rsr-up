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

package org.akvo.rsr.android.xml;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.Country;
import org.akvo.rsr.android.domain.User;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * http://rsr.uat.akvo.org/api/v1/user/2/?format=xml&depth=1
 * Example input:
 * 
<object>
<username>thomas</username>
<first_name>Thomas</first_name>
<last_name>Bjelkeman-Pettersson</last_name>
<user_profile>
	<organisation>/api/v1/organisation/42/</organisation>
	<is_org_admin type="boolean">False</is_org_admin>
	<is_org_editor type="boolean">False</is_org_editor>
	<user>/api/v1/user/2/</user>
	<resource_uri>/api/v1/user_profile/16/</resource_uri>
</user_profile>
<last_login>2013-11-14T12:21:59</last_login>
<email>thomas@akvo.org</email>
<resource_uri>/api/v1/user/2/</resource_uri>
</object>

 */



public class UserListHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_user = false;
	private boolean in_profile = false;
	private boolean in_id = false;
	private boolean in_username = false;
	private boolean in_firstname = false;
	private boolean in_lastname = false;
	private boolean in_email = false;
	private boolean in_org_id = false;

	private User currentUser;
	private int userCount;
	private boolean syntaxError = false;
	private int depth = 0;
	private String buffer;
	private String defaultUserId;
	
	//where to store results
	private RsrDbAdapter dba;
	
	/*
	 * constructor
	 */
	UserListHandler(RsrDbAdapter aDba, String defaultId){
		super();
		dba = aDba;
		defaultUserId = defaultId;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean getError() {
		return syntaxError;
	}

	public int getCount() {
		return userCount;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		dba.open();
		userCount = 0;
		depth = 0;
	}

	@Override
	public void endDocument() throws SAXException {
		dba.close();
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		buffer = "";
		if (localName.equals("object")) {
			this.in_user = true;
			currentUser = new User();
			currentUser.setId(defaultUserId);
		} else if (in_user) {
			if (!in_profile && localName.equals("resource_uri")) {
				this.in_id = true;
			} else if (localName.equals("username")) {
				this.in_username = true;
			} else if (localName.equals("user_profile")) {
				this.in_profile = true;
			} else if (localName.equals("first_name")) {
				this.in_firstname = true;
			} else if (localName.equals("last_name")) {
				this.in_lastname = true;
			} else if (localName.equals("email")) {
				this.in_email = true;
			}
			if (in_profile) {
				if (localName.equals("organisation")) {
				this.in_org_id = true;
				}
				
			}
		}
		depth++;
	}
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		depth--;

		if (in_id && localName.equals("resource_uri")) {
			this.in_id = false;
			currentUser.setId(idFromUrl(buffer));
		} else if (localName.equals("username")) {
			this.in_username = false;
			currentUser.setUsername(buffer);
		} else if (localName.equals("first_name")) {
			this.in_firstname = false;
			currentUser.setFirstname(buffer);
		} else if (localName.equals("last_name")) {
			this.in_lastname = false;
			currentUser.setLastname(buffer);
		} else if (localName.equals("email")) {
			this.in_email = false;
			currentUser.setEmail(buffer);
		} else if (in_org_id && localName.equals("organisation")) {
			this.in_org_id = false;
			currentUser.setOrgId(idFromUrl(buffer));
		} else if (localName.equals("object")) {
			this.in_user = false;
			if (currentUser != null) {
				dba.saveUser(currentUser);
				userCount++;
				currentUser = null;
			} else
				syntaxError=true;
		}
	}

	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (currentUser != null) {
			if (this.in_id ||
				this.in_username ||
				this.in_firstname ||
				this.in_lastname ||
				this.in_email ||
				this.in_org_id) { //remember content
				buffer += new String(ch, start, length);
				}	
		} else
			syntaxError = true; //set error flag
    }

	// extract id from things like /api/v1/project/574/
	private String idFromUrl(String s) {
		if (s.endsWith("/")) {
			int i = s.lastIndexOf('/',s.length()-2);
			if (i>=0) {
				return s.substring(i+1, s.length()-1);
			} else syntaxError = true;
		} else syntaxError = true;
		return null;
	}

}
