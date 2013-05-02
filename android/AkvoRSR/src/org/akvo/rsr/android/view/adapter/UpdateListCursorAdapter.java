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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	public UpdateListCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		//Text data
		TextView titleView = (TextView) view.findViewById(R.id.ulist_item_title);
		titleView.setText(cursor.getString(cursor.getColumnIndex(RsrDbAdapter.TITLE_COL)));

		TextView stateView = (TextView) view.findViewById(R.id.ulist_item_state);
		if (0 != cursor.getInt(cursor.getColumnIndex(RsrDbAdapter.DRAFT_COL))) {
			stateView.setText(R.string.state_draft);
			stateView.setTextColor(0x0000ff);//BLUE
		} else
			if (0 != cursor.getInt(cursor.getColumnIndex(RsrDbAdapter.UNSENT_COL))) {
				stateView.setText(R.string.state_unsent);	
				stateView.setTextColor(0xff0000);//RED
		}
		
			
				
		//Image
		ImageView thumbnail = (ImageView) view.findViewById(R.id.ulist_item_thumbnail);
		//Find file containing thumbnail
		String fn = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
		if (fn != null && new File(fn).exists()) {
			Bitmap bm = BitmapFactory.decodeFile(fn);
			if (bm != null)
				thumbnail.setImageBitmap(bm);
		} else {
			//Fall back to generic logo
			//TODO different for null and broken ref
			thumbnail.setImageResource(R.drawable.ic_launcher);
		}
		
		//set tag so we will know what got clicked
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
