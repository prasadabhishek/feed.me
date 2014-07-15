package com.feed.me;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class HistoryActivity extends ListActivity {

	private ArrayAdapter<String> adapter;
	private Context mCtx = this;
	private String SEARCH_TOKEN = "Google Search";

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				DragSortListView list = getListView();
				String item = adapter.getItem(from);
				adapter.remove(item);
				adapter.insert(item, to);
				list.moveCheckState(from, to);
			}
		}
	};

	private RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			DragSortListView list = getListView();
			String item = adapter.getItem(which);
			adapter.remove(item);
			list.removeCheckState(which);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historylistlayout);

		ArrayList<String> arrayList = new ArrayList<String>(getBrowserHistory());

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

	public ArrayList<String> getBrowserHistory() {
		ArrayList<String> result = new ArrayList<String>();
		String title;
		String temp;
		Cursor mCur = mCtx.getContentResolver().query(Browser.BOOKMARKS_URI,
				Browser.HISTORY_PROJECTION, null, null,
				Browser.BookmarkColumns.DATE + " DESC");
		mCur.moveToFirst();
		if (mCur.moveToFirst() && mCur.getCount() > 0) {
			while (mCur.isAfterLast() == false) {
				title = mCur.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX);
				if (title.contains(SEARCH_TOKEN)) {
					temp = title.subSequence(0,
							((title.length() - SEARCH_TOKEN.length()) - 3))
							.toString();
					temp = temp.trim();
					temp = temp.replaceAll(" ", "%20");
					if (!result.contains(temp))
						result.add(temp);
				}
				// Log.v("urlIdx",
				// mCur.getString(Browser.HISTORY_PROJECTION_URL_INDEX));
				mCur.moveToNext();
			}
		}
		return result;
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		String TABLE_NAME = "History";
		String COLLUMN_ROW_ID = "Id";
		String COLLUMN_TOPIC = "Topic";
		String COLLUMN_FLAG = "Flag";
		String COLLUMN_TIMESTAMP = "TimeStamp";

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
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
			// OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE
			// FROM OLDER VERSIONS.

		}

		public void getHistory(ArrayList<String> list) {
			mDb = new CustomSQLiteOpenHelper(mCtx).getWritableDatabase();
			for (int i = 0; i < list.size(); i++) {
				ContentValues values = new ContentValues();
				// this is how you add a value to a ContentValues object
				// we are passing in a key string and a value string
				// each time
				values.put(COLLUMN_TOPIC, list.get(i));
				values.put(COLLUMN_FLAG, 1);
				values.put(COLLUMN_TIMESTAMP,
						(int) (new Date().getTime() / 1000));
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
	}
}
