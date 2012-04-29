package com.adtworker.mail;

import java.net.URLDecoder;

public class AdtImage {

	boolean isAsset = false;
	boolean hasThumb = false;
	boolean isCached = false;

	String urlFull;
	String urlThumb;

	public AdtImage() {

	}

	public AdtImage(String url, boolean is_asset) {
		urlFull = decode(url);
		isAsset = is_asset;
	}

	public AdtImage(String url) {
		urlFull = decode(url);
	}

	public AdtImage(String url, String urlTb) {
		urlFull = decode(url);
		if (urlTb.contains("&")) {
			urlTb = urlTb.substring(0, urlTb.indexOf("&"));
		}
		urlThumb = decode(urlTb);
		hasThumb = true;
	}

	private String decode(String url) {
		while (url.contains("%25")) {
			url = URLDecoder.decode(url);
		}
		return url;
	}
}
