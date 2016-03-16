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

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.domain.IndicatorPeriodData;
import org.akvo.rsr.up.json.IndicatorPeriodDataJsonParser;
import org.akvo.rsr.up.util.Downloader.ProgressReporter;
import org.akvo.rsr.up.xml.AuthHandler;
import org.akvo.rsr.up.xml.UpdateRestHandler;
import org.akvo.rsr.up.xml.UpdateRestListHandler;
import org.json.JSONException;
import org.json.JSONObject;
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
public class Uploader {

	private static final String TAG = "Uploader";

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
	
			HttpRequest h = HttpRequest.post(url).contentType(ConstantUtil.xmlContent);//OutOfMemory here...
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
     * Publishes an indicator period data to the server; creating it in local db _if_ sucessful
     *  
     * @param urlTemplate
     * @param data
     * @param user
     * @param prog
     * 
     * @return id
     * @throws FailedPostException ??
     * 
     * There are three outcomes:
     *   true      Success, we got the server id back and updated the "update" object
     *   exception Failure, we never got to send the whole thing
     *   
     * What to submit:

    {
            "user_details": {
                "id": 1589, 
                "first_name": "Kasper", 
                "last_name": "Brandt", 
                "approved_organisations": []
            }, 
            "status_display": "Draft", 
            "photo_url": "", 
            "file_url": "", 
            !"id": 12, --not posted
            !"created_at": "2016-02-10T12:44:34.785015", 
            !"last_modified_at": "2016-02-10T12:44:34.785073", 
            "period": 8156, 
            "user": 1589, 
            "relative_data": true, --default
            "data": "25.00", 
            "period_actual_value": "", 
            "status": "D", --Defaults to "N"-new
            "text": "", --? a comment?
            "photo": "", 
            "file": "", 
            "update_method": "M" --Mobile; defaults to W(Web)
        }

     * To URL:
    http://rsr.test.akvo.org/rest/v1/indicator_period_data/

     * As:  
    application/json

