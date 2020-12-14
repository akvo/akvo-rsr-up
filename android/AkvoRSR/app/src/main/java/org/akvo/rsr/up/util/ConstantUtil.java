/*
 *  Copyright (C) 2012-2016C Stichting Akvo (Akvo Foundation)
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


package org.akvo.rsr.up.util;

/**
 * Class to hold all public constants used in the application
 * 
 * @author Stellan Lagerstroem
 * 
 */
public class ConstantUtil {

	/**
	 * server and API constants
	 */
	public static final String OLD_HOST = "http://rsr.akvo.org";
	public static final String LIVE_HOST = "https://rsr.akvo.org";
	public static final String PWD_URL = "/sign_in/";
    public static final String AUTH_URL = "/auth/token/?format=json";
    public static final String POST_UPDATE_URL = "/rest/v1/project_update/?format=xml";
    public static final String POST_RESULT_URL = "/rest/v1/indicator_period_data/?format=json";
    public static final String POST_EMPLOYMENT_PATTERN = "/rest/v1/user/%s/request_organisation/?format=json";
    public static final String IPD_ATTACHMENT_PATTERN = "/rest/v1/indicator_period_data/%d/upload_file/?format=json";
    public static final String FETCH_UPDATE_URL_PATTERN = "/rest/v1/project_update/?format=xml&project=%s&last_modified_at__gt=%s";//use default limit
    public static final String VERIFY_UPDATE_PATTERN = "/rest/v1/project_update/?format=xml&uuid=%s&limit=2";
    public static final String FETCH_PROJ_URL_PATTERN = "/rest/v1/project_up/%s/?format=xml&image_thumb_name=up&image_thumb_up_width=100"; //now asks for thumbnail size
    public static final String FETCH_COUNTRIES_URL = "/rest/v1/country/?format=json&limit=50"; //very small objects - get many at a time
    public static final String FETCH_USER_URL_PATTERN = "/rest/v1/user/%s/?format=json&depth=1";
    public static final String FETCH_ORGS_URL = "/rest/v1/organisation/?format=json&limit=10";//DEBUG
    public static final String FETCH_ORGS_TYPEAHEAD_URL = "/rest/v1/typeaheads/organisations?format=json";
    public static final String FETCH_ORG_URL_PATTERN = "/rest/v1/organisation/%s/?format=json";
    public static final String FETCH_RESULTS_URL_PATTERN = "/rest/v1/results_framework/?format=json&project=%s"; //includes results, indicators, periods and data
    public static final String FETCH_EMPLOYMENTS_URL_PATTERN = "/rest/v1/employment/?format=json&user=%s";
	public static final String SERVER_VERSION_HEADER = "X-RSR-Version";
    public static final String UPDATE_METHOD_MOBILE = "M";
    public static final String xmlContent = "application/xml";
    public static final String jsonContent = "application/json";

    /**
	 * file system constants
	 */
    public static final String LOG_FILE_NAME = "error_messages.txt";
	public static final String FILE_PROVIDER_AUTHORITY = "org.akvo.rsr.up.fileprovider";

	/**
	 * keys for saved state and bundle extras
	 */
	public static final String PROJECT_ID_KEY = "org.akvo.rsr.up.PROJECT";
    public static final String UPDATE_ID_KEY = "org.akvo.rsr.up.UPDATE";
    public static final String IMAGE_FILENAME_KEY = "org.akvo.rsr.up.IMAGE_FILENAME";

	/**
	 * settings keys
	 */
	public static final String HOST_SETTING_KEY = "data_host";
	public static final String SEND_IMG_SETTING_KEY = "setting_send_images";
	public static final String AUTH_USERNAME_KEY = "authorized_username";
	public static final String AUTH_APIKEY_KEY = "authorized_apikey";
	public static final String AUTH_USERID_KEY = "authorized_userid";
    public static final String AUTH_ORGID_KEY = "authorized_orgid";
    public static final String AUTH_PROJID_KEY = "authorized_projid";
    public static final String AUTH_EDIT_PROJID_KEY = "authorized_edit_projid";
    public static final String AUTH_APPVERSION_KEY = "authorized_app_version";
	public static final String LOCAL_ID_KEY	= "next_local_id";
	public static final String FETCH_TIME_KEY = "last_updates_fetch_time";

	/**
	 * intents
	 */
    public static final String UPDATES_VERIFIED_ACTION = "org.akvo.rsr.up.UPDATES_VERIFIED";
    public static final String RESULT_SENT_ACTION = "org.akvo.rsr.up.RESULT_SENT";

	/**
	 * intent extra keys
	 */
	public static final String PHASE_KEY = "PHASE_KEY";
	public static final String SOFAR_KEY = "SOFAR_KEY";
	public static final String TOTAL_KEY = "TOTAL_KEY";
	public static final String SERVICE_ERRMSG_KEY = "org.akvo.rsr.up.ERRMSG";
	public static final String SERVICE_UNRESOLVED_KEY = "org.akvo.rsr.up.UNRESOLVED";
	public static final String USERNAME_KEY = "org.akvo.rsr.up.USERNAME";
	public static final String PASSWORD_KEY = "org.akvo.rsr.up.PASSWORD";
    public static final String PERIOD_ID_KEY = "org.akvo.rsr.up.PERIOD_ID";
    public static final String DESCRIPTION_KEY = "org.akvo.rsr.up.DESCRIPTION";
    public static final String DATA_KEY = "org.akvo.rsr.up.IPD_DATA";
    public static final String CURRENT_ACTUAL_VALUE_KEY = "org.akvo.rsr.up.CURRENT_ACTUAL_VALUE";
    public static final String RELATIVE_DATA_KEY = "org.akvo.rsr.up.RELATIVE_IPD_DATA";
    public static final String PHOTO_FN_KEY = "org.akvo.rsr.up.PHOTO_FILENAME";
    public static final String FILE_FN_KEY = "org.akvo.rsr.up.FILE_FILENAME";
    public static final String ORG_ID_KEY = "org.akvo.rsr.up.ORGANISATION_ID";
    public static final String COUNTRY_ID_KEY = "org.akvo.rsr.up.COUNTRY_ID";
    public static final String JOB_TITLE_KEY = "org.akvo.rsr.up.JOB_TITLE";

	/**
	 * "code" to prevent unauthorized use of administrative settings/preferences
	 */
	public static final String ADMIN_AUTH_CODE = "12345";

    public static final int PHOTO_REQUEST = 777;

    /**
	 * prevent instantiation
	 */
	private ConstantUtil() {
	}

}
