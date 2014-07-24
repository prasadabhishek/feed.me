package com.feed.me;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class HistoryActivity extends ListActivity {

	private ArrayAdapter<String> adapter;
	private Context mCtx = this;
	private String SEARCH_TOKEN = "Google Search";
	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private int max_history;
	Boolean doUpdate = Boolean.FALSE;
	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				DragSortListView list = getListView();
				String item = adapter.getItem(from);
				adapter.remove(item);
				adapter.insert(item, to);
				// list.moveCheckState(from, to);
			}
		}
	};

	private RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			DragSortListView list = getListView();
			String item = adapter.getItem(which);
			adapter.remove(item);
			CustomSQLiteOpenHelper sql = new CustomSQLiteOpenHelper(mCtx);
			sql.insertRow(item, appWidgetId);
			doUpdate = Boolean.TRUE;
			// list.removeCheckState(which);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historylistlayout);

		assignAppWidgetId();

		// sql.deleteSelectedHistory();

		max_history = getPref(this, "history_" + appWidgetId);

		ArrayList<String> arrayList;

		arrayList = new ArrayList<String>(getBrowserHistory().keySet());
		/* initialize the selected Db */

		adapter = new ArrayAdapter<String>(this, R.layout.list_item_checkable,
				R.id.text, arrayList);
		setListAdapter(adapter);
		DragSortListView list = getListView();
		list.setDropListener(onDrop);
		list.setRemoveListener(onRemove);
	}

	@Override
	public DragSortListView getListView() {
		return (DragSortListView) super.getListView();
	}

	/**
	 * Widget configuration activity,always receives appwidget Id appWidget Id =
	 * unique id that identifies your widget analogy : same as setting view id
	 * via @+id/viewname on layout but appwidget id is assigned by the system
	 * itself
	 */
	private void assignAppWidgetId() {
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onBackPressed() {
		for (int i = 0; i < adapter.getCount(); i++) {
			Log.d("item at : " + String.valueOf(i), adapter.getItem(i));
		}
		setPref(mCtx, "history_cutomized_" + String.valueOf(appWidgetId), 1);
		/* update the widget */
		if (doUpdate)
			new WidgetProvider().onUpdate(mCtx,
					AppWidgetManager.getInstance(mCtx),
					new int[] { appWidgetId });
		finish();
	}

	public LinkedHashMap<String, String> getBrowserHistory() {
		LinkedHashMap<String, String> hash = new LinkedHashMap<String, String>();
		String title;
		String temp, temp_with_html;
		Integer count = 0;
		CustomSQLiteOpenHelper sql = new CustomSQLiteOpenHelper(mCtx);
		Cursor mCur = mCtx.getContentResolver().query(Browser.BOOKMARKS_URI,
				Browser.HISTORY_PROJECTION, null, null,
				Browser.BookmarkColumns.DATE + " DESC");
		mCur.moveToFirst();
		if (mCur.moveToFirst() && mCur.getCount() > 0) {
			while (mCur.isAfterLast() == false && count < max_history) {
				title = mCur.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX);
				if (title.contains(SEARCH_TOKEN)) {
					temp = title.subSequence(0,
							((title.length() - SEARCH_TOKEN.length()) - 3))
							.toString();
					temp = temp.trim();
					temp_with_html = temp.replaceAll(" ", "%20");
					if (!hash.containsKey(temp)
							&& !sql.isDeleted(temp, appWidgetId)) {
						hash.put(temp, temp_with_html);
						count++;
					}
				}
				mCur.moveToNext();
			}
		}
		return hash;
	}

	public static Integer getPref(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Integer count = prefs.getInt(key, 1);
		return count;
	}

	public static void setStringArrayPref(Context context, String key,
			ArrayList<String> values) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		JSONArray a = new JSONArray();
		for (int i = 0; i < values.size(); i++) {
			a.put(values.get(i));
		}
		if (!values.isEmpty()) {
			editor.putString(key, a.toString());
		} else {
			editor.putString(key, null);
		}
		editor.commit();
	}

	public static void setPref(Context context, String key, Integer val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Integer count = Integer.valueOf(val);
		prefs.edit().putInt(key, count).commit();
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		String TABLE_NAME = "Selected_History";
		String COLLUMN_ROW_ID = "Id";
		String COLLUMN_TOPIC = "Topic";
		String COLLUMN_WIDGET_ID = "WidgetId";
		String COLLUMN_TIMESTAMP = "TimeStamp";
		String COLLUMN_FLAG = "Flag";

		private static final String DATABASE_NAME = "feedme.db";
		private static final int DATABASE_VERSION = 1;
		SQLiteDatabase mDb;

		public CustomSQLiteOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mCtx = context;
		}

		// TODO: override the constructor and other methods for the parent class
		@Override
		public void onCreate(SQLiteDatabase db) {
			// the SQLite query string that will create our 3 column database
			// table.
			// execute the query string to the database.
			try {
			} catch (Exception e) {
			}
			// execute the query string to the database.
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
			// OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE
			// FROM OLDER VERSIONS.
		}

		public void insertRow(String val, int id) {
			mDb = new CustomSQLiteOpenHelper(mCtx).getWritableDatabase();
			ContentValues values = new ContentValues();
			Cursor c = null;
			// this is how you add a value to a ContentValues object
			// we are passing in a key string and a value string
			// each time
			values.put(COLLUMN_TOPIC, val);
			values.put(COLLUMN_WIDGET_ID, id);
			values.put(COLLUMN_TIMESTAMP, (int) (new Date().getTime() / 1000));
			values.put(COLLUMN_FLAG, 0);
			try {
				c = mDb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
						+ COLLUMN_TOPIC + " =? ", null);
			} catch (Exception e) {
				Log.e("DB ERROR", e.toString());
			}
			if (c.getCount() == 0) {
				// ask the database object to insert the new data
				try {
					mDb.insert(TABLE_NAME, null, values);
				} catch (Exception e) {
					Log.e("DB ERROR", e.toString()); // prints the error
														// message to
														// the log
				}
			}
			mDb.close();
		}

		public Boolean isDeleted(String val, int id) {
			mDb = new CustomSQLiteOpenHelper(mCtx).getWritableDatabase();
			Cursor flag = null;
			String sql = " SELECT " + COLLUMN_FLAG + " FROM " + TABLE_NAME
					+ " WHERE " + COLLUMN_TOPIC + "=?" + " AND "
					+ COLLUMN_WIDGET_ID + "=?";
			String[] whereArgs = new String[] { val, String.valueOf(id) };
			try {
				flag = mDb.rawQuery(sql, whereArgs);
				if (flag.getCount() > 0)
					return Boolean.TRUE;
			} catch (Exception e) {
				Log.e("DB ERROR", e.toString());
			}
			mDb.close();
			return Boolean.FALSE;
		}

	}
}
