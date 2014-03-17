package ganga.app;

import android.content.Context;
import android.content.Intent;

public class Application extends android.app.Application {
	/**
	 * As the name suggests, its the context of current state of the
	 * application/object. It lets newly created objects understand what has
	 * been going on. Typically you call it to get information regarding another
	 * part of your program (activity, package/application)
	 */
	public static Context context;
	public static LoggingService loggingService;
	@Override
	public final void onCreate() {
		super.onCreate();

		context = getApplicationContext();
		context.startService(new Intent(context, LoggingService.class));
		//loggingService = 
	}

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

}
