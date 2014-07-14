package com.feed.me;

import java.util.Date;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import com.feed.me.R;

public class AlarmService extends Service {
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		buildUpdate();

		return super.onStartCommand(intent, flags, startId);
	}

	private void buildUpdate() {
		String lastUpdated = DateFormat.format("MMMM dd, yyyy h:mmaa",
				new Date()).toString();

		RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget_layout);

		// Push update for this widget to the home screen
		ComponentName thisWidget = new ComponentName(this, WidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		manager.updateAppWidget(thisWidget, view);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
