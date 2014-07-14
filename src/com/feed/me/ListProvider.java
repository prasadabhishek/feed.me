package com.feed.me;

import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

	public ListProvider(Context context, Intent intent) {
		this.context = context;
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
		if (RemoteFetchService.listItemList != null)
			listItemList = (ArrayList<ListItem>) RemoteFetchService.listItemList
					.clone();
		else
			listItemList = new ArrayList<ListItem>();

	}

	@Override
	public int getCount() {
		return listItemList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public Bitmap buildUpdateHeading(String time) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		Bitmap myBitmap = Bitmap.createBitmap(width, 25,
				Bitmap.Config.ARGB_4444);
		Canvas myCanvas = new Canvas(myBitmap);
		Paint paint = new Paint();
		Typeface face = Typeface.createFromAsset(context.getAssets(),
				"Anke.ttf");
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setTypeface(face);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		paint.setTextSize(25);
		paint.setTextAlign(Align.LEFT);
		myCanvas.drawText(time, 0, 20f, paint);
		return myBitmap;
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

}
