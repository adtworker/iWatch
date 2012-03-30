package com.adtworker.mail.service;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Message;

import com.adtworker.mail.service.data.ImgManager;
import com.adtworker.mail.service.entity.ImgLoadSupporter;
import com.adtworker.mail.service.thread.ThreadProvider;
import com.adtworker.mail.service.thread.WorkerTask;

class ServiceImg {
	private final ArrayList<ImgLoadSupporter> imgLoadSupporterInTaskList = new ArrayList<ImgLoadSupporter>();
	private final ArrayList<ImgLoadSupporter> localImgLoadSupporterInTaskList = new ArrayList<ImgLoadSupporter>();
	private final ImgManager imgManager;
	private boolean isRunning = false;
	private boolean isLocalRunning = false;

	protected ServiceImg() {
		imgManager = new ImgManager();
	}

	protected void addTask(ImgLoadSupporter newImgLoadSupporter) {
		synchronized (imgLoadSupporterInTaskList) {
			if (newImgLoadSupporter != null) {
				imgLoadSupporterInTaskList.add(newImgLoadSupporter);
			}
			if ((imgLoadSupporterInTaskList.size() > 0) && !isRunning) {
				ThreadProvider.getInstance().scheduleTask(new ImgGetter());
			}
		}
	}

	protected void runNewTask(ImgLoadSupporter localImgLoadSupporter) {
		synchronized (localImgLoadSupporterInTaskList) {
			if (localImgLoadSupporterInTaskList != null) {
				localImgLoadSupporterInTaskList.add(localImgLoadSupporter);
			}
			if ((localImgLoadSupporterInTaskList.size() > 0) && !isLocalRunning) {
				ThreadProvider.getInstance().scheduleTask(new LocalImgGetter());
			}
		}
	}

	protected void runNewTask(
			ArrayList<ImgLoadSupporter> localImgLoadSupporterList) {
		synchronized (localImgLoadSupporterInTaskList) {
			while (localImgLoadSupporterList.size() > 0) {
				localImgLoadSupporterInTaskList.add(localImgLoadSupporterList
						.remove(0));
			}
			int size = localImgLoadSupporterInTaskList.size();
			if ((size > 0) && !isLocalRunning) {
				// Log.e("localImgTask  SIZE:", "" + size);
				ThreadProvider.getInstance().scheduleTask(new LocalImgGetter());
			}
		}
	}

	class LocalImgGetter extends WorkerTask {

		@Override
		public void onStart() {
			super.onStart();
			isLocalRunning = true;
		}

		@Override
		public void run() {
			super.run();
			while (localImgLoadSupporterInTaskList.size() > 0) {
				ImgLoadSupporter tempImgLoadSupporter = localImgLoadSupporterInTaskList
						.remove(0);
				if (tempImgLoadSupporter != null) {
					String url = tempImgLoadSupporter.url;
					CallbackHandler callbackHandler = tempImgLoadSupporter.callbackHandler;
					if ((url == null) || equals("")) {
						return;
					}
					Bitmap bitmap = null;
					try {
						bitmap = imgManager.getImg(url);
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if ((bitmap != null)
							&& (callbackHandler != null)
							&& callbackHandler.callBackType
									.equals(Service.CallbackType.img)) {
						Message msg = new Message();
						msg.obj = bitmap;
						bitmap = null;
						callbackHandler.sendMessage(msg);
						tempImgLoadSupporter = null;
					}
				}
			}

		}

		@Override
		public void onFinish() {
			super.onFinish();
			isLocalRunning = false;
		}

	}

	class ImgGetter extends WorkerTask {

		@Override
		public void onStart() {
			super.onStart();
			isRunning = true;
		}

		@Override
		public void run() {
			super.run();
			while (imgLoadSupporterInTaskList.size() > 0) {
				ImgLoadSupporter tempImgLoadSupporter = imgLoadSupporterInTaskList
						.remove(0);
				if (tempImgLoadSupporter != null) {
					String url = tempImgLoadSupporter.url;
					CallbackHandler callbackHandler = tempImgLoadSupporter.callbackHandler;
					if ((url == null) || equals("")) {
						return;
					}
					Bitmap bitmap = null;
					try {
						bitmap = imgManager.getImg(url);
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if ((bitmap != null) && (callbackHandler != null)) {
						Message msg = new Message();
						msg.obj = bitmap;
						bitmap = null;
						callbackHandler.sendMessage(msg);
					}
				}
			}
		}
		@Override
		public void onFinish() {
			super.onFinish();
			isRunning = false;
		}
	}
}
