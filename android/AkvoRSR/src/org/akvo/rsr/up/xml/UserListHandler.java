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

package org.akvo.rsr.up.xml;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.User;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * http://rsr.uat.akvo.org/api/v1/user/2/?format=xml&depth=1
 * Example input if not same org:
 * 
<object>
    <first_name>Robert </first_name>
    <last_login>2013-01-11T10:43:21</last_login>
    <last_name>Knol</last_name>
    <resource_uri>/api/v1/user/222/</resource_uri>
</object>

If called by an admin, get a nested list of organisations

<object><email>lissy@akvo.org</email><first_name>Lissy</first_name><last_login>2015-01-05T11:57:49</last_login><last_name>van Noort</last_name>
<organisations type="list">
<object><absolute_url>/organisation/42/</absolute_url><allow_edit type="boolean">True</allow_edit><contact_email>lissy@akvo.org</contact_email><contact_person>Lissy van Noort</contact_person><created_at>2008-08-04T00:00:00</created_at>
<description>Akvo develops and runs web and mobile services that are designed to support international development partnership networks. Akvo manages a number of unique and transformative services, backed by a partner support and training team: 
Akvo is a non-profit foundation with staff in the Netherlands, Britain, Sweden, Kenya, India and the United States. The Akvo tools are open source, used by over 700 organisations and applied throughout the world for better programming and reporting in areas such as water, sanitation, health, education and economic development.</description>
<facebook/><fax/><iati_org_id>NL-KVK-27327087</iati_org_id><id type="integer">42</id><language>en</language><last_modified_at>2015-03-18T09:29:15.913590</last_modified_at><linkedin/><locations type="list"><value>/api/v1/organisation_location/34/</value></locations><logo type="hash"><original>/media/db/org/42/Organisation_42_logo_2014-08-07_15.32.44.jpg</original><thumbnails type="null"/></logo><long_name>Akvo Foundation</long_name><mobile/><name>Akvo</name><new_organisation_type type="integer">22</new_organisation_type><notes>Organisation duplicate 2091 deleted</notes><organisation_type>N</organisation_type><partnerships type="list"><value>/api/v1/partnership/45021/</value><value>/api/v1/partnership/21186/</value><value>/api/v1/partnership/21296/</value><value>/api/v1/partnership/2226/</value><value>/api/v1/partnership/2389/</value><value>/api/v1/partnership/21396/</value><value>/api/v1/partnership/31/</value><value>/api/v1/partnership/27262/</value><value>/api/v1/partnership/42640/</value><value>/api/v1/partnership/8992/</value><value>/api/v1/partnership/12321/</value><value>/api/v1/partnership/12377/</value><value>/api/v1/partnership/16268/</value><value>/api/v1/partnership/41302/</value><value>/api/v1/partnership/324/</value><value>/api/v1/partnership/39398/</value><value>/api/v1/partnership/35739/</value><value>/api/v1/partnership/40636/</value><value>/api/v1/partnership/5752/</value><value>/api/v1/partnership/12028/</value><value>/api/v1/partnership/1099/</value><value>/api/v1/partnership/4950/</value><value>/api/v1/partnership/1515/</value><value>/api/v1/partnership/27522/</value><value>/api/v1/partnership/26412/</value><value>/api/v1/partnership/26448/</value><value>/api/v1/partnership/45772/</value><value>/api/v1/partnership/1130/</value><value>/api/v1/partnership/28067/</value><value>/api/v1/partnership/27000/</value><value>/api/v1/partnership/27261/</value><value>/api/v1/partnership/26441/</value><value>/api/v1/partnership/3452/</value><value>/api/v1/partnership/4123/</value><value>/api/v1/partnership/27258/</value><value>/api/v1/partnership/21338/</value><value>/api/v1/partnership/21340/</value><value>/api/v1/partnership/21341/</value><value>/api/v1/partnership/21342/</value><value>/api/v1/partnership/21343/</value><value>/api/v1/partnership/21345/</value><value>/api/v1/partnership/21346/</value><value>/api/v1/partnership/21348/</value><value>/api/v1/partnership/21349/</value><value>/api/v1/partnership/21350/</value><value>/api/v1/partnership/21351/</value><value>/api/v1/partnership/21352/</value><value>/api/v1/partnership/21353/</value><value>/api/v1/partnership/21354/</value><value>/api/v1/partnership/21355/</value><value>/api/v1/partnership/21356/</value><value>/api/v1/partnership/21357/</value><value>/api/v1/partnership/21359/</value><value>/api/v1/partnership/21360/</value><value>/api/v1/partnership/21361/</value><value>/api/v1/partnership/21362/</value><value>/api/v1/partnership/21363/</value><value>/api/v1/partnership/21364/</value><value>/api/v1/partnership/21365/</value><value>/api/v1/partnership/21366/</value><value>/api/v1/partnership/21367/</value><value>/api/v1/partnership/21405/</value><value>/api/v1/partnership/26355/</value><value>/api/v1/partnership/26402/</value><value>/api/v1/partnership/26405/</value><value>/api/v1/partnership/26406/</value><value>/api/v1/partnership/26407/</value><value>/api/v1/partnership/26408/</value><value>/api/v1/partnership/26410/</value><value>/api/v1/partnership/26411/</value><value>/api/v1/partnership/26413/</value><value>/api/v1/partnership/26414/</value><value>/api/v1/partnership/26415/</value><value>/api/v1/partnership/26416/</value><value>/api/v1/partnership/26440/</value><value>/api/v1/partnership/26456/</value><value>/api/v1/partnership/26457/</value><value>/api/v1/partnership/34071/</value><value>/api/v1/partnership/26547/</value><value>/api/v1/partnership/26701/</value><value>/api/v1/partnership/27259/</value><value>/api/v1/partnership/27257/</value><value>/api/v1/partnership/27007/</value><value>/api/v1/partnership/28091/</value><value>/api/v1/partnership/33923/</value><value>/api/v1/partnership/35701/</value><value>/api/v1/partnership/35702/</value><value>/api/v1/partnership/35703/</value><value>/api/v1/partnership/35704/</value><value>/api/v1/partnership/35705/</value><value>/api/v1/partnership/35706/</value><value>/api/v1/partnership/35707/</value><value>/api/v1/partnership/35708/</value><value>/api/v1/partnership/35709/</value><value>/api/v1/partnership/26998/</value><value>/api/v1/partnership/681/</value><value>/api/v1/partnership/851/</value><value>/api/v1/partnership/975/</value><value>/api/v1/partnership/26395/</value><value>/api/v1/partnership/1955/</value><value>/api/v1/partnership/2123/</value><value>/api/v1/partnership/26392/</value><value>/api/v1/partnership/3214/</value><value>/api/v1/partnership/3954/</value><value>/api/v1/partnership/26446/</value><value>/api/v1/partnership/21300/</value><value>/api/v1/partnership/21301/</value><value>/api/v1/partnership/21302/</value><value>/api/v1/partnership/21303/</value><value>/api/v1/partnership/21304/</value><value>/api/v1/partnership/21305/</value><value>/api/v1/partnership/21306/</value><value>/api/v1/partnership/21307/</value><value>/api/v1/partnership/21308/</value><value>/api/v1/partnership/21309/</value><value>/api/v1/partnership/21310/</value><value>/api/v1/partnership/21311/</value><value>/api/v1/partnership/21312/</value><value>/api/v1/partnership/21313/</value><value>/api/v1/partnership/21314/</value><value>/api/v1/partnership/21315/</value><value>/api/v1/partnership/21317/</value><value>/api/v1/partnership/21318/</value><value>/api/v1/partnership/21319/</value><value>/api/v1/partnership/21320/</value><value>/api/v1/partnership/21321/</value><value>/api/v1/partnership/21322/</value><value>/api/v1/partnership/21323/</value><value>/api/v1/partnership/21337/</value></partnerships><phone>+31-(0)20-8200175</phone><primary_location><address_1>'s-Gravenhekje 1A</address_1><address_2/><city>Amsterdam</city><country>/api/v1/country/3/</country><id type="integer">34</id><latitude type="float">52.3723</latitude><longitude type="float">4.907987</longitude><organisation>/api/v1/organisation/42/</organisation><postcode>1011 TG</postcode><resource_uri>/api/v1/organisation_location/34/</resource_uri><state>Noord-Holland</state></primary_location><resource_uri>/api/v1/organisation/42/</resource_uri><twitter/><url>http://www.akvo.org/</url>
</object>
</organisations>
<resource_uri>/api/v1/user/394/</resource_uri>
<username>Lissyvn</username>
</object>

 */



