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
 *  See the GNU Affero General Public License included with this program for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.rsr.up.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Country;
import org.akvo.rsr.up.domain.Location;
import org.akvo.rsr.up.domain.Organisation;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/*
 * Class to handle XML parsing for a project update from REST API.
 * Always requested as a list, where each update object's tags of the XML will be encapsulated in <root><results><list-item>
 * Example start of list:
 * 
<root>
<count>4944</count>
<next>http://rsr.tmp.akvo-ops.org/rest/v1/project_update/?page=2&format=xml</next>
<previous/>
<results>
<list-item>
    <photo>/media/db/project/2/update/5298/ProjectUpdate_5298_photo_2014-07-24_13.35.05.jpg</photo>
    <locations>
        <list-item>
            <id>2</id>
            <latitude>59.0</latitude><longitude>18.0</longitude>
            <city>Stockholm</city><state>Stockholms län</state>
            <country>18</country>
            <address_1/><address_2/><postcode/>
            <location_target>5298</location_target>
        </list-item>
    </locations>
    <primary_location>2</primary_location>
    <project>2</project>
    <user>
        <first_name>Gabriel</first_name><last_name>von Heijne</last_name>
        <organisation>
            <logo>/media/db/org/42/Organisation_42_logo_2012-10-23_10.56.32.jpg</logo>
            <long_name>Akvo Foundation</long_name>
            <name>Akvo</name>
            <primary_location>
                <id>34</id><latitude>52.3723</latitude><longitude>4.907987</longitude><city>Amsterdam</city><state>Noord-Holland</state>
                <country>
                    <id>3</id><name>Netherlands</name><iso_code>nl</iso_code><continent>Europe</continent><continent_code>eu</continent_code>
                </country>
                <address_1>'s-Gravenhekje 1A</address_1><address_2/><postcode>1011 TG</postcode>
            </primary_location>
            <absolute_url>/organisation/42/</absolute_url>
        </organisation>
    </user>
    <id>5298</id>
    <created_at>2014-07-24T13:35:03</created_at>
    <last_modified_at>2014-07-24T13:35:05</last_modified_at>
    <title>Electro-cute</title>
    <text>Moar warm!</text>
    <language>en</language>
    <photo_caption>Purrrr</photo_caption>
    <photo_credit/>
    <video/>
    <video_caption/>
    <video_credit/>
    <update_method>M</update_method>
    <user_agent/>
    <uuid/><notes/>
    <absolute_url>/project/2/update/5298/</absolute_url>
</list-item>
...
</results>
</root>

 */



public class UpdateExtraRestListHandler extends DefaultHandler {

    private static String ID = "id";
    private static String LIST_ITEM = "list-item";
    private static String PRIMARY_LOCATION = "primary_location";
    private static String COUNTRY = "country";
    private static String USER = "user";
    private static String ORGANISATION = "organisation";
    
    
	// ===========================================================
	// Fields
	// ===========================================================
	
    private boolean in_results = false;
    private boolean in_update = false;
    private boolean in_location = false;
    private boolean in_org = false;
    private boolean in_org_location = false;
    private boolean in_country = false;
    private boolean in_user = false;
    
    private boolean in_leaf = false;

    private Update currentUpd;
    private int updateCount;

    private Location currentLoc;  //for now always the update location

    private User currentUser;
    private int userCount;
    
    private Organisation currentOrg;
    private int orgCount;

    private Country currentCountry;
    private int countryCount;

    private boolean syntaxError = false;
    private boolean insert; //insert parsed objects in the database
	private int depth = 0;
	private SimpleDateFormat df1;
	private String buffer;
	
	//where to store results
	private RsrDbAdapter dba;
	
