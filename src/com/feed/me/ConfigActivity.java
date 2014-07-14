package com.feed.me;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import com.feed.me.R;

public class ConfigActivity extends Activity implements OnClickListener,
		TextWatcher, OnItemSelectedListener {

	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	Context mCtx = this;
	TextView HistoryText;
	TextView MaxText;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configactivity);

		setResult(Activity.RESULT_CANCELED);
		Spinner themespinner = (Spinner) findViewById(R.id.theme_spinner);
		themespinner.setOnItemSelectedListener(this);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.theme_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		themespinner.setAdapter(adapter);

		Spinner update_interval_spinner = (Spinner) findViewById(R.id.update_interval_spinner);
		update_interval_spinner.setOnItemSelectedListener(this);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> update_interval_adapter = ArrayAdapter
				.createFromResource(this, R.array.update_interval_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		update_interval_spinner.setAdapter(update_interval_adapter);

		assignAppWidgetId();
		findViewById(R.id.widgetStartButton).setOnClickListener(this);

		HistoryText = (TextView) findViewById(R.id.max_items_from_history);
		MaxText = (TextView) findViewById(R.id.max_items_per_history);

		HistoryText.addTextChangedListener(this);
		MaxText.addTextChangedListener(this);
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
		setBoolPref(mCtx, "ready_" + appWidgetId, Boolean.FALSE);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.widgetStartButton)
			startWidget();
	}

	/**
	 * This method right now displays the widget and starts a Service to fetch
	 * remote data from Server
	 */
	private void startWidget() {
		setBoolPref(mCtx, "ready_" + appWidgetId, Boolean.TRUE);
		new WidgetProvider().onUpdate(this, AppWidgetManager.getInstance(this),
				new int[] { appWidgetId });
		// this intent is essential to show the widget
		// if this intent is not included,you can't show
		// widget on homescreen
		Intent intent = new Intent();
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(Activity.RESULT_OK, intent);

		// start your service
		// to fetch data from web
		Intent serviceIntent = new Intent(this, RemoteFetchService.class);
		serviceIntent
				.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		startService(serviceIntent);

		// finish this activity
		this.finish();

	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// An item was selected. You can retrieve the selected item using
		// parent.getItemAtPosition(pos)
		switch (parent.getId()) {

		case R.id.theme_spinner: {
			if (pos == 0)
				setPref(mCtx, "theme_" + appWidgetId, 1);
			else if (pos == 1)
				setPref(mCtx, "theme_" + appWidgetId, 2);
			else if (pos == 2)
				setPref(mCtx, "theme_" + appWidgetId, 3);
			else
				setPref(mCtx, "theme_" + appWidgetId, 4);
		}
		case R.id.update_interval_spinner: {
			if (pos == 0)
				setPref(mCtx, "update_interval_" + appWidgetId, 10);
			else if (pos == 1)
				setPref(mCtx, "update_interval_" + appWidgetId, 30);
			else if (pos == 2)
				setPref(mCtx, "update_interval_" + appWidgetId, 60);
			else if (pos == 3)
				setPref(mCtx, "update_interval_" + appWidgetId, 120);
			else if (pos == 4)
				setPref(mCtx, "update_interval_" + appWidgetId, 240);
			else if (pos == 5)
				setPref(mCtx, "update_interval_" + appWidgetId, 480);
			else if (pos == 6)
				setPref(mCtx, "update_interval_" + appWidgetId, 720);
			else if (pos == 7)
				setPref(mCtx, "update_interval_" + appWidgetId, 1440);
			else
				setPref(mCtx, "update_interval_" + appWidgetId, 1);
		}
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}

	public static void setPref(Context context, String key, Integer val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Integer count = Integer.valueOf(val);
		prefs.edit().putInt(key, count).commit();
	}

	public static void setBoolPref(Context context, String key, Boolean val) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Boolean count = val;
		prefs.edit().putBoolean(key, count).commit();
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		if (!HistoryText.getText().toString().isEmpty()) {
			String history = HistoryText.getText().toString();
			if (Integer.valueOf(history) > 15 || Integer.valueOf(history) < 1) {
				Toast.makeText(mCtx, "Please Enter Value 1 - 15",
						Toast.LENGTH_LONG);
				HistoryText.setText("");
				HistoryText.setError("Please Enter Value 1 - 15");
			} else {
				setPref(mCtx, "history_" + appWidgetId,
						Integer.valueOf(history));
			}
		}
		if (!MaxText.getText().toString().isEmpty()) {
			String max = MaxText.getText().toString();
			if (Integer.valueOf(max) > 4 || Integer.valueOf(max) < 1) {
				Toast.makeText(mCtx, "Please Enter Value 1 - 4",
						Toast.LENGTH_LONG);
				MaxText.setText("");
				MaxText.setError("Please Enter Value 1 - 4");
			} else {
				setPref(mCtx, "max_" + appWidgetId, Integer.valueOf(max));
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}
}
