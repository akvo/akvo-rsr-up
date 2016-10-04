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
	public static final String TEST_HOST = "http://rsr.uat.akvo.org";
	public static final String LIVE_HOST = "http://rsr.akvo.org";
	public static final String PWD_URL = "/sign_in/";
//    public static final String AUTH_URL = "/auth/token/";
    public static final String AUTH_URL = "/auth/token/?format=json";
    public static final String POST_UPDATE_URL = "/rest/v1/project_update/?format=xml";
    public static final String POST_RESULT_URL = "/rest/v1/indicator_period_data/?format=json";
    public static final String POST_EMPLOYMENT_PATTERN = "/rest/v1/user/%s/request_organisation/?format=json";
    public static final String IPD_ATTACHMENT_PATTERN = "/rest/v1/indicator_period_data/%d/upload_file/?format=json";
//    public static final String FETCH_UPDATE_URL_PATTERN = "/rest/v1/project_update/?format=xml&project=%s";//use default limit
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
    public static final String FETCH_PARTNERSHIPS_BY_ORG_URL_PATTERN = "/rest/v1/partnership_more_link/?format=json&organisation=%s";
    public static final String FETCH_PARTNERSHIPS_BY_PROJ_URL_PATTERN = "/rest/v1/partnership_more_link/?format=json&project=%s";
	public static final int    MAX_IMAGE_UPLOAD_SIZE = 2000000; //Nginx POST limit is 3MB, B64 encoding expands 33% and there may be long text 
	public static final String SERVER_VERSION_HEADER = "X-RSR-Version";
    public static final String UPDATE_METHOD_MOBILE = "M";
    public static final String xmlContent = "application/xml";
    public static final String jsonContent = "application/json";

    /**
	 * file system constants
	 */
	public static final String XML_SUFFIX = ".xml";
	public static final String JPG_SUFFIX = ".jpg";
//	public static final String TOP_DIR = "/akvorsr/";
	public static final String PHOTO_DIR = "/akvorsr/photos/";
    public static final String IMAGECACHE_DIR = "/akvorsr/imagecache/";
    public static final String LOG_FILE_NAME = "error_messages.txt";

	/**
	 * status related constants
	 */
	public static final String COMPLETE_STATUS = "Complete";
	public static final String SENT_STATUS = "Sent";
	public static final String RUNNING_STATUS = "Running";
	public static final String IN_PROGRESS_STATUS = "In Progress";
	public static final String QUEUED_STATUS = "Queued";
	public static final String FAILED_STATUS = "Failed";

	
	/**
	 * notification types
	 */
	public static final String PROGRESS = "PROGRESS";
	public static final String FILE_COMPLETE = "FILE_COMPLETE";
	public static final String ERROR = "ERROR";

	
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
	public static final String DEBUG_SETTING_KEY = "setting_debug";
	public static final String SEND_IMG_SETTING_KEY = "setting_send_images";
	public static final String DELAY_IMG_SETTING_KEY = "setting_delay_image_fetch";
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
    public static final String PROJECTS_FETCHED_ACTION = "org.akvo.rsr.up.PROJECTS_FETCHED";
    public static final String PROJECTS_PROGRESS_ACTION = "org.akvo.rsr.up.PROJECTS_PROGRESS";
    public static final String UPDATES_SENT_ACTION = "org.akvo.rsr.up.UPDATES_SENT";
    public static final String UPDATES_SENDPROGRESS_ACTION = "org.akvo.rsr.up.UPDATES_PROGRESS";
    public static final String UPDATES_VERIFIED_ACTION = "org.akvo.rsr.up.UPDATES_VERIFIED";
    public static final String AUTHORIZATION_RESULT_ACTION = "org.akvo.rsr.up.AUTHORIZATION_RESULT";
    public static final String RESULT_SENT_ACTION = "org.akvo.rsr.up.RESULT_SENT";
    public static final String EMPLOYMENT_SENT_ACTION = "org.akvo.rsr.up.EMPLOYMENT_SENT";
    public static final String ORGS_FETCHED_ACTION = "org.akvo.rsr.up.ORGS_FETCHED";
    public static final String ORGS_PROGRESS_ACTION = "org.akvo.rsr.up.ORGS_PROGRESS";

	public static final String GPS_STATUS_INTENT = "com.eclipsim.gpsstatus.VIEW";
	public static final String BARCODE_SCAN_INTENT = "com.google.zxing.client.android.SCAN";

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
    public static final String COMMENT_KEY = "org.akvo.rsr.up.COMMENT";
    public static final String DATA_KEY = "org.akvo.rsr.up.IPD_DATA";
    public static final String CURRENT_ACTUAL_VALUE_KEY = "org.akvo.rsr.up.CURRENT_ACTUAL_VALUE";
    public static final String RELATIVE_DATA_KEY = "org.akvo.rsr.up.RELATIVE_IPD_DATA";
    public static final String PHOTO_FN_KEY = "org.akvo.rsr.up.PHOTO_FILENAME";
    public static final String FILE_FN_KEY = "org.akvo.rsr.up.FILE_FILENAME";
    public static final String PERIOD_START_KEY = "org.akvo.rsr.up.INDICATOR_PERIOD_START";
    public static final String PERIOD_END_KEY = "org.akvo.rsr.up.INDICATOR_PERIOD_END";
    public static final String ORG_ID_KEY = "org.akvo.rsr.up.ORGANISATION_ID";
    public static final String COUNTRY_ID_KEY = "org.akvo.rsr.up.COUNTRY_ID";
    public static final String JOB_TITLE_KEY = "org.akvo.rsr.up.JOB_TITLE";

	/**
	 * posting outcomes
	 */
	public static final int POST_SUCCESS = 0;
	public static final int POST_FAILURE = 1;
	public static final int POST_UNKNOWN = 2;
	
	
	/**
	 * language codes
	 */
	public static final String ENGLISH_CODE = "en";


	/**
	 * "code" to prevent unauthorized use of administrative settings/preferences
	 */
	public static final String ADMIN_AUTH_CODE = "12345";

	/**
	 * prevent instantiation
	 */
	private ConstantUtil() {
	}

}
