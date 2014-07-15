package com.feed.me;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.feed.me.R;

public class RemoteFetchService extends Service {

	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private AQuery aquery;
	Context mCtx = this;
	private String TAG_ENTRIES = "entries";
	private String TAG_RESULTS = "results";
	private String TAG_RESPONSEDATA = "responseData";
	// private String TAG_QUERY = "query";
	private String TAG_URL = "url";
	private String TAG_UNESCAPED_URL = "unescapedUrl";
	private String TAG_TITLE = "title";
	private String TAG_TITLE_NO_FORMAT = "titleNoFormatting";
	private String TAG_SNIPPET = "contentSnippet";
	private String TAG_KWIC = "kwic";
	private String TAG_CONTENT = "content";
	private String TAG_LINK = "link";
	private String SEARCH_TOKEN = "Google Search";
	public static ArrayList<ListItem> listItemList;
	public ArrayList<String> history;
	long seed;
	URL feedUrl;
	int histLength;
	int noOfTopics = 0;
	int HistLimit;
	int MaxLimit;
	HashMap<String, String> checkCache = new HashMap<String, String>();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
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

	/*
	 * Retrieve appwidget id from intent it is needed to update widget later
	 * initialize our AQuery class
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
			appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		aquery = new AQuery(getBaseContext());
		HistLimit = getPref(this, "history_" + appWidgetId);
		MaxLimit = getPref(this, "max_" + appWidgetId);
		noOfTopics = 0;
		history = getBrowserHistory();
		CustomSQLiteOpenHelper sql = new CustomSQLiteOpenHelper(mCtx);
		sql.addtoDB(history);
		seed = System.nanoTime();
		fetchDataFromWeb();
		return super.onStartCommand(intent, flags, startId);
	}

	public static Integer getPref(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Integer count = prefs.getInt(key, 1);
		return count;
	}

	/**
	 * method which fetches data(json) from web aquery takes params
	 * remoteJsonUrl = from where data to be fetched String.class = return
	 * format of data once fetched i.e. in which format the fetched data be
	 * returned AjaxCallback = class to notify with data once it is fetched
	 */
	private void fetchDataFromWeb() {
		// aquery.ajax(remoteJsonUrl, String.class, new AjaxCallback<String>() {
		// @Override
		// public void callback(String url, String result, AjaxStatus status) {
		// processResult(result);
		// super.callback(url, result, status);
		// }
		// });
		listItemList = new ArrayList<ListItem>();

		Log.d("History Size", String.valueOf(history.size()));

		if (history.size() > HistLimit)
			histLength = HistLimit;
		else
			histLength = history.size();

		// RSS READER
		try {

			AsyncTaskRunner runner = new AsyncTaskRunner();
			runner.execute();

		} catch (Exception I) {
			Log.d("RSS", I.toString());
		}

	}

	private class AsyncTaskRunner extends AsyncTask<String, String, String> {

		private String resp;
		private ProgressDialog pdia;
		RemoteViews remoteViews = new RemoteViews(mCtx.getPackageName(),
				R.layout.widget_layout);

		public void jsonCallback(String url, JSONObject json, AjaxStatus status) {
			noOfTopics++;
			if (json != null) {
				Log.d("CallBack Passed", "yay");
				processNews(json);
				// processFaroo(json);
			} else {
				// ajax error
				Log.d("CallBack Failed", "wtf");
			}

		}

