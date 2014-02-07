package org.akvo.rsr.android.util;

import org.akvo.rsr.android.domain.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsUtil {
	// String
    public static String Read(Context context, final String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(key, "");
    }
 
    public static void Write(Context context, final String key, final String value) {
          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
          SharedPreferences.Editor editor = settings.edit();
          editor.putString(key, value);
          editor.commit();        
    }
     
    // Boolean  
    public static boolean ReadBoolean(Context context, final String key, final boolean defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(key, defaultValue);
    }
 
    public static void WriteBoolean(Context context, final String key, final boolean value) {
          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
          SharedPreferences.Editor editor = settings.edit();
          editor.putBoolean(key, value);
          editor.commit();        
    }
    
    // Integer
    public static int ReadInt(Context context, final String key, final int defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(key, defaultValue);
    }
 
    public static void WriteInt(Context context, final String key, final int value) {
          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
          SharedPreferences.Editor editor = settings.edit();
          editor.putInt(key, value);
          editor.commit();        
    }

    //convenience method to read current data host
    public static String host(Context context) {
    	return Read(context,ConstantUtil.HOST_SETTING_KEY);
    }
   
    //TODO move auth keys to ConstantUtil
    public static void signOut(Context c) {
    	//destroy credentials
		SettingsUtil.Write(c, "authorized_username", "");
		SettingsUtil.Write(c, "authorized_userid",   "");
		SettingsUtil.Write(c, "authorized_orgid",    "");
		SettingsUtil.Write(c, "authorized_apikey",   "");
    }

   
    public static void signIn(Context c, User user) {
    	//save credentials
		SettingsUtil.Write(c, "authorized_username", user.getUsername());
		SettingsUtil.Write(c, "authorized_userid",   user.getId());
		SettingsUtil.Write(c, "authorized_orgid",    user.getOrgId());
		SettingsUtil.Write(c, "authorized_apikey",   user.getApiKey());
    }
    
    public static boolean haveCredentials(Context c) {
    	String u = SettingsUtil.Read(c, "authorized_username");
		String i = SettingsUtil.Read(c, "authorized_userid");
		String o = SettingsUtil.Read(c, "authorized_orgid");
		String k = SettingsUtil.Read(c, "authorized_apikey");
		return u != null && !u.equals("")
			&& i != null && !i.equals("")
			&& o != null && !o.equals("")
			&& k != null && !k.equals("");
    }

    public static User getAuthUser(Context c) {
		User user = new User();
		user.setUsername(SettingsUtil.Read(c, "authorized_username"));
		user.setId(SettingsUtil.Read(c, "authorized_userid"));
		user.setOrgId(SettingsUtil.Read(c, "authorized_orgid"));
		user.setApiKey(SettingsUtil.Read(c, "authorized_apikey"));
		return user;
    }


}
