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

public class ProjectListCursorAdapter extends CursorAdapter{

	private static final String imageCache = "/sdcard/akvorsr/imagecache";

/**
 * This adaptor formats Project list items. It can format date strings using the device's locale settings prior
 * to displaying them to the screen
 * 
 * @author Stellan Lagerstroem
 * 
 */
	public static final int PROJECT_ID_KEY = 1; //used send tags from this view to the activity

	public ProjectListCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		//Text data
		TextView titleView = (TextView) view.findViewById(R.id.list_item_title);
		titleView.setText(cursor.getString(cursor.getColumnIndex(RsrDbAdapter.TITLE_COL)));

		
		/*
  		long millis = cursor.getLong(cursor.getColumnIndex(RsrDbAdapter.DELIVERED_DATE_COL));

		// Format the date string
		Date date = new Date(millis);
		TextView dateView = (TextView) view.findViewById(R.id.text2);
		dateView.setText(status
				+ DateFormat.getLongDateFormat(context).format(date) + " "
				+ DateFormat.getTimeFormat(context).format(date));
		 */
		
		//Image
		ImageView thumbnail = (ImageView) view.findViewById(R.id.list_item_thumbnail);
		//Find file containing thumbnail		
		File f = new File(cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL)));
		if (f.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
			if (bm != null)
				thumbnail.setImageBitmap(bm);
		} else {
			//Fall back to generic logo
			thumbnail.setImageResource(R.drawable.ic_launcher);
		}
		
		//set tag so we will know what got clicked
		view.setTag(PROJECT_ID_KEY, cursor.getLong(cursor.getColumnIndex(RsrDbAdapter.PK_ID_COL)));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.project_list_item, null);
		bindView(view, context, cursor);
		
		return view;
	}

}
