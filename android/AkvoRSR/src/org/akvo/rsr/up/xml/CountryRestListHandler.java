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
import org.akvo.rsr.up.domain.Country;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * http://rsr.akvo.org/rest/v1/country/?format=xml
 * Example input:
 * 
<?xml version="1.0" encoding="utf-8"?>
<root>
    <count>139</count>
    <next>http://rsr.akvo.org/rest/v1/country/?page=2&amp;format=xml</next>
    <previous>
    </previous>
    <results>
        <list-item>
            <id>84</id>
            <name>Afghanistan</name>
            <iso_code>af</iso_code>
            <continent>Asia</continent>
            <continent_code>as</continent_code>
        </list-item>
        <list-item>
            <id>102</id>
            <name>Albania</name>
            <iso_code>al</iso_code>
            <continent>Europe</continent>
            <continent_code>eu</continent_code>
        </list-item>
 ...

 */

public class CountryRestListHandler extends DefaultHandler {

    // ===========================================================
    // Fields
    // ===========================================================

    private boolean in_next = false;
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
    private String mNextUrl = "";

    // where to store results
    private RsrDbAdapter dba;

    /*
     * constructor
     */
    public CountryRestListHandler(RsrDbAdapter aDba, String serverVersion) {
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

    public String getNextUrl() {
        return mNextUrl;
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
        if (depth == 1 && localName.equals("next")) {
            this.in_next = true;
        } else if (depth == 1 && localName.equals("list-item")) {
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

        if (in_next && localName.equals("next")) {
            this.in_next = false;
            mNextUrl = buffer.trim();
        } else if (in_country && localName.equals("list-item")) {
            this.in_country = false;
            if (currentCountry != null) {
                dba.saveCountry(currentCountry);
                countryCount++;
                currentCountry = null;
            } else {
                syntaxError = true;
            }
        } else if (localName.equals("id")) {
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
        } 
    }

    /**
     * Gets called on the following structure: <tag>characters</tag>
     */
    @Override
    public void characters(char ch[], int start, int length) {
        if (this.in_id ||
            this.in_next ||
            this.in_name ||
            this.in_continent ||
            this.in_iso_code) { // append content
            buffer += new String(ch, start, length);
        }
    }

}
