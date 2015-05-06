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

import org.akvo.rsr.up.domain.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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

    public static int ReadInt(Context context, final String key, final int defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(key, defaultValue);
    }

    public static long ReadLong(Context context, final String key, final long defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getLong(key, defaultValue);
    }

    public static void WriteInt(Context context, final String key, final int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void WriteLong(Context context, final String key, final long value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    // convenience method to read current data host
    public static String host(Context context) {
        return Read(context, ConstantUtil.HOST_SETTING_KEY);
    }

    public static void signOut(Context c) {
        // destroy credentials
        SettingsUtil.Write(c, ConstantUtil.AUTH_USERNAME_KEY, "");
        SettingsUtil.Write(c, ConstantUtil.AUTH_USERID_KEY, "");
        SettingsUtil.Write(c, ConstantUtil.AUTH_ORGID_KEY, "");
        SettingsUtil.Write(c, ConstantUtil.AUTH_PROJID_KEY, "");
        SettingsUtil.Write(c, ConstantUtil.AUTH_APIKEY_KEY, "");
        SettingsUtil.WriteLong(c, ConstantUtil.FETCH_TIME_KEY, 0); // if we later log in as sbdy else, force fetching of all updates
    }

    public static void signIn(Context c, User user) {
        // save credentials
        SettingsUtil.Write(c, ConstantUtil.AUTH_USERNAME_KEY, user.getUsername());
        SettingsUtil.Write(c, ConstantUtil.AUTH_USERID_KEY, user.getId());
        SettingsUtil.Write(c, ConstantUtil.AUTH_ORGID_KEY, user.getOrgIdsString());// comma-separated list, possibly empty, never null
        SettingsUtil.Write(c, ConstantUtil.AUTH_PROJID_KEY, user.getPublishedProjIdsString());// comma-separated list, possibly empty, never null
        SettingsUtil.Write(c, ConstantUtil.AUTH_APIKEY_KEY, user.getApiKey());
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            int version = pInfo.versionCode;
            SettingsUtil.WriteInt(c, ConstantUtil.AUTH_APPVERSION_KEY, version);
        } catch (Exception e) {
        }

    }

    public static boolean haveCredentials(Context c) {
        String u = SettingsUtil.Read(c, ConstantUtil.AUTH_USERNAME_KEY);
        String i = SettingsUtil.Read(c, ConstantUtil.AUTH_USERID_KEY);
        String o = SettingsUtil.Read(c, ConstantUtil.AUTH_ORGID_KEY);
        String p = SettingsUtil.Read(c, ConstantUtil.AUTH_PROJID_KEY);
        String k = SettingsUtil.Read(c, ConstantUtil.AUTH_APIKEY_KEY);
        return u != null && !u.equals("") && i != null && !i.equals("") && o != null && p != null && k != null && !k.equals("");
    }

    public static User getAuthUser(Context c) {
        User user = new User();
        user.setUsername(SettingsUtil.Read(c, ConstantUtil.AUTH_USERNAME_KEY));
        user.setId(SettingsUtil.Read(c, ConstantUtil.AUTH_USERID_KEY));
        String idstr = SettingsUtil.Read(c, ConstantUtil.AUTH_ORGID_KEY);
        if (idstr != null) {
            String ids[] = idstr.split(",");
            for (String id : ids)
                if (id.length() > 0)
                    user.addOrgId(id);
        }
        String projstr = SettingsUtil.Read(c, ConstantUtil.AUTH_PROJID_KEY);
        if (projstr != null) {
            String ids[] = projstr.split(",");
            for (String id : ids)
                if (id.length() > 0)
                    user.addPublishedProjId(id);
        }
        user.setApiKey(SettingsUtil.Read(c, ConstantUtil.AUTH_APIKEY_KEY));
        return user;
    }

}
