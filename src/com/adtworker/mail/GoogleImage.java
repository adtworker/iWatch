package com.adtworker.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleImage {
	static String REQUEST_TEMPLATE = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q={0}&start={1}&rsz={2}&imgsz=medium&imgtype=photo";

	static JSONObject json;

	/**
	 * @param keyword
	 * @param start
	 * @param resultSize
	 *            (1~8)
	 * @return
	 */
	public static ArrayList<String> getImgUrl(String keyword, int start,
			int resultSize) {
		ArrayList<String> listImages = new ArrayList<String>();
		String requestUrl = MessageFormat.format(REQUEST_TEMPLATE, keyword,
				start, resultSize);

		try {
			URL url = new URL(requestUrl);
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("Referer",
					"http://technotalkative.com");

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			json = new JSONObject(builder.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			JSONObject responseObject = json.getJSONObject("responseData");
			JSONArray resultArray = responseObject.getJSONArray("results");

			for (int i = 0; i < resultArray.length(); i++) {
				JSONObject obj;
				obj = resultArray.getJSONObject(i);

				System.out.println("Image URL => " + obj.getString("url"));
				System.out.println("Image tbURL => " + obj.getString("tbUrl"));

				listImages.add(obj.getString("url"));
			}

			System.out
					.println("Result array length => " + resultArray.length());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return listImages;
	}
}
