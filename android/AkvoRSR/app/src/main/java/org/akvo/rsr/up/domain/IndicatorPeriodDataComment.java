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

public class IndicatorPeriodDataComment {
    private String mId;
    private String mIpdcId;
    private String mUserId;
    private String mComment;
    private Date mCreated;
    private Date mModified;
   
	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public String getIpdcId() {
		return mIpdcId;
	}

	public void setIpdcId(String id) {
		this.mIpdcId = id;
	}

    public String getComment() {
        return mComment;
    }

    public void setComment(String c) {
        this.mComment = c;
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

	

}
