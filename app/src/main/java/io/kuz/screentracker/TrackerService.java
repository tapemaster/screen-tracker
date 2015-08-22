package io.kuz.screentracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class TrackerService extends Service {

	private NotificationManager notificationManager;
	private ScreenStateReceiver screenStateReceiver;
	private static final int NOTIFICATION_ID = 1;

	@Override
	public void onCreate() {
		IntentFilter screenStateFilter = new IntentFilter();
		screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenStateReceiver = new ScreenStateReceiver();
		registerReceiver(screenStateReceiver, screenStateFilter);

		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification not = new Notification.Builder(this)
				.setSmallIcon(R.drawable.clock)
				.setContentText("ScreenTracker running")
				.setContentTitle("ScreenTracker")
				.setOngoing(true)
				.setWhen(System.currentTimeMillis())
				.setContentIntent(contentIntent)
				.build();

		notificationManager.notify(NOTIFICATION_ID, not);

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		killNotification();
		unregisterReceiver(screenStateReceiver);
		super.onDestroy();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		killNotification();
		super.onTaskRemoved(rootIntent);
	}

	private void killNotification() {
		notificationManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
