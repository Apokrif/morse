package com.unilead;

import android.util.Log;

public class Logger {
	public static final String TAG = "Unilead";

	/**
	 * Send a {@link #VERBOSE} log message.
	 * 
	 * @param msg
	 *            The message you would like logged.
	 */
	public static int v(String msg) {
		return Log.v(TAG, msg);
	}

	public static int d(String msg) {
		return Log.d(TAG, msg);
	}
    public static int d(String msg, Throwable tr) {
    	return Log.d(TAG, msg, tr);
    }

	public static int e(String msg) {
		return Log.e(TAG, msg);
	}

	public static int i(String msg) {
		return Log.i(TAG, msg);
	}
}
