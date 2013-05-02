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

package org.akvo.rsr.android.xml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.Project;
import org.akvo.rsr.android.domain.Update;
import org.akvo.rsr.android.util.DialogUtil;
import org.apache.http.HttpResponse;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;

public class Downloader {

	private static final String TAG = "Downloader";

	
	/* Populate the projects table in the db from a server URL
	 * 
	 */
	public void FetchProjectList(Context ctx, String server, String localUrl) {
		try {
			URL url = new URL(server + localUrl);
		
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
		
			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			ProjectListHandler myProjectListHandler = new ProjectListHandler(new RsrDbAdapter(ctx));
			xr.setContentHandler(myProjectListHandler);
			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */
		
			/* Check if anything went wrong. */
			boolean err = myProjectListHandler.getError();

			Log.i(TAG, "Fetched "+myProjectListHandler.getCount()+" projects");

		} catch (Exception e) {
			/* Display any Error to the GUI. */
			DialogUtil.errorAlert(ctx, "Error fetching project list", e);
			Log.e(TAG, "FetchProjectList Error", e);
		}
	}

	
	/* Populate the updates table in the db from a server URL
	 * 
	 */
	public void FetchUpdateList(Context ctx, String server, String localUrl) {
		try {
			URL url = new URL(server + localUrl);
		
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
		
			/* Get the XMLReader of the SAXParser we created. */
			XMLReader xr = sp.getXMLReader();
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			UpdateListHandler myUpdateListHandler = new UpdateListHandler(new RsrDbAdapter(ctx));
			xr.setContentHandler(myUpdateListHandler);
			/* Parse the xml-data from our URL. */
			xr.parse(new InputSource(url.openStream()));
			/* Parsing has finished. */
		
			/* Check if anything went wrong. */
			boolean err = myUpdateListHandler.getError();
			Log.i(TAG, "Fetched "+myUpdateListHandler.getCount()+" updates");
	
		} catch (Exception e) {
			/* Display any Error to the GUI. */
			DialogUtil.errorAlert(ctx, "Error fetching update list", e);
			Log.e(TAG, "FetchProjectList Error", e);
		}
	}

	
	/* 
	 * Fetch one file from a URL
	 */
	public void HttpGetToFile(URL url, File file) {
		HttpRequest.get(url).receive(file);		
	}

	
	/* 
	 * Read a URL into a new file with a generated name
	 */
	public String HttpGetToNewFile(URL url, String directory) {
		String extension = null;
		int i = url.getFile().lastIndexOf('.');
		if (i >= 0) {
			extension = url.getFile().substring((url.getFile().lastIndexOf('.')));
		}
		File output = new File(directory + System.nanoTime() + extension);
		HttpGetToFile(url,output.getAbsoluteFile());
		return output.getAbsolutePath();
	}
	

	//fetch all unfetched thumbnails and photos
	//this may be excessive if list is long, we could be lazy until display, and do it in view adapter
	public void FetchNewThumbnails(Context ctx, String contextUrl, String directory) throws MalformedURLException{
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		int count = 0;
		try {
				URL curl = new URL(contextUrl);
				Cursor cursor = dba.listAllProjects();
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						String id = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL));
						String fn = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
						String url = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_URL_COL));
						if (fn == null) {
							//not fetched yet
							if (url == null) {
								Log.w(TAG, "Null image URL for update: "+id);
							} else try{
								fn = HttpGetToNewFile(new URL(curl,url), directory);
								dba.updateProjectThumbnailFile(id,fn);	
								count++;
							} catch (Exception e) {
								/* Display any Error to the GUI. */
								DialogUtil.errorAlert(ctx, "Error fetching proj image from URL " + url, e);
								Log.e(TAG, "FetchNewThumbnails p Error", e);
							}

						}
						cursor.moveToNext();
					}
					cursor.close();
				}
				//get update photos
				Cursor cursor2 = dba.listAllUpdates();
				if (cursor2 != null) {
					cursor2.moveToFirst();
					while (!cursor2.isAfterLast()) {
						String id = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.PK_ID_COL));
						String fn = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
						String url = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.THUMBNAIL_URL_COL));
						if (fn == null) {
							//not fetched yet
							if (url == null) {
								Log.w(TAG, "Null image URL for update: " + id);
							} else try{
								fn = HttpGetToNewFile(new URL(curl,url), directory);
								dba.updateUpdateThumbnailFile(id,fn);						
								count++;
							} catch (Exception e) {
								/* Display any Error to the GUI. */
								//TODO only once??
								DialogUtil.errorAlert(ctx, "Error fetching update image from URL " + url, e);
								Log.e(TAG, "FetchNewThumbnails u Error", e);
							}

							}
						cursor2.moveToNext();
					}
					cursor2.close();
				}
		} finally {
			dba.close();
		}
		Log.i(TAG, "Fetched " + count + " images");
		
	}

	
	/* 
	 * Publish an update, return new ID
	 */
	public String PostUpdate(Context ctx, URL url, Update update) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("title", update.getTitle());
		data.put("text", update.getText());
//		if (HttpRequest.post(url).form(data).created())
//			System.out.println("User was created");
		HttpRequest h = HttpRequest.post(url).form(data);
		int code = h.code();
		String s = h.body();//TODO: is this XML or just an ID?
		if (code != 200){
			DialogUtil.errorAlert(ctx, "Unable to post update", code + h.message());
		}
		return s;
	}
	
	
}
