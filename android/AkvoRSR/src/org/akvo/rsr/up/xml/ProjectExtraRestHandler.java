/*
 *  Copyright (C) 2015 Stichting Akvo (Akvo Foundation)
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

import org.akvo.rsr.up.domain.Project;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Parses output from the app-targeted REST API; One project with extra nested resource data;
 * Example input:
 * 
<root>
    <id>2339</id>
    <created_at>2014-10-03T11:23:16</created_at><last_modified_at>2014-10-08T21:17:15</last_modified_at>
    <title>WakaWaka</title>
    <subtitle>Testing a pay-as-you-go energy service in Rwanda using Akvo FLOW and RSR </subtitle>
    <primary_location>
        <id>636</id>
        <latitude>-25.18465</latitude><longitude>46.085121</longitude>
        <city>Ambovombe </city>
        <state>Androy</state>
        <address_1>Ambovombe </address_1><address_2/><postcode/>
        <location_target>16</location_target>
        <country>6</country>
        <reference/><location_code/><vocabulary/><name/>
        <description/>
        <activity_description/><administrative_code/><administrative_vocabulary/><administrative_level/><exactness/><location_reach/><location_class/><feature_designation/>
    </primary_location>
    <status>A</status>
    <project_plan_summary>WakaWaka will use Akvo FLOW and RSR in Rwanda to monitor the use of WakaWaka solar lights in a new pay-as-you-go energy service.</project_plan_summary>
    <current_image_caption>WakaWaka logo</current_image_caption><current_image_credit></current_image_credit>
    <goals_overview>The aim of this project is to support and train WakaWaka to use Akvo FLOW and RSR to monitor the WakaWaka Virtual Grid programme in Rwanda. </goals_overview>
    <current_status></current_status>
    <project_plan>September 2014 - Set up Akvo FLOW instance: An Akvo FLOW dashboard will be set up for WakaWaka.
    Ongoing - Strategic partnership building: WakaWaka and Akvo would like to enrich their partnership on strategic level.
    </project_plan>
    <sustainability>Akvo is a self-sustaining organisation</sustainability>
    <background>WakaWaka is starting a new project called Virtual Grid. Virtual Grid is a pilot to test a pay-as-you-go energy service. The pilot will involve 9,000 households in Rwanda. In order to do an impact evaluation of this pilot, baseline and follow-up studies will be conducted. Akvo FLOW will be used to conduct the baseline study. Akvo RSR will be used to provide updates from the field.</background>
    <target_group></target_group>
    <language>en</language>
    <project_rating>0</project_rating>
    <notes></notes>
    <currency>EUR</currency>
    <date_start_planned>2014-09-22</date_start_planned>
    <date_start_actual></date_start_actual>
    <date_end_planned>2015-09-22</date_end_planned>
    <date_end_actual></date_end_actual>
    <donate_button>False</donate_button>
    <sync_owner></sync_owner>
    <hierarchy></hierarchy>
    <project_scope></project_scope>
    <capital_spend_percentage></capital_spend_percentage>
    <collaboration_type></collaboration_type>
    <default_aid_type></default_aid_type>
    <default_finance_type></default_finance_type>
    <default_flow_type></default_flow_type>
    <default_tied_status></default_tied_status>
    <budget>15012.00</budget><funds>15012.00</funds><funds_needed>0.00</funds_needed>
    <categories>
        <list-item>10</list-item>
    </categories>
    <current_image type="hash">
        <original>/media/db/project/2856/Project_2856_current_image_2015-03-27_14.21.04.jpg</original>
        <up>/media/cache/53/95/53954c937cc643bffeca011dfbef9ae4.jpg</up>
    </current_image>
</root> 

 
 */



