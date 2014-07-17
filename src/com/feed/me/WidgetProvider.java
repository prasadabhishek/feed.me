package com.feed.me;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.feed.me.R;
import com.feed.me.ConfigActivity;

public class WidgetProvider extends AppWidgetProvider {

	// String to be sent on Broadcast as soon as Data is Fetched
	// should be included on WidgetProvider manifest intent action
	// to be recognized by this WidgetProvider to receive broadcast
	public static final String DATA_FETCHED = "com.wordpress.laaptu.DATA_FETCHED";
	public static final String EXTRA_ITEM = "com.feed.me.EXTRA_ITEM";
	public static final String URL_ACTION = "com.feed.me.URL_ACTION";
	public static String MANIFEST_DEFINED_STRING = "com.refresh.widget";
	public static final String FLAG_HISTORY = "com.feed.me.history";
	final static String WIDGET_UPDATE_ACTION = "UPDATE_WIDGET";
	public static int randomNumber;
	public static final String CLOCK_WIDGET_UPDATE = "com.refresh.widget.CLOCK_WIDGET_UPDATE";
	public static int themeNumber;
	public static int updateInterval;
	public static Boolean update;
	String[] days = { null, "Sunday", "Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday" };
	static String months[] = { "January", "February", "March", "April", "May",
			"June", "July", "August", "September", "October", "November",
			"December" };

	/*
	 * this method is called every 30 mins as specified on widgetinfo.xml this
	 * method is also called on every phone reboot from this method nothing is
	 * updated right now but instead RetmoteFetchService class is called this
	 * service will fetch data,and send broadcast to WidgetProvider this
	 * broadcast will be received by WidgetProvider onReceive which in turn
	 * updates the widget
	 */

	private PendingIntent createClockTickIntent(Context context) {
		Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d("onEnabled",
				"Widget Provider enabled.  Starting timer to update widget every minute");
		themeNumber = getPref(context, "theme");
	}

	public Bitmap buildUpdate(String time, Context context) {
		Bitmap myBitmap = Bitmap.createBitmap(300, 45, Bitmap.Config.ARGB_4444);
		Canvas myCanvas = new Canvas(myBitmap);
		Paint paint = new Paint();
		Typeface face = Typeface.createFromAsset(context.getAssets(),
				"Anke.ttf");
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setTypeface(face);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		paint.setTextSize(30);
		paint.setTextAlign(Align.LEFT);
		myCanvas.drawText(time, 0, 40, paint);
		return myBitmap;
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d("onDisabled", "Widget Provider disabled. Turning off timer");
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(createClockTickIntent(context));
		Toast.makeText(context, "onDisabled", Toast.LENGTH_SHORT);
		setBoolPref(context, "alarm_set", Boolean.FALSE);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			RemoteViews remoteViews;

			themeNumber = getPref(context, "theme_" + appWidgetIds[i]);
			Log.d("onUpdate theme number : ", String.valueOf(themeNumber));
			update = getBoolPref(context, "ready_" + appWidgetIds[i]);
			Log.d("onUpdate Status : ", String.valueOf(update));
			updateInterval = getPref(context, "update_interval_"
					+ appWidgetIds[i]);
			Log.d("onUpdate Interval : ", String.valueOf(updateInterval));
			/* Alarm Manager for Refresh Intervals */
			Calendar TIME = Calendar.getInstance();
			TIME.set(Calendar.MINUTE, 0);
			TIME.set(Calendar.SECOND, 0);
			TIME.set(Calendar.MILLISECOND, 0);

			if (update) {
				if (!getBoolPref(context, "alarm_set_" + appWidgetIds[i])) {
					AlarmManager alarmManager = (AlarmManager) context
							.getSystemService(Context.ALARM_SERVICE);
					alarmManager.setRepeating(AlarmManager.RTC,
							System.currentTimeMillis()
									+ (updateInterval * 60000),
							updateInterval * 60000,
							createClockTickIntent(context));
					Log.d("ALARM SIGNAL", String.valueOf(updateInterval));
					Log.d("ALARM STATUS",
							String.valueOf(getBoolPref(context, "alarm_set")));
					setBoolPref(context, "alarm_set_" + appWidgetIds[i],
							Boolean.TRUE);
				} else {
					Log.d("ALARM STATUS",
							String.valueOf(getBoolPref(context, "alarm_set_"
									+ appWidgetIds[i])));
				}
				if (themeNumber == 1) {
					remoteViews = new RemoteViews(context.getPackageName(),
							R.layout.widget_layout);
					remoteViews.setTextViewText(R.id.loading_view,
							"Loading.... ");
				} else if (themeNumber == 2) {
					remoteViews = new RemoteViews(context.getPackageName(),
							R.layout.widget_layout_transparent);
					remoteViews.setTextViewText(R.id.loading_view_transparent,
							"Loading.... ");
				} else if (themeNumber == 3) {
					remoteViews = new RemoteViews(context.getPackageName(),
							R.layout.widget_layout_white);
					remoteViews.setTextViewText(R.id.loading_view_white,
							"Loading.... ");
				} else {
					remoteViews = new RemoteViews(context.getPackageName(),
							R.layout.widget_layout_white_transparent);
					remoteViews
							.setTextViewText(
									R.id.loading_view_white_transparent,
									"Loading.... ");
				}

				int dayofweek = TIME.get(Calendar.DAY_OF_WEEK);
				int month = TIME.get(Calendar.MONTH);
				int date = TIME.get(Calendar.DATE);

				if (themeNumber == 1)
					remoteViews.setTextViewText(
							R.id.name_view,
							String.valueOf(days[dayofweek]) + " , "
									+ months[month] + " "
									+ String.valueOf(date));
				else if (themeNumber == 2)
					remoteViews.setTextViewText(
							R.id.name_view_transparent,
							String.valueOf(days[dayofweek]) + " , "
									+ months[month] + " "
									+ String.valueOf(date));
				else if (themeNumber == 3)
					remoteViews.setTextViewText(
							R.id.name_view_white,
							String.valueOf(days[dayofweek]) + " , "
									+ months[month] + " "
									+ String.valueOf(date));
				else
					remoteViews.setTextViewText(
							R.id.name_view_white_transparent,
							String.valueOf(days[dayofweek]) + " , "
									+ months[month] + " "
									+ String.valueOf(date));

				Intent serviceIntent = new Intent(context,
						RemoteFetchService.class);

				serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetIds[i]);
				context.startService(serviceIntent);

				String definePage = "http://www.google.com";
				Intent defineIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(definePage));
				PendingIntent pendingIntent = PendingIntent.getActivity(
						context, 0 /* no requestCode */, defineIntent, 0 /*
																		 * no
																		 * flags
																		 */);
				if (themeNumber == 1)
					remoteViews.setOnClickPendingIntent(R.id.heading,
							pendingIntent);
				else if (themeNumber == 2)
					remoteViews.setOnClickPendingIntent(
							R.id.heading_transparent, pendingIntent);
				else if (themeNumber == 3)
					remoteViews.setOnClickPendingIntent(R.id.heading_white,
							pendingIntent);
				else
					remoteViews.setOnClickPendingIntent(
							R.id.heading_white_transparent, pendingIntent);

