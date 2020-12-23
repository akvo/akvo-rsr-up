/*
 *  Copyright (C) 2015-2016,2020 Stichting Akvo (Akvo Foundation)
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

public class Period {
	private String mId;
	private String mIndicatorId;
	private String mTitle;
	private Date mPeriodStart;
	private Date mPeriodEnd;
	private String mActualValue;
	private String actualComment;
	private String targetValue;
    private String targetComment;
    private boolean mLocked;
   
    public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public String getIndicatorId() {
		return mIndicatorId;
	}

	public void setIndicatorId(String id) {
		this.mIndicatorId = id;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getActualValue() {
		return mActualValue;
	}

	public void setActualValue(String actualValue) {
		this.mActualValue = actualValue;
	}

	public String getActualComment() {
		return actualComment;
	}

	public void setActualComment(String actualComment) {
		this.actualComment = actualComment;
	}

	public String getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	public String getTargetComment() {
		return targetComment;
	}

	public void setTargetComment(String targetComment) {
		this.targetComment = targetComment;
	}

	public Date getPeriodStart() {
		return mPeriodStart;
	}

	public void setPeriodStart(Date d) {
		this.mPeriodStart = d;
	}

	public Date getPeriodEnd() {
		return mPeriodEnd;
	}

	public void setPeriodEnd(Date d) {
		this.mPeriodEnd = d;
	}

	
    public boolean getLocked() {
        return mLocked;
    }

    public void setLocked(boolean locked) {
        this.mLocked = locked;
    }

/*
	public String get() {
		return ;
	}

	public void set(String ) {
		this. = ;
	}
*/

}