     */
    //send the PeriodData
    public static int postIndicatorPeriodData(RsrDbAdapter dba,
            String mainUrl,
            int period,
            String data,
            String description,
            String actualValue,
            boolean relativeData,
            User user,
            String agent,
            ProgressReporter prog) throws FailedPostException {
                        
        JSONObject ipd = new JSONObject();
        try {
            ipd.put("period", period);
            ipd.put("user", Integer.parseInt(user.getId()));
            ipd.put("data", data);
            ipd.put("text", description);
            ipd.put("relative_data", relativeData);
            ipd.put("period_actual_value", actualValue);
            ipd.put("update_method", ConstantUtil.UPDATE_METHOD_MOBILE);
        
            URL url1 = new URL(mainUrl);
    
            String requestBody = ipd.toString();
    
            HttpRequest h = HttpRequest.post(url1).contentType(ConstantUtil.jsonContent);
//          h.connectTimeout(10000); //10 sec timeout
            
            h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization

            h.readTimeout(READ_TIMEOUT_MS);
            h.send(requestBody);
    

            int code = h.code(); //closes output
            String msg = h.message();
            String body = h.body(); //simplifies debugging
            if (code == 201) { //Created
                String serverVersion = h.header(ConstantUtil.SERVER_VERSION_HEADER);
                IndicatorPeriodDataJsonParser ipdp = new IndicatorPeriodDataJsonParser(dba, serverVersion);
                ipdp.parse(body);//saves to DB
                IndicatorPeriodData ipd2 = ipdp.getPeriodData();
                
                if (ipd2 != null) {
                    int ipd_id = Integer.parseInt(ipd2.getId());
                    return ipd_id; // Yes!
                } else {
                    throw new FailedPostException("IPD id not returned");
                }
            } else {
                String e = "Server rejected IPD, code " + code + " " + msg; //TODO: localize
                throw new FailedPostException(e);
            }
        }
        catch (HttpRequestException e) { //connection problem
            Log.w(TAG, "Failed post", e);
            throw new FailedPostException(e.getMessage());
        }
        catch (MalformedURLException e) { //server string is bad or coding error
            Log.e(TAG, "Bad URL", e);
            throw new FailedPostException(e.getMessage());
        }
        catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            throw new FailedPostException(e.getMessage());
        }
    }


    /**
     * Adds a photo to an indicator period data; update the local ipd _if_ sucessful
     *  
     * @param url
     * @param type
     * @param filename
     * 
     * @throws FailedPostException
     *   
     * What to submit:

------WebKitFormBoundaryGxbP7rzSaR8xMRX5
Content-Disposition: form-data; name="file"; filename="org logo.jpg"
Content-Type: image/jpeg


------WebKitFormBoundaryGxbP7rzSaR8xMRX5
Content-Disposition: form-data; name="type"

photo                                       or file
------WebKitFormBoundaryGxbP7rzSaR8xMRX5--

    {
    "type":"photo", or
    "type":"file",
    "file": "7A654A827A56987243659A87436589A7546"
    }

     * To URL:
    http://rsr.test.akvo.org/rest/v1/indicator_period_data/

     * As:  
    application/json
     */

    public static void postIndicatorPeriodDataAttachment(RsrDbAdapter dba, String url, User user, String type, String filename, ProgressReporter prog) throws FailedPostException {
        //TODO: if big, a streamed approach would be better
        String body = "";
        try {
            /*        
            File f = new File(filename);
            if (f.exists()) {
                RandomAccessFile raf = new RandomAccessFile(f, "r");
                try {
                    //base64-convert the attachment
                    //use origin buffer size divisible by 3 so no padding is inserted in the middle
                    final int fileSize = (int) raf.length(); //unlikely to be longer than 4GB
                    byte[] rawBuf = new byte[fileSize];
                    raf.readFully(rawBuf);
                    body = Base64.encodeBytes(rawBuf, 0, fileSize);
                } finally {
                    raf.close();
                }
            } else {
                throw new FailedPostException("IPD attachment file does not exist");
            }
*/
            Map<String, String> data = new HashMap<String, String>();
//            data.put("type", type);
//            data.put("file", body);
            URL url1 = new URL(url);

            HttpRequest h = HttpRequest.post(url1);
            h.header("Authorization", "Token " + user.getApiKey()); //This API needs authorization
            h.readTimeout(READ_TIMEOUT_MS);
            h.part("type", type);
            h.part("file", "image.jpg", "image/jpeg", new File(filename));            
    
            int code = h.code(); //closes output
            String msg = h.message();
            String abody = h.body(); //simplifies debugging
            if (code == 200) { //Saved
                return; // Yes! 
            } else {
                String e = "Server rejected IPD attachment, code " + code + " " + msg;
                throw new FailedPostException(e);
            }
        }
        catch (HttpRequestException e) { //connection problem
            Log.w(TAG, "Failed post", e);
            throw new FailedPostException(e.getMessage());
        }
        catch (MalformedURLException e) { //server string is bad or coding error
            Log.e(TAG, "Bad URL", e);
            throw new FailedPostException(e.getMessage());
        }
//        catch (FileNotFoundException e) {
//            Log.e(TAG, "Cannot find image file", e);
//            throw new FailedPostException(e.getMessage());
//        }
        catch (IOException e) {
            Log.e(TAG, "Cannot read image file", e);
            throw new FailedPostException(e.getMessage());
        }
    }

    
    /**
     * Sends one indicatordata item; not created in the database until it is reported as created by the server
     * @param ctx
     * @param localId
     * @param mainUrl
     * @param attachmentUrlTemplate
     * @param periodId
     * @param photoFn
     * @param fileFn
     * @param user
     * @param prog
     * @throws FailedPostException
     * @throws UnresolvedPostException
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     */
    static public void sendIndicatorPeriodData(Context ctx,
            String mainUrl,
            String attachmentUrlTemplate,
            int periodId,
            String data,
            String actualValue,
            boolean relativeData,
            String description,
            String photoFn,
            String fileFn,
            User user,
            ProgressReporter prog)
                    throws FailedPostException, UnresolvedPostException, MalformedURLException, ParserConfigurationException  {
        Log.v(TAG, "Sending indicator period data for " + periodId);
        RsrDbAdapter dba = new RsrDbAdapter(ctx);
        dba.open();
        try {
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

            try {
                int ipd_id = postIndicatorPeriodData(dba, SettingsUtil.host(ctx)+mainUrl, periodId, data, description, actualValue, relativeData, user, userAgent, null);
                if (photoFn != null && photoFn != "") {
                    postIndicatorPeriodDataAttachment(dba, SettingsUtil.host(ctx)+String.format(attachmentUrlTemplate,ipd_id), user, "photo", photoFn, null);
                  }
                if (fileFn != null && fileFn != "") {
                    postIndicatorPeriodDataAttachment(dba, SettingsUtil.host(ctx)+String.format(attachmentUrlTemplate,ipd_id), user, "file", fileFn, null);
                  }
            } catch (FailedPostException e) { //did not happen, user should try again
                throw e; //and tell user about it
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
