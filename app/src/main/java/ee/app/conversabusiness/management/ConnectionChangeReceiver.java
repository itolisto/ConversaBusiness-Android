/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversabusiness.management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import ee.app.conversabusiness.utils.Utils;

/**
 * ConnectionChangeReceiver
 * 
 * Sends broadcast on connection change.
 */

public class ConnectionChangeReceiver extends BroadcastReceiver {

	/* AblyConnection constants */
	public static final String INTERNET_CONNECTION_CHANGE = "internet_connection_change";
	public static final String HAS_INTERNET_CONNECTION = "has_internet_connection";
	private Intent mConnectionChangeBroadcast = new Intent(INTERNET_CONNECTION_CHANGE);

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean hasInternetConnection = Utils.hasNetworkConnection(context);

		if(hasInternetConnection) {
			mConnectionChangeBroadcast.putExtra(HAS_INTERNET_CONNECTION, true);
		} else {
			mConnectionChangeBroadcast.putExtra(HAS_INTERNET_CONNECTION, false);
		}

		LocalBroadcastManager.getInstance(context).sendBroadcast(mConnectionChangeBroadcast);
	}
}
