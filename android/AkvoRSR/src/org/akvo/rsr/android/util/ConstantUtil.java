/*
 *  Copyright (C) 2012-2013 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo RSR.
 *
 *  Akvo RSR is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */


package org.akvo.rsr.android.util;

import java.util.HashMap;

/**
 * Class to hold all public constants used in the application
 * 
 * @author Stellan Lagerstroem
 * 
 */
public class ConstantUtil {

	/**
	 * server constants
	 */
	public static final String HOST = "http://test.akvo.org";
	public static final String PWD_URL = "/rsr/accounts/password/reset/";
	public static final String AUTH_URL = "/rsr/auth/token/";
	public static final String API_KEY_PATTERN = "&api_key=%s&username=%s";
	public static final String POST_UPDATE_URL = "/api/v1/project_update/?format=xml";
	public static final String FETCH_PROJ_URL = "/api/v1/project/?format=xml&limit=0&partnerships__organisation=%s";
	public static final String FETCH_PROJ_COUNT_URL = "/api/v1/project/?format=xml&limit=0&partnerships__organisation=%s";//TODO: Hardcoded Akvo org

	/**
	 * file system constants
	 */
	public static final String XML_SUFFIX = ".xml";
	public static final String JPG_SUFFIX = ".jpg";
	public static final String TOP_DIR = "/akvorsr/";
	public static final String PHOTO_DIR = "/akvorsr/photos/";
	public static final String IMAGECACHE_DIR = "/akvorsr/imagecache/";
	
	
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
	 * deletion indicators
	 */
	public static final String IS_DELETED = "Y";
	public static final String NOT_DELETED = "N";


	/**
	 * notification types
	 */
	public static final String PROGRESS = "PROGRESS";
	public static final String FILE_COMPLETE = "FILE_COMPLETE";
	public static final String ERROR = "ERROR";

	/**
	 * keys for saved state and bundle extras
	 */
	public static final String PROJECT_ID_KEY = "org.akvo.rsr.android.PROJECT";
	public static final String UPDATE_ID_KEY = "org.akvo.rsr.android.UPDATE";

	/**
	 * settings keys
	 */
	public static final String SURVEY_LANG_SETTING_KEY = "survey.language";
	
	

	/**
	 * intents
	 */
    public static final String PROJECTS_FETCHED_ACTION = "org.akvo.rsr.android.PROJECTS_FETCHED";
    public static final String PROJECTS_PROGRESS_ACTION = "org.akvo.rsr.android.PROJECTS_PROGRESS";
    public static final String UPDATES_SENT_ACTION = "org.akvo.rsr.android.UPDATES_SENT";
    public static final String AUTHORIZATION_RESULT_ACTION = "org.akvo.rsr.android.AUTHORIZATION_RESULT";

	public static final String GPS_STATUS_INTENT = "com.eclipsim.gpsstatus.VIEW";
	public static final String BARCODE_SCAN_INTENT = "com.google.zxing.client.android.SCAN";

	/**
	 * intent extra keys
	 */
	public static final String PHASE_KEY = "PHASE_KEY";
	public static final String SOFAR_KEY = "SOFAR_KEY";
	public static final String TOTAL_KEY = "TOTAL_KEY";
	public static final String SERVICE_ERRMSG_KEY = "org.akvo.rsr.android.ERRMSG";
	public static final String USERNAME_KEY = "org.akvo.rsr.android.USERNAME";
	public static final String PASSWORD_KEY = "org.akvo.rsr.android.PASSWORD";


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
