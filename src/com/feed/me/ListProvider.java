package com.feed.me;

import java.util.ArrayList;
import java.util.Date;

import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.feed.me.R;

/**
 * If you are familiar with Adapter of ListView,this is the same as adapter with
 * few changes here it now takes RemoteFetchService ArrayList<ListItem> for data
 * which is a static ArrayList and this example won't work if there are multiple
 * widgets and they update at same time i.e they modify RemoteFetchService
 * ArrayList at same time. For that use Database or other techniquest
 */
public class ListProvider implements RemoteViewsFactory {
	private ArrayList<ListItem> listItemList = new ArrayList<ListItem>();
	private Context context = null;
	private int appWidgetId;
	private int theme;
	Context mCtx;

	public ListProvider(Context context, Intent intent) {
		this.context = context;
		mCtx = context;
		appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		theme = getPref(context, "theme_" + appWidgetId);
		populateListItem();
	}

	public static Integer getPref(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Integer count = prefs.getInt(key, 1);
		return count;
	}

	private void populateListItem() {
		CustomSQLiteOpenHelper sql = new CustomSQLiteOpenHelper(mCtx);
		try {
			if (RemoteFetchService.listItemList != null) {
				listItemList = (ArrayList<ListItem>) RemoteFetchService.listItemList
						.clone();
			} else {
				Log.d("Empty List " + String.valueOf(appWidgetId), " this ");
				listItemList = sql.getfromHistory(appWidgetId);
			}
		} catch (Exception e) {
			Log.d("populateListItem", e.toString());
			listItemList = new ArrayList<ListItem>();
		}

	}

	@Override
	public int getCount() {
		return listItemList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/*
	 * Similar to getView of Adapter where instead of Viewwe return RemoteViews
	 */
	@Override
	public RemoteViews getViewAt(int position) {
		final RemoteViews remoteView;
		if (theme == 1) {
			remoteView = new RemoteViews(context.getPackageName(),
					R.layout.list_row);
		} else if (theme == 2) {
			remoteView = new RemoteViews(context.getPackageName(),
					R.layout.list_row_transparent);
		} else if (theme == 3) {
			remoteView = new RemoteViews(context.getPackageName(),
					R.layout.list_row_white);
		} else {
			remoteView = new RemoteViews(context.getPackageName(),
					R.layout.list_row_white_transparent);
		}

		ListItem listItem = listItemList.get(position);
		if (theme == 1) {
			remoteView.setTextViewText(R.id.heading, listItem.heading);
			remoteView.setTextViewText(R.id.content, listItem.content);
			remoteView.setTextViewText(R.id.url, listItem.url);
		} else if (theme == 2) {
			remoteView.setTextViewText(R.id.heading_transparent,
					listItem.heading);
			remoteView.setTextViewText(R.id.content_transparent,
					listItem.content);
			remoteView.setTextViewText(R.id.url_transparent, listItem.url);
		} else if (theme == 3) {
			remoteView.setTextViewText(R.id.heading_white, listItem.heading);
			remoteView.setTextViewText(R.id.content_white, listItem.content);
			remoteView.setTextViewText(R.id.url_white, listItem.url);
		} else {
			remoteView.setTextViewText(R.id.heading_white_transparent,
					listItem.heading);
			remoteView.setTextViewText(R.id.content_white_transparent,
					listItem.content);
			remoteView
					.setTextViewText(R.id.url_white_transparent, listItem.url);
		}
		Intent i = new Intent();
		Bundle extras = new Bundle();
		extras.putString(WidgetProvider.EXTRA_ITEM, listItem.url);
		i.putExtras(extras);
		if (theme == 1)
			remoteView.setOnClickFillInIntent(R.id.list_parent, i);
		else if (theme == 2)
			remoteView.setOnClickFillInIntent(R.id.list_parent_transparent, i);
		else if (theme == 3)
			remoteView.setOnClickFillInIntent(R.id.list_parent_white, i);
		else
			remoteView.setOnClickFillInIntent(
					R.id.list_parent_white_transparent, i);
		return remoteView;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDataSetChanged() {
	}

	@Override
	public void onDestroy() {
	}

	private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
		String TABLE_NAME = "History";
		String COLLUMN_ROW_ID = "Id";
		String COLLUMN_TOPIC = "Topic";
		String COLLUMN_WIDGET_ID = "WidgetId";
		String COLLUMN_TIMESTAMP = "TimeStamp";
		String SELECTED_TABLE_NAME = "Selected_History";
		String CUSTOM_TABLE_NAME = "CustomTopics";
		String COLLUMN_FLAG = "Flag";
		String COLLUMN_HEADING = "Heading";
		String COLLUMN_CONTENT = "Content";
		String COLLUMN_URL = "Url";

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

		public ArrayList<ListItem> getfromHistory(int id) {
			mDb = new CustomSQLiteOpenHelper(mCtx).getWritableDatabase();

			ArrayList<ListItem> result = new ArrayList<ListItem>();
			try {
				Cursor c = mDb
						.rawQuery(
								"SELECT heading, content, url from History where WidgetId=?",
								new String[] { String.valueOf(id) });
				c.moveToFirst();

				while (!c.isAfterLast()) {
					ListItem l = new ListItem();
					l.heading = c.getString(0);
					l.content = c.getString(1);
					l.url = c.getString(2);
					result.add(l);
					c.moveToNext();
				}
			} catch (Exception e) {
				Log.e("DB Insert ERROR : getfromHistory : ", e.toString());
			}
			mDb.close();
			return result;
		}
	}
}