public class ProjectExtraRestHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
    private boolean in_project = false;
	private boolean in_proj_id = false;
	private boolean in_title = false;
	private boolean in_subtitle = false;
	private boolean in_summary = false;
	private boolean in_funds = false;
    private boolean in_primloc = false;
	
	private boolean in_current_image = false;
	private boolean in_original_url = false;
	private boolean in_thumbnail_url = false;

    private boolean in_loc_id = false;
    private boolean in_country_id = false;
	private boolean in_state = false;
	private boolean in_city = false;
	private boolean in_long = false;
	private boolean in_lat = false;

    private Project mCurrentProj;

	private int depth = 0;
	private boolean syntaxError = false;
	private String buffer; //used to accumulate a tag content

	
	/*
	 * constructor
	 */
	public ProjectExtraRestHandler(String serverVersion) {
		super();
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean getError() {
		return syntaxError;
	}

    public Project getProject() {
        return mCurrentProj;
    }

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		depth = 0;
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
		if (localName.equals("root") && depth == 0) {
                this.in_project = true;
                mCurrentProj = new Project();
		} else if (in_project)
		    if (localName.equals("id") && depth == 1) {
                this.in_proj_id = true;
			} else if (localName.equals("title") && depth == 1) {
				this.in_title = true;
			} else if (localName.equals("subtitle") && depth == 1) {
				this.in_subtitle = true;
			} else if (localName.equals("funds")) {
				this.in_funds = true;
            } else if (localName.equals("project_plan_summary") && depth==1) {
                this.in_summary = true;
			} else if (localName.equals("primary_location")) {
				this.in_primloc = true;
            } else if (localName.equals("id") && in_primloc) {
                this.in_loc_id = true;
			} else if (localName.equals("country") && in_primloc) {
				this.in_country_id = true;
			} else if (localName.equals("state") && in_primloc) {
				this.in_state = true;
			} else if (localName.equals("city") && in_primloc) {
				this.in_city = true;
			} else if (localName.equals("latitude") && in_primloc) {
				this.in_lat = true;
			} else if (localName.equals("longitude") && in_primloc) {
				this.in_long = true;
            } else if (localName.equals("current_image") && depth==1) {
                this.in_current_image = true;
            } else if (localName.equals("up") && in_current_image) {
                this.in_thumbnail_url = true;
            } else if (localName.equals("original") && in_current_image) {
                this.in_original_url = true;
			}
		depth++;
	}
		
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		depth--;
        if (localName.equals("id") && depth==1) {
            this.in_proj_id= false;
            mCurrentProj.setId(buffer);
        } else if (localName.equals("title") && depth==1) {
			this.in_title = false;
			mCurrentProj.setTitle(buffer);
		} else if (localName.equals("subtitle") && depth==1) {
			this.in_subtitle = false;
			mCurrentProj.setSubtitle(buffer);
		} else if (localName.equals("funds")) {
			this.in_funds = false;
			try {
				mCurrentProj.setFunds(Double.parseDouble(buffer));
			} catch (NumberFormatException e) {
				syntaxError = true;
			}
		} else if (localName.equals("primary_location")) {
			this.in_primloc = false;
		} else if (localName.equals("root") && depth==0) {
			this.in_project = false;
		} else if (localName.equals("project_plan_summary") && depth==1) {
			this.in_summary = false;
			mCurrentProj.setSummary(buffer);
        } else if (localName.equals("id") && in_primloc) {
            this.in_loc_id= false;
		} else if (localName.equals("country") && in_primloc) {
			this.in_country_id = false;
			mCurrentProj.setCountry(buffer);
		} else if (localName.equals("state") && in_primloc) {
			this.in_state = false;
			mCurrentProj.setState(buffer);
		} else if (localName.equals("city") && in_primloc) {
			this.in_city = false;
			mCurrentProj.setCity(buffer);
		} else if (localName.equals("latitude") && in_primloc) {
			this.in_lat = false;
			mCurrentProj.setLatitude(buffer);
		} else if (localName.equals("longitude") && in_primloc) {
			this.in_long = false;
			mCurrentProj.setLongitude(buffer);
        } else if (localName.equals("current_image") && depth==1) {
            this.in_current_image = false;
        } else if (localName.equals("original") && in_current_image) {
            this.in_original_url = false;
//TODO            mCurrentProj.setOriginalUrl(buffer);
        } else if (localName.equals("up") && in_current_image) {
            this.in_thumbnail_url = false;
            mCurrentProj.setThumbnailUrl(buffer);
		}
	}

	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (mCurrentProj != null) {
			if (this.in_proj_id ||
				this.in_summary ||
                this.in_thumbnail_url ||
                this.in_original_url ||
				this.in_title ||
				this.in_subtitle ||
				this.in_country_id ||
				this.in_state ||
				this.in_city ||
                this.in_funds ||
                this.in_loc_id ||
				this.in_long ||
				this.in_lat
				) {
				buffer += new String(ch, start, length);
	    	}
		} else
			syntaxError = true; //set error flag
    }

}
