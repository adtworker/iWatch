package com.adtworker.mail.util;

import java.text.NumberFormat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.adtworker.mail.R;
import com.adtworker.mail.WatchApp;

public class Notifications {
	final static String TAG = "iWatch-Notifications";

	public static void showDownloadingNotification(Context context,
			String title, int max, int progress, int id, boolean indeterminate) {
		Notification msg = new Notification(
				android.R.drawable.stat_sys_download, null,
				System.currentTimeMillis());
		msg.flags = msg.flags | Notification.FLAG_ONGOING_EVENT;

		RemoteViews contentView = new RemoteViews(context.getPackageName(),
				R.layout.download_progress_notify);
		contentView.setImageViewResource(R.id.content_view_image,
				R.drawable.icon);
		contentView.setTextViewText(R.id.content_view_text, title);
		contentView.setTextViewText(R.id.content_view_percentage, NumberFormat
				.getPercentInstance().format((double) progress / max));
		contentView.setProgressBar(R.id.content_view_progressbar, max,
				progress, indeterminate);
		msg.contentView = contentView;

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				null, 0);
		msg.contentIntent = contentIntent;

		NotificationManager mNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.notify(id, msg);
	}

	public static void showNotification(Context context, String title, String content, int id) {

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, WatchApp.class), 0);
		Notification msg = new Notification(
				android.R.drawable.stat_sys_download, null,
				System.currentTimeMillis());
		msg.setLatestEventInfo(context, title, content, contentIntent);

		NotificationManager mNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.notify(id, msg);
	}

	public static void showDownloadCompletedNotification(Context context,
			String title, int id) {
		Log.d(TAG, "showUploadCompletedNotification");
		Notification msg = new Notification(R.drawable.icon, null,
				System.currentTimeMillis());
		msg.flags = msg.flags | Notification.FLAG_AUTO_CANCEL;

		RemoteViews contentView = new RemoteViews(context.getPackageName(),
				R.layout.download_progress_notify);
		contentView.setImageViewResource(R.id.content_view_image,
				R.drawable.icon);
		contentView.setTextViewText(R.id.content_view_text, title);
		contentView.setTextViewText(R.id.content_view_percentage, "100%");
		contentView.setProgressBar(R.id.content_view_progressbar, 100, 100,
				false);
		msg.contentView = contentView;

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(), 0);
		msg.contentIntent = contentIntent;

		NotificationManager mNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.notify(id, msg);
	}

	public static void clearNotification(Context context, int id) {
		NotificationManager mNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.cancel(id);
	}

	public static void clearAllNotifications(Context context) {
		NotificationManager mNM = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.cancelAll();
	}
}
