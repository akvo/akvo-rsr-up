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

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Country;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * http://rsr.uat.akvo.org/api/v1/country/?format=xml&limit=0
 * Example input:
 * 
 <response>
 <objects type="list">
 <object><name>Afghanistan</name><continent_code>as</continent_code><id type="integer">84</id><iso_code>af</iso_code><continent>Asia</continent><resource_uri>/api/v1/country/84/</resource_uri></object>
 <object><name>Albania</name><continent_code>eu</continent_code><id type="integer">102</id><iso_code>al</iso_code><continent>Europe</continent><resource_uri>/api/v1/country/102/</resource_uri></object>
 ...

 */

public class CountryListHandler extends DefaultHandler {

    // ===========================================================
    // Fields
    // ===========================================================

    private boolean in_country = false;
    private boolean in_id = false;
    private boolean in_name = false;
    private boolean in_iso_code = false;
    private boolean in_continent = false;

    private Country currentCountry;
    private int countryCount;
    private boolean syntaxError = false;
    private int depth = 0;
    private String buffer;

    // where to store results
    private RsrDbAdapter dba;

    /*
     * constructor
     */
    public CountryListHandler(RsrDbAdapter aDba) {
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
        return countryCount;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    @Override
    public void startDocument() throws SAXException {
        dba.open();
        countryCount = 0;
        depth = 0;
    }

    @Override
    public void endDocument() throws SAXException {
        dba.close();
    }

    /**
     * Gets be called on opening tags like: <tag> Can provide attribute(s), when
     * xml was like: <tag attribute="attributeValue">
     */
    @Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        buffer = "";
        if (localName.equals("object")) {
            this.in_country = true;
            currentCountry = new Country();
        } else if (in_country)
            if (localName.equals("id")) {
                this.in_id = true;
            } else if (localName.equals("name")) {
                this.in_name = true;
            } else if (localName.equals("iso_code")) {
                this.in_iso_code = true;
            } else if (localName.equals("continent")) {
                this.in_continent = true;
            }
        depth++;
    }

    /**
     * Gets called on closing tags like: </tag>
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        depth--;

        if (localName.equals("id")) {
            this.in_id = false;
            currentCountry.setId(buffer);
        } else if (localName.equals("name")) {
            this.in_name = false;
            currentCountry.setName(buffer);
        } else if (localName.equals("iso_code")) {
            this.in_iso_code = false;
            currentCountry.setIsoCode(buffer);
        } else if (localName.equals("continent")) {
            this.in_continent = false;
            currentCountry.setContinent(buffer);
        } else if (localName.equals("object")) {
            this.in_country = false;
            if (currentCountry != null) {
                dba.saveCountry(currentCountry);
                countryCount++;
                currentCountry = null;
            } else {
                syntaxError = true;
            }
        }
    }

    /**
     * Gets called on the following structure: <tag>characters</tag>
     */
    @Override
    public void characters(char ch[], int start, int length) {
        if (currentCountry != null) {
            if (this.in_id ||
                    this.in_name ||
                    this.in_continent ||
                    this.in_iso_code) { // append content
                buffer += new String(ch, start, length);
            }
        } else {
            syntaxError = true; // set error flag
        }
    }

}
