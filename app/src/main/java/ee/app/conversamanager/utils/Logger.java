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

package ee.app.conversamanager.utils;

import android.util.Log;

import ee.app.conversamanager.BuildConfig;

/**
 * Logger
 *
 * Prints tag and value to LogCat.
 */

public class Logger {

	@SuppressWarnings("unused")
	public static void debug(String tag, String value) {

		if (BuildConfig.DEV_BUILD) {
			Log.d(tag, value);
		}
	}

	@SuppressWarnings("unused")
	public static void info(String tag, String value) {

		if (BuildConfig.DEV_BUILD) {
			Log.i(tag, value);
		}
	}

	public static void error(String tag, String value, Exception e) {

		if (BuildConfig.DEV_BUILD) {
			Log.e(tag, value, e);
		}
	}

	public static void error(String tag, String value) {

		if (BuildConfig.DEV_BUILD) {
			Log.e(tag, value);
		}
	}

	public static void error(String tag, Exception e) {

		if (BuildConfig.DEV_BUILD) {
			Log.e(tag, "", e);
		}
	}

	@SuppressWarnings("unused")
	public static void warning(String tag, String value) {

		if (BuildConfig.DEV_BUILD) {
			Log.w(tag, value);
		}
	}

	@SuppressWarnings("unused")
	public static void view(String tag, String value) {

		if (BuildConfig.DEV_BUILD) {
			Log.v(tag, value);
		}
	}

}