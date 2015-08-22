package io.kuz.screentracker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class ScreenTrackingContract {
	public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".ScreenTrackingProvider";
	public static final String PATH_SCREEN_TRACKING = "screenTracking";
	private static final String SQL_TYPE_LONG = "INTEGER";
	private static final String SCHEME = "content://";

	public static final int LIST_TIMES_LOADER_ID = 1;
	public static final int LIST_TRASH_LOADER_ID = 2;

	public static final Uri SCREEN_TRACKING_URI
			= Uri.parse(SCHEME + AUTHORITY + "/" + PATH_SCREEN_TRACKING + "/");

	public enum Column {
		ID("_id", "INTEGER PRIMARY KEY"),
		TIME("time", SQL_TYPE_LONG),
		TIME_REMOVED("time_removed", SQL_TYPE_LONG, "DEFAULT 0");

		private final String sqlName;
		private final String sqlType;
		private final String extras;

		Column(String sqlName, String sqlType) {
			this(sqlName, sqlType, "");
		}

		Column(String sqlName, String sqlType, String extras) {
			this.sqlName = sqlName;
			this.sqlType = sqlType;
			this.extras = extras;
		}

		public String sqlName() {
			return sqlName;
		}

		public String getSqlType() {
			return sqlType;
		}

		public String getExtras() {
			return extras;
		}
	}

	public static Loader<Cursor> createCursorLoader(Context context, boolean removed, Column... columns) {
		String[] projection = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			projection[i] = columns[i].sqlName();
		}

		String selection;
		if (removed) {
			selection = Column.TIME_REMOVED.sqlName + "<>?";
		} else {
			selection = Column.TIME_REMOVED.sqlName + "=?";
		}
		List<String> argsList = new ArrayList<String>();
		argsList.add("0");

		return new CursorLoader(context,
				SCREEN_TRACKING_URI,
				projection,
				selection,
				argsList.toArray(new String[argsList.size()]),
				ScreenTrackingProvider.DEFAULT_SORT_ORDER);
	}
}
