package com.edit.reach.system;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * This class is used to statically handle all HTTP requests.
 * At the time it's performing GET requests.
 */
public class Remote {

	private Remote() {
	}

	/**
	 * Starting an asynchronous GET request.
	 * @param url The URL to request against.
	 * @param responseHandler The object that will receive the response.
	 */
	public static void get(URL url, ResponseHandler responseHandler) {
		new GetDataTask(responseHandler).execute(url);
	}

	/**
	 * An asynchronous task that performs a GET request.
	 * The ResponseHandler will be notified of the response
	 * in a suitable method.
	 */
	private static class GetDataTask extends AsyncTask<URL, Void, String> {
		private final ResponseHandler responseHandler;

		public GetDataTask(ResponseHandler responseHandler) {
			this.responseHandler = responseHandler;
		}

		@Override
		protected String doInBackground(URL... urls) {
			try {
				return getData(urls[0]);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {

			if (result != null) {
				try {
					JSONObject json = new JSONObject(result);
					responseHandler.onGetSuccess(json);
				} catch (JSONException e) {
					responseHandler.onGetFail();
				}
			} else {
				responseHandler.onGetFail();
			}
		}

		/**
		 * Responsible of actually downloading the data from the server.
		 * @param url The URL to read from.
		 * @return A string with the server response.
		 * @throws IOException
		 */
		private String getData(URL url) throws IOException {
			InputStream inputStream = null;

			try {
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setReadTimeout(10000);
				httpConnection.setConnectTimeout(15000);
				httpConnection.setRequestMethod("GET");
				httpConnection.setDoInput(true);
				httpConnection.connect();

				int responseCode = httpConnection.getResponseCode();

				if (responseCode == 200) {
					inputStream = httpConnection.getInputStream();
					return readHttpResponse(inputStream);
				} else {
					throw new IOException();
				}

			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		}

		/**
		 * Parsing an InputStream to a String and returns it.
		 * @param stream The InputStream containing the HTTP response.
		 * @return The parsed HTTP response as a String.
		 */
		public String readHttpResponse(InputStream stream) {
			return new Scanner(stream).useDelimiter("\\A").next();
		}
	}
}