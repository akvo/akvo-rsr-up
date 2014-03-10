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

package org.akvo.rsr.android.view.adapter;

import java.io.File;
import java.util.Date;

import org.akvo.rsr.android.R;
import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.FileUtil;
import org.akvo.rsr.android.util.SettingsUtil;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UpdateListCursorAdapter extends CursorAdapter{

/**
 * This adaptor formats Update list items using the update_list_item.xml template
 * 
 * @author Stellan Lagerstroem
 * 
 */
	
	private boolean debug = false;
	private java.text.DateFormat dfmt;
	private int idcol, titleCol, draftCol, unsentCol;
	
	public UpdateListCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		debug = SettingsUtil.ReadBoolean(context, "setting_debug", false);
		dfmt = DateFormat.getDateFormat(context);
		idcol = cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL);
		titleCol = cursor.getColumnIndex(RsrDbAdapter.TITLE_COL);
		draftCol = cursor.getColumnIndex(RsrDbAdapter.DRAFT_COL);
		unsentCol = cursor.getColumnIndex(RsrDbAdapter.UNSENT_COL);
	}

	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		//Text data
		TextView titleView = (TextView) view.findViewById(R.id.ulist_item_title);
		if (debug) {
			titleView.setText("["+ cursor.getString(idcol) + "] "+
					cursor.getString(titleCol));
		} else {
			titleView.setText(cursor.getString(titleCol));
		}

		TextView dateView = (TextView) view.findViewById(R.id.ulist_item_date);
		long s = cursor.getLong(cursor.getColumnIndex(RsrDbAdapter.CREATED_COL));
		Date d = new Date(1000 * s);
		dateView.setText(dfmt.format(d));
		TextView stateView = (TextView) view.findViewById(R.id.ulist_item_state);

		if (0 != cursor.getInt(unsentCol)) {
			//Show synchronising updates as red with a "Synchronising" label
			view.setBackgroundColor(context.getResources().getColor(R.color.red));
			stateView.setVisibility(View.VISIBLE);
			stateView.setText(R.string.state_synchronising);
		} else {
			if (0 != cursor.getInt(draftCol)) {
				//Show draft updates as pink with a red label "Draft"
				view.setBackgroundColor(context.getResources().getColor(R.color.pink));
				stateView.setVisibility(View.VISIBLE);
				stateView.setText(R.string.state_draft);
				stateView.setTextColor(context.getResources().getColor(R.color.red));
			} else {
				//Published updates are on white, no state label
				view.setBackgroundColor(Color.WHITE);
				stateView.setVisibility(View.GONE);
			}
		}		
			
		//Image
		ImageView thumbnail = (ImageView) view.findViewById(R.id.ulist_item_thumbnail);
		//Find file containing thumbnail
		String fn = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
		String url = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_URL_COL));
		FileUtil.setPhotoFile(thumbnail, url, fn, null, cursor.getString(idcol));

		//set tags so we will know what got clicked
		view.setTag(R.id.project_id_tag, cursor.getLong(cursor.getColumnIndex(RsrDbAdapter.PROJECT_COL)));
		view.setTag(R.id.update_id_tag, cursor.getLong(idcol));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.update_list_item, null);
		bindView(view, context, cursor);
		
		return view;
	}

}
