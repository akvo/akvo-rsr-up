/*
 *  Copyright (C) 2012-2015 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.akvo.rsr.up.domain.Country;
import org.akvo.rsr.up.domain.Indicator;
import org.akvo.rsr.up.domain.IndicatorPeriodData;
import org.akvo.rsr.up.domain.IndicatorPeriodDataComment;
import org.akvo.rsr.up.domain.Organisation;
import org.akvo.rsr.up.domain.Period;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.domain.Result;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database class for the RSR db. It can create/upgrade the database as well
 * as select/insert/update.
 * 
 * @author Stellan Lagerstroem
 * 
 */
public class RsrDbAdapter {

    private static final String TAG = "RsrDbAdapter";
    private static final boolean LOG = true;

    // Table names
    private static final String DATABASE_NAME = "rsrdata";
    private static final String PROJECT_TABLE = "project";
    private static final String UPDATE_TABLE  = "_update";
    private static final String COUNTRY_TABLE = "country";
    private static final String USER_TABLE    = "user";
    private static final String ORG_TABLE     = "_organisation";
    private static final String RESULT_TABLE  = "_result";
    private static final String INDICATOR_TABLE  = "_indicator";
    private static final String PERIOD_TABLE  = "_period";
    private static final String IPD_TABLE     = "_ipd";
    private static final String IPDC_TABLE     = "_ipdc";

    //Column names
    public static final String PK_ID_COL = "_id";
    public static final String TITLE_COL = "title";
	public static final String SUBTITLE_COL = "subtitle";
	public static final String SUMMARY_COL = "summary";
	public static final String FUNDS_COL = "funds";
    public static final String THUMBNAIL_URL_COL = "thumbnail_url";
    public static final String THUMBNAIL_FILENAME_COL = "thumbnail_fn";
    public static final String VIDEO_URL_COL = "video_url";
    public static final String VIDEO_FILENAME_COL = "video_fn";
	public static final String PROJECT_COL = "project";
    public static final String USER_COL = "userid";//legacy
    public static final String USER_ID_COL = "user_id";
	public static final String TEXT_COL = "_text";
	public static final String DRAFT_COL = "draft";
	public static final String UNSENT_COL = "unsent";
	public static final String HIDDEN_COL = "hidden";
	public static final String CREATED_COL = "creation_date";
    public static final String UUID_COL = "uuid";
    public static final String PHOTO_CREDIT_COL = "photo_credit";
    public static final String PHOTO_CAPTION_COL = "photo_caption";
	public static final String LAST_FETCH_COL = "last_fetch";

	public static final String LAT_COL = "latitude";
    public static final String LON_COL = "longitude";
    public static final String ELE_COL = "elevation";
	public static final String COUNTRY_COL = "country_id";
	public static final String STATE_COL = "state";
	public static final String CITY_COL = "city";

    public static final String NAME_COL = "name";
	public static final String CONTINENT_COL = "continent";
	public static final String ISO_CODE_COL = "iso_code";
	
	public static final String USERNAME_COL = "username";
	public static final String FIRST_NAME_COL = "first_name";
	public static final String LAST_NAME_COL = "last_name";
	public static final String EMAIL_COL = "email";
	public static final String ORGANISATION_COL = "organisation";

    public static final String LONG_NAME_COL = "long_name";
    public static final String URL_COL = "url";
    public static final String DESCRIPTION_COL = "description";
    public static final String NEW_TYPE_COL = "new_type";
    public static final String OLD_TYPE_COL = "old_type";
    public static final String PRIMARY_COUNTRY_ID_COL = "primary_country_id";
    public static final String MODIFIED_COL = "modified_date";
    public static final String LOGO_URL_COL = "logo_url";
    public static final String LOGO_FN_COL = "logo_fn";

    public static final String RESULT_COL = "result";
    public static final String RESULT_ID_COL = "result_id";
    public static final String INDICATOR_ID_COL = "indicator_id";
    public static final String PROJECT_ID_COL = "project_id";
    public static final String PERIOD_ID_COL = "period_id";
    public static final String IPD_ID_COL = "ipd_id";
	public static final String MEASURE_COL = "measure";
	public static final String BASELINE_YEAR_COL = "baseline_year";
	public static final String BASELINE_VALUE_COL = "baseline_value";
	public static final String BASELINE_COMMENT_COL = "baseline_comment"; 
	public static final String TYPE_COL = "type";
	public static final String PERIOD_START_COL = "period_start";
	public static final String PERIOD_END_COL = "period_end";
	public static final String ACTUAL_VALUE_COL = "actual_value";
	public static final String ACTUAL_COMMENT_COL = "actual_comment";
	public static final String TARGET_VALUE_COL = "target_value";
    public static final String TARGET_COMMENT_COL = "target_comment";
    public static final String LOCKED_COL = "locked";
    public static final String DATA_COL = "data";
    public static final String RELATIVE_DATA_COL = "relative_data";
    public static final String PHOTO_URL_COL = "photo_url";
    public static final String PHOTO_FN_COL = "photo_fn";
    public static final String FILE_URL_COL = "file_url";
    public static final String FILE_FN_COL = "file_fn";
    public static final String COMMENT_COL = "comment";
    public static final String STATUS_COL = "status";
	
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;
	
    private static final String PROJECT_COUNTRY_JOIN = "project LEFT OUTER JOIN country ON (project.country_id = country._id)";
    private static final String UPDATE_COUNTRY_JOIN  = "_update LEFT OUTER JOIN country ON (_update.country_id = country._id)";
    private static final String ORG_COUNTRY_JOIN  = "_organisation LEFT OUTER JOIN country ON (_organisation.primary_country_id = country._id)";

	/**
	 * Database creation sql statements
	 * TODO: make them all use nn_COL constants
	 */
	private static final String PROJECT_TABLE_CREATE =
			"create table project (_id integer primary key, "+
			"title text not null, subtitle text, summary text, funds real, "+
			"thumbnail_url text, thumbnail_fn text," +
			"longitude text, latitude text, elevation text," +
			"country_id integer, state text, city text, hidden integer," +
			LAST_FETCH_COL + " integer);";
	private static final String UPDATE_TABLE_CREATE =
			"create table _update (_id integer primary key, project integer not null, userid integer not null, "+
			"title text not null, _text text, location text, uuid text,"+
            "thumbnail_url text, thumbnail_fn text," +
            "video_url text, video_fn text," +
            "photo_caption text, photo_credit text," +
			"draft integer, unsent integer," +
			CREATED_COL + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
			"longitude text, latitude text, country_id integer, state text, city text, elevation text);";
	private static final String COUNTRY_TABLE_CREATE =
			"create table country (_id integer primary key, "+
			"name text not null, continent text, "+
			"iso_code text);";
    private static final String USER_TABLE_CREATE =
            "create table user (_id integer primary key, "+
            "username text, organisation integer, "+
            "first_name text, last_name text, email text);";
    private static final String ORG_TABLE_CREATE =
            "create table " + ORG_TABLE
                    + "("
                    + PK_ID_COL + " integer primary key,"
                    + "name text, long_name text, email text, url text,"
                    + DESCRIPTION_COL  + " string, "
                    + MODIFIED_COL  + " integer, "
                    + OLD_TYPE_COL  + " string, "
                    + NEW_TYPE_COL  + " string, "
                    + LOGO_URL_COL  + " string, "
                    + LOGO_FN_COL  + " string, "
                    + PRIMARY_COUNTRY_ID_COL  + " string "
                    + ")";
    
