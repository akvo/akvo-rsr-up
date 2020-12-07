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

public class Indicator {
	private String  id;
	private String  resultId;
	private String  title;
	private String  description;
	private Integer baselineYear;
	private String  baselineValue;
	private String  baselineComment;
	private String  measure;
   
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResultId() {
		return resultId;
	}

	public void setResultId(String id) {
		this.resultId = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getBaselineYear() {
		return baselineYear;
	}

	public void setBaselineYear(Integer baselineYear) {
		this.baselineYear = baselineYear;
	}

	public String getBaselineValue() {
		return baselineValue;
	}

	public void setBaselineValue(String baselineValue) {
		this.baselineValue = baselineValue;
	}

	public String getBaselineComment() {
		return baselineComment;
	}

	public void setBaselineComment(String baselineComment) {
		this.baselineComment = baselineComment;
	}

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
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
