package com.edit.reach.system;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Remote {

    private Remote() {}

    public static void get(URL url, ResponseHandler responseHandler) {
        new GetDataTask(responseHandler).execute(url);
    }

    private static class GetDataTask extends AsyncTask<URL, Void, String> {
        private ResponseHandler responseHandler;

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

        public String readHttpResponse(InputStream stream) throws IOException {
            return new Scanner(stream).useDelimiter("\\A").next();
        }
    }
}