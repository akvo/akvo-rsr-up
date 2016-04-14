/*
 *  Copyright (C) 2012-2016 Stichting Akvo (Akvo Foundation)
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.json.CountryListJsonParser;
import org.akvo.rsr.up.json.BaseJsonParser;
import org.akvo.rsr.up.json.ListJsonParser;
import org.akvo.rsr.up.json.OrgJsonParser;
import org.akvo.rsr.up.json.OrgListJsonParser;
import org.akvo.rsr.up.json.OrgStreamingJsonParser;
import org.akvo.rsr.up.json.OrgTypeaheadJsonParser;
import org.akvo.rsr.up.json.ProjectResultListJsonParser;
import org.akvo.rsr.up.json.UserJsonParser;
import org.akvo.rsr.up.xml.ProjectExtraRestHandler;
import org.akvo.rsr.up.xml.UpdateRestListHandler;
import org.json.JSONException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.kevinsawicki.http.HttpRequest;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * @author stellanl@akvo.org
 *
 */
public class Downloader {

	private static final String TAG = "Downloader";

	public boolean err = false;
	
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
     * populates the country table in the db from a server URL
     * in the REST API
     * Typically the url will specify all countries.
     * 
     * @param ctx
     * @param url
     * @throws IOException
     * @throws FailedFetchException 
     */
    public Date fetchCountryListRestApiPaged(Context ctx, RsrDbAdapter dba, URL url) throws FailedFetchException {
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

                String jsonBody = h.body();
                ListJsonParser jp = new CountryListJsonParser(dba, serverVersion);
                /* Parse the JSON-data from our URL. */
                try {
                    jp.parse(jsonBody);
                }
                catch (JSONException e) {
                    throw new FailedFetchException("Invalid server response: " + e.getMessage());
                }

                Log.i(TAG, "Fetched " + jp.getCount() + " countries");
                total += jp.getCount();
                if (jp.getNextUrl().length() == 0) { //string must to be trimmed from whitespace
                    url = null; //we are done
                } else {
                    try {
                        url = new URL(jp.getNextUrl());
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
            Log.d(TAG, "URL " + url.toString());
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
     * populates the organisation table in the db from a server typeahead URL
     * in the REST API
     * 
     * @param ctx
     * @param dba
     * @param url
     * @param prog
     * @return
     * @throws FailedFetchException
     */
    public Date fetchTypeaheadOrgList(Context ctx, RsrDbAdapter dba, URL url, ProgressReporter prog) throws FailedFetchException {
        Date serverDate = null;
        User user = SettingsUtil.getAuthUser(ctx);
        Log.d(TAG, "URL " + url.toString());
        HttpRequest h = HttpRequest.get(url).connectTimeout(10000); //10 sec timeout
        h.header("Authorization", "Token " + user.getApiKey()); //This API actually needs no authorization
        int code = h.code(); //evaluation starts the exchange
        if (code == 200) {
            String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
            serverDate = new Date(h.date());
            OrgTypeaheadJsonParser jp = new OrgTypeaheadJsonParser(dba, serverVersion);
//            OrgStreamingJsonParser jp = new OrgStreamingJsonParser(dba, serverVersion); //tiny performance gain
            /* Parse the JSON-data from our URL. */
            try {
                jp.parse(h.body(), prog);//takes a long time
            }
            catch (JSONException e) {
//            catch (JsonParseException e) {
                throw new FailedFetchException("Invalid server response: " + e.getMessage());
            }
//            catch (IOException e) {
//                throw new FailedFetchException("IO error: " + e.getMessage());
//            }
            /* Parsing has finished. */
            Log.i(TAG, "Fetched " + jp.getCount() + " organisations; target total = "+ jp.getTotalCount());

        } else if (code == 404) {
            url = null;//we are done
        } else {
            //Vanilla case is 403 forbidden on an auth failure
            Log.e(TAG, "Fetch organisation list HTTP error code:" + code + ' ' + h.message());
            Log.e(TAG, h.body());
            throw new FailedFetchException("Unexpected server response: " + code + ' ' + h.message());
        }
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
        	BaseJsonParser up = new UserJsonParser(ad, serverVersion);
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
        	BaseJsonParser up = new OrgJsonParser(ad, serverVersion);
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
