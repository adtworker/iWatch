package com.adtworker.mail;

import java.util.List;

import com.adtworker.mail.service.entity.ImgInfo;

public class ImageSearchAdapter {

	public static List<ImgInfo> getImgList(String keyword, Integer width,
			Integer height, Integer start, Integer size) {
		return GoogleImage
				.getImgListByAjax(keyword, width, height, start, size);
	}

}