				Intent urlIntent = new Intent(context, WidgetProvider.class);
				urlIntent.setAction(WidgetProvider.URL_ACTION);
				urlIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetIds[i]);
				PendingIntent toastPendingIntent = PendingIntent.getBroadcast(
						context, 0, urlIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				if (themeNumber == 1)
					remoteViews.setPendingIntentTemplate(R.id.listViewWidget,
							toastPendingIntent);
				else if (themeNumber == 2)
					remoteViews
							.setPendingIntentTemplate(
									R.id.listViewWidget_transparent,
									toastPendingIntent);
				else if (themeNumber == 3)
					remoteViews.setPendingIntentTemplate(
							R.id.listViewWidget_white, toastPendingIntent);
				else
					remoteViews.setPendingIntentTemplate(
							R.id.listViewWidget_white_transparent,
							toastPendingIntent);

				serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetIds[i]);
				serviceIntent.setData(Uri.parse(serviceIntent
						.toUri(Intent.URI_INTENT_SCHEME)));
				PendingIntent pendingServiceIntent = PendingIntent.getService(
						context, 0, serviceIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				if (themeNumber == 1)
					remoteViews.setOnClickPendingIntent(R.id.refreshButton,
							pendingServiceIntent);
				else if (themeNumber == 2)
					remoteViews.setOnClickPendingIntent(
							R.id.feedButton_transparent, pendingServiceIntent);
				else if (themeNumber == 3)
					remoteViews.setOnClickPendingIntent(R.id.feedButton_white,
							pendingServiceIntent);
				else
					remoteViews.setOnClickPendingIntent(
							R.id.feedButton_white_transparent,
							pendingServiceIntent);

				Log.d("SETTING PENDING INTENT", "feedbutton");

				Intent historyIntent = new Intent(context, WidgetProvider.class);
				historyIntent.setAction(WidgetProvider.FLAG_HISTORY);
				historyIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetIds[i]);
				PendingIntent HistoryIntent = PendingIntent.getBroadcast(
						context, 0, historyIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(R.id.feedButton,
						HistoryIntent);

				appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
			}
			super.onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}

	@SuppressWarnings("deprecation")
	private RemoteViews updateWidgetListView(Context context, int appWidgetId) {
		themeNumber = getPref(context, "theme_" + appWidgetId);
		// which layout to show on widget
		RemoteViews remoteViews;

		if (themeNumber == 1) {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
		} else if (themeNumber == 2) {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_transparent);
		} else if (themeNumber == 3) {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_white);
		} else {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_white_transparent);
		}

		final Intent refreshIntent = new Intent(context, WidgetProvider.class);
		refreshIntent
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		refreshIntent.setAction(WidgetProvider.MANIFEST_DEFINED_STRING);

		// RemoteViews Service needed to provide adapter for ListView
		Intent svcIntent = new Intent(context, WidgetService.class);
		// passing app widget id to that RemoteViews Service
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// setting a unique Uri to the intent
		// don't know its purpose to me right now
		// svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		Random randomGenerator = new Random();
		randomNumber = randomGenerator.nextInt(100);
		svcIntent.setData(Uri.fromParts("content",
				String.valueOf(appWidgetId + randomNumber), null));
		// setting adapter to listview of the widget
		// remoteViews.setRemoteAdapter(appWidgetId, R.id.listViewWidget,
		// null);;
		if (themeNumber == 1) {
			remoteViews.setRemoteAdapter(appWidgetId, R.id.listViewWidget,
					svcIntent);
			remoteViews.setTextViewText(R.id.loading_view, " ");
			// setting an empty view in case of no data
			remoteViews.setEmptyView(R.id.listViewWidget, R.id.empty_view);

		} else if (themeNumber == 2) {
			remoteViews.setRemoteAdapter(appWidgetId,
					R.id.listViewWidget_transparent, svcIntent);
			remoteViews.setTextViewText(R.id.loading_view_transparent, " ");
			// setting an empty view in case of no data
			remoteViews.setEmptyView(R.id.listViewWidget_transparent,
					R.id.empty_view_transparent);
		} else if (themeNumber == 3) {
			remoteViews.setRemoteAdapter(appWidgetId,
					R.id.listViewWidget_white, svcIntent);
			remoteViews.setTextViewText(R.id.loading_view_white, " ");
			// setting an empty view in case of no data
			remoteViews.setEmptyView(R.id.listViewWidget_white,
					R.id.empty_view_white);
		} else {
			remoteViews.setRemoteAdapter(appWidgetId,
					R.id.listViewWidget_white_transparent, svcIntent);
			remoteViews.setTextViewText(R.id.loading_view_white_transparent,
					" ");
			// setting an empty view in case of no data
			remoteViews.setEmptyView(R.id.listViewWidget_white_transparent,
					R.id.empty_view_white_transparent);
		}
		return remoteViews;
	}

	public static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.d("log", "Entered update cycle");
		// Unimportant for these purposes
		RemoteViews remoteViews;
		if (themeNumber == 1) {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
		} else if (themeNumber == 2) {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_transparent);
		} else if (themeNumber == 3) {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_white);
		} else {
			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_white_transparent);
		}

		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}

	/*
	 * It receives the broadcast as per the action set on intent filters on
	 * Manifest.xml once data is fetched from RemotePostService,it sends
	 * broadcast and WidgetProvider notifies to change the data the data change
	 * right now happens on ListProvider as it takes RemoteFetchService
	 * listItemList as data
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		int appWidgetId = intent.getIntExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		// if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
		super.onReceive(context, intent);
		// }
		themeNumber = getPref(context, "theme_" + appWidgetId);
		RemoteViews views;
		if (themeNumber == 1) {
			views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
		} else if (themeNumber == 2) {
			views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_transparent);
		} else if (themeNumber == 3) {
			views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_white);
		} else {
			views = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout_white_transparent);
		}

		if (intent.getAction().equals(DATA_FETCHED)) {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			RemoteViews remoteViews = updateWidgetListView(context, appWidgetId);
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
		if (intent.getAction().equals(URL_ACTION)) {
			String url = intent.getStringExtra(EXTRA_ITEM);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
		if (intent.getAction().equals(FLAG_HISTORY)) {
			Intent historyintent = new Intent(context, HistoryActivity.class);
			historyintent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			historyintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(historyintent);
		}
		if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
			Log.d("onReceive", "Clock update");
			Toast.makeText(context, "onRecieve", Toast.LENGTH_SHORT);
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			// Get the widget manager and ids for this widget provider, then
			// call the shared
			// clock update method.
			if (ni != null) {
				ComponentName thisAppWidget = new ComponentName(
						context.getPackageName(), getClass().getName());
				AppWidgetManager appWidgetManager = AppWidgetManager
						.getInstance(context);
				int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
				for (int appWidgetID : ids) {
					// Intent serviceIntent = new Intent(context,
					// RemoteFetchService.class);
					// serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					// appWidgetID);
					// context.startService(serviceIntent);
					new WidgetProvider().onUpdate(context,
							AppWidgetManager.getInstance(context),
							new int[] { appWidgetID });
				}
			}
		}
	}

	public static Integer getPref(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Integer count = prefs.getInt(key, 1);
		return count;
	}

	public static Boolean getBoolPref(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean val = prefs.getBoolean(key, Boolean.FALSE);
		return val;
	}

	public static void setBoolPref(Context context, String key, Boolean val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean count = val;
		prefs.edit().putBoolean(key, count).commit();
	}
}
