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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Example input:
 * 
<response>
<objects type="list">
<object>
.........................................
</object>
</objects>
<meta type="hash">
<next>/api/v1/project/?offset=1&limit=1&partnerships__organisation=42&format=xml</next>
<total_count type="integer">2</total_count>
<previous type="null"/>
<limit type="integer">1</limit>
<offset type="integer">0</offset>
</meta>
</response>
 */



public class ProjectCountHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_meta = false;
	private boolean in_count = false;
	private int depth = 0;
	private int projectCount = 0;
	private boolean syntaxError = false;

	
//	private ParsedExampleDataSet myParsedExampleDataSet = new ParsedExampleDataSet();

	
	/*
	 * constructor
	 */
	ProjectCountHandler(RsrDbAdapter aDba){
		super();
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
		depth = 0;
		projectCount = 0;
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
		if (localName.equals("meta") && depth == 1) {
			this.in_meta = true;
		} else if (in_meta && localName.equals("total_count")) {
			this.in_count = true;
		}
		depth++;
	}
		
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)	throws SAXException {
		depth--;
		if (localName.equals("meta") && in_meta) {
			this.in_meta = false;
		} else if (in_count && localName.equals("total_count")) {
			this.in_count = false;
		}
	}
		
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
			if(this.in_count) {
				try {
					projectCount = Integer.parseInt(new String(ch, start, length));
				} catch (NumberFormatException e) {
					syntaxError = true;
				}
				
	    	}
    }

}
