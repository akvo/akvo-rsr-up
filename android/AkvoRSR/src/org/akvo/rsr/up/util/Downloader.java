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

package org.akvo.rsr.up.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.json.JsonParser;
import org.akvo.rsr.up.json.ListJsonParser;
import org.akvo.rsr.up.json.OrgJsonParser;
import org.akvo.rsr.up.json.OrgListJsonParser;
import org.akvo.rsr.up.json.ProjectResultListJsonParser;
import org.akvo.rsr.up.json.UserJsonParser;
import org.akvo.rsr.up.xml.AuthHandler;
import org.akvo.rsr.up.xml.CountryRestListHandler;
import org.akvo.rsr.up.xml.ProjectExtraRestHandler;
import org.akvo.rsr.up.xml.UpdateRestHandler;
import org.akvo.rsr.up.xml.UpdateRestListHandler;
import org.json.JSONException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.Base64;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * @author stellan
 *
 */
public class Downloader {

	private static final String TAG = "Downloader";

	public boolean err = false;
	
	private final static int READ_TIMEOUT_MS = 60000;
		
	public static class UnresolvedPostException extends IOException {
		UnresolvedPostException(String string) {
			super(string);
		}

		private static final long serialVersionUID = -630304430323100535L;
	}

    public static class FailedPostException extends Exception {
        public FailedPostException(String string) {
            super(string);
        }

        private static final long serialVersionUID = -8091570663513780467L;
    }


    public static class FailedFetchException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 2355800973621221158L;

