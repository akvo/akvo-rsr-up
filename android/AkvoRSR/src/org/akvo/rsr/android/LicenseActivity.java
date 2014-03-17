/*
 *  Copyright (C) 2012-2014 Stichting Akvo (Akvo Foundation)
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
package org.akvo.rsr.android;

import android.os.Bundle;
import android.webkit.WebView;
import android.app.Activity;

public class LicenseActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WebView webview = new WebView(this);
		setContentView(webview);
		 
		// Simplest usage: note that an exception will NOT be thrown
		// if there is an error loading this page (see below).
		//webview.loadUrl("http://akvo.org/");
		
		// OR, you can also load from an HTML string:
		String summary = "<html><body>" +
                "<H2>Akvo RSR Up</H2><H3>Android app</H3><P>Copyright &#169 2012-2014 Stichting Akvo</P>" +
		        "<P>Akvo RSR is free software: you can redistribute it and modify it under the terms of the "+
                    "GNU Affero General Public License (AGPL) as published by the Free Software Foundation,"+
                    " either version 3 of the License or any later version.</P>"+
		        "<P>Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;"+
                    " without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE."+
                    " See the GNU Affero General Public License at the link below for more details.</P>"+
                "<P>The full license can be seen at <A HREF=\"http://www.gnu.org/licenses/agpl.html\">http://www.gnu.org/licenses/agpl.html</A></P>" +
                "<H2>App dependencies</H2><H3>HTTP Request library</H3><P>Copyright &#169 2011 Kevin Sawicki &lt;kevinsawicki@gmail.com&gt;</P>" +
                "<P>Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), "+
                "to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or"+
                "sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:</P>"+
                "<P>The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.</P>" +
		        "</body></html>";
		webview.loadData(summary, "text/html", null);
	}
	
}
