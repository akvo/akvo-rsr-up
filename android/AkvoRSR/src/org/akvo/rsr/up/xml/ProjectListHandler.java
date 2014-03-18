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

package org.akvo.rsr.up.xml;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Example input:
 * 
 <object>
 <project_rating type="integer">0</project_rating>
 <subtitle>This is a project created for donation tests</subtitle>
 <links type="list"/>
 <invoices type="list">
 	<value>/api/v1/invoice/1609/</value>
 </invoices>
 <date_complete type="null"/>
 <locations type="list"/>
 <project_comments type="list"/>
 <currency>EUR</currency>
 <id type="integer">1347</id>
 <title>DonationTestProject1364643561.13</title>
 <project_updates type="list"/>
 <categories type="list">
 	<value>/api/v1/category/9/</value>
 </categories>
 <status>H</status>
 <funds>6992.52</funds>
 <project_plan_summary>This is a project created for donation tests</project_plan_summary>
 <date_request_posted>2013-03-30</date_request_posted>
 <sustainability>This is a project created for donation tests</sustainability>
 <goals type="list"/>
 <background/>
 <current_image_caption/>
 <current_image type="hash">
 	<original>http://test.akvo.org/rsr/media/db/project/141/Project_141_current_image_2011-04-06_10.40.13.jpg</original>
 	<thumbnails type="hash">
 		<map_thumb>http://test.akvo.org/rsr/media/db/project/141/Project_141_current_image_2011-04-06_10.40.13_jpg_160x120_autocrop_detail_q85.jpg</map_thumb>
 	</thumbnails>
 	..or..
 	<original type="null"/>
 	<thumbnails type="null"/>
 </current_image>
 <budget_items type="list">
 <value>/api/v1/budget_item/3480/</value>
 </budget_items><absolute_url>/rsr/project/1347/</absolute_url><notes/><project_plan/><funds_needed>7.48</funds_needed><budget>7000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14743/</value><value>/api/v1/benchmark/14744/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1347/</resource_uri>
 <primary_location type="null"/>
 <partnerships type="list"><value>/api/v1/partnership/4591/</value></partnerships></object><object><project_rating type="integer">0</project_rating><subtitle>This is a project created for donation tests</subtitle><links type="list"/><invoices type="list"><value>/api/v1/invoice/1607/</value></invoices><date_complete type="null"/><locations type="list"/><project_comments type="list"/><currency>EUR</currency><id type="integer">1346</id><title>DonationTestProject1364471063.03</title><project_updates type="list"/><categories type="list"><value>/api/v1/category/9/</value></categories><status>H</status><funds>6992.52</funds><project_plan_summary>This is a project created for donation tests</project_plan_summary><date_request_posted>2013-03-28</date_request_posted><sustainability>This is a project created for donation tests</sustainability><goals type="list"/><background/><current_image_caption/><current_image type="hash"><original type="null"/><thumbnails type="null"/></current_image><budget_items type="list"><value>/api/v1/budget_item/3479/</value></budget_items><absolute_url>/rsr/project/1346/</absolute_url><notes/><project_plan/><funds_needed>7.48</funds_needed><budget>7000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14741/</value><value>/api/v1/benchmark/14742/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1346/</resource_uri><primary_location type="null"/><partnerships type="list"><value>/api/v1/partnership/4590/</value></partnerships></object><object><project_rating type="integer">0</project_rating><subtitle>This is a project created for donation tests</subtitle><links type="list"/><invoices type="list"/><date_complete type="null"/><locations type="list"/><project_comments type="list"/><currency>EUR</currency><id type="integer">1345</id><title>DonationTestProject1364470674.77</title><project_updates type="list"/><categories type="list"><value>/api/v1/category/9/</value></categories><status>H</status><funds>0.00</funds><project_plan_summary>This is a project created for donation tests</project_plan_summary><date_request_posted>2013-03-28</date_request_posted><sustainability>This is a project created for donation tests</sustainability><goals type="list"/><background/><current_image_caption/><current_image type="hash"><original type="null"/><thumbnails type="null"/></current_image><budget_items type="list"><value>/api/v1/budget_item/3478/</value></budget_items><absolute_url>/rsr/project/1345/</absolute_url><notes/><project_plan/><funds_needed>1000000.00</funds_needed><budget>1000000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14739/</value><value>/api/v1/benchmark/14740/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1345/</resource_uri><primary_location type="null"/><partnerships type="list"><value>/api/v1/partnership/4589/</value></partnerships></object><object><project_rating type="integer">0</project_rating><subtitle>This is a project created for donation tests</subtitle><links type="list"/><invoices type="list"><value>/api/v1/invoice/1608/</value></invoices><date_complete type="null"/><locations type="list"/><project_comments type="list"/><currency>EUR</currency><id type="integer">1344</id><title>DonationTestProject1364470500.91</title><project_updates type="list"/><categories type="list"><value>/api/v1/category/9/</value></categories><status>H</status><funds>0.00</funds><project_plan_summary>This is a project created for donation tests</project_plan_summary><date_request_posted>2013-03-28</date_request_posted><sustainability>This is a project created for donation tests</sustainability><goals type="list"/><background/><current_image_caption/><current_image type="hash"><original type="null"/><thumbnails type="null"/></current_image><budget_items type="list"><value>/api/v1/budget_item/3477/</value></budget_items><absolute_url>/rsr/project/1344/</absolute_url><notes/><project_plan/><funds_needed>1000000.00</funds_needed><budget>1000000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14737/</value><value>/api/v1/benchmark/14738/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1344/</resource_uri><primary_location type="null"/><partnerships type="list"><value>/api/v1/partnership/4588/</value></partnerships></object>
 
 Example of location:
 
 <primary_location>
 <city>Nairobi</city>
 <country>/api/v1/country/27/</country>
 <primary type="boolean">True</primary>
 <longitude type="float">36.819185</longitude>
 <project>/api/v1/project/1350/</project>
 <state>PO Box 36655-0200</state>
 <address_1>Suite 4</address_1><address_2>25 Parklands Road, Westlands</address_2>
 <latitude type="float">-1.267924</latitude>
 <postcode/><id type="integer">2069</id><resource_uri>/api/v1/project_location/2069/</resource_uri>
 </primary_location>
 
 */



