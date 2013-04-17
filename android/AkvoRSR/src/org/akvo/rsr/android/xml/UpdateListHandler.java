package org.akvo.rsr.android.xml;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.Project;
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
 </budget_items><absolute_url>/rsr/project/1347/</absolute_url><notes/><project_plan/><funds_needed>7.48</funds_needed><budget>7000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14743/</value><value>/api/v1/benchmark/14744/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1347/</resource_uri><primary_location type="null"/><partnerships type="list"><value>/api/v1/partnership/4591/</value></partnerships></object><object><project_rating type="integer">0</project_rating><subtitle>This is a project created for donation tests</subtitle><links type="list"/><invoices type="list"><value>/api/v1/invoice/1607/</value></invoices><date_complete type="null"/><locations type="list"/><project_comments type="list"/><currency>EUR</currency><id type="integer">1346</id><title>DonationTestProject1364471063.03</title><project_updates type="list"/><categories type="list"><value>/api/v1/category/9/</value></categories><status>H</status><funds>6992.52</funds><project_plan_summary>This is a project created for donation tests</project_plan_summary><date_request_posted>2013-03-28</date_request_posted><sustainability>This is a project created for donation tests</sustainability><goals type="list"/><background/><current_image_caption/><current_image type="hash"><original type="null"/><thumbnails type="null"/></current_image><budget_items type="list"><value>/api/v1/budget_item/3479/</value></budget_items><absolute_url>/rsr/project/1346/</absolute_url><notes/><project_plan/><funds_needed>7.48</funds_needed><budget>7000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14741/</value><value>/api/v1/benchmark/14742/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1346/</resource_uri><primary_location type="null"/><partnerships type="list"><value>/api/v1/partnership/4590/</value></partnerships></object><object><project_rating type="integer">0</project_rating><subtitle>This is a project created for donation tests</subtitle><links type="list"/><invoices type="list"/><date_complete type="null"/><locations type="list"/><project_comments type="list"/><currency>EUR</currency><id type="integer">1345</id><title>DonationTestProject1364470674.77</title><project_updates type="list"/><categories type="list"><value>/api/v1/category/9/</value></categories><status>H</status><funds>0.00</funds><project_plan_summary>This is a project created for donation tests</project_plan_summary><date_request_posted>2013-03-28</date_request_posted><sustainability>This is a project created for donation tests</sustainability><goals type="list"/><background/><current_image_caption/><current_image type="hash"><original type="null"/><thumbnails type="null"/></current_image><budget_items type="list"><value>/api/v1/budget_item/3478/</value></budget_items><absolute_url>/rsr/project/1345/</absolute_url><notes/><project_plan/><funds_needed>1000000.00</funds_needed><budget>1000000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14739/</value><value>/api/v1/benchmark/14740/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1345/</resource_uri><primary_location type="null"/><partnerships type="list"><value>/api/v1/partnership/4589/</value></partnerships></object><object><project_rating type="integer">0</project_rating><subtitle>This is a project created for donation tests</subtitle><links type="list"/><invoices type="list"><value>/api/v1/invoice/1608/</value></invoices><date_complete type="null"/><locations type="list"/><project_comments type="list"/><currency>EUR</currency><id type="integer">1344</id><title>DonationTestProject1364470500.91</title><project_updates type="list"/><categories type="list"><value>/api/v1/category/9/</value></categories><status>H</status><funds>0.00</funds><project_plan_summary>This is a project created for donation tests</project_plan_summary><date_request_posted>2013-03-28</date_request_posted><sustainability>This is a project created for donation tests</sustainability><goals type="list"/><background/><current_image_caption/><current_image type="hash"><original type="null"/><thumbnails type="null"/></current_image><budget_items type="list"><value>/api/v1/budget_item/3477/</value></budget_items><absolute_url>/rsr/project/1344/</absolute_url><notes/><project_plan/><funds_needed>1000000.00</funds_needed><budget>1000000.00</budget><current_status/><benchmarks type="list"><value>/api/v1/benchmark/14737/</value><value>/api/v1/benchmark/14738/</value></benchmarks><goals_overview>This is a project created for donation tests</goals_overview><resource_uri>/api/v1/project/1344/</resource_uri><primary_location type="null"/><partnerships type="list"><value>/api/v1/partnership/4588/</value></partnerships></object>
 
 */



public class UpdateListHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_project = false;
	private boolean in_id = false;
	private boolean in_title = false;
	private boolean in_subtitle = false;
	private boolean in_funds = false;
	private boolean in_current_image = false;
	private boolean in_thumbnails = false;
	private boolean in_thumbnail_url = false;

	private Project currentProj;
	
	private boolean syntaxError = false;
	
	//where to store results
	private RsrDbAdapter dba;
	
//	private ParsedExampleDataSet myParsedExampleDataSet = new ParsedExampleDataSet();

	
	/*
	 * constructor
	 */
	UpdateListHandler(RsrDbAdapter aDba){
		super();
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
		if (localName.equals("id")) {
			this.in_id = true;
		} else if (localName.equals("title")) {
			this.in_title = true;
		} else if (localName.equals("subtitle")) {
			this.in_subtitle = true;
		} else if (localName.equals("funds")) {
			this.in_funds = true;
		} else if (localName.equals("object")) {
			this.in_project = true;
			currentProj = new Project();
/*
 		} else if (localName.equals("tagwithnumber")) {
			// Extract an Attribute
			String attrValue = atts.getValue("thenumber");
			int i = Integer.parseInt(attrValue);
			myParsedExampleDataSet.setExtractedInt(i);
			*/
		} else if (localName.equals("current_image")) {
			this.in_current_image = true;
		} else if (localName.equals("thumbnails") && in_current_image) {
			this.in_thumbnails = true;
		} else if (localName.equals("map_thumb") && in_thumbnails) {
			this.in_thumbnail_url = true;
		}
	}
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("id")) {
			this.in_id= false;
		} else if (localName.equals("title")) {
				this.in_title = false;
		} else if (localName.equals("subtitle")) {
			this.in_subtitle = false;
		} else if (localName.equals("funds")) {
			this.in_funds = false;
		} else if (localName.equals("object")) {
			this.in_project = false;
			if (currentProj != null) {
				dba.saveProject(currentProj);
				currentProj = null;
			}
		} else if (localName.equals("current_image")) {
			this.in_current_image = false;
		} else if (localName.equals("thumbnails") && in_current_image) {
			this.in_thumbnails = false;
		} else if (localName.equals("map_thumb") && in_thumbnails) {
			this.in_thumbnail_url = false;
		}
	}
	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (currentProj != null) {
			if(this.in_id) {
	    		currentProj.setId(new String(ch, start, length));
			} else if(this.in_title) {
		    		currentProj.setTitle(new String(ch, start, length));
			} else if(this.in_subtitle) {
				currentProj.setTitle(new String(ch, start, length));
			} else if(this.in_thumbnail_url) {
				currentProj.setThumbnailUrl(new String(ch, start, length));
			} else if(this.in_funds) {
				try {
					currentProj.setFunds(Double.parseDouble(new String(ch, start, length)));
				} catch (NumberFormatException e) {
					syntaxError = true;
				}
				
	    	}
		} else
			syntaxError = true; //set error flag
    }

}
