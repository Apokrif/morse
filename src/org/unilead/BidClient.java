package org.unilead;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.json.JSONException;
import org.json.JSONObject;

import org.unilead.openrtb.common.api.Bid;


/**
 * Command pattern
 * <p>Main method {@link BidClient#execute} </p>
 * @author Apocrypha
 *
 */
public class BidClient {
	public static final int TIMEOUT_MILLISEC = 100;
	public static String targetHost = "http://dsp.unileadmedia.com/new.php";
	/**
	 * Do main work by requesting Server specified by {@link BidClient#targetHost} for URLs
	 * @return
	 * @throws JSONException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws BidServerException
	 */
	public Bid execute() 
			throws JSONException, ClientProtocolException, IOException, BidServerException {
		JSONObject requestJson = buildRequest();
		HttpResponse response = getResponse(requestJson);
		validate(response);
		return parse(response);
	}// do post and other...
	
	/**
	 * Shutdown HttpClient
	 */
	public void shutdownHttpClient() {
		if (httpClient == null)
			return;
		ClientConnectionManager manager = httpClient.getConnectionManager();
		if (manager != null) {
			manager.shutdown();
		}
		httpClient = null;
	}
	
	// TODO: Compare to HttpURLConnection.
	// http://android-developers.blogspot.ru/2011/09/androids-http-clients.html
	// https://github.com/square/okhttp
	DefaultHttpClient httpClient;
	/** 
	 * build Bid Request Object To Send
	 * @return
	 * @throws JSONException
	 */
	private JSONObject buildRequest() throws JSONException {
		JSONObject requestJson = new JSONObject();
		requestJson.put("BidRequestId", 12345678);
		return requestJson;
	}

	/**
	 * Get Response from Bid Sever
	 * @param requestJson
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private HttpResponse getResponse(JSONObject requestJson)
			throws UnsupportedEncodingException, IOException,
			ClientProtocolException{

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
	httpClient = new DefaultHttpClient(); // using Apache HTTP Client
		/**
		 * The HTTP POST method is defined in section 9.5 of RFC2616:
		 * 
		 * The POST method is used to request that the origin server accept the
		 * entity enclosed in the request as a new subordinate of the resource
		 * identified by the Request-URI in the Request-Line. POST is designed to
		 * allow a uniform method to cover the following functions: Annotation of
		 * existing resources Posting a message to a bulletin board, newsgroup,
		 * mailing list, or similar group of articles Providing a block of data,
		 * such as the result of submitting a form, to a data-handling process
		 * Extending a database through an append operation
		 */
		HttpPost post = new HttpPost(targetHost);
		post.setHeader("Content-Type", "application/json");
		post.setEntity(new ByteArrayEntity(requestJson.toString().getBytes(
				"UTF8")));

		return httpClient.execute(post);
	}

	/**
	 * Validate getting response 
	 * @param response
	 * @throws BidServerException
	 */
	private void validate(HttpResponse response) throws BidServerException{
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
			throw new BidServerException(statusLine.getReasonPhrase());
		}
	}

	/**
	 * parse Response
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 */
	private Bid parse(HttpResponse response) throws IOException,
			UnsupportedEncodingException, JSONException {
		InputStream inputStream = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream, "UTF-8"), 8);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
		}
		Log.d("ganga", sb.toString());
		JSONObject responseJson = new JSONObject(sb.toString());
		JSONObject seatbid = responseJson.getJSONArray("seatbid")
				.getJSONObject(0);
		JSONObject bid = seatbid.getJSONArray("bid").getJSONObject(0);

		Bid resultBid = new Bid();
		resultBid.nurl = bid.getString("nurl");
		resultBid.iurl = bid.getString("iurl");
		return resultBid;
	}
}