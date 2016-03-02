/*
 *  Copyright (C) 2016 Stichting Akvo (Akvo Foundation)
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

package org.akvo.rsr.up.json;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Indicator;
import org.akvo.rsr.up.domain.Period;
import org.akvo.rsr.up.domain.IndicatorPeriodData;
import org.akvo.rsr.up.domain.Result;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Get period data (and comments?)
 * 
 * http://rsr.test.akvo.org/rest/v1/indicator_period_framework/8306/?format=json
 * 
 * sample:
 * 

 

    {
        "data":
        [
            {
                "comments":
                [
                ],
                "user_details":
                {
                    "id": 294,
                    "first_name": "Laura",
                    "last_name": "Roverts",
                    "approved_organisations":
                    [
                        {
                            "id": 42,
                            "name": "Akvo",
                            "long_name": "Akvo Foundation",
                            "absolute_url": "/en/organisation/42/"
                        }
                    ]
                },
                "status_display": "Approved",
                "photo_url": "",
                "file_url": "",
                "id": 44,
                "created_at": "2016-02-18T10:43:02.830771",
                "last_modified_at": "2016-02-18T10:53:38.248880",
                "period": 8306,
                "user": 294,
                "relative_data": false,
                "data": "",
                "period_actual_value": "20",
                "status": "A",
                "text": "kan ik dit ook gewoon zo saven zonder actual value?",
                "photo": "",
                "file": "",
                "update_method": "W"
            },
            {
                "comments":
                [
                ],
                "user_details":
                {
                    "id": 294,
                    "first_name": "Laura",
                    "last_name": "Roverts",
                    "approved_organisations":
                    [
                        {
                            "id": 42,
                            "name": "Akvo",
                            "long_name": "Akvo Foundation",
                            "absolute_url": "/en/organisation/42/"
                        }
                    ]
                },
                "status_display": "Approved",
                "photo_url": "",
                "file_url": "",
                "id": 45,
                "created_at": "2016-02-18T11:00:27.321430",
                "last_modified_at": "2016-02-18T11:20:58.840418",
                "period": 8306,
                "user": 294,
                "relative_data": false,
                "data": "20",
                "period_actual_value": "20",
                "status": "A",
                "text": "fjak;lfdsa",
                "photo": "",
                "file": "",
                "update_method": "W"
            },
            {
                "comments":
                [
                ],
                "user_details":
                {
                    "id": 6867,
                    "first_name": "mjl",
                    "last_name": "schoonman",
                    "approved_organisations":
                    [
                        {
                            "id": 42,
                            "name": "Akvo",
                            "long_name": "Akvo Foundation",
                            "absolute_url": "/en/organisation/42/"
                        },
                        {
                            "id": 2482,
                            "name": "ICCO (test)",
                            "long_name": "ICCO (test)",
                            "absolute_url": "/en/organisation/2482/"
                        }
                    ]
                },
                "status_display": "Pending approval",
                "photo_url": "",
                "file_url": "",
                "id": 37,
                "created_at": "2016-02-17T11:44:47.837716",
                "last_modified_at": "2016-02-18T10:37:31.590317",
                "period": 8306,
                "user": 6867,
                "relative_data": false,
                "data": "20",
                "period_actual_value": "20",
                "status": "P",
                "text": "",
                "photo": "",
                "file": "",
                "update_method": "W"
            },
            {
                "comments":
                [
                    {
                        "user_details":
                        {
                            "id": 1808,
                            "first_name": "Marten",
                            "last_name": "Schoonman",
                            "approved_organisations":
                            [
                                {
                                    "id": 42,
                                    "name": "Akvo",
                                    "long_name": "Akvo Foundation",
                                    "absolute_url": "/en/organisation/42/"
                                }
                            ]
                        },
                        "id": 6,
                        "created_at": "2016-02-18T10:39:22.471176",
                        "last_modified_at": "2016-02-18T10:39:22.471231",
                        "data": 32,
                        "user": 1808,
                        "comment": "aa"
                    },
                    {
                        "user_details":
                        {
                            "id": 1808,
                            "first_name": "Marten",
                            "last_name": "Schoonman",
                            "approved_organisations":
                            [
                                {
                                    "id": 42,
                                    "name": "Akvo",
                                    "long_name": "Akvo Foundation",
                                    "absolute_url": "/en/organisation/42/"
                                }
                            ]
                        },
                        "id": 7,
                        "created_at": "2016-02-18T10:39:57.041996",
                        "last_modified_at": "2016-02-18T10:39:57.042047",
                        "data": 32,
                        "user": 1808,
                        "comment": "aa"
                    },
                    {
                        "user_details":
                        {
                            "id": 1808,
                            "first_name": "Marten",
                            "last_name": "Schoonman",
                            "approved_organisations":
                            [
                                {
                                    "id": 42,
                                    "name": "Akvo",
                                    "long_name": "Akvo Foundation",
                                    "absolute_url": "/en/organisation/42/"
                                }
                            ]
                        },
                        "id": 8,
                        "created_at": "2016-02-18T10:40:34.466366",
                        "last_modified_at": "2016-02-18T10:40:34.466428",
                        "data": 32,
                        "user": 1808,
                        "comment": "dsfa"
                    }
                ],
                "user_details":
                {
                    "id": 6867,
                    "first_name": "mjl",
                    "last_name": "schoonman",
                    "approved_organisations":
                    [
                        {
                            "id": 42,
                            "name": "Akvo",
                            "long_name": "Akvo Foundation",
                            "absolute_url": "/en/organisation/42/"
                        },
                        {
                            "id": 2482,
                            "name": "ICCO (test)",
                            "long_name": "ICCO (test)",
                            "absolute_url": "/en/organisation/2482/"
                        }
                    ]
                },
                "status_display": "New",
                "photo_url": "",
                "file_url": "",
                "id": 32,
                "created_at": "2016-02-17T11:37:01.134943",
                "last_modified_at": "2016-02-17T11:37:01.134990",
                "period": 8306,
                "user": 6867,
                "relative_data": false,
                "data": "20",
                "period_actual_value": "20",
                "status": "N",
                "text": "",
                "photo": "",
                "file": "",
                "update_method": "W"
            }
        ],
        "parent_period": null,
        "percent_accomplishment": 250,
        "id": 8306,
        "indicator": 8238,
        "locked": true,
        "period_start": "2016-02-01",
        "period_end": "2016-02-03",
        "target_value": "10",
        "target_comment": "",
        "actual_value": "25",
        "actual_comment": "comment actual value komt hier"
    }




 */

