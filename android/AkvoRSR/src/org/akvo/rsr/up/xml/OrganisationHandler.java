/*
 *  Copyright (C) 2012-2014 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.xml;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Organisation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * http://rsr.uat.akvo.org/api/v1/user/2/?format=xml&depth=1
 * Example input:
 * 
<object>
<locations type="list"><value>/api/v1/organisation_location/34/</value></locations>
<long_name>Akvo Foundation</long_name>
<logo type="hash"><original>/media/db/org/42/Organisation_42_logo_2012-10-23_10.56.32.jpg</original><thumbnails type="hash"><fb_thumb>/media/db/org/42/Organisation_42_logo_2012-10-23_10.56.32_jpg_200x200_pad_q85.jpg</fb_thumb><map_thumb>/media/db/org/42/Organisation_42_logo_2012-10-23_10.56.32_jpg_160x120_autocrop_q85.jpg</map_thumb></thumbnails></logo>
<id type="integer">42</id>
<resource_uri>/api/v1/organisation/42/</resource_uri>
<last_modified_at>2014-04-07T14:29:57</last_modified_at><fax/>
<organisation_type>N</organisation_type>
<description>
Akvo develops and runs web and mobile services that are designed to support international development partnership networks.</description>
<phone>+31-(0)20-8200175</phone>
<contact_email>peter@akvo.org</contact_email>
<iati_org_id>NL-KVK-27327087</iati_org_id><name>Akvo</name>
<language>en</language>
<url>http://www.akvo.org/</url>
<absolute_url>/organisation/42/</absolute_url>
<created_at type="boolean">True</created_at>
<mobile/>
<contact_person>Peter van der Linde</contact_person>
<new_organisation_type type="integer">22</new_organisation_type>
<primary_location><city>Amsterdam</city><country>/api/v1/country/3/</country><organisation>/api/v1/organisation/42/</organisation><primary type="boolean">True</primary><longitude type="float">4.907987</longitude><state>Noord-Holland</state><address_1>'s-Gravenhekje 1A</address_1><address_2/><latitude type="float">52.3723</latitude><postcode>1011 TG</postcode><id type="integer">34</id><resource_uri>/api/v1/organisation_location/34/</resource_uri>
</primary_location>
<notes/>
.....
</object>

 */



public class OrganisationHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_org = false;
	private boolean in_id = false;
	private boolean in_name = false;
	private boolean in_longname = false;
    private boolean in_email = false;
    private boolean in_url = false;

	private Organisation currentOrg;
	private boolean syntaxError = false;
	private int depth = 0;
    private String defaultOrgId;
    private String buffer;
	
	//where to store results
	private RsrDbAdapter dba;
	
	/*
	 * constructor
	 */
	public OrganisationHandler(RsrDbAdapter aDba, String defaultId){
		super();
		defaultOrgId = defaultId;
		dba = aDba;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean getError() {
		return syntaxError;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		dba.open();
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
		if (localName.equals("object") && depth == 0) {
			this.in_org = true;
			currentOrg = new Organisation();
			currentOrg.setId(defaultOrgId);
		} else if (in_org && depth == 1) {
			if (localName.equals("id")) {
				this.in_id = true;
			} else if (localName.equals("name")) {
				this.in_name = true;
            } else if (localName.equals("long_name")) {
                this.in_longname = true;
            } else if (localName.equals("contact_email")) {
                this.in_email = true;
            } else if (localName.equals("url")) {
                this.in_url = true;
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

		if (in_org && depth == 1) {
    		if (in_id && localName.equals("id")) {
    			this.in_id = false;
    			currentOrg.setId(buffer);
    		} else if (localName.equals("name")) {
    			this.in_name = false;
    			currentOrg.setName(buffer);
    		} else if (localName.equals("long_name")) {
    			this.in_longname = false;
    			currentOrg.setLongName(buffer);
            } else if (localName.equals("contact_email")) {
                this.in_email = false;
            } else if (localName.equals("url")) {
                this.in_url = false;
            }
		} else if (localName.equals("object") && depth == 0) { //Done
			this.in_org = false;
			if (currentOrg != null) {
				dba.saveOrganisation(currentOrg);
				currentOrg = null;
			} else
				syntaxError = true;
		}
	}

	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (currentOrg != null) {
			if (this.in_id ||
			    this.in_email ||
			    this.in_url ||
			    this.in_name ||
				this.in_longname
			        ) { //remember content
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