	/*
	 * constructor
	 */
	public UpdateExtraRestListHandler(RsrDbAdapter aDba, boolean insert, String serverRsrVersion) {
		super();
		dba = aDba;
        this.insert = insert;
		df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		df1.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean getError() {
		return syntaxError;
	}

	public int getCount() {
		return updateCount;
	}

	public Update getLastUpdate() {
		return currentUpd; //only valid if insert==False
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		dba.open();
        updateCount = 0;
        countryCount = 0;
        userCount = 0;
        orgCount = 0;
		depth = 0;
		syntaxError = false;
	}

	@Override
	public void endDocument() throws SAXException {
		dba.close();
	}

	
    void startLocationElement(String localName) {
        if (localName.equals(COUNTRY)) {
            this.in_country = true;
        } else if ( localName.equals("state")
            || localName.equals("city")
            || localName.equals("latitude")
            || localName.equals("longitude")) {
            in_leaf = true;
        }
    }

    void startCountryElement(String localName) {
        if (   localName.equals(ID)
            || localName.equals("name")
            || localName.equals("iso_code")
            || localName.equals("continent")) {
            in_leaf = true;
        }
    }

    void startOrgElement(String localName) {
        //TODO: We do not care about org location yet
//        if (localName.equals(PRIMARY_LOCATION)) {
//            this.in_org_location = true;
//        } else 
        if (   localName.equals(ID) //?? In API ??
            || localName.equals("name")
            || localName.equals("long_name")) {
            in_leaf = true;
        }
    }

    void startUserElement(String localName) {
        if (localName.equals(ORGANISATION)) {
            this.in_org = true;
        } else 
        if (   localName.equals(ID) //?? In API ??
            || localName.equals("first_name")
            || localName.equals("last_name")) {
            in_leaf = true;
        }
    }

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		buffer = "";
        if (depth == 1 && localName.equals("results")) {
            this.in_results = true;
        } else if (depth == 2 && in_results && localName.equals(LIST_ITEM)) {
			this.in_update = true;
			currentUpd = new Update();
            currentLoc = currentUpd.getLocation();
		} else if (in_update) {
		    if (in_country) { //countries are always inside location
                startCountryElement(localName);
            } else if (in_location) {
                startLocationElement(localName);
            } else if (in_user) {
                startUserElement(localName);
            } else if (in_org) {
                startOrgElement(localName);
            } else if (localName.equals(ID)
                    || localName.equals("title")
                    || localName.equals("text")
                    || localName.equals("time")
                    || localName.equals("project")
                    || localName.equals("user")
                    || localName.equals("uuid")
                    || localName.equals("photo")
                    || localName.equals("photo_credit")
                    || localName.equals("photo_caption")
                    || localName.equals("video")) {
                in_leaf = true;
            } else if (localName.equals(USER)) {
                this.in_user = true;
                currentUser = new User();
                //TODO apply a local ID here?
            } else if (localName.equals(PRIMARY_LOCATION)) {
                this.in_location = true;
            }
		}
		
		depth++;
	}
	
	
    public void endCountryElement(String localName) throws SAXException {
        if (currentCountry == null) {
            syntaxError = true;
            return;
        }
        if (localName.equals("id")) {
            currentCountry.setId(buffer);
        } else if (localName.equals("name")) {
            currentCountry.setName(buffer);
        } else if (localName.equals("iso_code")) {
            currentCountry.setIsoCode(buffer);
        } else if (localName.equals("continent")) {
            currentCountry.setContinent(buffer);
        } else if (localName.equals("country")) {
            this.in_country = false;
            dba.saveCountry(currentCountry);
            countryCount++;
            currentCountry = null;
        }
    }

	
    public void endLocElement(String localName) throws SAXException {
        if (currentLoc == null) {
            syntaxError = true;
            return;
        }
        if (localName.equals("id")) {
            //currentLoc.setId(buffer); //not independent table on this end
        } else if (localName.equals("latitude")) {
            currentLoc.setLatitude(buffer);
        } else if (localName.equals("longitude")) {
            currentLoc.setLongitude(buffer);
        } else if (localName.equals("state")) {
            currentLoc.setState(buffer);
        } else if (localName.equals("city")) {
            currentLoc.setCity(buffer);
        } else if (localName.equals(PRIMARY_LOCATION)) {
            this.in_location = false;
        }
    }

    public void endUserElement(String localName) throws SAXException {
        if (currentUser == null) {
            syntaxError = true;
            return;
        }
        if (localName.equals("id")) {
            currentUser.setId(buffer);
        } else if (localName.equals("first_name")) {
            currentUser.setFirstname(buffer);
        } else if (localName.equals("last_name")) {
            currentUser.setLastname(buffer);
        } else if (localName.equals(USER)) {
            this.in_user = false;
            dba.saveCountry(currentCountry);
            userCount++;
            currentUser = null;
        }
    }

    
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		depth--;

        if (in_country) endCountryElement(localName); else
            
        if (in_location) endLocElement(localName); else
                
        if (in_user) endUserElement(localName); else
            
		if (localName.equals(LIST_ITEM)) { //we are done with entire update
            this.in_update = false;
            if (currentUpd != null && currentUpd.getId() != null) {
                updateCount++;
                if (insert) {
                    dba.saveUpdate(currentUpd, false); //preserve name of any cached image
                    currentUpd = null;
                }
            }
        } else if (localName.equals("id")) {
			currentUpd.setId(buffer);
		} else if (localName.equals("title")) {
			currentUpd.setTitle(buffer);
		} else if (localName.equals("text")) {
			currentUpd.setText(buffer);
		} else if (localName.equals("time")) {
			try {
				currentUpd.setDate(df1.parse(buffer));
			} catch (ParseException e1) {
				syntaxError = true;
			}
		} else if (localName.equals("project")) {
			currentUpd.setProjectId(buffer);
		} else if (localName.equals("uuid")) {
			currentUpd.setUuid(buffer);
		} else if (localName.equals("photo")) {
			currentUpd.setThumbnailUrl(buffer);
        } else if (localName.equals("photo_credit")) {
            currentUpd.setPhotoCredit(buffer);
        } else if (localName.equals("photo_caption")) {
            currentUpd.setPhotoCaption(buffer);
        } else if (localName.equals("video")) {
            currentUpd.setVideoUrl(buffer);
            currentUpd.setCity(buffer);
        }
        in_leaf = false;
	}
	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	// May be called multiple times for pieces of the same tag contents!
	@Override
    public void characters(char ch[], int start, int length) {
		if (this.in_leaf) { //remember content
			buffer += new String(ch, start, length);
		}
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
