package io.kuz.screentracker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

public class ScreenTrackingProvider extends ContentProvider {

	private static final String DATABASE_NAME = "tracker.db";
	private static final int DATABASE_VERSION = 2;
	public static final String DEFAULT_SORT_ORDER = ScreenTrackingContract.Column.TIME.sqlName() + " DESC";

	private static final String TABLE_NAME = "screen";

	private static final UriMatcher sUriMatcher;
	private static final int SCREEN_TRACKER_URI_ID = 1;
	private DatabaseHelper openHelper;

	private static final HashMap<String, String> sScreenTrackingProjectionMap;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(ScreenTrackingContract.AUTHORITY, ScreenTrackingContract.PATH_SCREEN_TRACKING, SCREEN_TRACKER_URI_ID);

		sScreenTrackingProjectionMap = generateProjectionMap(ScreenTrackingContract.Column.values());
	}

	private static HashMap<String, String> generateProjectionMap(ScreenTrackingContract.Column... columns) {
		HashMap<String, String> res = new HashMap<>();
		for (ScreenTrackingContract.Column column : columns) {
			res.put(column.sqlName(), column.sqlName());
		}
		return res;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			StringBuilder sqlText = new StringBuilder();

			sqlText.append("CREATE TABLE " + TABLE_NAME + " (");
			String prefix = "";
			for (ScreenTrackingContract.Column column : ScreenTrackingContract.Column.values()) {
				sqlText.append(prefix);
				sqlText.append(column.sqlName());
				sqlText.append(" ");
				sqlText.append(column.getSqlType());
				sqlText.append(" ");
				sqlText.append(column.getExtras());
				prefix = ", ";
			}
			sqlText.append(");");

			db.execSQL(sqlText.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
				case 1:
					db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " +
							ScreenTrackingContract.Column.TIME_REMOVED.sqlName() + " " +
							ScreenTrackingContract.Column.TIME_REMOVED.getSqlType() + " DEFAULT 0");
					break;
			}
		}
	}

	@Override
	public boolean onCreate() {
		openHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
			case SCREEN_TRACKER_URI_ID:
				qb.setProjectionMap(sScreenTrackingProjectionMap);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}


		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = openHelper.getWritableDatabase();

		Cursor c = qb.query(
				db,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				orderBy
		);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != SCREEN_TRACKER_URI_ID) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		if (!values.containsKey(ScreenTrackingContract.Column.TIME.sqlName())) {
			throw new IllegalArgumentException("Unknown time to insert " + uri);
		}

		SQLiteDatabase db = openHelper.getWritableDatabase();

		long rowId = db.insert(TABLE_NAME, null, values);

		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(ScreenTrackingContract.SCREEN_TRACKING_URI, null);
			return ScreenTrackingContract.SCREEN_TRACKING_URI;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;

		switch (sUriMatcher.match(uri)) {
			case SCREEN_TRACKER_URI_ID:
				count = db.update(
						TABLE_NAME,
						values,
						selection,
						selectionArgs
				);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(ScreenTrackingContract.SCREEN_TRACKING_URI, null);
		return count;
	}
}