        public FailedFetchException(String string) {
            super(string);
        }

    }
    


    /**
     * populates the Results/Indicators/Periods tables for a single project
     * URL should specify all results for a single project
     * The returned results will typically fit on a single page, but we do it paged, just in case
     *
     * @param ctx
     * @param dba
     * @param url
     * @return
     * @throws FailedFetchException
     */
    public Date fetchProjectResultsPaged(Context ctx, RsrDbAdapter dba, URL url) throws FailedFetchException {
        Date serverDate = null;
        User user = SettingsUtil.getAuthUser(ctx);
        int total = 0;
        int page = 0;
        //the fetch is called in a loop to get it page by page, otherwise it would take too long for server
        //and it would not scale beyond 1000 items in any case
        while (url != null) {
            HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
            h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
            int code = h.code();//evaluation starts the exchange
            if (code == 200) {
                page++;
                String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
                serverDate = new Date(h.date());
                String jsonBody = h.body();
                ListJsonParser jp = new ProjectResultListJsonParser(dba, serverVersion);
                /* Parse the JSON-data from our URL. */
                try {
                	jp.parse(jsonBody);
                }
                catch (JSONException e) {
                    throw new FailedFetchException("Invalid server response: " + e.getMessage());
                }
                /* Parsing has finished. */
                Log.d(TAG, "URL " + url.toString());
                Log.i(TAG, "Fetched " + jp.getCount() + " updates; target total = "+ jp.getTotalCount());
               
                total += jp.getCount();
                if (jp.getNextUrl().length() == 0) { //string needs to be trimmed from whitespace
                    url = null;//we are done
                } else {
                    try {
                        url = new URL(jp.getNextUrl());//TODO is this URL-escaped? xml-escaped?
                    }
                    catch (MalformedURLException e) {
                    	//TODO tell user?
                        url = null;
                    }
                }
    
            } else if (code == 404) {
                url = null;//we are done
            } else {
                //Vanilla case is 403 forbidden on an auth failure
                Log.e(TAG, "Fetch update list HTTP error code:" + code + ' ' + h.message());
                Log.e(TAG, h.body());
                throw new FailedFetchException("Unexpected server response: " + code + ' ' + h.message());
            }
        }
        Log.i(TAG, "Grand total of " + page + " pages, " + total + " Results");
        return serverDate;
    	
    }
    

	/**
	 * populates the projects table in the db from a server URL
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Date fetchProject(Context ctx, RsrDbAdapter dba, URL url) throws ParserConfigurationException, SAXException, IOException {

        User user = SettingsUtil.getAuthUser(ctx);
        HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
        h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
        int code = h.code();//evaluation starts the exchange
        String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
        Date serverDate = new Date(h.date());
        if (code == 200) {
    		/* Get a SAXParser from the SAXPArserFactory. */
    		SAXParserFactory spf = SAXParserFactory.newInstance();
    		SAXParser sp = spf.newSAXParser();
    		/* Get the XMLReader of the SAXParser we created. */
    		XMLReader xr = sp.getXMLReader();
    		/* Create a new ContentHandler and apply it to the XML-Reader*/
            ProjectExtraRestHandler myHandler = new ProjectExtraRestHandler(serverVersion);
    		xr.setContentHandler(myHandler);
    		/* Parse the xml-data from our URL. */
            xr.parse(new InputSource(h.stream()));
    		/* Parsing has finished. */
    		Project proj  = myHandler.getProject();
    		if (proj != null) {
    		    dba.saveProject(proj);
    	        Log.i(TAG, "Fetched project #" + proj.getId());
    		} else {
                Log.e(TAG, "Fetch update failed:" + myHandler.getError());
    		    
    		}
        } else {
            //Vanilla case is 403 forbidden on an auth failure
            Log.e(TAG, "Fetch update list HTTP error code:" + code);
            Log.e(TAG, h.body());
            throw new IOException("Unexpected server response " + code);
        }
        return serverDate;

	}

	
    /**
     * populates the updates table in the db from a server URL
     * in the REST API
     * Typically the url will specify updates for a single project.
     * should eventually call project_update_extra call
     * this will avoid having to call country/org/user APIs separately
     * 
     * @param ctx
     * @param url
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Date fetchUpdateListRestApi(Context ctx, URL url) throws ParserConfigurationException, SAXException, IOException {

        User user = SettingsUtil.getAuthUser(ctx);
        HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
        h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
        int code = h.code();//evaluation starts the exchange
        Date serverDate = new Date(h.date());
        if (code == 200) {
            String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
            /* Get a SAXParser from the SAXPArserFactory. */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            /* Get the XMLReader of the SAXParser we created. */
            XMLReader xr = sp.getXMLReader();
            /* Create a new ContentHandler and apply it to the XML-Reader*/ 
            UpdateRestListHandler myUpdateListHandler = new UpdateRestListHandler(new RsrDbAdapter(ctx), true, serverVersion);
            //the following will need to be called in a loop to get it page by page, or it would probably take too long for server
            xr.setContentHandler(myUpdateListHandler);
            /* Parse the xml-data from our URL. */
            xr.parse(new InputSource(h.stream()));
            /* Parsing has finished. */
            /* Check if anything went wrong. */
            err = myUpdateListHandler.getError();
            Log.i(TAG, "Fetched " + myUpdateListHandler.getCount() + " updates");

        } else {
            //Vanilla case is 403 forbidden on an auth failure
            Log.e(TAG, "Fetch update list HTTP error code:" + code);
            Log.e(TAG, h.body());
            throw new IOException("Unexpected server response " + code);
        }
        return serverDate;
    }


    /**
     * populates the updates table in the db from a server URL
     * in the REST API
     * Typically the url will specify updates for a single project.
     * should eventually call project_update_extra call
     * this will avoid having to call country/org/user APIs separately
     * 
     * @param ctx
     * @param url
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws FailedFetchException 
     */
    public Date fetchUpdateListRestApiPaged(Context ctx, URL url) throws ParserConfigurationException, SAXException, IOException, FailedFetchException {
        Date serverDate = null;
        User user = SettingsUtil.getAuthUser(ctx);
        int total = 0;
        int page = 0;
        //the fetch is called in a loop to get it page by page, otherwise it would take too long for server
        //and it would not scale beyond 1000 updates in any case
        RsrDbAdapter dba = new RsrDbAdapter(ctx);
        while (url != null) {
            HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
            h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
            int code = h.code();//evaluation starts the exchange
            if (code == 200) {
                page++;
                String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
                serverDate = new Date(h.date());
                SAXParserFactory spf = SAXParserFactory.newInstance();
                XMLReader xr = spf.newSAXParser().getXMLReader();
                UpdateRestListHandler xmlHandler = new UpdateRestListHandler(dba, true, serverVersion);
                xr.setContentHandler(xmlHandler);
                /* Parse the xml-data from our URL. */
                xr.parse(new InputSource(h.stream()));
                /* Parsing has finished. */
                /* Check if anything went wrong. */
                err = xmlHandler.getError();
                Log.d(TAG, "URL " + url.toString());
                Log.i(TAG, "Fetched " + xmlHandler.getCount() + " updates; target total = "+ xmlHandler.getTotalCount());

//                dba.open();
//                Log.d(TAG, "Updates in db: " + dba.listAllUpdates().getCount());
//                dba.close();
                
                total += xmlHandler.getCount();
                if (xmlHandler.getNextUrl().length() == 0) { //string needs to be trimmed from whitespace
                    url = null;//we are done
                } else {
                    try {
                        url = new URL(xmlHandler.getNextUrl());//TODO is this xml-escaped?
                    }
                    catch (MalformedURLException e) {
                        url = null;
                    }
                }
    
            } else if (code == 404) {
                url = null;//we are done
            } else {
                //Vanilla case is 403 forbidden on an auth failure
                Log.e(TAG, "Fetch update list HTTP error code:" + code + ' ' + h.message());
                Log.e(TAG, h.body());
                throw new FailedFetchException("Unexpected server response " + code + ' ' + h.message());
            }
        }
        Log.i(TAG, "Grand total of " + page + " pages, " + total + " updates");
        return serverDate;
    }


	/**
	 * Verify status at server of a single Update
	 * 
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws FailedPostException 
	 */
	public static boolean verifyUpdate(Context ctx, URL url, RsrDbAdapter dba, String localId) throws ParserConfigurationException, FailedPostException {

		Log.v(TAG, "Verifying update " + localId);

        User user = SettingsUtil.getAuthUser(ctx);
        HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
        h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
        int code = h.code();//evaluation starts the exchange
        if (code == 200) {
            String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
            /* Get a SAXParser from the SAXPArserFactory. */
    		SAXParserFactory spf = SAXParserFactory.newInstance();
    		try {
        		SAXParser sp = spf.newSAXParser();
        
        		/* Get the XMLReader of the SAXParser we created. */
        		XMLReader xr = sp.getXMLReader();
        		/* Create a new ContentHandler and apply it to the XML-Reader*/ 
        		UpdateRestListHandler updateHandler = new UpdateRestListHandler(null, false, serverVersion);
        		xr.setContentHandler(updateHandler);
        		/* Parse the xml-data from our URL. */
                xr.parse(new InputSource(h.stream()));
        		/* Parsing has finished. */
        
        		/* Check if anything went wrong. */
        		boolean err = updateHandler.getError();
        		if (err) {
        			Log.e(TAG, "Verification error");
        			return false;
        		}
        		int count = updateHandler.getCount();
        		Log.v(TAG, "Verification count: " + count);
        		if (count == 1) {  //1 is good, Update present on server, so record that fact
        			Update u = updateHandler.getLastUpdate(); //this is the result, db has not been changed
        			u.setUnsent(false); //we are done
        			u.setDraft(false); //published, not draft
        			dba.updateUpdateVerifiedByUuid(u);
        			return true;
        		} else {
        			if (count == 0) {  //0 is unfortunate, update never made it to server, will need to be re-sent
        				Update u = dba.findUpdate(localId);
        				u.setUnsent(false); //status is resolved
        				u.setDraft(true); //go back to being draft
        				dba.updateUpdateVerifiedByUuid(u);
                        Log.e(TAG,"Update " + localId + " is not on server");
                        throw new FailedPostException(ctx.getResources().getString(R.string.msg_update_interrupted)); //This is what must have happened to get here
        			} else { //more than one is bad! 
                        Log.e(TAG,"Verify got more than one match for Update UUID!");
        	            throw new FailedPostException(ctx.getResources().getString(R.string.msg_update_duplicated));
        			}
        		}
    		} 
    		catch (IOException e) {
    			return false; //connection problem - transient, still don't know
    		}
    		catch (SAXException e) { //broken XML? - probably transient, still don't know
    			return false;
    		}
        } else {
            //Vanilla case is 403 forbidden on an auth failure
            Log.e(TAG, "Fetch update list HTTP error code:" + code);
            Log.e(TAG, h.body());
            return false;
        }
	}

	
	/**
	 * try to verify what happened to any unresolved posted Updates
	 * 
	 * @param ctx
	 * @param urlPattern
	 * @throws ParserConfigurationException
	 * @throws MalformedURLException 
	 * @throws FailedPostException
	 */
	public static int verifyUpdates(Context ctx, String urlPattern) throws  MalformedURLException, ParserConfigurationException, FailedPostException{
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		int count = 0, unresolvedCount = 0;
		try {
			Cursor cursor = dba.listAllUpdatesUnsent();
			while (cursor.moveToNext()) {
				count++;
				String localId = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL));
				String uuid = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.UUID_COL));
				URL url = new URL(String.format(urlPattern, uuid));
		        try {
		            if (!verifyUpdate(ctx, url, dba, localId)) {
		                unresolvedCount++;
		            }
		        } catch (FailedPostException e) {
		            //This is OK here, it just notes the status for the Update
		        }
			}
			cursor.close();
		}
		finally {
			dba.close();
		}
		Log.i(TAG, "Updates checked: " + count);
		return unresolvedCount;
	}
			
					

    /**
     * populates the country table in the db from a server URL
     * in the REST API
     * Typically the url will specify all countries.
     * 
     * @param ctx
     * @param url
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws FailedFetchException 
     */
    public Date fetchCountryListRestApiPaged(Context ctx, URL url) throws ParserConfigurationException, SAXException, IOException, FailedFetchException {
        Date serverDate = null;
        User user = SettingsUtil.getAuthUser(ctx);
        int total = 0;
        //the fetch is called in a loop to get it page by page, otherwise it would take too long for server
        //and it would not scale beyond 1000 updates in any case
        while (url != null) {
            HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
            h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
            int code = h.code();//evaluation starts the exchange
            if (code == 200) {
                String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
                serverDate = new Date(h.date());
                SAXParserFactory spf = SAXParserFactory.newInstance();
                XMLReader xr = spf.newSAXParser().getXMLReader();
                CountryRestListHandler xmlHandler = new CountryRestListHandler(new RsrDbAdapter(ctx), serverVersion);
                xr.setContentHandler(xmlHandler);
                /* Parse the xml-data from our URL. */
                xr.parse(new InputSource(h.stream()));
                /* Parsing has finished. */
                /* Check if anything went wrong. */
                err = xmlHandler.getError();
//                Log.i(TAG, "Fetched " + xmlHandler.getCount() + " updates; target total = "+ xmlHandler.getTotalCount());
                Log.i(TAG, "Fetched " + xmlHandler.getCount() + " countries");
                total += xmlHandler.getCount();
                if (xmlHandler.getNextUrl().length() == 0) { //string must to be trimmed from whitespace
                    url = null; //we are done
                } else {
                    try {
                        url = new URL(xmlHandler.getNextUrl());
                    }
                    catch (MalformedURLException e) {
                        url = null;
                    }
                }
    
            } else if (code == 404) {
                url = null;//we are done
            } else {
                //Vanilla case is 403 forbidden on an auth failure
                Log.e(TAG, "Fetch update list HTTP error code:" + code);
                Log.e(TAG, h.body());
                throw new FailedFetchException("Unexpected server response " + code);
            }
        }
        Log.i(TAG, "Grand total of " + total + " countries");
        return serverDate;
    }



    /**
     * populates the organisation table in the db from a server URL
     * in the REST API
     * Typically the url will specify all organisations.
     * 
     * @param ctx
     * @param dba
     * @param url
     * @param prog
     * @return
     * @throws FailedFetchException
     */
    public Date fetchOrgListRestApiPaged(Context ctx, RsrDbAdapter dba, URL url, ProgressReporter prog) throws FailedFetchException {
        Date serverDate = null;
        User user = SettingsUtil.getAuthUser(ctx);
        int runningTotal = 0;
        int page = 0;
        while (url != null) { //one page at a time
            HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
            h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
            int code = h.code(); //evaluation starts the exchange
            if (code == 200) {
                page++;
                String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
                serverDate = new Date(h.date());
                String jsonBody = h.body();
                ListJsonParser jp = new OrgListJsonParser(dba, serverVersion);
                /* Parse the JSON-data from our URL. */
                try {
                    jp.parse(jsonBody);
                }
                catch (JSONException e) {
                    throw new FailedFetchException("Invalid server response: " + e.getMessage());
                }
                /* Parsing has finished. */
                Log.d(TAG, "URL " + url.toString());
                Log.i(TAG, "Fetched " + jp.getCount() + " organisations; target total = "+ jp.getTotalCount());
                prog.sendUpdate(runningTotal, jp.getTotalCount());

                runningTotal += jp.getCount();
                if (jp.getNextUrl().length() == 0) { //string needs to be trimmed from whitespace
                    url = null;//we are done
                } else {
                    try {
                        url = new URL(jp.getNextUrl());//TODO is this URL-escaped? xml-escaped?
                    }
                    catch (MalformedURLException e) {
                        //TODO tell user?
                        url = null;
                    }
                }
    
            } else if (code == 404) {
                url = null;//we are done
            } else {
                //Vanilla case is 403 forbidden on an auth failure
                Log.e(TAG, "Fetch organisation list HTTP error code:" + code + ' ' + h.message());
                Log.e(TAG, h.body());
                throw new FailedFetchException("Unexpected server response: " + code + ' ' + h.message());
            }
        }
        Log.i(TAG, "Grand total of " + page + " pages, " + runningTotal + " Results");
        return serverDate;
        
    }



    /**
     * fetches one user to the db from a server URL
     * 
     * @param ctx
     * @param url
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws FailedFetchException 
     */
    public void fetchUser(Context ctx, RsrDbAdapter ad, URL url, String defaultId) throws ParserConfigurationException, SAXException, IOException, FailedFetchException {
        User user = SettingsUtil.getAuthUser(ctx);
        HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
        h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
        int code = h.code();//evaluation starts the exchange
        if (code == 200) {
        	String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
        	String jsonBody = h.body();
        	JsonParser up = new UserJsonParser(ad, serverVersion);
        	/* Parse the JSON-data from our URL. */
        	try {
        		up.parse(jsonBody);
             	}
        	catch (JSONException e) {
        		throw new FailedFetchException("Invalid server response: " + e.getMessage());
               	}
        }
    }

    
    /**
     * fetches one organisation to the db from a server URL
     * 
     * @param ctx
     * @param url
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws FailedFetchException 
     */
    public void fetchOrg(Context ctx, RsrDbAdapter ad, URL url, String defaultId) throws ParserConfigurationException, SAXException, IOException, FailedFetchException {
        User user = SettingsUtil.getAuthUser(ctx);
        HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
        h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
        int code = h.code();//evaluation starts the exchange
        if (code == 200) {
        	String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
        	String jsonBody = h.body();
        	JsonParser up = new OrgJsonParser(ad, serverVersion);
        	/* Parse the JSON-data from our URL. */
        	try {
        		up.parse(jsonBody);
             	}
        	catch (JSONException e) {
        		throw new FailedFetchException("Invalid server response: " + e.getMessage());
               	}
        }
    }


	/**
	 * fetches one file from a URL
	 * @param url
	 * @param file
	 */
	public static void httpGetToFile(URL url, File file) {
		HttpRequest.get(url).receive(file);		
	}


	/**
	 * reads content from a URL into a new file with a generated name
	 * @param url
	 * @param directory - directory filename (no final /)
	 * @param prefix
	 * @return
	 */
	public static String httpGetToNewFile(URL url, String directory, String prefix) {
		String extension = "";
		int i = url.getFile().lastIndexOf('.');
		if (i >= 0) {
			extension = url.getFile().substring((i));
		}
		File output = new File(directory + File.separator + prefix + System.nanoTime() + extension);
		httpGetToFile(url,output.getAbsoluteFile());
		return output.getAbsolutePath();
	}

	public abstract static interface ProgressReporter {
		public abstract void sendUpdate(int sofar, int total);
	}

	/**
	 * fetches all unfetched thumbnails and photos
	 * 
	 * @param ctx
	 * @param contextUrl
	 * @param directory - directory filename (no final /)
	 * @param prog
	 * @throws MalformedURLException
	 * 
	 * TODO this may take excessive time if list is long
	 * It could be made a preference, or if we sacrifice offline-usability
	 * fetch could be lazy until display, and do it in view adapter

	 */
	public void fetchMissingThumbnails(Context ctx, String contextUrl, String directory, ProgressReporter prog) throws MalformedURLException{
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		int count = 0, fetchCount = 0;
		int total = 0;
		try {
			URL curl = new URL(contextUrl);
			Cursor cursor = dba.listAllProjects();
			Cursor cursor2 = dba.listAllUpdates();
			total = cursor.getCount() + cursor2.getCount();
			while (cursor.moveToNext()) {
				count++;
				String id = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL));
				String fn = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
				String url = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_URL_COL));
				if (fn == null) {
					//not fetched yet
					if (url == null) {
						//Log.v(TAG, "Null image URL for update: "+id);
					} else try{
						fn = httpGetToNewFile(new URL(curl,url), directory, "prj" + id + "_");
						dba.updateProjectThumbnailFile(id,fn);	
						fetchCount++;
					} catch (Exception e) {
						//DialogUtil.errorAlert(ctx, "Error fetching proj image from URL " + url, e);
						Log.e(TAG, "FetchNewThumbnails p Error", e);
					}

				}
				prog.sendUpdate(count, total);
			}
			cursor.close();
			//get update photos

			while (cursor2.moveToNext()) {
				count++;
				String id = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.PK_ID_COL));
				String fn = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
				String url = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.THUMBNAIL_URL_COL));
				if (fn == null || ! new File(fn).exists()) {

					//not fetched yet, or deleted
					if (url == null) {
						//Log.v(TAG, "Null image URL for update: " + id);
					} else try {
						fn = httpGetToNewFile(new URL(curl,url), directory, "upd" + id + "_");
						dba.updateUpdateThumbnailFile(id,fn);						
						fetchCount++;
					} catch (Exception e) {
						//TODO only once??
						//DialogUtil.errorAlert(ctx, "Error fetching update image from URL " + url, e);
						Log.e(TAG, "FetchNewThumbnails u Error", e);
					}
				}
				prog.sendUpdate(count, total);
			}
			cursor2.close();
		} finally {
			dba.close();
		}
		Log.i(TAG, "Fetched " + fetchCount + " images");

	}


	private final static char SPC = '\u0020';

    /**
     * returns a string without control chars or XML syntax elements and with a maximum length
     * @param s
     * @param maxLength
     * @return
     */
    private static String oneLine(String s, int maxLength) {
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            String t;
            if (s.charAt(i) < SPC) {
                t = Character.toString(SPC);
            } else if (s.charAt(i) == '&') {
                t = "&amp;";
            } else if (s.charAt(i) == '<') {
                t = "&lt;";
            } else if (s.charAt(i) == '>') {
                t = "&gt;";
            } else if (s.charAt(i) > '~') {
                t = "&#"+String.valueOf((int)s.charAt(i))+";";
            } else t = Character.toString(s.charAt(i));
            //Would it make it overflow?
            if (result.length() + t.length() > maxLength) {
                return result;
            }
            result += t;
        }
        return result;
    }


    /**
     * returns a string without control chars or XML syntax elements
     * @param s
     * @return
     */
    private static String xmlQuote(String s) {
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            String t;
            if (s.charAt(i) == '&') {
                t = "&amp;";
            } else if (s.charAt(i) == '<') {
                t = "&lt;";
            } else if (s.charAt(i) == '>') {
                t = "&gt;";
            } else if (s.charAt(i) > '~') {
                t = "&#"+String.valueOf((int)s.charAt(i))+";";
            } else t = Character.toString(s.charAt(i));
            result += t;
        }
        return result;
    }


	
	/**
	 * Publishes an update to the server
	 *  
	 * @param urlTemplate
	 * @param update
	 * @param sendImage
     * @param user
     * @param prog
	 * 
	 * @return boolean
     * @throws FailedPostException 
	 * 
	 * There are three outcomes:
	 *   true      Success, we got the server id back and updated the "update" object
	 *   false     Unknown, server may or may not have got it. Verification will be necessary.
     *   exception Failure, we never got to send the whole thing
	 *   
	 * What to submit:
	<object>
	<update_method>M</update_method>
	<project>/api/v1/project/277/</project>
	<user>/api/v1/project/1/</user>
	<uuid>nn-nn-nn-nnnnnnn</uuid>
	<title>The Rain in Spain...</title>
	<photo_location>E</photo_location>
	<text>
	The rain in Spain stays mainly in the plain!

	By George, she's got it! By George, she's got it!
	</text>
	<photo type=\"hash\"><name>dummy.jpg</name><content_type>image/jpeg</content_type><file>AfDBGjhdfhjkjh==</file></photo>
	</object>

	 * To URL:
	/api/v1/project_update/?format=xml&api_key=62nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn96&username=uuuuuuuu

	 * As:  
	application/xml

	 */
	//TODO must return more info to user on failure! Probably should throw a fail exception and return false if unknown
	public static boolean postXmlUpdateStreaming(String urlTemplate, Update update, boolean sendImage, User user, String agent, ProgressReporter prog) throws FailedPostException {
		final String contentType = "application/xml";
		final String bodyTemplate1  =	"<root><update_method>M</update_method><project>%s</project>" + //Mobile update method
				"<photo_location>E</photo_location><uuid>%s</uuid><user>%s</user><title>%s</title>" +
				"<user_agent>%s</user_agent><text>%s</text>";
		final String bodyTemplate2  = "</root>";
//        final String imagePreamble  = "<photo type=\"hash\"><name>dummy.jpg</name><content_type>image/jpeg</content_type><file>";
        final String imagePreamble  = "<photo>";
//        final String imagePostamble = "</file></photo>";
        final String imagePostamble = "</photo>";
        final String imageCaptionTemplate = "<photo_caption>%s</photo_caption>";
        final String imageCreditTemplate = "<photo_credit>%s</photo_credit>";
        //Just long+lat for location. We do not currently do reverse geocoding in the app.
        //Location used to be mandatory, but that was changed in or before RSR 3.6
        final String locationTemplate = "<locations><list-item><longitude>%s</longitude><latitude>%s</latitude></list-item></locations>";
//        final String noLocation = "<locations></locations>"; //This works in JSON but not in XML
        final String noLocation = "";
        final boolean simulateUnresolvedPost = false;
        
        boolean allSent = false;
		try {
			URL url = new URL(String.format(Locale.US, urlTemplate
			        //, user.getApiKey(), user.getUsername()
			        ));
	
			String requestBody = String.format(Locale.US, bodyTemplate1,
			        update.getProjectId(), update.getUuid(), user.getId(),
					oneLine(update.getTitle(), 50),
					agent,
					xmlQuote(update.getText()));
	
			HttpRequest h = HttpRequest.post(url).contentType(contentType);//OutOfMemory here...
//	        h.connectTimeout(10000); //10 sec timeout
	        h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization

			h.readTimeout(READ_TIMEOUT_MS);
			h.send(requestBody);
	
			if (sendImage) {
				String fn = update.getThumbnailFilename();
				if (fn != null) {
					File f = new File (fn);
					if (f.exists()) {
						
						h.send(imagePreamble);
						RandomAccessFile raf = new RandomAccessFile(f, "r");
						try {
							//base64-convert the photo in chunks and stream them to server
							//use origin buffer size divisible by 3 so no padding is inserted in the middle
							final int bufferSize = 6 * 1024;
							final long fileSize = raf.length();
							final long wholeChunks = fileSize / bufferSize;
							byte[] rawBuf = new byte[bufferSize];
							for (long i = 0; i < wholeChunks; i++) {
								raf.readFully(rawBuf);
								byte[] b64buf = Base64.encodeBytesToBytes(rawBuf, 0, bufferSize);
								h.send(b64buf);
								//only send progress updates for image, which is almost all the payload
								if (prog != null) {
								    prog.sendUpdate((int)i, (int)wholeChunks);
								}
							}
							int n = raf.read(rawBuf); //read last piece
							byte[] b64buf = Base64.encodeBytesToBytes(rawBuf, 0, n);
							h.send(b64buf);
						} finally {
							raf.close();
						}
						h.send(imagePostamble);
						//any image metadata?
                        if (update.getPhotoCaption() != null) {
                            h.send(String.format(imageCaptionTemplate, oneLine(update.getPhotoCaption(), 75)));
                        }
                        if (update.getPhotoCredit() != null) {
                            h.send(String.format(imageCreditTemplate, oneLine(update.getPhotoCredit(), 25)));
                        }
					}
				}
			} //end image

			if (update.validLatLon()) {
                h.send(String.format(locationTemplate, update.getLongitude(), update.getLatitude()));
            } else {
                h.send(noLocation);
            }
			
			h.send(bodyTemplate2);
			allSent = true;
			
			int code = h.code(); //closes output
			if (simulateUnresolvedPost) return false;
			String msg = h.message();
			if (code == 201) { //Created
			    /* Get a SAXParser from the SAXPArserFactory. */
		        SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
		        /* Get the XMLReader of the SAXParser we created. */
		        XMLReader xr = sp.getXMLReader();
		        /* Create a new ContentHandler and apply it to the XML-Reader */
		        UpdateRestHandler updateHandler = new UpdateRestHandler();
		        xr.setContentHandler(updateHandler);
		        /* Parse the xml-data from our URL. */
		        xr.parse(new InputSource(h.stream()));
		        /* Parsing has finished. Check if anything went wrong. */
		        boolean err = updateHandler.getError();
		        if (err) {
		            Log.e(TAG, "Post parse error");
		            return false;
		        }
		        int count = updateHandler.getCount();
		        Log.v(TAG, "Post returned obj count: " + count);
		        if (count == 1) {  //1 is good
		            Update u = updateHandler.getLastUpdate(); //this is the result, db has not been changed
		            update.setId(u.getId());
		            //TODO copy other things? like country-id that may have been filled in by server?
		            return true; // Yes!
		        } else {
	                throw new FailedPostException("More than one Update with same UUID!"); //TODO: localize
		        }
			} else {
				String e = "Server rejected Update, code " + code + " " + msg; //TODO: localize
				String body = h.body(); //for debug
                throw new FailedPostException(e);
			}
		}
		catch (HttpRequestException e) { //connection problem
			if (allSent) {
				Log.w(TAG, "Unknown-result post", e);
				return false;
			} else {
				Log.w(TAG, "Failed post", e);
                throw new FailedPostException(e.getMessage());
			}
		}
		catch (MalformedURLException e) { //server string is bad or coding error
			Log.e(TAG, "Bad URL", e);
            throw new FailedPostException(e.getMessage());
		}
		catch (FileNotFoundException e) {
			Log.e(TAG, "Cannot find image file", e);
            throw new FailedPostException(e.getMessage());
		}
		catch (IOException e) {
			Log.e(TAG, "Cannot read image file", e);
            throw new FailedPostException(e.getMessage());
		}
		catch (SAXException e) {
            Log.e(TAG, "SAX parser error", e);
            throw new FailedPostException(e.getMessage());
        }
		catch (ParserConfigurationException e) {
            Log.e(TAG, "SAX parser config error", e);
            throw new FailedPostException(e.getMessage());
        }
	}


	/**
	 * Sends one update, verifying if status was indeterminate
	 * @param ctx
	 * @param urlTemplate
	 * @param sendImages
	 * @param user
	 * @throws FailedPostException 
	 * @throws UnresolvedPostException 
	 * @throws MalformedURLException 
	 * @throws ParserConfigurationException 
	 */
	static public void sendUpdate(Context ctx, String localId,
			String urlTemplate, String verifyUrlTemplate,
			boolean sendImages, User user,
			ProgressReporter prog)
					throws FailedPostException, UnresolvedPostException, MalformedURLException, ParserConfigurationException  {
		//Log.v(TAG, "Sending update " + localId);
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		try {
			Update upd = dba.findUpdate(localId);
			String userAgent;
			try {
			    //Not to be localized
			    userAgent = "Akvo RSR Up v" + ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName +
                        " on Android " + android.os.Build.VERSION.RELEASE +
                        " device " + android.os.Build.MANUFACTURER + 
                        " " + android.os.Build.MODEL;			
            } catch (NameNotFoundException e) {
                userAgent = "(not found)";
            }

			boolean resolved;
			try {
    			resolved = postXmlUpdateStreaming(urlTemplate, upd, sendImages, user, userAgent, prog);
                if (resolved) {  //remember new ID and status for this update
                    upd.setUnsent(false);//TODO: this fails if verifyUpdate worked
                    upd.setDraft(false);
                    dba.updateUpdateIdSent(upd, localId);
                }
            } catch (FailedPostException e) { //did not happen, user should try again
                upd.setUnsent(false);
                upd.setDraft(true);
                dba.updateUpdateIdSent(upd, localId); //remember status for this update
                throw e; //and tell user about it
            }
    
			if (!resolved) { //try to check on sts immediately
				URL url = new URL(String.format(verifyUrlTemplate, upd.getUuid()));
				resolved = verifyUpdate(ctx, url, dba, localId);
			}	

			if (resolved) {  //remember new ID and status for this update
				Log.i(TAG, "Sent update" + localId);
				return;
			} else {
			    //still unresolved, leave record in limbo state and retry later
			    throw new UnresolvedPostException("Update status unknown, needs verification");
            }

		} finally {
			dba.close();
		}
	}

	
	/**
	 * logs in to server and fetches API key
	 * @param url
	 * @param username
	 * @param password
	 * @return user if success, null on simple authorization failure
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws HttpRequestException
	 * @throws IOException
	 */
	static public User authorize(URL url, String username, String password) throws ParserConfigurationException, SAXException, HttpRequestException, IOException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("username", username);
		data.put("password", password);
		data.put("handles_unemployed", "True");
		
		HttpRequest h = HttpRequest.post(url).form(data).connectTimeout(10000); //10 sec timeout
		int code = h.code();
		if (code == 200) {
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			AuthHandler myAuthHandler = new AuthHandler();
			xr.setContentHandler(myAuthHandler);
			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(h.stream()));
			/* Parsing has finished. */

			Log.i(TAG, "Fetched API key");

			return myAuthHandler.getUser();
		} else {
			//Vanilla case is 403 forbidden on an auth failure
			//TODO raise exception if we get a 500
			Log.e(TAG, "Authorization HTTP error:" + code);
			String why = h.body();
			return null;
		}
	}

	
    /**
     * checks connectivity by asking Android.
     */
    public static boolean haveNetworkConnection(Context context, boolean wifiOnly) {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo[] infoArr = connMgr.getAllNetworkInfo();
            if (infoArr != null) {
                for (int i = 0; i < infoArr.length; i++) {
                    if (!wifiOnly) {
                        // if we don't care what KIND of
                        // connection we have, just that there is one
                        if (NetworkInfo.State.CONNECTED == infoArr[i].getState()) {
                            return true;
                        }
                    } else {
                        // if we only want to use wifi, we need to check the
                        // type
                        if (infoArr[i].getType() == ConnectivityManager.TYPE_WIFI
                                && NetworkInfo.State.CONNECTED == infoArr[i].getState()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
