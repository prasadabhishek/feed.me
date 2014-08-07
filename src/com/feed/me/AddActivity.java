package com.feed.me;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class AddActivity extends Activity implements OnClickListener {

	EditText textbox;
	ImageButton addbutton;
	ListView list;
	Context mCtx = this;
	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	Boolean topicAdded = Boolean.FALSE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addtopic);

		textbox = (EditText) findViewById(R.id.editText);
		addbutton = (ImageButton) findViewById(R.id.addButton);
		list = (ListView) findViewById(R.id.addList);

		assignAppWidgetId();

		addbutton.setOnClickListener(this);

		CustomSQLiteOpenHelper sql = new CustomSQLiteOpenHelper(mCtx);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				sql.getCustomList(appWidgetId));

		list.setAdapter(arrayAdapter);

	}

	private void assignAppWidgetId() {
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onBackPressed() {
		this.finish();
		Intent historyintent = new Intent(mCtx, HistoryActivity.class);
		historyintent
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		historyintent.putExtra("TopicAdded", topicAdded);
		historyintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.startActivity(historyintent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.addButton) {
			if (textbox.getText().toString().length() < 1) {
				textbox.setError("Enter a topic to add");
			} else {
				CustomSQLiteOpenHelper sql = new CustomSQLiteOpenHelper(mCtx);
				sql.insertRow(textbox.getText().toString(), appWidgetId);
				textbox.setText("");
				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
						this, android.R.layout.simple_list_item_1,
						sql.getCustomList(appWidgetId));
				topicAdded = Boolean.TRUE;
				list.setAdapter(arrayAdapter);
			}
		}
	}

	public static void setPref(Context context, String key, Integer val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Integer count = Integer.valueOf(val);
		prefs.edit().putInt(key, count).commit();
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		String TABLE_NAME = "CustomTopics";
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
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
			// OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE
			// FROM OLDER VERSIONS.
		}

		public ArrayList<String> getCustomList(int id) {
			mDb = new CustomSQLiteOpenHelper(mCtx).getWritableDatabase();
			ArrayList<String> result = new ArrayList<String>();
			Cursor c;
			c = mDb.rawQuery(
					"Select Topic from CustomTopics where WidgetId = ?",
					new String[] { String.valueOf(id) });
			c.moveToFirst();

			while (!c.isAfterLast()) {
				result.add(c.getString(0));
				c.moveToNext();
			}

			return result;
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
						+ COLLUMN_TOPIC + " =? AND " + COLLUMN_WIDGET_ID
						+ " =? ", new String[] { val, String.valueOf(id) });
			} catch (Exception e) {
				Log.e("DB Select Error : insertRow : ", e.toString());
			}
			if (c.getCount() == 0) {
				// ask the database object to insert the new data
				try {
					mDb.insert(TABLE_NAME, null, values);
				} catch (Exception e) {
					Log.e("DB Insert ERROR : insertRow : ", e.toString()); // prints
																			// the
																			// error
					// message to
					// the log
				}
			}
		}
	}
}
