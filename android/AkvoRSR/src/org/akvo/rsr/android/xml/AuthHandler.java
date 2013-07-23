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
</credentials>

 */



public class AuthHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_cred = false;
	private boolean in_apikey = false;
	private boolean in_userid = false;
	private boolean in_orgid = false;
	private boolean syntaxError = false;
	private String apiKey = null;
	private String userId = null;
	private String orgId = null;
	private int level;

	/*
	 * constructor
	 */
	AuthHandler(){
		super();
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean getError() {
		return syntaxError;
	}

	public String getApiKey() {
		return apiKey;
	}
	
	public String getUserId() {
		return userId;
	}

	public String getOrgId() {
		return orgId;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		level = 0;
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
		if (localName.equals("credentials") && level == 0) {
			this.in_cred = true;
		} else if (in_cred && localName.equals("api_key")) {
			this.in_apikey = true;
		} else if (in_cred && localName.equals("user_id")) {
			this.in_userid = true;
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
		} else if (localName.equals("user_id")) {
			this.in_userid = false;
		} else if (localName.equals("org_id")) {
			this.in_orgid = false;
		}
	}
	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (this.in_apikey) {
			apiKey = new String(ch, start, length);
		} else if (this.in_userid) {
			userId = new String(ch, start, length);
		} else if (this.in_orgid) {
			orgId = new String(ch, start, length);
		}
    }

}
