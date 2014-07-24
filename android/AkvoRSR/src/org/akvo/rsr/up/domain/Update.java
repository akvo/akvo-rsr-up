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

package org.akvo.rsr.up.domain;

import java.util.Date;

/**
 * Holds information about one project update
 */
public class Update {
	private String id;
	private String projectId;
	private String userId;
	private String title;
	private boolean draft;
	private boolean unsent;
	private String text;
	private String location; //unused
	private String thumbnailUrl;
	private String thumbnailFilename;	
	private String uuid;	
	private Date date;
    private String photoCredit;
    private String photoCaption;
    private String videoUrl;
    private String videoFilename;
    private String longitude; //fractional degrees with decimal point
    private String latitude;  //ditto
    private String elevation;  //m
    private String country;
    private String state;
    private String city;

    private boolean isEmpty(String s) {
        if ((s != null) && (s.length() > 0))
            return false;
        else
            return true;
    }
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String id) {
		this.projectId = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String id) {
		this.userId = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text= text;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getThumbnailFilename() {
		return thumbnailFilename;
	}

	public void setThumbnailFilename(String thumbnailFilename) {
		this.thumbnailFilename = thumbnailFilename;
	}

	public boolean getDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean getUnsent() {
		return unsent;
	}

	public void setUnsent(boolean unsent) {
		this.unsent = unsent;
	}


	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    public String getPhotoCredit() {
        return photoCredit;
    }

    public void setPhotoCredit(String credit) {
        photoCredit = credit;
    }

    public String getPhotoCaption() {
        return photoCaption;
    }

    public void setPhotoCaption(String caption) {
        photoCaption = caption;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String url) {
        videoUrl = url;
    }

    public String getVideoFilename() {
        return videoFilename;
    }

    public void setVideoFilename(String fn) {
        videoFilename = fn;
    }
    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String lon) {
        this.longitude = lon;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String lat) {
        this.latitude = lat;
    }

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String ele) {
        this.elevation = ele;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean validLatLon() {
        return (!isEmpty(latitude)) && (!isEmpty(longitude));
    }
    
    public boolean validLatLonEle() {
        return (!isEmpty(latitude)) && (!isEmpty(longitude)) && (!isEmpty(elevation));
    }

}
