// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package io.kuz.screentracker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private RecyclerAdapter adapter;
	private DrawerLayout drawerLayout;
	private int expectedLoader;
	Toolbar toolbar;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ListView drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_item, new String[]{"Logs", "Trash"}));

		final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

		drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						expectedLoader = ScreenTrackingContract.LIST_TIMES_LOADER_ID;
						setTitle(R.string.app_name);
						toolbar.setBackgroundColor(getResources().getColor(R.color.blueDark));
						recyclerView.setBackgroundDrawable(null);
						break;

					case 1:
						expectedLoader = ScreenTrackingContract.LIST_TRASH_LOADER_ID;
						setTitle(R.string.trash);
						toolbar.setBackgroundColor(getResources().getColor(R.color.red));
						Drawable trash = getResources().getDrawable(R.drawable.trash);
						trash.setColorFilter(0x22222222, PorterDuff.Mode.MULTIPLY);
						recyclerView.setBackgroundDrawable(trash);
						break;
				}
				getSupportLoaderManager().restartLoader(expectedLoader, null, MainActivity.this);
				drawerLayout.closeDrawers();
			}
		});

		Intent intent = new Intent(this, TrackerService.class);
		startService(intent);


		recyclerView.setHasFixedSize(true);

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);

		adapter = new RecyclerAdapter(this, this, null);
		recyclerView.setAdapter(adapter);
		ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
				RecyclerAdapter.ViewHolder holder = (RecyclerAdapter.ViewHolder) viewHolder.itemView.getTag();
				DatabaseWriterService.removeEntry(MainActivity.this, holder.getId());
			}
		};
		ItemTouchHelper touchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		touchHelper.attachToRecyclerView(recyclerView);

		expectedLoader = ScreenTrackingContract.LIST_TIMES_LOADER_ID;
		getSupportLoaderManager().initLoader(ScreenTrackingContract.LIST_TIMES_LOADER_ID, null, this);
		getSupportLoaderManager().initLoader(ScreenTrackingContract.LIST_TRASH_LOADER_ID, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_exit) {
			stopService(new Intent(this, TrackerService.class));
			finish();
		}

		if (id == R.id.action_clear_all) {
			DatabaseWriterService.clearAll(MainActivity.this);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final boolean removed;
		switch(id) {
			case ScreenTrackingContract.LIST_TIMES_LOADER_ID:
				removed = false;
				break;

			case ScreenTrackingContract.LIST_TRASH_LOADER_ID:
				removed = true;
				break;

			default:
				throw new RuntimeException("Unknown loader");
		}

		return ScreenTrackingContract.createCursorLoader(this,
				removed,
				ScreenTrackingContract.Column.ID,
				ScreenTrackingContract.Column.TIME);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (loader.getId() == expectedLoader) {
			adapter.changeCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
	}
}
