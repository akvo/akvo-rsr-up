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

import org.akvo.rsr.android.R;
import org.akvo.rsr.android.dao.RsrDbAdapter;
import org.akvo.rsr.android.util.SettingsUtil;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
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
	
	public UpdateListCursorAdapter(Context context, Cursor c) {
		super(context, c);
		debug = SettingsUtil.ReadBoolean(context, "setting_debug", false);
	}

	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		//Text data
		TextView titleView = (TextView) view.findViewById(R.id.ulist_item_title);
		if (debug) {
			titleView.setText("["+ cursor.getString(cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL))+"] "+cursor.getString(cursor.getColumnIndex(RsrDbAdapter.TITLE_COL)));
		} else {
			titleView.setText(cursor.getString(cursor.getColumnIndex(RsrDbAdapter.TITLE_COL)));
		}

		TextView stateView = (TextView) view.findViewById(R.id.ulist_item_state);
		if (0 != cursor.getInt(cursor.getColumnIndex(RsrDbAdapter.DRAFT_COL))) {
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
		
			
		//Image
		ImageView thumbnail = (ImageView) view.findViewById(R.id.ulist_item_thumbnail);
		//Find file containing thumbnail
		String fn = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
		if (fn != null && new File(fn).exists()) {
			//need to subsample if large to prevent filling memory
			Options option = new Options();
			option.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fn,option);//fetch dimensions
			option.inJustDecodeBounds = false;
			option.inSampleSize = Math.max(option.outHeight, option.outWidth) / 120; //only need 120 pixels for a thumbnail
			Bitmap bm = BitmapFactory.decodeFile(fn, option);
			if (bm != null)
				thumbnail.setImageBitmap(bm);
		} else {
			//Fall back to generic logo
			//TODO different for null and broken ref?
			thumbnail.setImageResource(R.drawable.ic_launcher);
		}
		
		//set tags so we will know what got clicked
		view.setTag(R.id.project_id_tag, cursor.getLong(cursor.getColumnIndex(RsrDbAdapter.PROJECT_COL)));
		view.setTag(R.id.update_id_tag, cursor.getLong(cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL)));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.update_list_item, null);
		bindView(view, context, cursor);
		
		return view;
	}

}