    private static final String RESULT_TABLE_CREATE =
            "create table " + RESULT_TABLE
                    + "("
                    + PK_ID_COL + " integer primary key,"
            		+ " project_id integer not null, title text, description text, type text)";
    private static final String INDICATOR_TABLE_CREATE =
            "create table " + INDICATOR_TABLE
                    + "("
                    + PK_ID_COL + " integer primary key,"
            		+ RESULT_ID_COL + " integer not null,"
            		+ TITLE_COL +" text,"
            		+ DESCRIPTION_COL + " text,"
            		+ "baseline_year integer, baseline_value text, baseline_comment text, "
            		+ MEASURE_COL + " text"
            		+ ")";
    private static final String PERIOD_TABLE_CREATE =
            "create table " + PERIOD_TABLE
                    + "("
                    + PK_ID_COL + " integer primary key,"
                    + INDICATOR_ID_COL + " integer not null,"
                    + TITLE_COL + " text,"
                    + LOCKED_COL + " boolean,"
                    + "actual_value text, actual_comment text, "
                    + "target_value text, target_comment text, "
                    + "period_start integer, period_end integer "
                    + ")";
    private static final String IPD_TABLE_CREATE =
            "create table " + IPD_TABLE
                    + "("
                    + PK_ID_COL + " integer primary key,"
                    + PERIOD_ID_COL + " integer not null, "
                    + DATA_COL + " text, "
                    + DESCRIPTION_COL + " text, "
                    + STATUS_COL + " text,"
                    + RELATIVE_DATA_COL + " boolean, "
                    + PHOTO_URL_COL + " text, "
                    + PHOTO_FN_COL + " text, "
                    + FILE_URL_COL + " text, "
                    + FILE_FN_COL +" text, "
                    + USER_ID_COL + " integer "
                    + ")";
    private static final String IPDC_TABLE_CREATE =
            "create table " + IPDC_TABLE
                    + "("
                    + PK_ID_COL + " integer primary key,"
                    + IPD_ID_COL + " integer not null,"
                    + COMMENT_COL + " text,"
                    + USER_ID_COL + " integer "
                    + ")";


//	private static final int DATABASE_VERSION = 5;
//	private static final int DATABASE_VERSION = 6; //added project columns:long, lat, country, state, city
//	private static final int DATABASE_VERSION = 7; //added project.hidden
//	private static final int DATABASE_VERSION = 8; //added country table
//	private static final int DATABASE_VERSION = 9; //added update.creation_date
//	private static final int DATABASE_VERSION = 10; //added update.user and user table
//	private static final int DATABASE_VERSION = 11; //user columns attribute change
//  private static final int DATABASE_VERSION = 12; //uuid for updates
//  private static final int DATABASE_VERSION = 13; //org table
//  private static final int DATABASE_VERSION = 14; //update now has photo metadata and video
//  private static final int DATABASE_VERSION = 15; //update now has location
//  private static final int DATABASE_VERSION = 16; //project gets a last_fetch datetime to optimize fetches
    private static final int DATABASE_VERSION = 17; //results framework (added result, indicator, period, ipd and ipdc tables). New Org columns.

	private final Context context;

	/**
	 * Helper class for creating the database tables and loading reference data
	 * 
	 * It is declared with package scope for VM optimizations
	 * 
	 * @author Stellan Lagerstroem
	 * 
	 */
	static class DatabaseHelper extends SQLiteOpenHelper {

		private static SQLiteDatabase database;
		@SuppressLint("UseValueOf")
		private static volatile Long LOCK_OBJ = new Long(1);
		private volatile static int instanceCount = 0;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(PROJECT_TABLE_CREATE);
			db.execSQL(UPDATE_TABLE_CREATE);
			db.execSQL(COUNTRY_TABLE_CREATE);
            db.execSQL(USER_TABLE_CREATE);
            db.execSQL(ORG_TABLE_CREATE);
            db.execSQL(RESULT_TABLE_CREATE);
            db.execSQL(INDICATOR_TABLE_CREATE);
            db.execSQL(PERIOD_TABLE_CREATE);
            db.execSQL(IPD_TABLE_CREATE);
            db.execSQL(IPDC_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);

			
            if (oldVersion < 12) { //prereleases only, start over fresh, consider everything to be a cache
                db.execSQL("DROP TABLE IF EXISTS " + PROJECT_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + UPDATE_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + COUNTRY_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + ORG_TABLE);
                onCreate(db);
            } else {
                if (oldVersion < 13) { //need an org table
                    db.execSQL(ORG_TABLE_CREATE);
                }           

                if (oldVersion < 14) { //more update columns
                    db.execSQL("alter table " + UPDATE_TABLE + " add column video_url text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column video_fn text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column photo_caption text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column photo_credit text");
                }
                if (oldVersion < 15) { //more update columns
                    db.execSQL("alter table " + UPDATE_TABLE + " add column longitude text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column latitude text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column elevation text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column state text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column city text");
                    db.execSQL("alter table " + UPDATE_TABLE + " add column country_id integer");
                }
                if (oldVersion < 16) { //remember last fetch of a project
                    db.execSQL("alter table " + PROJECT_TABLE + " add column " + LAST_FETCH_COL + " integer");
                }
                if (oldVersion < 17) { //need results tables and more org columns
                    db.execSQL(RESULT_TABLE_CREATE);
                    db.execSQL(INDICATOR_TABLE_CREATE);
                    db.execSQL(PERIOD_TABLE_CREATE);
                    db.execSQL(IPD_TABLE_CREATE);
                    db.execSQL(IPDC_TABLE_CREATE);
                    db.execSQL("alter table " + ORG_TABLE + " add column " + DESCRIPTION_COL + " string");
                    db.execSQL("alter table " + ORG_TABLE + " add column " + PRIMARY_COUNTRY_ID_COL + " string");
                    db.execSQL("alter table " + ORG_TABLE + " add column " + OLD_TYPE_COL + " string");
                    db.execSQL("alter table " + ORG_TABLE + " add column " + NEW_TYPE_COL + " string");
                    db.execSQL("alter table " + ORG_TABLE + " add column " + LOGO_URL_COL + " string");
                    db.execSQL("alter table " + ORG_TABLE + " add column " + LOGO_FN_COL  + " string");
                    db.execSQL("alter table " + ORG_TABLE + " add column " + MODIFIED_COL + " integer");
                }           
            }
		}

		
		@Override
		public SQLiteDatabase getWritableDatabase() {
			synchronized (LOCK_OBJ) {

				if (database == null || !database.isOpen()) {
					database = super.getWritableDatabase();
					instanceCount = 0;
				}
				instanceCount++;
				return database;
			}
		}

		
		@Override
		public void close() {
			synchronized (LOCK_OBJ) {
				instanceCount--;
				if (instanceCount <= 0) {
					// close the database held by the helper (if any)
					super.close();
					if (database != null && database.isOpen()) {
						// we may be holding a different database than the
						// helper so
						// close that too if it's still open.
						database.close();
					}
					database = null;
				}
			}
		}
	}

	
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public RsrDbAdapter(Context ctx) {
		this.context = ctx;
	}

	/**
	 * Open or create the db
	 * 
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public RsrDbAdapter open() throws SQLException {
	    if (LOG) Log.d(TAG, "Opening DB");
		databaseHelper = new DatabaseHelper(context);
		database = databaseHelper.getWritableDatabase();
		return this;
	}

	/**
	 * close the db
	 */
	public void close() {
        if (LOG) Log.d(TAG, "Closing DB");
		databaseHelper.close();
	}

	
	/**
	 * Create a new project using the title and body provided. If the project is
	 * successfully created return the new id, otherwise return a -1 to indicate
	 * failure.
	 * 
	 * @param title
	 *            project title
	 * 
	 * @return rowId or -1 if failed
	 */
	public long createProject(String title) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(TITLE_COL, title);
		initialValues.put(FUNDS_COL, "0");
		return database.insert(PROJECT_TABLE, null, initialValues);
	}

	/**
	* creates or updates a project in the db
	*
	* @param project
	* @return
	*/
	public void saveProject(Project project) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, project.getId());
		// updatedValues.put(SERVER_ID_COL, project.getServerId());
		updatedValues.put(TITLE_COL, project.getTitle());
		updatedValues.put(SUBTITLE_COL, project.getSubtitle());
		updatedValues.put(SUMMARY_COL, project.getSummary());
		updatedValues.put(FUNDS_COL, project.getFunds());
		updatedValues.put(THUMBNAIL_URL_COL, project.getThumbnailUrl());
		//not done here to preserve a cache connection
