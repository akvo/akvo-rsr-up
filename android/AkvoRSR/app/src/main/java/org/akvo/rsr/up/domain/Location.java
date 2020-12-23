/*
 *  Copyright (C) 2012-2015,2020 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.domain;

/**
 * Holds information about a location
 */
public class Location {
	private String mId;
    private String mLongitude; //fractional degrees with decimal point
    private String mLatitude;  //ditto
    private String mElevation;  //m
    private String mCountry;
    private String mCountryId;
    private String mState;
    private String mCity;
    private String mAddress1;
    private String mAddress2;
    private String mPostcode;

    private boolean isEmpty(String s) {
        if ((s != null) && (s.length() > 0))
            return false;
        else
            return true;
    }
    
	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String lon) {
        mLongitude = lon;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String lat) {
        mLatitude = lat;
    }

    public String getElevation() {
        return mElevation;
    }

    public void setElevation(String ele) {
        mElevation = ele;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public String getCountryId() {
        return mCountryId;
    }

    public void setCountryId(String countryId) {
        mCountryId = countryId;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        mState = state;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getAddress1() {
        return mAddress1;
    }

    public void setAddress1(String address) {
        mAddress1 = address;
    }

    public String getAddress2() {
        return mAddress2;
    }

    public void setAddress2(String address) {
        mAddress2 = address;
    }

    public String getPostcode() {
        return mPostcode;
    }

    public void setPostcode(String postcode) {
        mPostcode = postcode;
    }

    public void clear() {
        mId = null;
        mLatitude = null;
        mLongitude = null;
        mElevation = null;
        mCountry = null;
        mCountryId = null;
        mState = null;
        mAddress1 = null;
        mAddress2 = null;
        mPostcode = null;
    }

    public boolean validLatLon() {
        return (!isEmpty(mLatitude)) && (!isEmpty(mLongitude));
    }
    
    public boolean validLatLonEle() {
        return (!isEmpty(mLatitude)) && (!isEmpty(mLongitude)) && (!isEmpty(mElevation));
    }

}
