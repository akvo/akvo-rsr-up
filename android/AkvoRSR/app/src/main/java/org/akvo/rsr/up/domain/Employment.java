/*
 *  Copyright (C) 2016,2020 Stichting Akvo (Akvo Foundation)
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
 * Holds information about one employment "connection"
 */
public class Employment {
    private String mId;
    private String mUserId;
    private String mOrganisationId;
    private String mCountryId;
    private String mGroupId;
    private String mGroupName;
    private boolean mApproved;
    private String mJobTtitle;

    public String toString() {
        return "User " + mUserId + " employed by org " + mOrganisationId + " as " + mJobTtitle + (mApproved?"": " -- (pending)");
    }
    
    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String id) {
        mUserId = id;
    }

    public String getOrganisationId() {
        return mOrganisationId;
    }

    public void setOrganisationId(String id) {
        mOrganisationId = id;
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String id) {
        mGroupId = id;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String id) {
        mGroupName = id;
    }

    public boolean getApproved() {
        return mApproved;
    }

    public void setApproved(boolean approved) {
        mApproved = approved;
    }

    public String getCountryId() {
        return mCountryId;
    }

    public void setCountryId(String id) {
        mCountryId = id;
    }

    public String getJobTitle() {
        return mJobTtitle;
    }

    public void setJobTitle(String title) {
        mJobTtitle = title;
    }


}
