package net.ddns.iztok.tanki;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public abstract class HttpRequest extends AsyncTask<String, Void, String> implements HttpRequestInterface {
	private String response;
	private int responseCode;
	private WeakReference<Activity> activityRef;

	HttpRequest(Activity a) {
		activityRef = new WeakReference<>(a);
	}

	@Override
	protected String doInBackground(String... strings) {
		URL url;
		HttpURLConnection connection = null;

		try {
			url = new URL(strings[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			responseCode = connection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				response = readStream(connection.getInputStream());
				return response;
			}
		} catch (SocketTimeoutException e2) {
			response = "\0";
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("Tanki", e.getMessage());
		}

		return null;
	}

	@Override
	protected void onPostExecute(String s) {
		super.onPostExecute(s);

		Activity a = activityRef.get();
		if (a == null || a.isFinishing()) return;

		Log.w("Tanki", "Response code: " + responseCode);
		Log.w("Tanki", "Response: " + response);
		onResponse(responseCode, response);
	}

	private String readStream(InputStream in) {
		BufferedReader reader = null;
		StringBuffer response = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		} catch (IOException e) {
			Log.e("Tanki", e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e2) {
					Log.e("Tanki", e2.getMessage());
				}
			}
		}
		return response.toString();
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponse() {
		return response;
	}

	//public abstract void onResponse();
}
