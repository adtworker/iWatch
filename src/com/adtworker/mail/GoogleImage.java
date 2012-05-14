package com.adtworker.mail;

import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import com.adtworker.mail.constants.Constants;
import com.adtworker.mail.util.HttpUtils;

public class GoogleImage {
	static String REQUEST_TEMPLATE = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q={0}&start={1}&rsz={2}&imgsz=medium&imgtype=photo";
	private static String GOOGLE_AJAX_URL_TEMPLATE = "http://www.google.com.hk/search?q={0}&hl=zh-CN&newwindow=1&safe=strict&biw=1399&bih=347&tbm=isch&ijn=2&ei=PyCKT8DJAavimAWUiJjgCQ&page={1}&start={2}&ijn={3}";

	private static String GOOGLE_IMG_URL_TEMPLATE = "http://images.google.com.hk/images?q={0}&ndsp={1}&start={2}&filter=1&safe=strict";
	// http://www.codeproject.com/Articles/11876/An-API-for-Google-Image-Search

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
			json = new JSONObject(HttpUtils.executeGet(requestUrl));
			JSONObject responseObject = json.getJSONObject("responseData");
			JSONArray resultArray = responseObject.getJSONArray("results");

			for (int i = 0; i < resultArray.length(); i++) {
				JSONObject obj;
				obj = resultArray.getJSONObject(i);

				Log.e(Constants.TAG, "Image URL => " + obj.getString("url"));
				Log.e(Constants.TAG, "Image tbURL => " + obj.getString("tbUrl"));

				listImages.add(obj.getString("url"));
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, "getImgUrl request error", e);
		}
		return listImages;
	}

	private static Pattern googleScriptImgRegex = Pattern
			.compile(".*tbnid=(.*):&amp;.*imgurl=(.*.[png|jpg|jpeg])&amp.*data-src=\"(.*)\" height.*");

	public static Map<String, String> getImgByAjaxUrl(String keyword,
			Integer width, Integer height, Integer page, Integer start) {
		String requestUrl = MessageFormat.format(GOOGLE_AJAX_URL_TEMPLATE,
				keyword, page, start, page);
		if (width != 0 && height != 0) {
			requestUrl += String.format("&tbs=isz:ex,iszw:%d,iszh:%d", width,
					height);
		} else {
			// requestUrl += "&tbs=isz:m";
		}
		Log.d(Constants.TAG, requestUrl);

		Map<String, String> imageMap = new HashMap<String, String>();
		try {
			String response = HttpUtils.executeGet(requestUrl).trim();
			// Log.d(Constants.TAG, response);
			String[] imageDivs = response.split("a href");
			Log.d(Constants.TAG, "divs=" + imageDivs.length);
			Intent intent = new Intent(Constants.SET_PROGRESSBAR);
			intent.putExtra("prg_items", imageDivs.length);
			WatchApp.getInstance().sendBroadcast(intent);
			int count = 0;
			for (String imageDiv : imageDivs) {
				Log.d(Constants.TAG, imageDiv);

				Intent intent0 = new Intent(Constants.SET_PROGRESSBAR);
				intent0.putExtra("prg_item", ++count);
				if (count % 10 == 0)
					WatchApp.getInstance().sendBroadcast(intent0);

				try {
					Matcher m = googleScriptImgRegex.matcher(imageDiv);
					if (m.matches() && m.groupCount() == 3) {
						String url = m.group(2).trim();
						int index = url.indexOf("&amp;");
						if (index != -1) {
							// remove trash suffix
							url = url.substring(0, index);
						}
						Log.d(Constants.TAG, imageMap.size() + ") " + url);
						// imageMap.put(url, m.group(3).trim());
						imageMap.put(url,
								"http://images.google.com/images?q=tbn:"
										+ m.group(1).trim() + ":" + url);
					}
				} catch (Exception e) {
					Log.e(Constants.TAG, "get img error", e);
					continue;
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "get img error", e);
			return imageMap;
		}

		return imageMap;
	}

	private static Pattern imagesRegex = Pattern
			.compile(".*imgurl=(.*.[png|jpg|jpeg])&amp.*tbnid=(.*:)&amp;.*");

	public static Map<String, String> getImgByUrl(String keyword,
			Integer width, Integer height, Integer page, Integer start) {
		String requestUrl = MessageFormat.format(GOOGLE_IMG_URL_TEMPLATE,
				keyword, 20, start, width, height);
		Log.d(Constants.TAG, requestUrl);

		Map<String, String> imageMap = new HashMap<String, String>();
		try {
			String response = HttpUtils.executeGet(requestUrl).trim();
			// Log.d(Constants.TAG, response);
			String[] imageDivs = response.split("/imgres?");
			Log.d(Constants.TAG, "divs = " + imageDivs.length);
			for (String imageDiv : imageDivs) {
				try {
					Log.d(Constants.TAG, "div: " + imageDiv);
					Matcher m = imagesRegex.matcher(imageDiv);
					if (m.matches() && m.groupCount() == 2) {
						String url = m.group(1).trim();
						url = url.substring(0, url.indexOf("&amp;"));
						url = URLDecoder.decode(url);
						Log.d(Constants.TAG, imageMap.size() + "):" + url
								+ ", " + m.group(2).trim());
						imageMap.put(url, m.group(2).trim());
					}
				} catch (Exception e) {
					Log.e(Constants.TAG, "get img error", e);
					continue;
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "get img error", e);
			return imageMap;
		}
		return imageMap;
	}

	public static List<AdtImage> getImgListByAjax(String keyword,
			Integer width, Integer height, Integer page, Integer start) {
		Map<String, String> imgMaps = getImgByAjaxUrl(keyword, width, height,
				page, start);
		List<AdtImage> imgList = new ArrayList<AdtImage>();
		if (imgMaps != null && imgMaps.size() > 0) {
			for (String key : imgMaps.keySet()) {
				AdtImage imgInfo = new AdtImage(key, imgMaps.get(key));
				imgList.add(imgInfo);
			}
		}
		return imgList;
	}

        public static List<AdtImage> getImgListFromHtml(String keyword, int width, int height, int page, int start) {

		String requestUrl = MessageFormat.format(GOOGLE_AJAX_URL_TEMPLATE,
				keyword, page, start, page);
		if (width != 0 && height != 0) {
			requestUrl += String.format("&tbs=isz:ex,iszw:%d,iszh:%d", width,
					height);
		} else {
			// requestUrl += "&tbs=isz:m";
		}
		Log.d(Constants.TAG, requestUrl);

		List<AdtImage> imgList = new ArrayList<AdtImage>();
		try {
			String response = HttpUtils.executeGet(requestUrl).trim();
			// Log.d(Constants.TAG, response);
			String[] imageDivs = response.split("a href");
			Log.d(Constants.TAG, "divs=" + imageDivs.length);
			Intent intent = new Intent(Constants.SET_PROGRESSBAR);
			intent.putExtra("prg_items", imageDivs.length);
			WatchApp.getInstance().sendBroadcast(intent);
			int count = 0;
			for (String imageDiv : imageDivs) {
				// Log.d(Constants.TAG, imageDiv);
				Intent intent0 = new Intent(Constants.SET_PROGRESSBAR);
				intent0.putExtra("prg_item", ++count);
				if (count % 10 == 0)
					WatchApp.getInstance().sendBroadcast(intent0);

				try {   AdtImage image = null;
					Matcher m = Pattern.compile(".*imgurl=(.*.[png|jpg|jpeg])&amp.*").matcher(imageDiv);
					if (m.matches() && m.groupCount() == 1) {
						String url = m.group(1).trim();
						// Log.d(Constants.TAG, imgList.size() + ") " + url);
                                                image = new AdtImage(url);
					}
                                        m = Pattern.compile(".*tbnid=(.*):&amp.*").matcher(imageDiv);
                                        if (m.matches() && m.groupCount() == 1) {
                                                String tbnid = m.group(1).trim();
                                                if (image != null)
                                                        image.setThumbId(tbnid);
                                        }
                                        m = Pattern.compile(".*data-src=\"(.*)\" height=.*").matcher(imageDiv);
                                        if (m.matches() && m.groupCount() == 1) {
                                                String tbUrl = m.group(1).trim();
                                                if (image != null)
                                                        image.setThumbUrl(tbUrl);
                                        }
                                        m = Pattern.compile(".*imgrefurl=(.*)&amp.*").matcher(imageDiv);
                                        if (m.matches() && m.groupCount() == 1) {
                                                String url = m.group(1).trim();
                                                if (image != null)
                                                        image.setImgRefUrl(url);
                                        }
                                        if (image != null)
                                                imgList.add(image);

				} catch (Exception e) {
					Log.e(Constants.TAG, "get img error", e);
					continue;
				}
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, "get img error", e);
			return imgList;
		}

		return imgList;

        }
}
