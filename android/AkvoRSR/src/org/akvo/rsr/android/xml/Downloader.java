package org.akvo.rsr.android.xml;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.domain.Project;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.github.kevinsawicki.http.HttpRequest;

import android.content.Context;
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
	
		} catch (Exception e) {
			/* Display any Error to the GUI. */
			Log.e(TAG, "FetchProjectList Error", e);
		}
	}

	/* Populate the projects table in the db from a server URL
	 * 
	 */
	public void HttpGetToFile(String server, String localUrl, File file) {
		try {
			URL url = new URL(server + localUrl);
	
//			File output = new File("/output/request.out");
			HttpRequest.get(url).receive(file);		
		
		} catch (Exception e) {
			/* Display any Error to the GUI. */
			Log.e(TAG, "HttpGetToFile Error", e);
		}
	}

	/* 
	 * Read a URL into a new file with a generated name
	 */
	public String HttpGetToNewFile(String server, String localUrl, String directory) {
		File output = new File("error");
		try {
			URL url = new URL(server + localUrl);
			String extension = url.getFile().substring((url.getFile().lastIndexOf('.')));
			output = new File(directory + System.nanoTime() + extension);
			HttpGetToFile(server,localUrl,output.getAbsoluteFile());
		} catch (Exception e) {
			/* Display any Error to the GUI. */
			Log.e(TAG, "HttpGetToNewFile Error", e);
		}
		return output.getAbsolutePath();
	}
	
	//fetch all unfetched thumbnails
	//this may be excessive if list is long, we could be lazy until display, and do it in view adapter
	public void FetchNewThumbnails(Context ctx, String server, String localUrl, String directory){
		RsrDbAdapter dba = new RsrDbAdapter(ctx);
		dba.open();
		try {
			Cursor cursor = dba.findAllProjects();
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					String id = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL));
					String fn = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
					String tit = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.TITLE_COL));
					String url = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_URL_COL));
					if (fn == null) {
						//not fetched yet
						fn = HttpGetToNewFile(server, url, directory);
						//TODO can we use dba like this?
						Project p = new Project();
						p.setId(id);
						p.setTitle(tit);
						p.setThumbnail(fn);
						//TODO, will clear other fields...
						dba.saveProject(p);
						
						}
					cursor.moveToNext();
				}
			}
		} finally {
			dba.close();
		}
		
		
		
	}

}
