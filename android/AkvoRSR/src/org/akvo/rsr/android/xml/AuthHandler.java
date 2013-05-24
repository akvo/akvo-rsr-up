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
import org.akvo.rsr.android.domain.Project;
import org.akvo.rsr.android.domain.Update;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Example input:
 * 
<credentials>
	<api_key>asdjklfhlasufhkjasdjfnhalkjdnkjsdhfkjsdnkjfnsdfkjhsdkjfs</api_key>
</credentials>

 */



public class AuthHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_cred = false;
	private boolean in_apikey = false;
	private boolean syntaxError = false;
	private String apiKey = null;

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

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
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
		if (localName.equals("credentials")) {
			this.in_cred = true;
		} else if (localName.equals("api_key")) {
			this.in_apikey = true;
		}
	}
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)	throws SAXException {
		if (localName.equals("credentials")) {
			this.in_cred = false;
		} else if (localName.equals("api_key")) {
			this.in_apikey = false;
		}
	}
	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if(this.in_apikey) {
			apiKey=new String(ch, start, length);
		} else
			syntaxError = true; //set error flag
    }

}
