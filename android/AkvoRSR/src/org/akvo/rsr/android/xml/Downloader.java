package org.akvo.rsr.android.xml;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;

public class Downloader {

	private static final String TAG = "Downloader";

	/* Create a URL we want to load some xml-data from. */
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
		
			/* Our ProjectListHandler now provides the parsed data to us. */
			boolean err = myProjectListHandler.getError();
	
		
		} catch (Exception e) {
			/* Display any Error to the GUI. */
			Log.e(TAG, "FetchProjectList Error", e);
		}
	}
}
