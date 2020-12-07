/*
 *  Copyright (C) 2015 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.viewadapter;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.ThumbnailUtil;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OrgListCursorAdapter extends CursorAdapter{

/**
 * This adapter formats Project list items.
 * 
 * @author Stellan Lagerstroem
 * 
 */
    private final String TAG = "OrgListCursorAdapter";
	private boolean mDebug;
	
	public OrgListCursorAdapter(Context context, Cursor c) {
		super(context, c);
		mDebug = SettingsUtil.ReadBoolean(context, "setting_debug", false);
	}

	
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

        Long thisId = cursor.getLong(cursor.getColumnIndexOrThrow(RsrDbAdapter.PK_ID_COL));

        //UI elements
        TextView nameView = (TextView) view.findViewById(R.id.list_item_name);
        TextView longNameView = (TextView) view.findViewById(R.id.list_item_long_name);
        TextView cView = (TextView) view.findViewById(R.id.list_item_country);
        ImageView thumbnail = (ImageView) view.findViewById(R.id.list_item_thumbnail);

        //Text data
        String n = cursor.getString(cursor.getColumnIndexOrThrow(RsrDbAdapter.NAME_COL));
        if (mDebug) {
            n = "[" + thisId + "] " + n;
        }

        nameView.setText(n);
        longNameView.setText(cursor.getString(cursor.getColumnIndexOrThrow(RsrDbAdapter.LONG_NAME_COL)));
        cView.setText(cursor.getString(cursor.getColumnIndexOrThrow("country_name")));
				
		//Image
		String fn = cursor.getString(cursor.getColumnIndexOrThrow(RsrDbAdapter.LOGO_FN_COL));
		String url = cursor.getString(cursor.getColumnIndexOrThrow(RsrDbAdapter.LOGO_URL_COL));
		thumbnail.setImageResource(R.drawable.ic_menu_camera);;

		//		ThumbnailUtil.setPhotoFile(thumbnail, url, fn, projId, null, false);
		
		//set tag so we will know what got clicked
		view.setTag(R.id.org_id_tag, thisId);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.org_list_item, null);
		bindView(view, context, cursor);
		
		return view;
	}

}
