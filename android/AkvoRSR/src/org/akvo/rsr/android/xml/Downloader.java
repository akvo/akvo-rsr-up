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
import java.io.IOException;
import java.io.OutputStreamWriter;
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
	
	private final int READ_TIMEOUT_MS = 60000;

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
		UpdateListHandler myUpdateListHandler = new UpdateListHandler(new RsrDbAdapter(ctx));
		xr.setContentHandler(myUpdateListHandler);
		/* Parse the xml-data from our URL. */
		xr.parse(new InputSource(url.openStream()));
		/* Parsing has finished. */

		/* Check if anything went wrong. */
		err = myUpdateListHandler.getError();
		Log.i(TAG, "Fetched " + myUpdateListHandler.getCount() + " updates");
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
	 * fetches one file from a URL
	 * @param url
	 * @param file
	 */
	public void httpGetToFile(URL url, File file) {
		HttpRequest.get(url).receive(file);		
	}


	/**
	 * reads content from a URL into a new file with a generated name
	 * @param url
	 * @param directory - directory filename (no final /)
	 * @param prefix
	 * @return
	 */
	public String httpGetToNewFile(URL url, String directory, String prefix) {
		String extension = null;
		int i = url.getFile().lastIndexOf('.');
		if (i >= 0) {
			extension = url.getFile().substring((url.getFile().lastIndexOf('.')));
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
						fn = httpGetToNewFile(new URL(curl,url), directory, "upd"+id+"_");
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

	private final char SPC = '\u0020';

	/**
	 * returns a string without newlines and with a maximum length
	 * @param s
	 * @param maxLength
	 * @return
	 */
	private String oneLine(String s, int maxLength) {
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
	 *  Publishes an update, returns true if success
	 * @param urlTemplate
	 * @param update
	 * @param sendImage
	 * @param u
	 * @throws Exception

	 * What to submit:
	 * 
	<object>
	<update_method>W</update_method>
	<project>/api/v1/project/277/</project>
	<user>/api/v1/project/1/</user>
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
	public void postXmlUpdate(String urlTemplate, Update update, boolean sendImage, User u) throws Exception {
		final String contentType = "application/xml";
		final String bodyTemplate1 =	"<object><update_method>S</update_method><project>%s</project>" +
				"<photo_location>E</photo_location><user>%s</user><title>%s</title>" +
				"<text>%s</text>";
		final String bodyTemplate2 = "</object>";
		final String imagePreamble =	 "<photo type=\"hash\"><name>dummy.jpg</name><content_type>image/jpeg</content_type><file>";
		final String imagePostamble = "</file></photo>";
		URL url;
		//		try {
		url = new URL(String.format(Locale.US, urlTemplate, u.getApiKey(), u.getUsername()));
		//		} catch (MalformedURLException e1) {
		//			Log.e(TAG, "Unable to make post URL:",e1);
		//			return false;
		//		}
		String projectPath = "/api/v1/project/" + update.getProjectId() + "/";//TODO move to constantutil
		String userPath = "/api/v1/user/" + u.getId() + "/";

		String requestBody = String.format(Locale.US, bodyTemplate1,
				projectPath, userPath,
				oneLine(update.getTitle(),50), //TODO: WHAT ABOUT XML?
				update.getText());

		HttpRequest h = HttpRequest.post(url).contentType(contentType);
		h.readTimeout(READ_TIMEOUT_MS);
		h.send(requestBody);//OutOfMemory here...
		if (sendImage) {
			String fn = update.getThumbnailFilename();
			if (fn != null) {
				File f = new File (fn);
				if (f.exists()) {
					byte[] barr;
					try {
						barr = FileUtil.readFile(f);
						h.send(imagePreamble);
						h.send(Base64.encodeBytes(barr));
						barr = null; //release memory
						h.send(imagePostamble);
					} catch (IOException e) {
						Log.e(TAG, "Image encoding problem", e);
					}
				}
			}
		}
		h.send(bodyTemplate2);//terminate object
		int code = h.code();
		String msg = h.message();
		String b = h.body(); //On success, XML representation of created object
		if (code == 201) {//Created
			update.setUnsent(false);
			String idPath = h.header(HttpRequest.HEADER_LOCATION);//Path-ified ID
			int penSlash = idPath.lastIndexOf('/', idPath.length() - 2);
			String id = idPath.substring(penSlash + 1, idPath.length() - 1);
			update.setId(id);
		} else {
			String e = "Unable to post update, code " + code + " " +  msg;
			Log.e(TAG, e);
			Log.e(TAG, b);
			throw new Exception(e);
		}
	}

	/**
	 *  Publishes an update, returns true if success
	 * @param urlTemplate
	 * @param update
	 * @param sendImage
	 * @param user
	 * @throws Exception
	 */
	public void postXmlUpdateStreaming(String urlTemplate, Update update, boolean sendImage, User user) throws Exception {
		final String contentType = "application/xml";
//		final String bodyTemplate1  =	"<object><update_method>S</update_method><project>%s</project>" + //SMS update method
		final String bodyTemplate1  =	"<object><update_method>M</update_method><project>%s</project>" + //New! Mobile update method
				"<photo_location>E</photo_location><user>%s</user><title>%s</title>" +
				"<text>%s</text>";
		final String bodyTemplate2  = "</object>";
		final String imagePreamble  = "<photo type=\"hash\"><name>dummy.jpg</name><content_type>image/jpeg</content_type><file>";
		final String imagePostamble = "</file></photo>";
		URL url;
		url = new URL(String.format(Locale.US, urlTemplate, user.getApiKey(), user.getUsername()));

		//user and project references have to be in URL form
		String projectPath = String.format(Locale.US, ConstantUtil.PROJECT_PATH_PATTERN, update.getProjectId());
		String userPath = String.format(Locale.US, ConstantUtil.USER_PATH_PATTERN, user.getId());

		String requestBody = String.format(Locale.US, bodyTemplate1,
				projectPath, userPath,
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
					
					RandomAccessFile raf = new RandomAccessFile(f, "r");
					h.send(imagePreamble);
					//base64-convert the photo in chunks and stream them to server
					//use origin buffer size divisible by 3 so no padding is inserted in the middle
					final int bufSiz = 6 * 1024;
					final long fsize = raf.length();
					final int wholeChunks = (int) (fsize / bufSiz);
					byte[] rawBuf = new byte[bufSiz];
					try {
						for (int i = 0; i < wholeChunks; i++) {
							raf.readFully(rawBuf);
							byte[] b64buf = Base64.encodeBytesToBytes(rawBuf,0,bufSiz);
							h.send(b64buf);
						}
						int n = raf.read(rawBuf); //read last piece
						byte[] b64buf = Base64.encodeBytesToBytes(rawBuf,0,n);
						h.send(b64buf);
					} catch (IOException e) {
						Log.e(TAG, "Image encoding problem", e);
					}
					h.send(imagePostamble);
				}
			}
		}
		
		h.send(bodyTemplate2);
		
		int code = h.code();//closes output
		String msg = h.message();
		String b = h.body(); //On success, XML representation of created object
		if (code == 201) {//Created
			update.setUnsent(false);
			String idPath = h.header(HttpRequest.HEADER_LOCATION);//Path-ified ID
			int penSlash = idPath.lastIndexOf('/', idPath.length() - 2);
			String id = idPath.substring(penSlash + 1, idPath.length() - 1);
			update.setId(id);
		} else {
			String e = "Unable to post update, code " + code + " " +  msg;
			Log.e(TAG, e);
			Log.e(TAG, b);
			throw new Exception(e);
		}
	}


	/**
	 * Sends all unsent updates
	 * @param ctx
	 * @param urlTemplate
	 * @param sendImages
	 * @param user
	 * @throws Exception
	 */
	public void sendUnsentUpdates(Context ctx, String urlTemplate, boolean sendImages, User user) throws Exception {
		Log.i(TAG, "Sending unsent updates");
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		int count = 0;
		try {
			Cursor cursor2 = dba.listAllUpdatesUnsent();
			if (cursor2 != null) {
				while (cursor2.moveToNext()) {
					String id = cursor2.getString(cursor2.getColumnIndex(RsrDbAdapter.PK_ID_COL));
					Update upd = dba.findUpdate(id);
//					postXmlUpdate(urlTemplate, upd, sendImages, user);
					postXmlUpdateStreaming(urlTemplate, upd, sendImages, user);
					dba.updateUpdateIdSent(upd, id); //remember new ID and status for this update
					count++;
				}
				cursor2.close();
			}
		} finally {
			dba.close();
		}
		Log.i(TAG, "Sent " + count + " updates");
	}


	/**
	 * logs in to server and fetches API key
	 * @param url
	 * @param username
	 * @param password
	 * @param user
	 * @return true if success
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws HttpRequestException
	 * @throws IOException
	 */
	public boolean authorize(URL url, String username, String password, User user) throws ParserConfigurationException, SAXException, HttpRequestException, IOException {
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
			user.setUsername(username);
			user.setId(myAuthHandler.getUserId());
			user.setOrgId(myAuthHandler.getOrgId());
			user.setApiKey(myAuthHandler.getApiKey());

			Log.i(TAG, "Fetched API key");

			return true;
		} else {
			Log.e(TAG, "Authorization HTTP error:" + code);
			return false;
		}
	}



}
