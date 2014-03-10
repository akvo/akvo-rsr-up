/*
 *  Copyright (C) 2012-2014 Stichting Akvo (Akvo Foundation)
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.Update;
import org.akvo.rsr.android.domain.User;
import org.akvo.rsr.android.util.ConstantUtil;
import org.akvo.rsr.android.util.FileUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.Base64;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class Downloader {

	private static final String TAG = "Downloader";

	public boolean err = false;
	
	private final static int READ_TIMEOUT_MS = 60000;
		
	public static class PostUnresolvedException extends Exception {
		public PostUnresolvedException(String string) {
			super(string);
		}

		private static final long serialVersionUID = -630304430323100535L;
	}

	public static class PostFailedException extends Exception {
		public PostFailedException(String string) {
			super(string);
		}

		private static final long serialVersionUID = -8091570663513780467L;
	}


	/**
	 * populates the projects table in the db from a server URL
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void fetchProjectList(Context ctx, URL url) throws ParserConfigurationException, SAXException, IOException {

		Log.i(TAG, "Fetching project list from " + url);

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader*/ 
		ProjectListHandler myProjectListHandler = new ProjectListHandler(new RsrDbAdapter(ctx));
		xr.setContentHandler(myProjectListHandler);
		/* Parse the xml-data from our URL. */
		//TODO THIS MIGHT HANG, no timeout defined...
		xr.parse(new InputSource(url.openStream()));
		/* Parsing has finished. */

		/* Check if anything went wrong. */
		err = myProjectListHandler.getError();

		Log.i(TAG, "Fetched " + myProjectListHandler.getCount() + " projects");
	}

	
	/**
	 * populates the projects table in the db from a server URL
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void enableAuthorizedProjects(Context ctx, URL url) throws ParserConfigurationException, SAXException, IOException {

		Log.i(TAG, "Fetching project list from " + url);

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader*/ 
		ProjectListHandler myProjectListHandler = new ProjectListHandler(new RsrDbAdapter(ctx));
		xr.setContentHandler(myProjectListHandler);
		/* Parse the xml-data from our URL. */
		//TODO THIS MIGHT HANG, no timeout defined...
		xr.parse(new InputSource(url.openStream()));
		/* Parsing has finished. */

		/* Check if anything went wrong. */
		err = myProjectListHandler.getError();

		Log.i(TAG, "Fetched " + myProjectListHandler.getCount() + " projects");
	}
	
	/**
	 * populates the updates table in the db from a server URL
	 * Typically the url will specify updates for a single project.
	 * 
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void fetchUpdateList(Context ctx, URL url) throws ParserConfigurationException, SAXException, IOException {

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader*/ 
		UpdateListHandler myUpdateListHandler = new UpdateListHandler(new RsrDbAdapter(ctx), true);
		xr.setContentHandler(myUpdateListHandler);
		/* Parse the xml-data from our URL. */
		xr.parse(new InputSource(url.openStream()));
		/* Parsing has finished. */

		/* Check if anything went wrong. */
		err = myUpdateListHandler.getError();
		Log.i(TAG, "Fetched " + myUpdateListHandler.getCount() + " updates");
	}


	/**
	 * Verify status at server of a single Update
	 * 
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 */
	public static int verifyUpdate(Context ctx, URL url, RsrDbAdapter dba, String localId) throws ParserConfigurationException {

		Log.v(TAG, "Verifying update " + localId);

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader*/ 
		UpdateListHandler myUpdateListHandler = new UpdateListHandler(dba, false);
		xr.setContentHandler(myUpdateListHandler);
		/* Parse the xml-data from our URL. */
		xr.parse(new InputSource(url.openStream()));
		/* Parsing has finished. */

		/* Check if anything went wrong. */
		boolean err = myUpdateListHandler.getError();
		if (err) {
			Log.e(TAG, "Verification error");
			return ConstantUtil.POST_UNKNOWN;
		}
		int count = myUpdateListHandler.getCount();
		Log.v(TAG, "Verification count: " + count);
		 //TODO, check that more than UUID matches?
		if (count == 1) {  //1 is good, Update present on server, just note that
			Update u = myUpdateListHandler.getLastUpdate(); //this is the result, db has not been changed
			u.setUnsent(false); //we are done
			u.setDraft(false); //published, not draft
			dba.updateUpdateVerifiedByUuid(u);
			return ConstantUtil.POST_SUCCESS;
		} else {
			if (count == 0) {  //0 is bad, update never made it to server, will need to be re-sent
				Update u = myUpdateListHandler.getLastUpdate(); //this is the result, db has not been changed
				u.setUnsent(false); //status is resolved
				u.setDraft(true); //go back to being draft
				dba.updateUpdateVerifiedByUuid(u);
			} else {
				Log.e(TAG, "more than one match for Update UUID!");
			}
			return ConstantUtil.POST_FAILURE;
		}
		} 
		catch (IOException e) {
			return ConstantUtil.POST_UNKNOWN;
					
		}
		catch (SAXException e) {
			return ConstantUtil.POST_UNKNOWN;
			
		}
		
	}

	
	/**
	 * try to verify what happened to any unresolved posted Updates
	 * 
	 * @param ctx
	 * @param urlPattern
	 * @throws ParserConfigurationException
	 * @throws MalformedURLException 
	 */
	public static int verifyUpdates(Context ctx, String urlPattern) throws  MalformedURLException, ParserConfigurationException{
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
				switch (verifyUpdate(ctx, url, dba, localId)) {
					case ConstantUtil.POST_SUCCESS:
						break;
					case ConstantUtil.POST_FAILURE:
						break;
					case ConstantUtil.POST_UNKNOWN:
						unresolvedCount++;
						break;
				}
				
			}
		}
		finally {
			dba.close();
		}
		Log.i(TAG, "Updates checked: " + count);
		return unresolvedCount;
	}
			
					
	/**
	 * populates the country table in the db from a server URL
	 * 
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void fetchCountryList(Context ctx, URL url) throws ParserConfigurationException, SAXException, IOException {

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader*/ 
		CountryListHandler myCountryListHandler = new CountryListHandler(new RsrDbAdapter(ctx));
		xr.setContentHandler(myCountryListHandler);
		/* Parse the xml-data from our URL. */
		xr.parse(new InputSource(url.openStream()));
		/* Parsing has finished. */

		/* Check if anything went wrong. */
		err = myCountryListHandler.getError();
		Log.i(TAG, "Fetched " + myCountryListHandler.getCount() + " countries");
	}


	/**
	 * populates the user table in the db from a server URL
	 * 
	 * @param ctx
	 * @param url
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void fetchUser(Context ctx, URL url, String defaultId) throws ParserConfigurationException, SAXException, IOException {

		/* Get a SAXParser from the SAXPArserFactory. */
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = sp.getXMLReader();
		/* Create a new ContentHandler and apply it to the XML-Reader*/ 
		UserListHandler myUserListHandler = new UserListHandler(new RsrDbAdapter(ctx), defaultId);
		xr.setContentHandler(myUserListHandler);
		/* Parse the xml-data from our URL. */
		xr.parse(new InputSource(url.openStream()));
		/* Parsing has finished. */

		/* Check if anything went wrong. */
		err = myUserListHandler.getError();
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
	public void fetchNewThumbnails(Context ctx, String contextUrl, String directory, ProgressReporter prog) throws MalformedURLException{
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
						Log.w(TAG, "Null image URL for update: "+id);
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
						Log.i(TAG, "Null image URL for update: " + id);
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


	/* 
	 * Publish an update, return true if success
	 *
	public boolean unusedPostUpdate(Context ctx, URL url, Update update) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("title", update.getTitle());
		data.put("text", update.getText());
		String projectPath = "/api/v1/project/" + update.getProjectId() + "/";//todo move to constantutil
		data.put("project", projectPath);

//		if (HttpRequest.post(url).form(data).created())
//			System.out.println("User was created");
		HttpRequest h = HttpRequest.post(url).form(data);
		int code = h.code();
		String s = h.body();//TODO: is this XML or just an ID?
		if (code != 201){
			DialogUtil.errorAlert(ctx, "Unable to post update", code + h.message());
			return false;
		}
		return true;
	}
	 */

	private final static char SPC = '\u0020';

	/**
	 * returns a string without newlines and with a maximum length
	 * @param s
	 * @param maxLength
	 * @return
	 */
	private static String oneLine(String s, int maxLength) {
		String result = "";
		for (int i = 0; i < Math.min(s.length(), maxLength); i++)
			if (s.charAt(i) < SPC) {
				result += SPC;
			} else {
				result += s.charAt(i);
			}
		return result;
	}


	
	/**
	 *  Publishes an update to the server
	 *  
	 * @param urlTemplate
	 * @param update
	 * @param sendImage
	 * @param user
	 * 
	 * @return int
	 * 
	 * There are three outcomes:
	 *   0  Success, we got the server id back
	 *   1  Failure, we never got to send the whole thing
	 *   2  Unknown, server may or may not have got it. Verification will be necessary.
	 *   
	 * What to submit:
	<object>
	<update_method>W</update_method>
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
	/api/v1/project_update/?format=xml&api_key=62a101a36893397300cbf62fbbf0debaa2818496&username=gabriel

	 * As:  
	application/xml

	 */
	public static int postXmlUpdateStreaming(String urlTemplate, Update update, boolean sendImage, User user) {
		final String contentType = "application/xml";
		final String bodyTemplate1  =	"<object><update_method>M</update_method><project>%s</project>" + //Mobile update method
				"<photo_location>E</photo_location><uuid>%s</uuid><user>%s</user><title>%s</title>" +
				"<text>%s</text>";
		final String bodyTemplate2  = "</object>";
		final String imagePreamble  = "<photo type=\"hash\"><name>dummy.jpg</name><content_type>image/jpeg</content_type><file>";
		final String imagePostamble = "</file></photo>";
		boolean allSent = false;
		try {
			URL url = new URL(String.format(Locale.US, urlTemplate, user.getApiKey(), user.getUsername()));
	
			//user and project references have to be in URL form
			String projectPath = String.format(Locale.US, ConstantUtil.PROJECT_PATH_PATTERN, update.getProjectId());
			String userPath = String.format(Locale.US, ConstantUtil.USER_PATH_PATTERN, user.getId());
	
			String requestBody = String.format(Locale.US, bodyTemplate1,
					projectPath, update.getUuid(), userPath,
					oneLine(update.getTitle(),50), //TODO: WHAT ABOUT XML?
					update.getText());
	
			HttpRequest h = HttpRequest.post(url).contentType(contentType);//OutOfMemory here...
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
							}
							int n = raf.read(rawBuf); //read last piece
							byte[] b64buf = Base64.encodeBytesToBytes(rawBuf, 0, n);
							h.send(b64buf);
						} finally {
							raf.close();
						}
						h.send(imagePostamble);
					}
				}
			}
			
			h.send(bodyTemplate2);
			allSent = true;
			
			int code = h.code(); //closes output
			String msg = h.message();
			String bod = h.body(); //On success, XML representation of created object
			if (code == 201) { //Created
				String idPath = h.header(HttpRequest.HEADER_LOCATION);//Path-ified ID
				int penSlash = idPath.lastIndexOf('/', idPath.length() - 2);
				String id = idPath.substring(penSlash + 1, idPath.length() - 1);
				update.setId(id);
				return ConstantUtil.POST_SUCCESS; //Yes!
			} else {
				String e = "Unable to post update, code " + code + " " +  msg;
				Log.e(TAG, e);
				Log.e(TAG, bod);
				return ConstantUtil.POST_FAILURE;
			}
		}
		catch (HttpRequestException e) { //connection problem
			if (allSent) {
				Log.w(TAG, "Unknown-result post", e);
				return ConstantUtil.POST_UNKNOWN;
			} else {
				Log.w(TAG, "Failed post", e);
				return ConstantUtil.POST_FAILURE;
			}
		}
		catch (MalformedURLException e) { //server string is bad or coding error
			Log.e(TAG, "Bad URL", e);
			return ConstantUtil.POST_FAILURE;
		}
		catch (FileNotFoundException e) {
			Log.e(TAG, "Cannot find image file", e);
			return ConstantUtil.POST_FAILURE;
		}
		catch (IOException e) {
			Log.e(TAG, "Cannot read image file", e);
			return ConstantUtil.POST_FAILURE;
		}
	}


	/**
	 * Sends one update, retrying if verification shows post failed.
	 * @param ctx
	 * @param urlTemplate
	 * @param sendImages
	 * @param user
	 * @throws PostFailedException 
	 * @throws PostUnresolvedException 
	 * @throws MalformedURLException 
	 * @throws ParserConfigurationException 
	 * @throws Exception
	 */
	static public void sendUpdate(Context ctx, String localId, String urlTemplate, boolean sendImages, User user) throws PostFailedException, PostUnresolvedException, MalformedURLException, ParserConfigurationException  {
		Log.i(TAG, "Sending update " + localId);
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		try {
			Update upd = dba.findUpdate(localId);
			
			int status = postXmlUpdateStreaming(urlTemplate, upd, sendImages, user);

			if (status == ConstantUtil.POST_UNKNOWN) { //try to check on sts immediately
				URL url = new URL(String.format(urlTemplate, upd.getUuid()));
				status = verifyUpdate(ctx, url, dba, localId);
			}	

			switch (status) {
				case ConstantUtil.POST_SUCCESS:
					upd.setUnsent(false);
					upd.setDraft(false);
					dba.updateUpdateIdSent(upd, localId); //remember new ID and status for this update
					Log.i(TAG, "Sent update" + localId);
					return;
				case ConstantUtil.POST_FAILURE:
					upd.setUnsent(false);
					//stays as draft
					dba.updateUpdateIdSent(upd, localId); //remember new ID and status for this update
					throw new PostFailedException("Could not post Update");
				case ConstantUtil.POST_UNKNOWN: //try to check sts immediately
					URL url = new URL(String.format(urlTemplate, upd.getUuid()));
					status = verifyUpdate(ctx, url, dba, localId);
					throw new PostUnresolvedException("Update status unknown, still trying");
			}
		} finally {
			dba.close();
		}
	}

	
	/**
	 * Sends all unsent updates - currently unused
	 * @param ctx
	 * @param urlTemplate
	 * @param sendImages
	 * @param user
	 * @throws Exception
	 */
	public void sendAllUnsentUpdates(Context ctx, String urlTemplate, boolean sendImages, User user) throws Exception {
		Log.i(TAG, "Sending all unsent updates");
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		int successCount = 0;
		int failCount = 0;
		int unknownCount = 0;
		try {
			Cursor cursor2 = dba.listAllUpdatesUnsent();
			if (cursor2 != null) {
				while (cursor2.moveToNext()) {
					String localId = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.PK_ID_COL));
					Update upd = dba.findUpdate(localId);
					
					switch (postXmlUpdateStreaming(urlTemplate, upd, sendImages, user)) {
					case ConstantUtil.POST_SUCCESS:
						upd.setUnsent(false);
						dba.updateUpdateIdSent(upd, localId); //remember new ID and status for this update
						successCount++;
						break;
					case ConstantUtil.POST_FAILURE:
						upd.setUnsent(false);
						dba.updateUpdateIdSent(upd, localId); //remember new ID and status for this update
						failCount++;
						break;
					case ConstantUtil.POST_UNKNOWN:
						//dba.updateUpdateIdSent(upd, localId); //no change in status for this update
						unknownCount++;
						break;						
					}
				}
				cursor2.close();
			}
		} finally {
			dba.close();
		}
		Log.i(TAG, "Sent " + successCount + " updates");
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



}
