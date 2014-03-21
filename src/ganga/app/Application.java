package ganga.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Application extends android.app.Application {
	/**
	 * As the name suggests, its the context of current state of the
	 * application/object. It lets newly created objects understand what has
	 * been going on. Typically you call it to get information regarding another
	 * part of your program (activity, package/application)
	 */
	public static Context context;
	public static LoggingService loggingService;
	public static final String TAG = "Morse Code Application";
		
	@Override
	public final void onCreate() {
		super.onCreate();

		context = getApplicationContext();
		ComponentName service = context.startService(new Intent(context, LoggingService.class));
		if (null==service){
			Log.e(TAG, "Could not start logging service.");
		}
		//loggingService = 
	}//onCreate

	/**
	 * Can be called when the application object is terminated. Note that there
	 * is no guarantee of this method handler's being called. s terminated by
	 * the kernel in order to free resources for other applications, the process
	 * will be terminated without warning and without a call to the application
	 * object's onTerminate handler.
	 * 
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public final void onTerminate() {
		super.onTerminate();
	}

	/**
	 * Show message by pop-up toast for a short period of time.
	 * 
	 * @param message
	 *            what to show
	 */
	public void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

}
