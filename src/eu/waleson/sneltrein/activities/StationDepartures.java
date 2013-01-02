package eu.waleson.sneltrein.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.STApplication;
import eu.waleson.sneltrein.adapters.LinearLayoutAdapter;
import eu.waleson.sneltrein.cornerstones.Station;
import eu.waleson.sneltrein.cornerstones.TrainTravel;
import eu.waleson.sneltrein.cornerstones.TrainTravelDeparture;
import eu.waleson.sneltrein.cornerstones.TripPlan;
import eu.waleson.sneltrein.downloader.ASyncNSDownloader;
import eu.waleson.sneltrein.parsers.DeparturesParser;

public class StationDepartures extends Activity implements OnClickListener {
	Station station;
	ProgressDialog dialog;
	LinearLayoutAdapter<TrainTravelDeparture> lla;
	LayoutInflater inflater;
	String lastURL;
	String fallBackURL = "";
	STApplication app = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.app = (STApplication) getApplication();

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		setContentView(R.layout.activity_station_departures);
		lla = new LinearLayoutAdapter<TrainTravelDeparture>(
				(LinearLayout) (findViewById(R.id.LinearLayoutStationDepartures)),
				this);
		station = new Station(getIntent().getExtras());

		((TextView) findViewById(R.id.TextViewHeader)).setText(station.name);
		loadData();
	}

	public void loadData() {
		lla.clear();
		dialog = ProgressDialog.show(this, "", "", true);
		dialog.setCancelable(true);
		lastURL = "http://webservices.ns.nl/ns-api-avt?station=" + station.code;
		fallBackURL = "http://m.ns.nl/actvertrektijden.action?from="
				+ station.code;
		Downloader downloader = new Downloader(lastURL, dialog);
		dialog.show();
		downloader.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_open_browser_refresh, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_button_refresh) {
			loadData();
			return true;
		} else if (item.getItemId() == R.id.menu_button_openbrowser) {
			startActivity(new Intent("android.intent.action.VIEW",
					Uri.parse(fallBackURL)));
			return true;
		} else if (item.getItemId() == R.id.menu_disruptions) {
			Intent i = new Intent(this, Disruptions.class);
			i.putExtras(station.getBundle());
			startActivity(i);
		}
		return false;
	}

	private class Downloader extends ASyncNSDownloader {
		public Downloader(String url, ProgressDialog dialog) {
			super(url, new DeparturesParser(station), dialog);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			finish();
		}

		@Override
		protected void onPostExecute(Integer result) {
			boolean openBrowser = false;
			if (dialog != null)
				dialog.dismiss();

			try {
				DeparturesParser parse = (DeparturesParser) handler;
				lla.refresh(parse.result, inflater);
				if (lla.getCount() == 0)
					openBrowser = true;
			} catch (Exception e) {
				e.printStackTrace();
				openBrowser = true;
			}
			if (openBrowser && app.fallBackOnBrowser) {
				(Toast.makeText(getApplicationContext(),
						R.string.failed_parsing_opening_site, Toast.LENGTH_LONG))
						.show();
				startActivity(new Intent("android.intent.action.VIEW",
						Uri.parse(fallBackURL)));
			}
		}
	}

	@Override
	public void onClick(View v) {
		TrainTravel orig = (TrainTravel) v.getTag();
		String stationName = orig.trainstopB.station.name;

		if (stationName.contains("/") && !stationName.contains("Brussel")
				&& !stationName.contains("Alphen"))
			makeChoice(stationName.split("/"), orig);
		else
			openTripPlan(orig, stationName);
	}

	public void openTripPlan(TrainTravel tt, String stationName) {
		Station newStation = new Station(
				app.findStationCodeFromName(stationName), stationName);
		if (newStation.hasCode()) {
			TripPlan tp = tt.getTripPlan();
			tp.stationTo = newStation;

			Intent returnIntent = new Intent();
			returnIntent.putExtras(tp.getBundle());
			setResult(RESULT_OK, returnIntent);
			finish();
		}

	}

	public void makeChoice(CharSequence[] list, TrainTravel tt) {
		final CharSequence[] items = list;
		final TrainTravel tt1 = tt;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pickastation);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				openTripPlan(tt1, items[item].toString());
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
