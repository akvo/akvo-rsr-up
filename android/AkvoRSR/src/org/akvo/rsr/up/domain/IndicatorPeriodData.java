/*
 *  Copyright (C) 2016 Stichting Akvo (Akvo Foundation)
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

public class IndicatorPeriodData {
    private String mId;
    private String mPeriodId;
    private String mUserId;
    private String mData;
    private boolean mRelativeData;
    private String mDescription;
    private String mStatus;
    private String mPhotoUrl;
    private String mFileUrl;
    private String mPhotoFn;
    private String mFileFn;
    private Date mCreated;
    private Date mModified;
   
	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public String getPeriodId() {
		return mPeriodId;
	}

	public void setPeriodId(String id) {
		this.mPeriodId = id;
	}

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        this.mData = data;
    }

    public boolean getRelativeData() {
        return mRelativeData;
    }

    public void setRelativeData(boolean relativeData) {
        this.mRelativeData = relativeData;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String d) {
        this.mDescription = d;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String s) {
        this.mStatus = s;
    }

	public String getUserId() {
		return mUserId;
	}

	public void setUserId(String id) {
		this.mUserId = id;
	}

	public Date getCreated() {
		return mCreated;
	}

	public void setCreated(Date d) {
		this.mCreated = d;
	}

	public Date getModified() {
		return mModified;
	}

	public void setModified(Date d) {
		this.mModified = d;
	}

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String u) {
        this.mPhotoUrl = u;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public void setFileUrl(String u) {
        this.mFileUrl = u;
    }

    public String getPhotoFn() {
        return mPhotoFn;
    }

    public void setPhotoFn(String u) {
        this.mPhotoFn = u;
    }

    public String getFileFn() {
        return mFileFn;
    }

    public void setFileFn(String u) {
        this.mFileFn = u;
    }

	

}
