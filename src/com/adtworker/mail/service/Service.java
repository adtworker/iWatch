package com.adtworker.mail.service;

import java.util.ArrayList;

import android.content.Context;

import com.adtworker.mail.service.entity.ImgLoadSupporter;
import com.adtworker.mail.service.thread.ThreadProvider;

public class Service {

	protected Context context;
	private final ServiceImg serviceImgDownloader;

	public enum CallbackType {
		shop, product, wallProduct, img, user
	}

	private static Service service;

	public static Service getInstance(Context context) {
		if (service == null) {
			service = new Service(context);
		}
		return service;
	}

	private Service(Context context) {
		this.context = context;
		serviceImgDownloader = new ServiceImg();
	}

	public void loadImgFromLocal(ImgLoadSupporter localImgLoadSupporter) {
		serviceImgDownloader.runNewTask(localImgLoadSupporter);
	}

	public void loadImgFromLocal(
			ArrayList<ImgLoadSupporter> localImgLoadSupporterList) {
		serviceImgDownloader.runNewTask(localImgLoadSupporterList);
	}

	public void loadImg(ImgLoadSupporter imgLoadSupporter) {
		serviceImgDownloader.addTask(imgLoadSupporter);
	}

	public void destroy() {
		ThreadProvider.getInstance().removeAllTask();
	}
}