public class IndicatorPeriodFrameworkJsonParser extends JsonParser {

    Period period = null;

    /*
     * constructor
     */
    public IndicatorPeriodFrameworkJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mRoot != null) {
    		period = new Period();
            period.setId(mRoot.getString("id"));
            period.setLocked(mRoot.getBoolean("locked"));
            mDba.savePeriod(period);

            //loop over IPDs 
    		JSONArray ipdArray = mRoot.getJSONArray("data");
            for (int ri = 0; ri < ipdArray.length(); ri++) {
                JSONObject aIpd = ipdArray.getJSONObject(ri);
                IndicatorPeriodData r = new IndicatorPeriodData();
                r.setId(aIpd.getString("id"));
                r.setData(aIpd.getString("data"));
                r.setDescription(aIpd.getString("text"));
                r.setPeriodId(aIpd.getString("period"));
                r.setRelativeData(aIpd.getBoolean("relative_data"));
                //TODO file/photo
                mDba.saveIpd(r);
                
                //Loop on nested comments?
                /*
                JSONArray indicatorsArray = aIpd.getJSONArray("indicators"); 
                for (int ii = 0; ii < indicatorsArray.length(); ii++) {
                    JSONObject aIndicator = indicatorsArray.getJSONObject(ii);
                    Indicator i = new Indicator();
                    i.setId(aIndicator.getString("id"));
                    i.setResultId(aIndicator.getString("result"));
                    i.setTitle(aIndicator.getString("title"));
                    i.setDescription(aIndicator.getString("description"));
                    mDba.saveIndicator(i);
       */
    		
            }	
    	}
    }
    
    //useful??
    public Period getPeriod() {
        return period;
    }
}