//		updatedValues.put(THUMBNAIL_FILENAME_COL, project.getThumbnailFilename());
		updatedValues.put(COUNTRY_COL, project.getCountry());
		updatedValues.put(STATE_COL, project.getState());
		updatedValues.put(CITY_COL, project.getCity());
		updatedValues.put(LAT_COL, project.getLatitude());
		updatedValues.put(LON_COL, project.getLongitude());
		updatedValues.put(HIDDEN_COL, project.getHidden()?"1":"0");
		
		Cursor cursor = database.query(PROJECT_TABLE,
		new String[] { PK_ID_COL },
		PK_ID_COL + " = ?",
		new String[] { project.getId(), },
		null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			// if we found an item, it's an update, otherwise, it's an insert
			database.update(PROJECT_TABLE, updatedValues, PK_ID_COL + " = ?",
					new String[] { project.getId() });
		} else {
			database.insert(PROJECT_TABLE, null, updatedValues);
		}
		
		if (cursor != null) {
			cursor.close();
		}
	}

	
	/*
	 *  Update the local filename of a cached image
	 */
	public void updateProjectThumbnailFile(String id, String filename) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(THUMBNAIL_FILENAME_COL, filename);
		database.update(PROJECT_TABLE, updatedValues, PK_ID_COL + " = ?", new String[] { id });
	}

	
	/*
	 *  Update the last-fetch date for the updates in a project
	 */
	public void updateProjectLastFetch(String id, Date lastfetch) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(LAST_FETCH_COL, lastfetch.getTime()/1000); //1-second precision only
		//int n = 
		database.update(PROJECT_TABLE, updatedValues, PK_ID_COL + " = ?", new String[] { id });
	}

	
	/*
	 *  Clear the local filenames of all projects
	 */
	public void clearProjectThumbnailFiles() {
		ContentValues updatedValues = new ContentValues();
		updatedValues.putNull(THUMBNAIL_FILENAME_COL);
		database.update(PROJECT_TABLE, updatedValues, null, null);
	}


	/*
	 *  Set hidden attribute for all projects not visible to current user
	 */
	public void setVisibleProjects(Set<String> ids) {
		//Hide all
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(HIDDEN_COL, 1);		
		database.update(PROJECT_TABLE, updatedValues, null, null);
		//Show selected
		updatedValues = new ContentValues();
		updatedValues.put(HIDDEN_COL, 0);
	    Iterator<String> itr = ids.iterator();
	    if (itr.hasNext()) {
	    	String whereList = "";
	    	whereList += PK_ID_COL + "=" + itr.next();
	    	while(itr.hasNext()) {
	    		whereList += " OR " + PK_ID_COL + "=" + itr.next();
	    	}
	    	database.update(PROJECT_TABLE, updatedValues, whereList, null);
	    }
	}

	

	/**
	 * saves or updates an Update in the db
	 * 
	 * @param survey
	 * @return
	 */
	public void saveUpdate(Update update, boolean saveFn) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, update.getId());
		updatedValues.put(PROJECT_COL, update.getProjectId());
		updatedValues.put(TITLE_COL, update.getTitle());
		updatedValues.put(TEXT_COL, update.getText());
		updatedValues.put(USER_COL, update.getUserId());
		updatedValues.put(UUID_COL, update.getUuid());
        updatedValues.put(THUMBNAIL_URL_COL, update.getThumbnailUrl());
        updatedValues.put(VIDEO_URL_COL, update.getVideoUrl());
		//not always done here to preserve a cache connection
		if (saveFn) {
            updatedValues.put(THUMBNAIL_FILENAME_COL, update.getThumbnailFilename());
            updatedValues.put(VIDEO_FILENAME_COL, update.getVideoFilename());
		}
        updatedValues.put(PHOTO_CAPTION_COL, update.getPhotoCaption());
        updatedValues.put(PHOTO_CREDIT_COL, update.getPhotoCredit());

        updatedValues.put(COUNTRY_COL, update.getLocation().getCountryId());
        updatedValues.put(STATE_COL, update.getState());
        updatedValues.put(CITY_COL, update.getCity());
        updatedValues.put(LAT_COL, update.getLatitude());
        updatedValues.put(LON_COL, update.getLongitude());
        updatedValues.put(ELE_COL, update.getElevation());

        updatedValues.put(DRAFT_COL, update.getDraft()?"1":"0");
		updatedValues.put(UNSENT_COL, update.getUnsent()?"1":"0");
		updatedValues.put(CREATED_COL, update.getDate().getTime()/1000); //1-second precision only

		Cursor cursor = database.query(UPDATE_TABLE,
				new String[] { PK_ID_COL },
				PK_ID_COL + " = ?",
				new String[] { update.getId(), },
				null, null, null);

		if (cursor != null && cursor.getCount() > 0) {
			// if we found an item, it's an update, otherwise, it's an insert
			database.update(UPDATE_TABLE, updatedValues, PK_ID_COL + " = ?",
					new String[] { update.getId() });
		} else {
			database.insert(UPDATE_TABLE, null, updatedValues);
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	/**
	 * updates an update in the db, after the real ID is returned from the server
	 * 
	 * @param update
	 * @param old_id
	 * @return
	 */
	public boolean updateUpdateIdSent(Update update, String old_id) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, update.getId());
		updatedValues.put(UNSENT_COL, update.getUnsent()?"1":"0");
		updatedValues.put(DRAFT_COL, update.getDraft()?"1":"0");

		// if we change exactly one item, we are good
		int rowsAffected = database.update(UPDATE_TABLE, updatedValues, PK_ID_COL + " = ?",
		        new String[] { old_id });
        if (rowsAffected == 1) {
            return true;
        } else {
            Log.e(TAG, "Tried to update id/sent/draft sts of nonexistent update " + old_id);
            return false;
        }
	}

	
	/**
	 * updates an update in the db, after the uuid verified by the server
	 * 
	 * @param update
	 * @return
	 */
	public boolean updateUpdateVerifiedByUuid(Update update) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, update.getId());
		updatedValues.put(UNSENT_COL, update.getUnsent()?"1":"0");
		updatedValues.put(DRAFT_COL, update.getDraft()?"1":"0");

		// if we changed exactly one item, we are done
		int rowsAffected = database.update(UPDATE_TABLE, updatedValues,
					UUID_COL + " = ?",
					new String[] { update.getUuid() }
					);
		if (rowsAffected == 1) {
			return true;
		} else {
			Log.e(TAG, "Tried to update id/sent/draft sts of nonexistent update " + update.getUuid());
			return false;
		}

	}
	
	/*
	 *  Update the local filename of a cached image
	 */
	public void updateUpdateThumbnailFile(String id, String filename) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(THUMBNAIL_FILENAME_COL, filename);
		database.update(UPDATE_TABLE, updatedValues, PK_ID_COL + " = ?", new String[] { id });
	}


	/*
	 *  Clear the local filenames of all updates
	 */
	public void clearUpdateMediaFiles() {
		ContentValues updatedValues = new ContentValues();
        updatedValues.putNull(THUMBNAIL_FILENAME_COL);
        updatedValues.putNull(VIDEO_FILENAME_COL);
		database.update(UPDATE_TABLE, updatedValues, null, null);
	}


	/*
	 *  Delete an Update
	 */
	public void deleteUpdate(String id) {
		database.delete(UPDATE_TABLE, PK_ID_COL + " = ?", new String[] { id });
	}


	/**
	 * Gets all projects, all columns
	 */
	public Cursor listAllProjects() {
		Cursor cursor = database.query(PROJECT_TABLE,
										null,
										null,
										null,
										null,
										null,
										null);
		return cursor;
	}

	/**
	 * Gets updates for a specific project, all columns
	 */
	public Cursor listVisibleProjects() {
		Cursor cursor = database.query(PROJECT_TABLE,
										null,
										HIDDEN_COL + " = ?",
										new String[] { "0" },
										null,
										null,
										null);

		return cursor;
	}


    /**
     * Gets all projects, all columns and country data
     */
    public Cursor listAllProjectsWithCountry() {
        Cursor cursor = database.query(PROJECT_COUNTRY_JOIN,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null);
        return cursor;
    }


    /**
     * Gets visible projects, all columns and country data
     */
    public Cursor listVisibleProjectsWithCountry() {
        Cursor cursor = database.query(PROJECT_COUNTRY_JOIN,
                new String[] { "project._id", "project.title", "project.hidden", "project.thumbnail_url", "project.thumbnail_fn", "country.name", "country.continent" },
                HIDDEN_COL + " = ?",
                new String[] { "0" },
                null,
                null,
                null);
        return cursor;
    }


    /**
     * Gets visible projects, all columns and country data
     * where project title or country name or continent matches the search string
     */
    public Cursor listVisibleProjectsWithCountryMatching(String search) {
        //Prevent any SQL injection problems
        /* probably not necessary
        search = search.replaceAll("\"", " ");
        search = search.replaceAll("'", " ");
        search = search.replaceAll("(", " ");
        search = search.replaceAll(")", " ");
        search = search.replaceAll(",", " ");
        */
        //Match caseless, assume country or continent is present in entirety
        Cursor cursor = database.query(PROJECT_COUNTRY_JOIN,
                new String[] { "project._id", "project.title", "project.hidden", "project.thumbnail_url", "project.thumbnail_fn", "country.name", "country.continent" },
                HIDDEN_COL + " = ? AND ( title LIKE ? OR name LIKE ? OR continent LIKE ? OR project._id = ?)",
                new String[] { "0", "%" + search + "%", search, search, search },
                null,
                null,
                null);
        return cursor;
    }


	/**
	 * Gets all updates, all columns
	 */
	public Cursor listAllUpdates() {
		Cursor cursor = database.query(UPDATE_TABLE,
										null,
										null,
										null,
										null,
										null,
										null);
		return cursor;
	}


	/**
	 * Gets updates for a specific project, all columns
	 */
	public Cursor listAllUpdatesFor(String _id) {
		Cursor cursor = database.query(UPDATE_TABLE,
										null,
										PROJECT_COL + " = ?",
										new String[] { _id },
										null,
										null,
										null);

		return cursor;
	}

	/**
	 * Gets updates for a specific project, all columns
	 */
	public Cursor listAllUpdatesNewestFirstFor(String _id) {
		Cursor cursor = database.query(UPDATE_TABLE,
										null,
										PROJECT_COL + " = ?",
										new String[] { _id },
										null,
										null,
										CREATED_COL + " DESC");

		return cursor;
	}

	/**
	 * Gets unsent updates, all columns
	 */
	public Cursor listAllUpdatesUnsent() {
		Cursor cursor = database.query(UPDATE_TABLE,
										null,
										UNSENT_COL + " <> 0",
										new String[] { },
										null,
										null,
										null);

		return cursor;
	}


	/**
	 * Counts state sums for updates for a specific project
	 */
	public int[] countAllUpdatesFor(String _id) {
		int draftCount = 0;
		int unsentCount = 0;
		int otherCount = 0;
		Cursor cursor = listAllUpdatesFor(_id);
		if (cursor !=null) {
			if (cursor.getCount() > 0) {
				int draftCol = cursor.getColumnIndexOrThrow(DRAFT_COL);
				int unsentCol = cursor.getColumnIndexOrThrow(UNSENT_COL);//Not used
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					if (cursor.getInt(draftCol) > 0) {
						draftCount++;
					} else	if (cursor.getInt(unsentCol) > 0) {
						unsentCount++;
					} else {
						otherCount++;
					}
					cursor.moveToNext();
				}
			}
			cursor.close();
		}	
		return new int[] { draftCount, unsentCount, otherCount };
	}


	/**
     * Gets all users, all columns
     */
    public Cursor listAllUsers() {
        Cursor cursor = database.query(USER_TABLE,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null);
        return cursor;
    }



    /**
     * gets users that are referenced by updates but not loaded
     * 
     * query crashes with NullPointerException if update table is empty
     * always returns at least an empty list, not null
     */
    public List<String> getMissingUsersList() {
        List<String> idList = new ArrayList<String>();  
        try {
            Cursor cursor = database.query(true, //distinct
                                            "_update LEFT JOIN user ON (userid = user._id)",
                                            new String[] {"userid", "user._id"},
                                            "user._id IS NULL",
                                            null,//selection vals
                                            "userid",
                                            null,
                                            null,
                                            null);
            int c = cursor.getColumnIndex("userid");
            while (cursor.moveToNext()) {
                idList.add(cursor.getString(c));
            }
            cursor.close();
        }
        catch (NullPointerException e) {
        }
        return idList;
    }


    /**
     * Gets all orgs, all columns
     */
    public Cursor listAllOrgs() {
        Cursor cursor = database.query(ORG_COUNTRY_JOIN,
                new String[] {
                    "_organisation._id", "_organisation.name", "_organisation.long_name", "_organisation.logo_url", "_organisation.logo_fn",
                    "country.name AS country_name", "country.continent"
                },
                null,
                null,
                null,
                null,
               "_organisation.name");
        return cursor;
    }


    /**
     * Gets all orgs, all columns
     */
    public Cursor listAllOrgsMatching(String search) {
        //Match caseless, assume id, country or continent is present in entirety (or else "1" would match more than 10% of records) 
        Cursor cursor = database.query(ORG_COUNTRY_JOIN,
                new String[] {
                "_organisation._id", "_organisation.name", "_organisation.long_name", "_organisation.logo_url", "_organisation.logo_fn",
                "country.name AS country_name", "country.continent"
            },
            "( _organisation.name LIKE ? OR _organisation.long_name LIKE ? OR country_name LIKE ? OR continent LIKE ? OR _organisation._id = ?)",
            new String[] { "%" + search + "%", "%" + search + "%", search, search, search },
            null,
            null,
            "_organisation.name");
        return cursor;
    }


    /**
     * gets orgs that are referenced by users but not loaded
     * 
     * query might crash with NullPointerException if user table is empty
     * always return at least an empty list, not null
     */
    public List<String> getMissingOrgsList() {
        List<String> idList = new ArrayList<String>();  
        try {
            Cursor cursor = database.query(true, //distinct
                                            "user LEFT JOIN _organisation ON (organisation = _organisation._id)",
                                            new String[] {"organisation", "_organisation._id"},
                                            "(organisation NOT NULL) AND (_organisation._id IS NULL)",
                                            null,//selection vals
                                            "organisation",
                                            null,
                                            null,
                                            null);
            int c = cursor.getColumnIndex("organisation");
            while (cursor.moveToNext()) {
                idList.add(cursor.getString(c));
            }
            cursor.close();
        }
        catch (NullPointerException e) {
        }
        return idList;
    }


	/**
	 * Gets a single project from the db using its primary key
	 */
	public Project findProject(String _id) {
		Project project = null;
		Cursor cursor = database.query(PROJECT_COUNTRY_JOIN,
									   null,
									   "project._id = ?",
									   new String[] { _id }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				project = new Project();
				project.setId(_id); //no confusion with country id
				project.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL)));
				project.setSubtitle(cursor.getString(cursor.getColumnIndexOrThrow(SUBTITLE_COL)));
				project.setSummary(cursor.getString(cursor.getColumnIndexOrThrow(SUMMARY_COL)));
				project.setThumbnailUrl(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_URL_COL)));
				project.setThumbnail(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_FILENAME_COL)));
				project.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)));
				project.setState(cursor.getString(cursor.getColumnIndexOrThrow(STATE_COL)));
				project.setCity(cursor.getString(cursor.getColumnIndexOrThrow(CITY_COL)));
				project.setLatitude(cursor.getString(cursor.getColumnIndexOrThrow(LAT_COL)));
				project.setLongitude(cursor.getString(cursor.getColumnIndexOrThrow(LON_COL)));
				project.setHidden(0 != cursor.getInt(cursor.getColumnIndexOrThrow(HIDDEN_COL)));
				project.setLastFetch(new Date(1000L * cursor.getInt(cursor.getColumnIndexOrThrow(LAST_FETCH_COL))));
				//Log.d(TAG, "Project "+_id+" last fetch (s)"+cursor.getInt(cursor.getColumnIndexOrThrow(LAST_FETCH_COL)));
			}
			cursor.close();
			}

		return project;
	}


	/**
	 * Gets a single update from the db using its primary key
	 */
	public Update findUpdate(String _id) {
	    Update update = null;
		Cursor cursor = database.query(UPDATE_COUNTRY_JOIN,
										null, //all columns
										"_update._id = ?",
										new String[] { _id }, null, null, null);
		if (cursor != null) {
		    if (cursor.moveToFirst()) {
		        update = new Update();
				update.setId(_id);
				update.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL)));
				update.setProjectId(cursor.getString(cursor.getColumnIndexOrThrow(PROJECT_COL)));
				update.setText(cursor.getString(cursor.getColumnIndexOrThrow(TEXT_COL)));
				update.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(UUID_COL)));
				update.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(USER_COL)));
                update.setThumbnailUrl(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_URL_COL)));
                update.setThumbnailFilename(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_FILENAME_COL)));
                update.setVideoUrl(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_URL_COL)));
                update.setVideoFilename(cursor.getString(cursor.getColumnIndexOrThrow(VIDEO_FILENAME_COL)));
                update.setPhotoCaption(cursor.getString(cursor.getColumnIndexOrThrow(PHOTO_CAPTION_COL)));
                update.setPhotoCredit(cursor.getString(cursor.getColumnIndexOrThrow(PHOTO_CREDIT_COL)));
				update.setDraft(0 != cursor.getInt(cursor.getColumnIndexOrThrow(DRAFT_COL)));
				update.setUnsent(0 != cursor.getInt(cursor.getColumnIndexOrThrow(UNSENT_COL)));
				update.setDate(new Date(1000L * cursor.getLong(cursor.getColumnIndexOrThrow(CREATED_COL))));
                update.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)));
                update.getLocation().setCountryId(cursor.getString(cursor.getColumnIndexOrThrow(COUNTRY_COL)));
				update.setState(cursor.getString(cursor.getColumnIndexOrThrow(STATE_COL)));
				update.setCity(cursor.getString(cursor.getColumnIndexOrThrow(CITY_COL)));
				update.setLatitude(cursor.getString(cursor.getColumnIndexOrThrow(LAT_COL)));
                update.setLongitude(cursor.getString(cursor.getColumnIndexOrThrow(LON_COL)));
                update.setElevation(cursor.getString(cursor.getColumnIndexOrThrow(ELE_COL)));
			}
			cursor.close();
		    }
		return update;
	}


	/**
	 * Lists all non-deleted surveys from the database
	 */