public class ProjectListHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_project = false;
	private boolean in_id = false;
	private boolean in_title = false;
	private boolean in_subtitle = false;
	private boolean in_summary = false;
	private boolean in_funds = false;
	
	private boolean in_current_image = false;
	private boolean in_thumbnails = false;
	private boolean in_thumbnail_url = false;

	private boolean in_location = false;
	private boolean in_country = false;
	private boolean in_state = false;
	private boolean in_city = false;
	private boolean in_long = false;
	private boolean in_lat = false;

	private Project currentProj;

	private int depth = 0;
	private int projectCount = 0;
	private boolean syntaxError = false;
	private String buffer; //used to accumulate a tag ocntent

	//where to store results
	private RsrDbAdapter dba;
	
//	private ParsedExampleDataSet myParsedExampleDataSet = new ParsedExampleDataSet();

	
	/*
	 * constructor
	 */
	public ProjectListHandler(RsrDbAdapter aDba){
		super();
		dba = aDba;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public boolean getError() {
		return syntaxError;
	}

	public int getCount() {
		return projectCount;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		dba.open();
		depth = 0;
		projectCount = 0;
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
		if (localName.equals("object") && depth == 2) { //ignore root tag completely
			this.in_project = true;
			currentProj = new Project();
		} else if (in_project)
			if (localName.equals("id") && depth == 3) {
				this.in_id = true;
			} else if (localName.equals("title") && depth == 3) {
				this.in_title = true;
			} else if (localName.equals("subtitle") && depth == 3) {
				this.in_subtitle = true;
			} else if (localName.equals("funds")) {
				this.in_funds = true;
			} else if (localName.equals("primary_location")) {
				this.in_location = true;
	/*
	 		} else if (localName.equals("tagwithnumber")) {
				// Extract an Attribute
				String attrValue = atts.getValue("thenumber");
				int i = Integer.parseInt(attrValue);
				myParsedExampleDataSet.setExtractedInt(i);
				*/
			} else if (localName.equals("project_plan_summary") && depth==3) {
				this.in_summary = true;
			} else if (localName.equals("current_image") && depth==3) {
				this.in_current_image = true;
			} else if (localName.equals("country") && in_location) {
				this.in_country = true;
			} else if (localName.equals("state") && in_location) {
				this.in_state = true;
			} else if (localName.equals("city") && in_location) {
				this.in_city = true;
			} else if (localName.equals("latitude") && in_location) {
				this.in_lat = true;
			} else if (localName.equals("longitude") && in_location) {
				this.in_long = true;
			} else if (localName.equals("thumbnails") && in_current_image) {
				this.in_thumbnails = true;
			} else if (localName.equals("map_thumb") && in_thumbnails) {
				this.in_thumbnail_url = true;
			}
		depth++;
	}
		
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		depth--;
		if (localName.equals("id") && depth==3) {
			this.in_id= false;
    		currentProj.setId(buffer);
		} else if (localName.equals("title") && depth==3) {
			this.in_title = false;
			currentProj.setTitle(buffer);
		} else if (localName.equals("subtitle") && depth==3) {
			this.in_subtitle = false;
			currentProj.setSubtitle(buffer);
		} else if (localName.equals("funds")) {
			this.in_funds = false;
			try {
				currentProj.setFunds(Double.parseDouble(buffer));
			} catch (NumberFormatException e) {
				syntaxError = true;
			}
		} else if (localName.equals("primary_location")) {
			this.in_location = false;
		} else if (localName.equals("object") && depth==2) {
			this.in_project = false;
			if (currentProj != null) {
				dba.saveProject(currentProj);
				currentProj = null;
				projectCount++;
			}
		} else if (localName.equals("project_plan_summary") && depth==3) {
			this.in_summary = false;
			currentProj.setSummary(buffer);
		} else if (localName.equals("current_image") && depth==3) {
			this.in_current_image = false;
		} else if (localName.equals("country") && in_location) {
			this.in_country = false;
			currentProj.setCountry(buffer);
		} else if (localName.equals("state") && in_location) {
			this.in_state = false;
			currentProj.setState(buffer);
		} else if (localName.equals("city") && in_location) {
			this.in_city = false;
			currentProj.setCity(buffer);
		} else if (localName.equals("latitude") && in_location) {
			this.in_lat = false;
			currentProj.setLatitude(buffer);
		} else if (localName.equals("longitude") && in_location) {
			this.in_long = false;
			currentProj.setLongitude(buffer);
		} else if (localName.equals("thumbnails") && in_current_image) {
			this.in_thumbnails = false;
		} else if (localName.equals("map_thumb") && in_thumbnails) {
			this.in_thumbnail_url = false;
			currentProj.setThumbnailUrl(buffer);
		}
	}

	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (currentProj != null) {
			if (this.in_id ||
				this.in_summary ||
				this.in_thumbnail_url ||
				this.in_title ||
				this.in_subtitle ||
				this.in_country ||
				this.in_state ||
				this.in_city ||
				this.in_funds ||
				this.in_long ||
				this.in_lat
				) {
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
