package com.adtworker.mail.service;

import android.os.Handler;

public class CallbackHandler extends Handler {
	public enum CallbackType {
		shop, product, wallProduct, img, user
	}
	public CallbackType callBackType;

	public CallbackHandler(CallbackType type) {
		callBackType = type;
	}

}