/*	public ArrayList<Survey> listSurveys(String language) {
		ArrayList<Survey> surveys = new ArrayList<Survey>();
		String whereClause = DELETED_COL + " <> ?";
		String[] whereParams = null;
		if (language != null) {
			whereClause += " and " + LANGUAGE_COL + " = ?";
			whereParams = new String[] { ConstantUtil.IS_DELETED,
					language.toLowerCase().trim() };
		} else {
			whereParams = new String[] { ConstantUtil.IS_DELETED };
		}
		Cursor cursor = database.query(SURVEY_TABLE, new String[] { PK_ID_COL,
				DISP_NAME_COL, LOCATION_COL, FILENAME_COL, TYPE_COL,
				LANGUAGE_COL, HELP_DOWNLOADED_COL, VERSION_COL }, whereClause,
				whereParams, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					Survey survey = new Survey();
					survey.setId(cursor.getString(cursor
							.getColumnIndexOrThrow(PK_ID_COL)));
					survey.setName(cursor.getString(cursor
							.getColumnIndexOrThrow(DISP_NAME_COL)));
					survey.setLocation(cursor.getString(cursor
							.getColumnIndexOrThrow(LOCATION_COL)));
					survey.setFileName(cursor.getString(cursor
							.getColumnIndexOrThrow(FILENAME_COL)));
					survey.setType(cursor.getString(cursor
							.getColumnIndexOrThrow(TYPE_COL)));
					survey.setHelpDownloaded(cursor.getString(cursor
							.getColumnIndexOrThrow(HELP_DOWNLOADED_COL)));
					survey.setLanguage(cursor.getString(cursor
							.getColumnIndexOrThrow(LANGUAGE_COL)));
					survey.setVersion(cursor.getDouble(cursor
							.getColumnIndexOrThrow(VERSION_COL)));
					surveys.add(survey);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return surveys;
	}
*/


	/**
	 * deletes all the projects from the database
	 */
	public void deleteAllProjects() {
		database.delete(PROJECT_TABLE, null, null);
		database.delete(UPDATE_TABLE, null, null);
	}

	
    /**
     * Gets a single user from the db using its primary key
     */
    public User findUser(String _id) {
        User user = null;
        Cursor cursor = database.query(USER_TABLE,
                                       null,
                                       "_id = ?",
                                       new String[] { _id }, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                user = new User();
                user.setId(_id); //no confusion with country id
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(USERNAME_COL)));
                user.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow(FIRST_NAME_COL)));
                user.setLastname(cursor.getString(cursor.getColumnIndexOrThrow(LAST_NAME_COL)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(EMAIL_COL)));
                user.setOrgId(cursor.getString(cursor.getColumnIndexOrThrow(ORGANISATION_COL)));
                }
            cursor.close();
            }

        return user;
    }


    /**
    * creates or updates a user in the db
    *
    * @param user
    * @return
    */
    public void saveUser(User user) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PK_ID_COL, user.getId());
        updatedValues.put(USERNAME_COL, user.getUsername());
        updatedValues.put(FIRST_NAME_COL, user.getFirstname());
        updatedValues.put(LAST_NAME_COL, user.getLastname());
        updatedValues.put(EMAIL_COL, user.getEmail());
        updatedValues.put(ORGANISATION_COL, user.getOrgId());
        
        Cursor cursor = database.query(USER_TABLE,
                                        new String[] { PK_ID_COL },
                                        PK_ID_COL + " = ?",
                                        new String[] { user.getId(), },
                                        null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            // if we found an item, it's an update, otherwise, it's an insert
            database.update(USER_TABLE, updatedValues, PK_ID_COL + " = ?",
                    new String[] { user.getId() });
        } else {
            database.insert(USER_TABLE, null, updatedValues);
        }
        
        if (cursor != null) {
            cursor.close();
        }
    }

    
    /**
     * Gets a single user from the db using its primary key
     */
    public Organisation findOrganisation(String _id) {
        Organisation org = null;
        Cursor cursor = database.query(ORG_TABLE,
                                       null,
                                       "_id = ?",
                                       new String[] { _id }, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                org = new Organisation();
                org.setId(_id);
                org.setName(cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)));
                org.setLongName(cursor.getString(cursor.getColumnIndexOrThrow(LONG_NAME_COL)));
                org.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(EMAIL_COL)));
                org.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(URL_COL)));
                org.setLogo(cursor.getString(cursor.getColumnIndexOrThrow(LOGO_URL_COL)));
                //org.setLogoFilename(cursor.getString(cursor.getColumnIndexOrThrow(LOGO_FN_COL)));
                org.setPrimaryCountryId(cursor.getString(cursor.getColumnIndexOrThrow(PRIMARY_COUNTRY_ID_COL)));
                }
            cursor.close();
            }

        return org;
    }


    /**
    * creates or updates a user in the db
    *
    * @param org the organisation data to be updated
    * @return
    */
    public void saveOrganisation(Organisation org) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PK_ID_COL, org.getId());
        updatedValues.put(NAME_COL, org.getName());
        updatedValues.put(LONG_NAME_COL, org.getLongName());
        updatedValues.put(EMAIL_COL, org.getEmail());
        updatedValues.put(URL_COL, org.getUrl());
        updatedValues.put(PRIMARY_COUNTRY_ID_COL, org.getPrimaryCountryId());
        updatedValues.put(LOGO_URL_COL, org.getLogo());
        //not logo filename, to preserve cache connection
        
        Cursor cursor = database.query(ORG_TABLE,
                                        new String[] { PK_ID_COL },
                                        PK_ID_COL + " = ?",
                                        new String[] { org.getId(), },
                                        null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            // if we found an item, it's an update, otherwise, it's an insert
            database.update(ORG_TABLE, updatedValues, PK_ID_COL + " = ?",
                    new String[] { org.getId() });
        } else {
            database.insert(ORG_TABLE, null, updatedValues);
        }
        
        if (cursor != null) {
            cursor.close();
        }
    }

    

    /**
     * Gets a single result from the db using its primary key
     */
    public Result findResult(String _id) {
        Result res = null;
        Cursor cursor = database.query(RESULT_TABLE,
                                       null,
                                       "_id = ?",
                                       new String[] { _id }, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                res = new Result();
                res.setId(_id);
                res.setProjectId(cursor.getString(cursor.getColumnIndexOrThrow(PROJECT_COL)));
                res.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL)));
                res.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COL)));
                }
            cursor.close();
            }

        return res;
    }


    /**
    * creates or updates a result in the db
    *
    * @param res the result data to be updated
    * @return
    */
    public void saveResult(Result res) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PK_ID_COL, res.getId());
        updatedValues.put(TITLE_COL, res.getTitle());
        updatedValues.put(DESCRIPTION_COL, res.getDescription());
        updatedValues.put(PROJECT_ID_COL, res.getProjectId());
        updatedValues.put(TYPE_COL, res.getType());
        
        Cursor cursor = database.query(RESULT_TABLE,
                                        new String[] { PK_ID_COL },
                                        PK_ID_COL + " = ?",
                                        new String[] { res.getId(), },
                                        null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            // if we found an item, it's an update, otherwise, it's an insert
            database.update(RESULT_TABLE, updatedValues, PK_ID_COL + " = ?",
                    new String[] { res.getId() });
        } else {
            database.insert(RESULT_TABLE, null, updatedValues);
        }
        
        if (cursor != null) {
            cursor.close();
        }
    }

	/**
	 * Gets updates for a specific project, all columns
	 */
	public Cursor listResultsFor(String _id) {
		Cursor cursor = database.query(RESULT_TABLE,
										null,
										PROJECT_ID_COL + " = ?",
										new String[] { _id },
										null,
										null,
										null);

		return cursor;
	}

	
	/**
	 * Gets updates for a specific project, all columns
	 * TODO: ipd and ipdc
	 */
	public Cursor listResultsIndicatorsPeriodsFor(String _id) {
		Cursor cursor = database.query(RESULT_TABLE + 
										" LEFT JOIN " + INDICATOR_TABLE + " ON  " + RESULT_TABLE + "._id = " + INDICATOR_TABLE + ".result_id" +
                                        " LEFT JOIN " + PERIOD_TABLE + " ON  " + INDICATOR_TABLE + "._id = " + PERIOD_TABLE + ".indicator_id" +
                                        " LEFT JOIN " + IPD_TABLE + " ON  " + PERIOD_TABLE + "._id = " + IPD_TABLE + ".period_id",
										new String[] {
		                                    "_result._id as result_id",
		                                    "_indicator._id as indicator_id",
                                            "_period._id as period_id",
                                            "_ipd._id as ipd_id",
		                                    "_result.title as result_title",
		                                    "_indicator.title as indicator_title",
                                            "_period.period_start",
                                            "_period.period_end",
                                            "_period.actual_value",
                                            "_period.target_value",
                                            "_period.locked",
                                            "_ipd.data",
                                            "_ipd.description",
                                            "_ipd.user_id",
                                            "_ipd.status"
		                                    },
										PROJECT_ID_COL + " = ?",
										new String[] { _id },
										"_result._id,_indicator._id,_period._id", //group by
										null,
										"_result._id,_indicator._id,_period._id, _ipd._id"); //order by

		return cursor;
	}

	
    /**
    * creates or updates an indicator in the db
    *
    * @param ind the indicator data to be updated
    * @return
    */
    public void saveIndicator(Indicator ind) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PK_ID_COL, ind.getId());
        updatedValues.put(TITLE_COL, ind.getTitle());
        updatedValues.put(DESCRIPTION_COL, ind.getDescription());
        updatedValues.put(RESULT_ID_COL, ind.getResultId());
        updatedValues.put(MEASURE_COL, ind.getMeasure());
        updatedValues.put(BASELINE_YEAR_COL, ind.getBaselineYear());
        updatedValues.put(BASELINE_VALUE_COL, ind.getBaselineValue());
        updatedValues.put(BASELINE_COMMENT_COL, ind.getBaselineComment());
        
        Cursor cursor = database.query(INDICATOR_TABLE,
                                        new String[] { PK_ID_COL },
                                        PK_ID_COL + " = ?",
                                        new String[] { ind.getId(), },
                                        null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            // if we found an item, it's an update, otherwise, it's an insert
            database.update(INDICATOR_TABLE, updatedValues, PK_ID_COL + " = ?",
                    new String[] { ind.getId() });
        } else {
            database.insert(INDICATOR_TABLE, null, updatedValues);
        }
        
        if (cursor != null) {
            cursor.close();
        }
    }

    
    /**
    * creates or updates an indicator in the db
    *
    * @param period the indicator data to be updated
    * @return
    */
    public void savePeriod(Period period) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PK_ID_COL, period.getId());
        updatedValues.put(TITLE_COL, period.getTitle());
        updatedValues.put(INDICATOR_ID_COL, period.getIndicatorId());
        if (period.getPeriodStart() != null) {
            updatedValues.put(PERIOD_START_COL, period.getPeriodStart().getTime()/1000);
        } else {
            updatedValues.putNull(PERIOD_START_COL);
        }
        if (period.getPeriodEnd() != null) {
            updatedValues.put(PERIOD_END_COL, period.getPeriodEnd().getTime()/1000);
        } else {
            updatedValues.putNull(PERIOD_END_COL);
        }
        updatedValues.put(ACTUAL_VALUE_COL, period.getActualValue());
        updatedValues.put(ACTUAL_COMMENT_COL, period.getActualComment());
        updatedValues.put(TARGET_VALUE_COL, period.getTargetValue());
        updatedValues.put(TARGET_COMMENT_COL, period.getTargetComment());
        updatedValues.put(LOCKED_COL, period.getLocked());
        
        Cursor cursor = database.query(PERIOD_TABLE,
                                        new String[] { PK_ID_COL },
                                        PK_ID_COL + " = ?",
                                        new String[] { period.getId(), },
                                        null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            // if we found an item, it's an update, otherwise, it's an insert
            database.update(PERIOD_TABLE, updatedValues, PK_ID_COL + " = ?",
                    new String[] { period.getId() });
        } else {
            database.insert(PERIOD_TABLE, null, updatedValues);
        }
        
        if (cursor != null) {
            cursor.close();
        }
    }

    
    /**
    * creates or updates an indicator period data record in the db
    *
    * @param ipd the indicator period data to be updated
    * @return
    */
    public void saveIpd(IndicatorPeriodData ipd) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PK_ID_COL, ipd.getId());
        updatedValues.put(DATA_COL, ipd.getData());
        updatedValues.put(RELATIVE_DATA_COL, ipd.getRelativeData());
        updatedValues.put(DESCRIPTION_COL, ipd.getDescription());
        updatedValues.put(PERIOD_ID_COL, ipd.getPeriodId());
        updatedValues.put(USER_ID_COL, ipd.getPeriodId());
        updatedValues.put(STATUS_COL, ipd.getStatus());
        updatedValues.put(PHOTO_URL_COL, ipd.getPhotoUrl());
        updatedValues.put(FILE_URL_COL, ipd.getFileUrl());
        //The filename fields are saved when images are fetched
