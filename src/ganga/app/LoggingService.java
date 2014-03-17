/**
 * 
 * Apocrypha : དལ་འབྱོར་
 * ГИБ       : Только вперёд!
 * Dagrs     : Пожелание лёгкости, бодрости и здоровья! Чтоб работало и не ломалось.
 * Victor    : Меньше слов, больше кода.
 * Juko      : Прогресс бесконечен
 * Cuarto    : Свобода мысли
 */
package ganga.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * This service can manifest inner events 
 * @author Apocrypha
 *
 */
public class LoggingService extends Service {

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
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