		@Override
		protected String doInBackground(String... params) {
			if (listItemList.size() < histLength * 2) {
				for (int j = 0; j < histLength; j++) {
					if (!checkCache.containsKey(history.get(j))) {
						checkCache.put(history.get(j), "done");
						try {
							Log.d("Thread ",
									"History Item " + String.valueOf(j) + " "
											+ String.valueOf(appWidgetId) + " "
											+ history.get(j));
							Thread.sleep(2000);

							aquery.ajax(
									"https://ajax.googleapis.com/ajax/services/search/news?"
											+ "v=1.0&q=" + history.get(j)
											+ "&userip=INSERT-USER-IP",
									JSONObject.class, this, "jsonCallback");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.d("Thread ", "Done");
					}
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			// execution of result of Long time consuming operation
			if (listItemList.size() > 0)
				populateWidget();
			Log.d("Thread ", "Executed");
			// remoteViews.setTextViewText(R.id.loading_view, "");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// Things to be done before execution of long running operation. For
			// example showing ProgessDialog
			// remoteViews.setTextViewText(R.id.loading_view, "loading.....");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(String... text) {
			// Things to be done while execution of long running operation is in
			// progress. For example updating ProgessDialog
			// remoteViews.setTextViewText(R.id.loading_view, "loading.....");

		}
	}

	/**
	 * Json parsing of result and populating ArrayList<ListItem> as per json
	 * data retrieved from the string
	 */

	private void processNews(JSONObject json) {
		try {
			if (json != null) {
				json = json.getJSONObject(TAG_RESPONSEDATA);
				JSONArray data = json.getJSONArray(TAG_RESULTS);

				int max;
				if (data.length() > MaxLimit)
					max = MaxLimit;
				else
					max = data.length();

				// if (data.length() == 0)
				// noOfTopics--;

				// looping through All Contacts
				for (int i = 0; i < max; i++) {
					JSONObject c = data.getJSONObject(i);

					String feedUrl = c.getString(TAG_UNESCAPED_URL);
					String feedTitle = c.getString(TAG_TITLE_NO_FORMAT);
					String feedSnippet = c.getString(TAG_CONTENT);

					ListItem listItem = new ListItem();
					listItem.heading = Html.fromHtml(feedTitle).toString();
					listItem.content = Html.fromHtml(feedSnippet).toString();
					listItem.url = feedUrl;

					Log.i("Heading", listItem.heading);
					Log.i("Content", listItem.content);
					// Log.i("imageUrl", listItem.imageUrl);
					listItemList.add(listItem);
				}
				Log.d("No of Topics " + appWidgetId, String.valueOf(noOfTopics));
				Log.d("History Limit " + appWidgetId,
						String.valueOf(histLength));
			} else {
				Log.d("lafda", "null hai be");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method which sends broadcast to WidgetProvider so that widget is notified
	 * to do necessary action and here action == WidgetProvider.DATA_FETCHED
	 */
	private void populateWidget() {

		Intent widgetUpdateIntent = new Intent();
		widgetUpdateIntent.setAction(WidgetProvider.DATA_FETCHED);
		widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				appWidgetId);
		sendBroadcast(widgetUpdateIntent);

		this.stopSelf();
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
			String newTableQueryString = "create table " + TABLE_NAME + " ("
					+ COLLUMN_ROW_ID
					+ " integer primary key autoincrement not null,"
					+ COLLUMN_TOPIC + " text," + COLLUMN_FLAG + " integer,"
					+ COLLUMN_TIMESTAMP + " integer" + ");";
			// execute the query string to the database.
			try {
				db.execSQL(newTableQueryString);
			} catch (Exception e) {
				Log.e("DB Error while creating Tables", e.toString());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
			// OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE
			// FROM OLDER VERSIONS.
			String newTableQueryString = "create table " + TABLE_NAME + " ("
					+ COLLUMN_ROW_ID
					+ " integer primary key autoincrement not null,"
					+ COLLUMN_TOPIC + " text," + COLLUMN_FLAG + " integer,"
					+ COLLUMN_TIMESTAMP + " integer" + ");";
			// execute the query string to the database.
			try {
				db.execSQL(newTableQueryString);
			} catch (Exception e) {
				Log.e("DB Error while creating Tables", e.toString());
			}
		}

		public void addtoDB(ArrayList<String> list) {
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

		public void checkFlag(ArrayList<String> list) {
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