//        updatedValues.put(PHOTO_FN_COL, ipd.getPhotoFn());
//        updatedValues.put(FILE_FN_COL, ipd.getFileFn());
        
        Cursor cursor = database.query(IPD_TABLE,
                                        new String[] { PK_ID_COL },
                                        PK_ID_COL + " = ?",
                                        new String[] { ipd.getId(), },
                                        null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            // if we found an item, it's an update, otherwise, it's an insert
            database.update(IPD_TABLE, updatedValues, PK_ID_COL + " = ?",
                    new String[] { ipd.getId() });
        } else {
            database.insert(IPD_TABLE, null, updatedValues);
        }
        
        if (cursor != null) {
            cursor.close();
        }
    }

    
    /*
     *  Update the local filename of a cached photo
     */
    public void updateIpdcPhotoFile(String id, String filename) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PHOTO_FN_COL, filename);
        database.update(IPDC_TABLE, updatedValues, PK_ID_COL + " = ?", new String[] { id });
    }


    /*
     *  Update the local filename of a cached attachment
     */
    public void updateIpdcAttachedFile(String id, String filename) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(FILE_FN_COL, filename);
        database.update(IPDC_TABLE, updatedValues, PK_ID_COL + " = ?", new String[] { id });
    }


    /*
     *  Clear the local filenames of all updates
     */
    public void clearIpdcMediaFiles() {
        ContentValues updatedValues = new ContentValues();
        updatedValues.putNull(PHOTO_FN_COL);
        updatedValues.putNull(FILE_FN_COL);
        database.update(IPDC_TABLE, updatedValues, null, null);
    }


    
    /**
    * creates or updates an indicator period data record in the db
    *
    * @param ipdc the indicator period data comment to be updated
    * @return
    */
    public void saveIpdc(IndicatorPeriodDataComment ipdc) {
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(PK_ID_COL, ipdc.getId());
        updatedValues.put(COMMENT_COL, ipdc.getComment());
        updatedValues.put(USER_COL, ipdc.getUserId());
        
        Cursor cursor = database.query(IPDC_TABLE,
                                        new String[] { PK_ID_COL },
                                        PK_ID_COL + " = ?",
                                        new String[] { ipdc.getId(), },
                                        null, null, null);
        
        if (cursor != null && cursor.getCount() > 0) {
            // if we found an item, it's an update, otherwise, it's an insert
            database.update(IPDC_TABLE, updatedValues, PK_ID_COL + " = ?",
                    new String[] { ipdc.getId() });
        } else {
            database.insert(IPDC_TABLE, null, updatedValues);
        }
        
        if (cursor != null) {
            cursor.close();
        }
    }

    
	public int countResults() {
		int c = -1;
		Cursor cursor = database.query(RESULT_TABLE, new String[] {"COUNT (*) as row_count"}, 
        								null, null, null, null, null);
        if (cursor != null) {
        	if ( cursor.moveToFirst() ) {
        		c = cursor.getInt(0);
        	}
        	cursor.close();
        }
        return c;
    }

    public int countResultsFor(String pid) {
        int c = -1;
        Cursor cursor = database.query( RESULT_TABLE, new String[] {"COUNT (*) as row_count"}, 
                                        RESULT_TABLE + ".project_id = ?",
                                        new String[] {pid},
                                        null, null, null);
        if (cursor != null) {
            if ( cursor.moveToFirst() ) {
                c = cursor.getInt(0);
            }
            cursor.close();
        }
        return c;
    }



    public int countIndicators() {
        int c = -1;
        Cursor cursor = database.query(INDICATOR_TABLE, new String[] {"COUNT (*) as row_count"}, 
                null, null, null, null, null);
        if (cursor != null) {
            if ( cursor.moveToFirst() ) {
                c = cursor.getInt(0);
            }
            cursor.close();
        }
        return c;
    }


    public int countIndicatorsFor(String pid) {
        int c = -1;
        Cursor cursor = database.query(RESULT_TABLE + " LEFT JOIN " + INDICATOR_TABLE + " ON " + RESULT_TABLE + "._id = " + INDICATOR_TABLE + ".result_id" ,
                                       new String[] {"COUNT (*) as row_count"}, 
                                       RESULT_TABLE + ".project_id = ?",
                                       new String[] {pid},
                                       null, null, null);
        if (cursor != null) {
            if ( cursor.moveToFirst() ) {
                c = cursor.getInt(0);
            }
            cursor.close();
        }
        return c;
    }


	public int countPeriods() {
		int c = -1;
		Cursor cursor = database.query(PERIOD_TABLE, new String[] {"COUNT (*) as row_count"}, 
				null, null, null, null, null);
        if (cursor != null) {
        	if ( cursor.moveToFirst() ) {
        		c = cursor.getInt(0);
        	}
        	cursor.close();
        }
        return c;
    }


	
	


	/**
	 * executes a single insert/update/delete DML or any DDL statement without
	 * any bind arguments.
	 * 
	 * @param sql
	 */
	public void executeSql(String sql) {
		database.execSQL(sql);
	}


	/**
	 * permanently deletes all records
	 * from the database
	 */
	public void clearAllData() {
	    executeSql("delete from " + PERIOD_TABLE);
	    executeSql("delete from " + INDICATOR_TABLE);
	    executeSql("delete from " + RESULT_TABLE);
	    executeSql("delete from " + UPDATE_TABLE);
	    executeSql("delete from " + USER_TABLE);
	    executeSql("delete from " + ORG_TABLE);
	    executeSql("delete from " + PROJECT_TABLE);
	    executeSql("delete from " + COUNTRY_TABLE);
	}

    /**
     * lists all Countries
     * 
     * @return a Cursor containing all countries
     */
	public Cursor listAllCountries() {
		Cursor cursor = database.query(COUNTRY_TABLE,
				null,
				null,
				null,
				null,
				null,
				null);
		return cursor;
	}
	
	
	public int getCountryCount() {
	    Cursor cursor = listAllCountries();
	    int c = 0;
	    if (cursor != null) {
	        c = cursor.getCount();
	        cursor.close();
	    }
	    return c;    
	}
	

	/**
	 * saves or updates a Country in the db
	 * 
	 * @param country
	 * @return
	 */
	public void saveCountry(Country country) {
		ContentValues updatedValues = new ContentValues();
		updatedValues.put(PK_ID_COL, country.getId());
		updatedValues.put(NAME_COL, country.getName());
		updatedValues.put(CONTINENT_COL, country.getContinent());
		updatedValues.put(ISO_CODE_COL, country.getIsoCode());

		Cursor cursor = database.query(COUNTRY_TABLE,
				new String[] { PK_ID_COL },
				PK_ID_COL + " = ?",
				new String[] { country.getId(), },
				null, null, null);

		if (cursor != null && cursor.getCount() > 0) {
			// if we found an item, it's an update, otherwise, it's an insert
			database.update(COUNTRY_TABLE, updatedValues, PK_ID_COL + " = ?",
					new String[] { country.getId() });
		} else {
			database.insert(COUNTRY_TABLE, null, updatedValues);
		}

		if (cursor != null) {
			cursor.close();
		}
	}



}
