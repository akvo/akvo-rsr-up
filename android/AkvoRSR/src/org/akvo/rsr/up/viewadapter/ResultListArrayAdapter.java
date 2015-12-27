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

import java.util.List;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.viewadapter.ResultNode;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class ResultListArrayAdapter extends ArrayAdapter<ResultNode>{
    //To use something other than TextViews for the array display, for instance, ImageViews, or to have some of data besides toString() results
    //fill the views, override getView(int, View, ViewGroup) to return the type of view you want.



/**
 * This adapter formats Update list items using the update_list_item.xml template
 * 
 * @author Stellan Lagerstroem
 * 
 */
	
	private boolean mDebug = false;
	private java.text.DateFormat mDateformat;
	
	public ResultListArrayAdapter(Context context, int resource, int textViewResourceId, List<ResultNode> objects) {
		super(context, textViewResourceId, textViewResourceId, objects);
		mDebug = SettingsUtil.ReadBoolean(context, "setting_debug", false);
		mDateformat = DateFormat.getDateFormat(context);
	}

	
	@Override
	public View getView(int i, View convertView, ViewGroup g) {

	    View v = convertView;
	    Context c = getContext();
	    
	    if (v == null) {
	        LayoutInflater inflater = LayoutInflater.from(c);
	        v = inflater.inflate(R.layout.result_list_item, null);
	    }

        ResultNode rn = getItem(i);
        String indent = "";
        TextView titleView = (TextView) v.findViewById(R.id.result_item_text);
        ImageView img = (ImageView) v.findViewById(R.id.result_item_icon);

        switch (rn.mNodeType) {
            case RESULT:
                titleView.setTextColor(c.getResources().getColor(R.color.white));
                titleView.setTypeface(Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD));
                titleView.setBackgroundColor(c.getResources().getColor(R.color.rsr_purple));
                img.setVisibility(View.GONE);
                break;
            case INDICATOR:
                titleView.setTextColor(c.getResources().getColor(R.color.orange));
                titleView.setTypeface(Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD));
                titleView.setBackgroundColor(c.getResources().getColor(R.color.white));
                indent = "  ";
                img.setVisibility(View.VISIBLE);
                break;
            case PERIOD:
                titleView.setTextColor(c.getResources().getColor(R.color.black));
                titleView.setBackgroundColor(c.getResources().getColor(R.color.white));
                titleView.setTypeface(Typeface.SANS_SERIF);
                img.setVisibility(View.INVISIBLE);
                indent = "    ";
                break;
        }
        
		if (mDebug) {
			titleView.setText(indent+ "["+ rn.mId + "] " + rn.mText);
		} else {
			titleView.setText(indent + rn.mText);
		}


/*
		//Image
		ImageView thumbnail = (ImageView) view.findViewById(R.id.ulist_item_thumbnail);
		//Find file containing thumbnail
		String fn = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_FILENAME_COL));
		String url = cursor.getString(cursor.getColumnIndex(RsrDbAdapter.THUMBNAIL_URL_COL));
		ThumbnailUtil.setPhotoFile(thumbnail, url, fn, null, cursor.getString(idcol), false);

		//set tags so we will know what got clicked
		view.setTag(R.id.project_id_tag, cursor.getLong(cursor.getColumnIndex(RsrDbAdapter.PROJECT_COL)));
		view.setTag(R.id.update_id_tag, cursor.getLong(idcol));
*/
		return v;
	}


}