public class UserListHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_user = false;
	private boolean in_id = false;
	private boolean in_username = false;
	private boolean in_firstname = false;
	private boolean in_lastname = false;
	private boolean in_email = false;
    private boolean in_org = false;
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
	public UserListHandler(RsrDbAdapter aDba, String defaultId){
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
        if (depth==0 && localName.equals("object")) {
            this.in_user = true;
            currentUser = new User();
            currentUser.setId(defaultUserId);
        } else if (in_user && depth==1 && !in_org) {
            if (localName.equals("resource_uri")) {
                this.in_id = true;
			} else if (localName.equals("username")) {
				this.in_username = true;
			} else if (localName.equals("first_name")) {
				this.in_firstname = true;
			} else if (localName.equals("last_name")) {
				this.in_lastname = true;
			} else if (localName.equals("email")) {
				this.in_email = true;
			}
        } else if (in_user && depth == 2 && localName.equals("object")) {
            this.in_org = true;
        } else if (in_org && depth == 3) {
            if (localName.equals("id")) {
                this.in_org_id = true;
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
		} else if (in_username && localName.equals("username")) {
			this.in_username = false;
			currentUser.setUsername(buffer);
		} else if (in_firstname && localName.equals("first_name")) {
			this.in_firstname = false;
			currentUser.setFirstname(buffer);
		} else if (in_lastname && localName.equals("last_name")) {
			this.in_lastname = false;
			currentUser.setLastname(buffer);
		} else if (in_email && localName.equals("email")) {
			this.in_email = false;
			currentUser.setEmail(buffer);
        } else if (in_org_id && localName.equals("id")) {
            this.in_org_id = false;
//            currentUser.setOrgId(idFromUrl(buffer));
            currentUser.setOrgId(buffer);
        } else if (in_org && depth==2 && localName.equals("object")) {
            this.in_org = false;
		} else if (in_user && depth==0 && localName.equals("object")) {
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
