package com.adtworker.mail;

import java.net.URLDecoder;

public class AdtImage {

	long byteLocal = 0;
	long byteRemote = 0;
	boolean isAsset = false;
	boolean hasThumb = false;

	private String urlFull;
	private String urlThumb;
	private String idThumb;
	private boolean bCached = false;

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

	public void setThumbId(String tbnid) {
		if (tbnid == null || tbnid.isEmpty())
			return;
		idThumb = tbnid;
		hasThumb = true;
	}

	public String getTbnUrl() {
		if (!hasThumb || isAsset)
			return null;

		if (!urlThumb.isEmpty())
			return urlThumb;
		else if (!idThumb.isEmpty())
			return "http://images.google.com/images?q=tbn:" + idThumb + ":"
					+ urlFull;
		else
			return null;
	}

	public String getFullUrl() {
		if (!urlFull.isEmpty())
			return urlFull;
		else
			return null;
	}

	private String decode(String url) {
		while (url.contains("%25")) {
			url = URLDecoder.decode(url);
		}
		return url;
	}

	public boolean isCached() {
		return bCached;
	}

	public void setCached(boolean cached) {
		bCached = cached;
	}
}
