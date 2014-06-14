package org.unilead;

import java.io.IOException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.unilead.BidClient;
import org.unilead.BidServerException;
import org.unilead.openrtb.common.api.Bid;

import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.unilead.Engine;

import android.os.AsyncTask;
import android.widget.ImageView;

public class PostTask extends AsyncTask<URL, Void, Bid> {
	Bid bid;
	Engine engine;

	public void initialize(Engine engine){
		this.engine = engine;
	}
	@Override
	protected Bid doInBackground(URL... urls) {
		BidClient client = new BidClient();

		try {
			bid = client.execute();
			//imageLoader.displayImage(bid.nurl, imageView);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BidServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			client.shutdownHttpClient();
		}
		return bid;
	}
	/**
	 *  Invoked on the UI thread after the background computation finishes.
	 */
	@Override
	protected void onPostExecute(Bid result) {
		if (result == null)
			return;
		//engine.imageLoader.displayImage(bid.nurl.toString(), null);
	}
}
