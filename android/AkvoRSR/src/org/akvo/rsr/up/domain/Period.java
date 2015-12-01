/*
 *  Copyright (C) 2015 Stichting Akvo (Akvo Foundation)
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
	private String id;
	private String indicatorId;
	private String title;
	private Date periodStart;
	private Date periodEnd;
	private String actualValue;
	private String actualComment;
	private String targetValue;
	private String targetComment;
   
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIndicatorId() {
		return indicatorId;
	}

	public void setIndicatorId(String id) {
		this.indicatorId = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getActualValue() {
		return actualValue;
	}

	public void setActualValue(String actualValue) {
		this.actualValue = actualValue;
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
		return periodStart;
	}

	public void setPeriodStart(Date d) {
		this.periodStart= d;
	}

	public Date getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(Date d) {
		this.periodEnd= d;
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
