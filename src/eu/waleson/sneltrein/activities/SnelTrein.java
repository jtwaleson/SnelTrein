package eu.waleson.sneltrein.activities;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.STApplication;
import eu.waleson.sneltrein.adapters.LinearLayoutAdapter;
import eu.waleson.sneltrein.cornerstones.Station;
import eu.waleson.sneltrein.cornerstones.TripPlan;
import eu.waleson.sneltrein.utils.ILocationActivity;

public class SnelTrein extends Activity implements OnClickListener,
		OnLongClickListener, ILocationActivity {

	LayoutInflater inflater = null;
	STApplication app = null;

	private static final int GET_A_STATION = 0;
	private static final int GET_A_TRIPPLAN = 1;

	protected int stationLinearLayout;
	protected int tripLinearLayout;

	LinearLayoutAdapter<Station> llaStations;
	LinearLayoutAdapter<TripPlan> llaTrips;

	private boolean locationForced = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		app = (STApplication) getApplication();
		((Button) findViewById(R.id.ButtonStation)).setOnClickListener(this);
		((Button) findViewById(R.id.ButtonTrip)).setOnClickListener(this);
		((Button) findViewById(R.id.ButtonTripVia)).setOnClickListener(this);

		refreshView(true);
		File file = this.getFileStreamPath("opened");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
			}
			String whatsNewTitle = getResources().getString(
					R.string.whatsNewTitle);
			String whatsNewText = getResources().getString(
					R.string.whatsNewText);
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(whatsNewTitle)
					.setMessage(whatsNewText)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == findViewById(R.id.ButtonStation)) {
			OpenGetTripOrStation(1);
		} else if (v == findViewById(R.id.ButtonTrip)) {
			OpenGetTripOrStation(2);
		} else if (v == findViewById(R.id.ButtonTripVia)) {
			OpenGetTripOrStation(3);
		} else if (v.getTag() != null) {
			if (v.getTag() instanceof Station) {
				OpenStationDepartures((Station) v.getTag());
			} else if (v.getTag() instanceof TripPlan) {
				OpenJourneySelector((TripPlan) v.getTag());
			}
		}
	}

	public void refreshView(boolean hard) {
		if (app.settingsChanged || hard) {
			if (app.tripsAboveStations) {
				tripLinearLayout = R.id.LinearLayoutTrips;
				stationLinearLayout = R.id.LinearLayoutStations;
				((TextView) findViewById(R.id.TextViewMyTrips))
						.setText(getText(R.string.my_trips) + " ("
								+ app.numberOfTrips + ")");
				((TextView) findViewById(R.id.TextViewMyStations))
						.setText(getText(R.string.my_stations) + " ("
								+ app.numberOfStations + ")");
			} else {
				tripLinearLayout = R.id.LinearLayoutStations;
				stationLinearLayout = R.id.LinearLayoutTrips;
				((TextView) findViewById(R.id.TextViewMyStations))
						.setText(getText(R.string.my_trips) + " ("
								+ app.numberOfTrips + ")");
				((TextView) findViewById(R.id.TextViewMyTrips))
						.setText(getText(R.string.my_stations) + " ("
								+ app.numberOfStations + ")");
			}

			((LinearLayout) findViewById(stationLinearLayout)).removeAllViews();
			((LinearLayout) findViewById(tripLinearLayout)).removeAllViews();
			llaStations = new LinearLayoutAdapter<Station>(
					(LinearLayout) findViewById(stationLinearLayout), this);
			llaTrips = new LinearLayoutAdapter<TripPlan>(
					(LinearLayout) findViewById(tripLinearLayout), this, this);
			app.settingsChanged = false;
		}
		llaStations.refresh(app.myStationsAndNearStations, getLayoutInflater());
		llaTrips.refresh(app.myTrips, getLayoutInflater());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_button_preferences) {
			Intent i = new Intent(this, Preferences.class);
			startActivity(i);
			return true;
		} else if (item.getItemId() == R.id.menu_report_bug) {
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { "android.sneltrein@gmail.com" });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getString(R.string.emailsuggestion));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
			this.startActivity(Intent.createChooser(emailIntent,
					getString(R.string.emailnotification)));
			return true;
		} else if (item.getItemId() == R.id.menu_refresh_position) {
			locationForced = true;
			app.getNewFix();
			return true;
		} else if (item.getItemId() == R.id.menu_disruptions) {
			OpenDisruptions();
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshView(false);
		app.locationActivity = this;
		app.getNewFix();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GET_A_STATION) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				if (b.getInt("Count") == 1) {
					Station s = new Station(b, 1);
					app.database.AddStationHit(s);
					app.database.refreshStations(app.sortStationsByUsage);
					OpenStationDepartures(s);
				} else if (b.getInt("Count") == 2 || b.getInt("Count") == 3) {
					TripPlan tp = TripPlan.fromResultBundle(b);
					app.database.AddTripHit(tp);
					app.refreshTrips();
					OpenJourneySelector(tp);
				}
			}
		} else if (requestCode == GET_A_TRIPPLAN) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				app.departureOrArrival = true;
				OpenJourneySelector(TripPlan.createFromBundle(b));
			}
		}
	}

	public void OpenGetTripOrStation(int count) {
		Intent i = new Intent(this, StationSelector.class);
		Bundle bu = new Bundle();
		bu.putInt("StationCount", count);
		i.putExtras(bu);
		startActivityForResult(i, GET_A_STATION);
	}

	public void OpenStationDepartures(Station s) {
		Intent i = new Intent(this, StationDepartures.class);
		app.database.AddStationHit(s);
		i.putExtras(s.getBundle());
		startActivityForResult(i, GET_A_TRIPPLAN);
	}

	public void OpenDisruptions() {
		Intent i = new Intent(this, Disruptions.class);
		startActivity(i);
	}

	public void OpenJourneySelector(TripPlan t) {
		app.database.AddTripHit(t);
		Intent i = new Intent(this, JourneySelector.class);
		i.putExtras(t.getBundle());
		startActivity(i);
	}

	@Override
	public boolean onLongClick(View v) {
		final CharSequence[] items = { getString(R.string.removefromlist),
				getString(R.string.planreturn), getString(R.string.grey),
				getString(R.string.green), getString(R.string.red),
				getString(R.string.orange) };
		final int itemsbeforecolors = 2;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pickacolor);
		final TripPlan tp = ((TripPlan) v.getTag());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (item >= itemsbeforecolors) {
					tp.userColor = item - itemsbeforecolors;
					app.database.UpdateColor(tp);
					refreshView(false);
				}
				if (item == 0) {
					app.database.Remove(tp);
					app.refreshTrips();
					refreshView(true);
				}
				if (item == 1) {
					OpenJourneySelector(tp.getReverse());
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
		return true;
	}

	@Override
	public void processLocation() {
		llaStations.refresh(app.myStationsAndNearStations, getLayoutInflater());
		if (locationForced) {
			Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT)
					.show();
			locationForced = false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		app.locationActivity = null;
	}
}