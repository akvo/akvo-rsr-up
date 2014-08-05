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

import org.akvo.rsr.up.domain.Location;
import org.akvo.rsr.up.domain.Update;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/*
 * Class to handle XML parsing for a project update.
 * Always requested as a list, where each object's tags of the XML will be encapsulated in <root><results><list-item>
 * Example start of list:
 * 
<root>
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
    <photo>db/project/2/update/5298/ProjectUpdate_5298_photo_2014-07-24_13.35.05.jpg</photo>
    <id>5298</id>
    <created_at>2014-07-24 13:35:03</created_at>
    <last_modified_at>2014-07-24 13:35:05</last_modified_at>
    <project>2</project>
    <user>1</user>
    <title>Electro-cute</title>
    <text>Moar warm!</text><language>en</language>
    <primary_location>2</primary_location>
    <photo_caption>Purrrr</photo_caption>
    <photo_credit/><video/>
    <video_caption/>
    <video_credit/>
    <update_method>M</update_method>
    <user_agent/>
    <uuid/>
    <notes/>
</root>

 */



public class UpdateRestHandler extends DefaultHandler {


    private static String LIST_ITEM = "list-item";
    
	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_update = false;
	private boolean in_id = false;
	private boolean in_title = false;
	private boolean in_project_id = false;
	private boolean in_user_id = false;
    private boolean in_photo = false;
    private boolean in_photo_credit = false;
    private boolean in_photo_caption = false;
    private boolean in_video = false;
	private boolean in_text = false;
	private boolean in_time = false;
	private boolean in_uuid = false;
    private boolean in_primary_location_ref = false;
    private boolean in_locations = false;

	private boolean in_location = false;
    private boolean in_location_id = false;
    private boolean in_country_id = false;
    private boolean in_state = false;
    private boolean in_city = false;
    private boolean in_postcode = false;
    private boolean in_address1 = false;
    private boolean in_address2 = false;
    private boolean in_long = false;
    private boolean in_lat = false;

    private Update currentUpd;
    private Location currentLoc; //just one for now
	private int updateCount;
	private boolean syntaxError = false;
    private boolean insert;
	private int depth = 0;
	private SimpleDateFormat df1;
    private String buffer;
    private String primary_location_ref;
	
	/*
	 * constructor
	 */
	public UpdateRestHandler() {
		super();
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
		updateCount = 0;
		depth = 0;
		syntaxError = false;
	}

	@Override
	public void endDocument() throws SAXException {
	}

