package io.kuz.screentracker;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class DatabaseWriterService extends IntentService {

	public static final String ACTION_SCREEN_ON = "ACTION_SCREEN_ON";
	public static final String ACTION_SCREEN_OFF = "ACTION_SCREEN_OFF";
	public static final String ACTION_REMOVE_ENTRY = "ACTION_REMOVE_ENTRY";
	public static final String ACTION_CLEAR_ALL = "ACTION_CLEAR_ALL";

	private static final String KEY_ENTRY_ID = "ENTRY_ID";

	public DatabaseWriterService() {
		super("DatabaseWriterService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			return;
		}

		if (intent.getAction().equals(ACTION_SCREEN_ON)) {
			ContentValues values = new ContentValues();
			values.put(ScreenTrackingContract.Column.TIME.sqlName(), System.currentTimeMillis());
			getContentResolver().insert(ScreenTrackingContract.SCREEN_TRACKING_URI, values);
		} else if (intent.getAction().equals(ACTION_REMOVE_ENTRY)) {
			ContentValues values = new ContentValues();
			int id = intent.getIntExtra(KEY_ENTRY_ID, -1);
			values.put(ScreenTrackingContract.Column.ID.sqlName(), id);
			values.put(ScreenTrackingContract.Column.TIME_REMOVED.sqlName(), System.currentTimeMillis());

			String selection = ScreenTrackingContract.Column.ID.sqlName() + "=?";
			String[] selectionArgs = new String[]{Integer.toString(id)};

			Cursor cursor = null;

			try {
				cursor = getContentResolver().query(ScreenTrackingContract.SCREEN_TRACKING_URI,
						new String[]{ScreenTrackingContract.Column.ID.sqlName()},
						selection,
						selectionArgs, null);

				if (cursor.moveToFirst()) {
					getContentResolver().update(ScreenTrackingContract.SCREEN_TRACKING_URI, values, selection, selectionArgs);
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		} else if (intent.getAction().equals(ACTION_CLEAR_ALL)) {
			ContentValues values = new ContentValues();
			values.put(ScreenTrackingContract.Column.TIME_REMOVED.sqlName(), System.currentTimeMillis());
			getContentResolver().update(ScreenTrackingContract.SCREEN_TRACKING_URI, values, null, null);
		}
	}

	public static void screenOff(Context context) {
		Intent intent = new Intent(context, DatabaseWriterService.class);
		intent.setAction(ACTION_SCREEN_OFF);
		context.startService(intent);
	}

	public static void screenOn(Context context) {
		Intent intent = new Intent(context, DatabaseWriterService.class);
		intent.setAction(ACTION_SCREEN_ON);
		context.startService(intent);
	}

	public static void removeEntry(Context context, int id) {
		Intent intent = new Intent(context, DatabaseWriterService.class);
		intent.setAction(ACTION_REMOVE_ENTRY);
		intent.putExtra(KEY_ENTRY_ID, id);
		context.startService(intent);
	}

	public static void clearAll(Context context) {
		Intent intent = new Intent(context, DatabaseWriterService.class);
		intent.setAction(ACTION_CLEAR_ALL);
		context.startService(intent);
	}
}
