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

package org.akvo.rsr.up.json;

import org.akvo.rsr.up.dao.RsrDbAdapter;
import org.akvo.rsr.up.domain.Indicator;
import org.akvo.rsr.up.domain.Period;
import org.akvo.rsr.up.domain.Result;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * http://rsr.akvo.org/rest/v1/country/?format=xml
 * Example input:
 * 
{
    "count": 4,
    "next": null,
    "previous": null,
    "results":
    [
        {
            "indicators":
            [
                {
                    "periods":
                    [
                        {
                            "id": 5912,
                            "indicator": 5699,
                            "period_start": "2016-12-31",
                            "period_end": "2017-04-01",
                            "target_value": "10",
                            "target_comment": "",
                            "actual_value": "",
                            "actual_comment": ""
                        },
                        {
                            "id": 5911,
                            "indicator": 5699,
                            "period_start": "2015-01-01",
                            "period_end": "2015-12-31",
                            "target_value": "7",
                            "target_comment": "",
                            "actual_value": "",
                            "actual_comment": ""
                        },
                        {
                            "id": 5707,
                            "indicator": 5699,
                            "period_start": "2014-04-01",
                            "period_end": "2014-12-31",
                            "target_value": "5",
                            "target_comment": "",
                            "actual_value": "5",
                            "actual_comment": "New Akvopedia features developed in 2014 include: a login procedure for partners to add information to Akvopedia, a cookie policy plugin, an Akvopedia visitors survey, an easy-to-use Editor was added, and a new partner work space for publishing new content."
                        }
                    ],
                    "id": 5699,
                    "result": 13364,
                    "title": "Number of new features ",
                    "measure": "1",
                    "ascending": null,
                    "description": "",
                    "baseline_year": 2014,
                    "baseline_value": "0",
                    "baseline_comment": ""
                },
                {
                    "periods":
                    [
                        {
                            "id": 5910,
                            "indicator": 5698,
                            "period_start": "2016-12-31",
                            "period_end": "2017-04-01",
                            "target_value": "4",
                            "target_comment": "",
                            "actual_value": "",
                            "actual_comment": ""
                        },
                        {
                            "id": 5909,
                            "indicator": 5698,
                            "period_start": "2015-01-01",
                            "period_end": "2015-12-31",
                            "target_value": "3",
                            "target_comment": "",
                            "actual_value": "",
                            "actual_comment": ""
                        },
                        {
                            "id": 5706,
                            "indicator": 5698,
                            "period_start": "2014-04-01",
                            "period_end": "2014-12-31",
                            "target_value": "2",
                            "target_comment": "",
                            "actual_value": "2",
                            "actual_comment": "Two Akvopedia software releases were done. The source code of Akvopedia is shared openly. "
                        }
                    ],
                    "id": 5698,
                    "result": 13364,
                    "title": "Number of software releases ",
                    "measure": "1",
                    "ascending": null,
                    "description": "",
                    "baseline_year": 2014,
                    "baseline_value": "1",
                    "baseline_comment": ""
                },
                {
                    "periods":
                    [
                        {
                            "id": 5710,
                            "indicator": 5700,
                            "period_start": "2016-12-31",
                            "period_end": "2017-04-01",
                            "target_value": "Available",
                            "target_comment": "",
                            "actual_value": "",
                            "actual_comment": ""
                        },
                        {
                            "id": 5708,
                            "indicator": 5700,
                            "period_start": "2014-04-01",
                            "period_end": "2014-12-31",
                            "target_value": "",
                            "target_comment": "",
                            "actual_value": "Not yet available",
                            "actual_comment": ""
                        },
                        {
                            "id": 5709,
                            "indicator": 5700,
                            "period_start": "2015-01-01",
                            "period_end": "2015-12-31",
                            "target_value": "Available",
                            "target_comment": "",
                            "actual_value": "",
                            "actual_comment": ""
                        }
                    ],
                    "id": 5700,
                    "result": 13364,
                    "title": "Akvopedia Editor is available",
                    "measure": "1",
                    "ascending": null,
                    "description": "",
                    "baseline_year": 2014,
                    "baseline_value": "Not yet available",
                    "baseline_comment": ""
                }
            ],
            "id": 13364,
            "project": 2849,
            "title": "Akvopedia software is continuously improved, source code is shared online and documented with an open license.",
            "type": "2",
            "aggregation_status": null,
            "description": ""
        },
 ...

 */

public class ProjectResultListJsonParser extends ListJsonParser {

	

    /*
     * constructor
     */
    public ProjectResultListJsonParser(RsrDbAdapter aDba, String serverVersion) {
        super(aDba, serverVersion);
    }

    @Override
    public void parse(String body) throws JSONException {

    	super.parse(body);

    	if (mResultsArray != null) {
			//Loop on results
    		for (int ri = 0; ri < mResultsArray.length(); ri++) {
    			JSONObject aResult = mResultsArray.getJSONObject(ri);
    			Result r = new Result();
    			r.setId(aResult.getString("id"));
    			r.setProjectId(aResult.getString("project"));
    			r.setTitle(aResult.getString("title"));
    			r.setDescription(aResult.getString("description"));
    			mDba.saveResult(r);
                mItemCount++;
                
    			//Loop on nested indicators
    			JSONArray indicatorsArray = aResult.getJSONArray("indicators"); 
        		for (int ii = 0; ii < indicatorsArray.length(); ii++) {
        			JSONObject aIndicator = indicatorsArray.getJSONObject(ii);
        			Indicator i = new Indicator();
        			i.setId(aIndicator.getString("id"));
        			i.setResultId(aIndicator.getString("result"));
        			i.setTitle(aIndicator.getString("title"));
        			i.setDescription(aIndicator.getString("description"));
        			mDba.saveIndicator(i);
        			
        			//Loop on nested periods
        			JSONArray periodsArray = aIndicator.getJSONArray("periods"); 
            		for (int pi = 0; pi < periodsArray.length(); pi++) {
            			JSONObject aPeriod = periodsArray.getJSONObject(pi);
            			Period p = new Period();
            			p.setId(aPeriod.getString("id"));
            			p.setIndicatorId(aPeriod.getString("indicator"));
//            			p.setTitle(aPeriod.getString("title")); //Not in API yet, but proposed and so in the app db
            			p.setActualValue(aPeriod.getString("actual_value"));
            			p.setActualComment(aPeriod.getString("actual_comment"));
            			p.setTargetValue(aPeriod.getString("target_value"));
            			p.setTargetComment(aPeriod.getString("target_comment"));
            			p.setPeriodStart(dateOrNull(aPeriod.getString("period_start")));
            			p.setPeriodEnd(dateOrNull(aPeriod.getString("period_end")));
            			mDba.savePeriod(p);
            		}        		
        		}
    		}
    	}
    }
}
