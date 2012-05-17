package com.adtworker.mail;

import java.net.URLDecoder;

public class AdtImage {

	long byteLocal = 0;
	long byteRemote = 0;
	boolean isAsset = false;
	boolean hasThumb = false;

	private String urlFull;
	private String urlThumb;
	private String urlImgRef;
	private String idThumb;
	private boolean bCached = false;
	private static boolean bTbnidFirst = true;

	public static void setTbnIdFirst(boolean tbnidFirst) {
		bTbnidFirst = tbnidFirst;
	}

	public AdtImage() {

	}

	public AdtImage(String url, boolean is_asset) {
		urlFull = removeTrashTail(decode(url));
		isAsset = is_asset;
	}

	public AdtImage(String url) {
		urlFull = removeTrashTail(decode(url));
	}

	public AdtImage(String url, String urlTb) {
		urlFull = removeTrashTail(decode(url));
		urlThumb = removeTrashTail(decode(urlTb));
		hasThumb = true;
	}

	public void setThumbId(String tbnid) {
		if (tbnid == null || tbnid.isEmpty())
			return;
		idThumb = removeTrashTail(tbnid);
		hasThumb = true;
	}

	public void setThumbUrl(String url) {
		if (url == null || url.isEmpty())
			return;
		urlThumb = removeTrashTail(url);
		hasThumb = true;
	}

	public void setImgRefUrl(String url) {
		if (url == null || url.isEmpty())
			return;
		urlImgRef = removeTrashTail(decode(url));
	}

	public String getTbnUrl() {
		if (!hasThumb || isAsset)
			return null;

		String tbUrlTemplate = "http://images.google.com/images?q=tbn:%s:%s";

		if (bTbnidFirst && idThumb != null && !idThumb.isEmpty())
			return String.format(tbUrlTemplate, idThumb, urlFull);

		else if (urlThumb != null && !urlThumb.isEmpty())
			return urlThumb;

		else if (idThumb != null && !idThumb.isEmpty())
			return String.format(tbUrlTemplate, idThumb, urlFull);
		else
			return null;
	}

	public String getFullUrl() {
		if (!urlFull.isEmpty())
			return urlFull;
		else
			return null;
	}

	public String getImgRefUrl() {
		if (urlImgRef != null && !urlImgRef.isEmpty())
			return urlImgRef;
		else
			return null;
	}

	private String decode(String url) {
		while (url.contains("%25") || url.contains("%")) {
			url = URLDecoder.decode(url);
		}
		return url;
	}

	private String removeTrashTail(String url) {
		int index = url.indexOf("&amp;");
		if (index != -1)
			url = url.substring(0, index);
		return url;
	}

	public boolean isCached() {
		return bCached;
	}

	public void setCached(boolean cached) {
		bCached = cached;
	}
}
