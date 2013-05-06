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
	public static final String HOST = "http://uat.akvo.org";
	public static final String PWD_URL = "/rsr/accounts/password/reset/";	
	public static final String TEST_API_KEY = "&api_key=9527895794156530589a28e5fa43e9cc5ecaca46&username=stellanl ";
	public static final String POST_UPDATE_URL = "/api/v1/project_update/&format=xml";
	/**
	 * file system constants
	 */
	public static final String XML_SUFFIX = ".xml";
	public static final String JPG_SUFFIX = ".jpg";
	public static final String TOP_DIR = "akvorsr/";
	public static final String PHOTO_DIR = "akvorsr/photos/";
	public static final String IMAGECACHE_DIR = "akvorsr/imagecache/";
	
	
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
//	public static final String USER_ID_KEY = "UID";
//	public static final String ID_KEY = "_id";

	/**
	 * settings keys
	 */
	public static final String SURVEY_LANG_SETTING_KEY = "survey.language";
	
	

	/**
	 * intents
	 */
	public static final String GPS_STATUS_INTENT = "com.eclipsim.gpsstatus.VIEW";
	public static final String BARCODE_SCAN_INTENT = "com.google.zxing.client.android.SCAN";

	/**
	 * zxing barcode extra keys
	 */
	public static final String BARCODE_CONTENT = "SCAN_RESULT";
	public static final String BARCODE_FORMAT = "SCAN_RESULT_FORMAT";


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