	/** Gets be called on opening tags like: 
	 * <tag> 
	 * Can provide attribute(s), when xml was like:
	 * <tag attribute="attributeValue">*/
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		buffer = "";
		if (localName.equals("root")) {
            this.in_update = true;
            currentUpd = new Update();
            currentLoc = currentUpd.getLocation();
            currentUpd.setUuid("");//db cannot take null
        } else if (depth == 1 && in_update) {
			if (localName.equals("id")) {
				this.in_id = true;
			} else if (localName.equals("title")) {
				this.in_title = true;
			} else if (localName.equals("text")) {
				this.in_text = true;
			} else if (localName.equals("created_at")) {
				this.in_time = true;
			} else if (localName.equals("project")) {
				this.in_project_id = true;
			} else if (localName.equals("user")) {
				this.in_user_id = true;
			} else if (localName.equals("uuid")) {
				this.in_uuid = true;
            } else if (localName.equals("photo")) {
                this.in_photo = true;
            } else if (localName.equals("photo_credit")) {
                this.in_photo_credit = true;
            } else if (localName.equals("photo_caption")) {
                this.in_photo_caption = true;
            } else if (localName.equals("video")) {
                this.in_video = true;
            } else if (localName.equals("primary_location")) {
                this.in_primary_location_ref = true;
            } else if (localName.equals("locations")) {
                this.in_locations = true;
            }
        } else if (in_locations && localName.equals(LIST_ITEM)) {
            this.in_location = true;
            //forget any previous location info
            currentLoc.clear();
            currentLoc.setElevation(""); //Not received from server
        } else if (localName.equals("id") && in_location) {
            this.in_location_id = true;
        } else if (localName.equals("country") && in_location) {
            this.in_country_id = true;
        } else if (localName.equals("state") && in_location) {
            this.in_state = true;
        } else if (localName.equals("city") && in_location) {
            this.in_city = true;
        } else if (localName.equals("postcode") && in_location) {
            this.in_postcode = true;
        } else if (localName.equals("address1") && in_location) {
            this.in_address1 = true;
        } else if (localName.equals("address2") && in_location) {
            this.in_address2 = true;
        } else if (localName.equals("latitude") && in_location) {
            this.in_lat = true;
        } else if (localName.equals("longitude") && in_location) {
            this.in_long = true;           
        }
		depth++;
	}
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		depth--;

		if (localName.equals("root")) { //we are done with an update
                //TODO: verify that stored location is the primary one, otherwise raise an error
		    in_update = false;
		    if (currentUpd != null && currentUpd.getId() != null) {
		        updateCount++;
		    }
        } else if (localName.equals(LIST_ITEM) && in_location) { 
            in_location = false;
        } else if (localName.equals("id")) {
            if (in_location_id) {
                in_location_id = false;
                currentLoc.setId(buffer);
            } else {
                this.in_id = false;
                currentUpd.setId(buffer);
            }
		} else if (localName.equals("title")) {
			this.in_title = false;
			currentUpd.setTitle(buffer);
		} else if (localName.equals("text")) {
			this.in_text = false;
			currentUpd.setText(buffer);
		} else if (localName.equals("created_at")) {
			this.in_time = false;
			try {
				currentUpd.setDate(df1.parse(buffer));
			} catch (ParseException e1) {
				syntaxError = true;
			}
		} else if (localName.equals("project")) {
			this.in_project_id = false;
			currentUpd.setProjectId(buffer);
		} else if (localName.equals("user")) {
			this.in_user_id = false;
			currentUpd.setUserId(buffer);
		} else if (localName.equals("uuid")) {
			this.in_uuid = false;
			currentUpd.setUuid(buffer);
		} else if (localName.equals("photo")) {
			this.in_photo = false;
			currentUpd.setThumbnailUrl(buffer);
        } else if (localName.equals("photo_credit")) {
            this.in_photo_credit = false;
            currentUpd.setPhotoCredit(buffer);
        } else if (localName.equals("photo_caption")) {
            this.in_photo_caption = false;
            currentUpd.setPhotoCaption(buffer);
        } else if (localName.equals("video")) {
            this.in_video = false;
            currentUpd.setVideoUrl(buffer);
        } else if (localName.equals("primary_location")) {
            this.in_primary_location_ref = false;
            primary_location_ref = buffer;
        } else if (localName.equals("country") && in_location) {
            this.in_country_id = false;
            currentLoc.setCountryId(buffer);
        } else if (localName.equals("state") && in_location) {
            this.in_state = false;
            currentLoc.setState(buffer);
        } else if (localName.equals("city") && in_location) {
            this.in_city = false;
            currentLoc.setCity(buffer);
        } else if (localName.equals("postcode") && in_location) {
            this.in_postcode = false;
            currentLoc.setPostcode(buffer);
        } else if (localName.equals("address1") && in_location) {
            this.in_address1 = false;
            currentLoc.setAddress1(buffer);
        } else if (localName.equals("address2") && in_location) {
            this.in_address2 = false;
            currentLoc.setAddress2(buffer);
        } else if (localName.equals("latitude") && in_location) {
            this.in_lat = false;
            currentLoc.setLatitude(buffer);
        } else if (localName.equals("longitude") && in_location) {
            this.in_long = false;
            currentLoc.setLongitude(buffer);
        } else if (localName.equals("locations")) {
            this.in_locations = false;
        }
	}
	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	// May be called multiple times for pieces of the same tag contents!
	@Override
    public void characters(char ch[], int start, int length) {
			if (this.in_id
			 || this.in_title
			 || this.in_uuid
             || this.in_user_id
             || this.in_location_id
			 || this.in_project_id
             || this.in_photo
             || this.in_photo_credit
             || this.in_photo_caption
             || this.in_video
			 || this.in_text
			 || this.in_time
			 || this.in_country_id
			 || this.in_state
             || this.in_city
             || this.in_postcode
             || this.in_address1
             || this.in_address2
			 || this.in_long
			 || this.in_lat
			 ) { //remember content
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
