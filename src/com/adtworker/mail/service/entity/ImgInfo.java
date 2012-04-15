package com.adtworker.mail.service.entity;

public class ImgInfo {

	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ImgInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ImgInfo(String givenUrl) {
		super();
		this.url = givenUrl;
	}
	private static Integer tagId = 0;
	public String getTagId() {
		tagId = tagId + 1;
		return String.valueOf(tagId);
	}
}
