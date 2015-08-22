package io.kuz.screentracker;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

	private MainActivity mainActivity;
	private Adapter cursorAdapter;
	private Context context;

	private class Adapter extends CursorAdapter {

		private int entryIdColumnIndex = -1;
		private int timeColumnIndex = -1;

		public Adapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();

			final long timeStamp = cursor.getLong(timeColumnIndex);
			final int id = cursor.getInt(entryIdColumnIndex);
			String time = new SimpleDateFormat("dd-MMM-yy HH:mm").format(new Date(timeStamp));
			holder.time.setText(time);
			holder.id = id;
		}

		public Filter getFilter() {
			return super.getFilter();
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			ViewHolder holder;
			View view = mainActivity.getLayoutInflater().inflate(R.layout.list_item, parent, false);
			holder = new ViewHolder(view);

			view.setTag(holder);

			if (entryIdColumnIndex == -1 || timeColumnIndex == -1) {
				entryIdColumnIndex = cursor.getColumnIndex(ScreenTrackingContract.Column.ID.sqlName());
				timeColumnIndex = cursor.getColumnIndex(ScreenTrackingContract.Column.TIME.sqlName());
			}

			return view;
		}
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView time;
		int id;

		public ViewHolder(View itemView) {
			super(itemView);
			time = (TextView) itemView.findViewById(R.id.time);
		}

		public int getId() {
			return id;
		}
	}

	public RecyclerAdapter(MainActivity activity, Context context, Cursor cursor) {
		mainActivity = activity;
		cursorAdapter = new Adapter(context, cursor, 0);
		this.context = context;
	}

	public void changeCursor(Cursor cursor) {
		cursorAdapter.changeCursor(cursor);
		notifyDataSetChanged();
	}

	public int getItemCount() {
		return cursorAdapter.getCount();
	}

	public void onBindViewHolder(ViewHolder viewholder, int i) {
		cursorAdapter.getCursor().moveToPosition(i);
		cursorAdapter.bindView(viewholder.itemView, context, cursorAdapter.getCursor());
	}

	public ViewHolder onCreateViewHolder(ViewGroup viewgroup, int i) {
		return new ViewHolder(cursorAdapter.newView(context, cursorAdapter.getCursor(), viewgroup));
	}

}
