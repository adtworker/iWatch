package com.adtworker.mail.service.entity;

public class ImgInfo {

	private String url;
	private String previewUrl;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ImgInfo() {
		super();
	}
	public ImgInfo(String sourceUrl, String previewUrl) {
		super();
		this.url = sourceUrl;
		this.previewUrl = previewUrl;
	}
	private static Integer tagId = 0;
	public String getTagId() {
		tagId = tagId + 1;
		return String.valueOf(tagId);
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}
}
