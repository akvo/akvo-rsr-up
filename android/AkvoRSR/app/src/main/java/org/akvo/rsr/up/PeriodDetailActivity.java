/*
 *  Copyright (C) 2012-2015,2020 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.akvo.rsr.up.R;
import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Indicator;
import org.akvo.rsr.up.domain.Organisation;
import org.akvo.rsr.up.domain.Period;
import org.akvo.rsr.up.domain.Project;
import org.akvo.rsr.up.domain.Update;
import org.akvo.rsr.up.domain.User;
import org.akvo.rsr.up.util.ConstantUtil;
import org.akvo.rsr.up.util.DialogUtil;
import org.akvo.rsr.up.util.FileUtil;
import org.akvo.rsr.up.util.SettingsUtil;
import org.akvo.rsr.up.util.ThumbnailUtil;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

public class PeriodDetailActivity extends BackActivity {
	
	private String mPeriodId = null;
	private Period period = null;
	private boolean mEditable;
	private boolean mDebug;
	//UI
	private TextView indTitleLabel;
    private TextView projupdUser;
    private TextView perActualValue;
    private TextView perTargetValue;
    private TextView perStart;
    private TextView perEnd;
    private ImageView mLockedIndicator;
    private Button mBtnEdit;
	//Database
	private RsrDbAdapter mDba;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDebug = SettingsUtil.ReadBoolean(this, "setting_debug", false);

		//find which period we are showing
//        projectId = getIntent().getStringExtra(ConstantUtil.PROJECT_ID_KEY);
        mPeriodId = getIntent().getStringExtra(ConstantUtil.PERIOD_ID_KEY);
		if (/*projectId == null || */ mPeriodId == null) {
			DialogUtil.errorAlert(this,R.string.noid_dialog_title, R.string.noid_dialog_msg);//TODO: helpful for periods?
		}
		
		//get the look
		setContentView(R.layout.activity_period_detail);
		//find the fields
		indTitleLabel   = (TextView) findViewById(R.id.perdet_indicator_title);
        perStart        = (TextView) findViewById(R.id.perdet_start);
        perEnd          = (TextView) findViewById(R.id.perdet_end);
        perActualValue  = (TextView) findViewById(R.id.perdet_actual_value);
        perTargetValue  = (TextView) findViewById(R.id.perdet_target_value);
        mLockedIndicator= (ImageView) findViewById(R.id.perdet_locked_indicator);

		//Activate buttons
        mBtnEdit = (Button) findViewById(R.id.btn_add_data);
        mBtnEdit.setOnClickListener( new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), ResultEditorActivity.class);
				i.putExtra(ConstantUtil.PERIOD_ID_KEY, mPeriodId);
				startActivity(i);
			}
		});

        final SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		
		mDba = new RsrDbAdapter(this);
		mDba.open();
		try {
    		period = mDba.findPeriod(mPeriodId);
    		if (period == null) {
    			DialogUtil.errorAlert(this, R.string.noupd_dialog_title, R.string.noupd_dialog_msg);//TODO
    		} else {
                Indicator indicator = mDba.findIndicator(period.getIndicatorId());
                indTitleLabel.setText(indicator.getTitle());

    			//populate fields
    			mEditable = !period.getLocked();
                mLockedIndicator.setVisibility(mEditable?View.GONE:View.VISIBLE);
                mBtnEdit.setVisibility(!mEditable?View.GONE:View.VISIBLE);
                mBtnEdit.setEnabled(mEditable);
//    			perTitleText.setText(period.getTitle());	
                perStart.setText(period.getPeriodStart()==null?"":dateOnly.format(period.getPeriodStart()));
                perEnd.setText(period.getPeriodEnd()==null?"":dateOnly.format(period.getPeriodEnd()));
                perActualValue.setText(period.getActualValue());
                perTargetValue.setText(period.getTargetValue());
//    		    ThumbnailUtil.setPhotoFile(projupdImage,period.getThumbnailUrl(),period.getThumbnailFilename(), null, periodId, true);

    		}
		}
		finally {
		    mDba.close();
		}
		
		
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
    
	
}
