/*
 * 
 * Copyright 2016-2017 Tom Misawa, riversun.org@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in the 
 * Software without restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
 * Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.riversun.fcm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.riversun.fcm.model.DeviceMessage;
import org.riversun.fcm.model.FcmResponse;

/**
 * A Simple Firebase Cloud Messaging client<br>
 * Easy to send notification to devices
 *
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class FcmClient {

	private static final Logger LOGGER = Logger.getLogger(FcmClient.class.getName());

	private String mFcmSendEndpoint = "https://fcm.googleapis.com/fcm/send";

	private String mFcmServerAPIKey = null;

	public FcmClient() {

	}

	/**
	 * 
	 * @param fcmSendEndpoint
	 *            Set endpoint of fcm if needed.
	 */
	public FcmClient(String fcmSendEndpoint) {
		mFcmSendEndpoint = fcmSendEndpoint;
	}

	/**
	 * 
	 * To send messages to specific device(s) specified by registration token(s)
	 * that can be retrieved by FirebaseInstanceId.getInstance().getToken() on
	 * the mobile device(s). <br>
	 * To generate JSON message and to make a HTTP POST request like followings<br>
	 * <code>
	 * https://fcm.googleapis.com/fcm/send<br>
	 * Content-Type:application/json<br>
	 * Authorization:key=AIzaSyZ-1u...0GBYzPu7Udno5aA<br>
	 * 
	 * { "data":{
	 *     "myKey1":"myValue1",
	 *     "myKey2":"myValue2"
	 *   },
	 *   "registration_ids":["your_registration_token1","your_registration_token2]
	 * }
	 * </code>
	 * 
	 * @param msg
	 * @return
	 */
	public FcmResponse pushToDevices(DeviceMessage msg) {
		return new FcmResponse(pushNotify(msg.toJsonObject()));
	}

	/**
	 * Set the server API key <br>
	 * Where is server API key. Open https://console.firebase.google.com and
	 * select your project.Click project settings and you can find the Server
	 * Key on the "CloudMessaging Tab"
	 * 
	 */

	public void setAPIKey(String serverApiKey) {
		mFcmServerAPIKey = serverApiKey;
	}

	/**
	 * Send json to fcm endpoint to execute push notification.
	 * 
	 * @param json
	 * @return
	 */
	public JSONObject pushNotify(JSONObject json) {

		final String requestText = json.toString();
		LOGGER.fine("request:\n" + requestText);

		URL url = null;

		PrintWriter pw = null;
		BufferedWriter bw = null;
		OutputStreamWriter osw = null;
		OutputStream os = null;

		BufferedReader br = null;
		InputStreamReader isr = null;
		InputStream is = null;
		HttpURLConnection con = null;

		try {
			url = new URL(mFcmSendEndpoint);

			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "key=" + mFcmServerAPIKey);
			con.setRequestMethod("POST");
			con.setInstanceFollowRedirects(false);
			// connect
			con.connect();

			os = con.getOutputStream();
			osw = new OutputStreamWriter(os, "UTF-8");
			bw = new BufferedWriter(osw);
			pw = new PrintWriter(bw);

			// send request
			pw.print(requestText);
			pw.flush();

			is = con.getInputStream();
			isr = new InputStreamReader(is, "UTF-8");
			br = new BufferedReader(isr);

			final StringBuilder sb = new StringBuilder();

			// receive response
			for (String line; (line = br.readLine()) != null;) {
				sb.append(line);
			}

			final String responseText = sb.toString();
			LOGGER.fine("response:\n" + responseText);

			final JSONObject ret = new JSONObject(responseText);

			return ret;

		} catch (MalformedURLException e) {

		} catch (IOException e) {

			// when network error occurred
			try {
				con.getErrorStream().close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			LOGGER.log(Level.WARNING, "Network error occurred while sending to firebase.", e);

		} finally {

			if (pw != null) {
				pw.close();
			}

			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
				}
			}
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}

		}
		return null;

	}

}