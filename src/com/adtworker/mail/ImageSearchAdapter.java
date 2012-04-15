package com.adtworker.mail;

import java.util.List;

public class ImageSearchAdapter {

	public static List<AdtImage> getImgList(String keyword, Integer width,
			Integer height, Integer start, Integer size) {
		return GoogleImage
				.getImgListByAjax(keyword, width, height, start, size);
	}

}
