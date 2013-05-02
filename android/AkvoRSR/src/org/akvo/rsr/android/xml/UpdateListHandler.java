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
<object>
<update_method>W</update_method>
<photo_credit/>
<photo_caption/>
<title>Video screening improves farms productivity</title>
<photo>http://test.akvo.org/rsr/media/db/project/363/update/2505/ProjectUpdate_2505_photo_2013-02-04_10.56.30.JPG</photo>
<absolute_url>/rsr/project/363/update/2505/</absolute_url>
<project>/api/v1/project/363/</project>
<video_caption/>
<photo_location>E</photo_location>
<video_credit/>
<video/>
<user>/api/v1/user/460/</user>
<time>2013-02-04T10:54:12</time>
<time_last_updated>2013-02-04T10:56:30</time_last_updated>
<text>After training on audio Visual content development(supported by IICD) ADS-Nyanza is currently using the videos to train farmers on how they can improve their farm productivity.

http://www.iicd.org/articles/video-screenings-are-starting-point-for-better-crops-in-kenya</text>
<id type="integer">2505</id>
<resource_uri>/api/v1/project_update/2505/</resource_uri>
</object>

 */



public class UpdateListHandler extends DefaultHandler {


	// ===========================================================
	// Fields
	// ===========================================================
	
	private boolean in_update = false;
	private boolean in_id = false;
	private boolean in_title = false;
	private boolean in_project_id = false;
	private boolean in_photo = false;
	private boolean in_text = false;

	private Update currentUpd;
	private int updateCount;
	private boolean syntaxError = false;
	
	//where to store results
	private RsrDbAdapter dba;
	
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

	public int getCount() {
		return updateCount;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		dba.open();
		updateCount = 0;
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
		} else if (localName.equals("text")) {
			this.in_text = true;
		} else if (localName.equals("project")) {
			this.in_project_id = true;
		} else if (localName.equals("object")) {
			this.in_update = true;
			currentUpd = new Update();
		} else if (localName.equals("photo")) {
			this.in_photo = true;
		}
	}
	
	/** Gets called on closing tags like: 
	 * </tag> */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("id")) {
			this.in_id = false;
		} else if (localName.equals("title")) {
				this.in_title = false;
		} else if (localName.equals("text")) {
			this.in_text = false;
		} else if (localName.equals("project")) {
			this.in_project_id = false;
		} else if (localName.equals("object")) {
			this.in_update = false;
			if (currentUpd != null) {
				dba.saveUpdate(currentUpd);
				updateCount++;
				currentUpd = null;
			} else syntaxError=true;
		} else if (localName.equals("photo")) {
			this.in_photo = false;
		}
	}
	
	/** Gets called on the following structure: 
	 * <tag>characters</tag> */
	@Override
    public void characters(char ch[], int start, int length) {
		if (currentUpd != null) {
			if(this.in_id) {
				currentUpd.setId(new String(ch, start, length));
			} else if(this.in_title) {
				currentUpd.setTitle(new String(ch, start, length));
			} else if(this.in_project_id) { // <project>/api/v1/project/574/</project>
				String s = new String(ch, start, length);
				if (s.endsWith("/")) {
					int i = s.lastIndexOf('/',s.length()-2);
					if (i>=0) {
						s = s.substring(i+1, s.length()-1);
						currentUpd.setProjectId(s);
					} else syntaxError = true;
				} else syntaxError = true;
			} else if(this.in_photo) {
				currentUpd.setThumbnailUrl(new String(ch, start, length));
			} else if(this.in_text) {
				currentUpd.setText(new String(ch, start, length));
			}
		} else
			syntaxError = true; //set error flag
    }

}
