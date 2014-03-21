package ganga.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class PostTask extends AsyncTask<URL , Void, HttpResponse> {

	@Override
	protected HttpResponse doInBackground(URL... urls) {

		HttpResponse response = null;
		String targetHost = "http://dsp.unileadmedia.com";
		DefaultHttpClient httpClient = new DefaultHttpClient(); 
		
		//BuildJSONObjectToSend
		JSONObject json = new JSONObject();
	    try {
			json.put("BidRequestID",12345671);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * The HTTP POST method is defined in section 9.5 of RFC2616:
		 * 
		 * The POST method is used to request that the origin server accept the
		 * entity enclosed in the request as a new subordinate of the resource
		 * identified by the Request-URI in the Request-Line. POST is designed
		 * to allow a uniform method to cover the following functions:
		 * Annotation of existing resources Posting a message to a bulletin
		 * board, newsgroup, mailing list, or similar group of articles
		 * Providing a block of data, such as the result of submitting a form,
		 * to a data-handling process Extending a database through an append
		 * operation
		 */
		HttpPost post = new HttpPost(targetHost);
		post.setHeader( "Content-Type", "application/json" );
		try {
			post.setEntity(new ByteArrayEntity(json.toString().getBytes("UTF8")));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		try {
			response =  httpClient.execute(post);
			StatusLine statusLine = response.getStatusLine();
			//response.
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	private void doPost(){
		
	}

}
