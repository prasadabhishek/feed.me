package com.feed.me;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {
	/*
	 * So pretty simple just defining the Adapter of the listview
	 * here Adapter is ListProvider
	 * */

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
//		int appWidgetId = intent.getIntExtra(
//				AppWidgetManager.EXTRA_APPWIDGET_ID,
//				AppWidgetManager.INVALID_APPWIDGET_ID);
		int appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart()) - WidgetProvider.randomNumber;
		return (new ListProvider(this.getApplicationContext(), intent));
	}

}
