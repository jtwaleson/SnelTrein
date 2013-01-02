package eu.waleson.sneltrein.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TimePicker;
import android.widget.Toast;

import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.STApplication;
import eu.waleson.sneltrein.adapters.LinearLayoutAdapter;
import eu.waleson.sneltrein.cornerstones.CDate;
import eu.waleson.sneltrein.cornerstones.Journey;
import eu.waleson.sneltrein.cornerstones.TripPlan;
import eu.waleson.sneltrein.cornerstones.TripPlanWithDeparture;
import eu.waleson.sneltrein.downloader.ASyncNSDownloader;
import eu.waleson.sneltrein.parsers.JourneyParser;

public class JourneySelector extends Activity implements OnClickListener {

	LayoutInflater inflater;
	STApplication app;
	int daysPlus = 0;

	ArrayList<Journey> journeys = null;
	LinearLayoutAdapter<Journey> lla;

	TripPlan lastTripPlan;
	CDate lastDate;

	JourneyParser journeyParser;
	boolean currentlyDownloading = false;
	boolean dialogCurrentlyOpen = false;

	boolean morePressed = false;

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (STApplication) getApplication();
		setContentView(R.layout.activity_journey_selector);
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		((Button) findViewById(R.id.ButtonMore)).setOnClickListener(this);
		((Button) findViewById(R.id.ButtonSelectTime)).setOnClickListener(this);

		lastTripPlan = TripPlan.createFromBundle(getIntent().getExtras());
		lastDate = CDate.now();
		lla = new LinearLayoutAdapter<Journey>(
				(LinearLayout) findViewById(R.id.LinearLayoutOther), this);
		journeyParser = new JourneyParser(app.database);
		journeys = new ArrayList<Journey>();

		GetJourneysFromDBAsync gfdb = new GetJourneysFromDBAsync();
		gfdb.execute();

		if (((STApplication) getApplication()).startSearching)
			startDownloading(5, 5, false);
		else
			showDateTimeDialog();

		setTitle(lastTripPlan.stationFrom.name + " - "
				+ lastTripPlan.stationTo.name);
	}

	public void refreshJourneys(boolean hard) {
		if (hard)
			loadNewJourneys();
		long t = CDate.now().getTimeinMillis();
		for (Journey j : journeys)
			j.currentTime = t;

		int i;
		if (!morePressed)
			for (i = journeys.size() - 1; i > -1; i--) {
				journeys.get(i).expanded = 0;
			}
		for (i = journeys.size() - 1; i > -1; i--) {
			if (app.departureOrArrival) {
				if (journeys.get(i).plannedDeparture.compareTo(lastDate) < 0)
					break;
			} else {
				if (journeys.get(i).plannedArrival.compareTo(lastDate) < 0)
					break;
			}
		}
		if (!morePressed && (i + 1) >= 0 && (i + 1) < journeys.size())
			journeys.get(i + 1).expanded = 1;

		lla.refresh(journeys, inflater);

		if (i + 1 >= 0 && i + 1 < journeys.size()) {
			int top = ((LinearLayout) findViewById(R.id.LinearLayoutOther))
					.getChildAt(i + 1).getTop();
			((ScrollView) findViewById(R.id.ScrollView01)).scrollTo(0, top);
		}

	}

	public void loadNewJourneys() {
		journeys = app.database.getJourneys(lastTripPlan, lastDate);
	}

	@Override
	public void onClick(View v) {
		if (v == findViewById(R.id.ButtonSelectTime)) {
			showDateTimeDialog();
		} else if (v.getTag() != null && v.getTag().getClass() == Journey.class) {
			for (int i = journeys.size() - 1; i >= 0; i--) {
				if (journeys.get(i).expanded > 0
						&& journeys.get(i) != v.getTag()) {
					journeys.get(i).setExpanded(0);
					lla.update(i, journeys.get(i), inflater);
				}
			}
			((Journey) v.getTag()).toggleExpanded();
			((Journey) v.getTag()).updateView(inflater, v, 0);

		} else if (v == findViewById(R.id.ButtonMore)) {
			morePressed = true;
			CDate d;
			if (journeys.size() == 0)
				d = CDate.now();
			else if (app.departureOrArrival)
				d = new CDate(
						journeys.get(journeys.size() - 1).plannedDeparture);
			else
				d = new CDate(journeys.get(journeys.size() - 1).plannedArrival);
			d.addMinutes(10);
			lastDate = d;
			startDownloading(0, 5, true);
		}
	}

	public void startDownloading(int before, int after, boolean scrollDown) {
		((ProgressBar) findViewById(R.id.ImageViewLoading2))
				.setVisibility(View.VISIBLE);
		ProgressDialog dialog = null;
		// if (journeys == null || journeys.size() == 0) {
		dialog = ProgressDialog.show(JourneySelector.this, "", "", true);
		dialog.setCancelable(true);
		// }
		Downloader d = new Downloader(getURL(lastTripPlan, lastDate, before,
				after), dialog, scrollDown);
		d.execute();
	}

	public void showDateTimeDialog() {
		if (!dialogCurrentlyOpen) {
			dialogCurrentlyOpen = true;
			final Dialog dialog2 = new Dialog(JourneySelector.this);
			dialog2.setContentView(R.layout.date_time_picker);
			dialog2.setTitle(R.string.selecttime);
			dialog2.setCancelable(true);
			dialog2.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					dialogCurrentlyOpen = false;
				}
			});

			((TimePicker) dialog2.findViewById(R.id.TimePicker01))
					.setIs24HourView(true);
			((TimePicker) dialog2.findViewById(R.id.TimePicker01))
					.setCurrentHour(lastDate.hour);
			((TimePicker) dialog2.findViewById(R.id.TimePicker01))
					.setCurrentMinute(lastDate.minute);

			((Button) dialog2.findViewById(R.id.ButtonGo))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							CDate d = CDate.now();
							d.addDays(daysPlus);
							d.hour = (((TimePicker) dialog2
									.findViewById(R.id.TimePicker01))
									.getCurrentHour());
							d.minute = (((TimePicker) dialog2
									.findViewById(R.id.TimePicker01))
									.getCurrentMinute());
							lastDate = d;
							dialog2.dismiss();
							app.database
									.deleteJourneys("SELECT Id FROM Journeys WHERE "
											+ lastTripPlan.getDBCacheClause());
							refreshJourneys(true);
							startDownloading(5, 5, false);
							dialogCurrentlyOpen = false;
						}
					});
			((Button) dialog2.findViewById(R.id.ButtonArrDep))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							app.departureOrArrival = !app.departureOrArrival;
							((Button) dialog2.findViewById(R.id.ButtonArrDep))
									.setText((app.departureOrArrival ? R.string.departure
											: R.string.arrival));
						}
					});
			((Button) dialog2.findViewById(R.id.ButtonDay))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							daysPlus += 1;
							daysPlus = daysPlus % 3;
							Button b = (Button) v;
							switch (daysPlus) {
							case 0:
								b.setText(R.string.today);
								break;
							case 1:
								b.setText(R.string.tomorrow);
								break;
							case 2:
								b.setText(R.string.dayaftertomorrow);
								break;
							}
						}
					});

			Button b = (Button) dialog2.findViewById(R.id.ButtonArrDep);
			if (app.departureOrArrival)
				b.setText(R.string.departure);
			else
				b.setText(R.string.arrival);

			dialog2.show();
		}
	}

	public void storeParsedJourneysInDB() {
		// app.sdb.storeParsedJourneysInDB(journeyParser.lastTPWD.orig,journeyParser.result);
	}

	public String getURL(TripPlan tp, CDate d, int numberBefore, int numberAfter) {
		return "http://webservices.ns.nl/ns-api-treinplanner?" + "departure="
				+ (app.departureOrArrival ? "true" : "false")
				+ tp.getSearchTerms() + "&dateTime=" + d.getISOString(true)
				+ "&previousAdvices" + numberBefore + "&nextAdvices"
				+ numberAfter + "&hslAllowed="
				+ (app.takeHiSpeedTrains ? "true" : "false") + "&yearCard="
				+ (app.userHasAnnualPass ? "true" : "false");
	}

	public String getFallBackURL(TripPlan tp, CDate d) {
		return "http://m.ns.nl/planner.action?" + "departure="
				+ (app.departureOrArrival ? "true" : "false")
				+ tp.getSearchTerms2(true) + "&date=" + d.getDayMonth()
				+ "&time" + d.getHourMinute() + "&hsl="
				+ (app.takeHiSpeedTrains ? "true" : "false") + "&card="
				+ (app.userHasAnnualPass ? "true" : "false")
				+ "&planroute=Journey+advice";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_open_browser, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_button_refresh) {
			int scroll = ((ScrollView) findViewById(R.id.ScrollView01))
					.getScrollY()
					+ ((ScrollView) findViewById(R.id.ScrollView01))
							.getHeight() / 2;
			int i;
			for (i = 0; i < journeys.size(); i++) {
				View v = lla.getView(i);
				if (scroll >= v.getTop() && scroll <= v.getBottom())
					break;
			}

			if (i >= 0 && i < journeys.size())
				lastDate = (app.departureOrArrival ? journeys.get(i).plannedDeparture
						: journeys.get(i).plannedArrival);
			startDownloading(5, 5, false);

			return true;
		} else if (item.getItemId() == R.id.menu_button_openbrowser) {
			startActivity(new Intent("android.intent.action.VIEW",
					Uri.parse(getFallBackURL(lastTripPlan, lastDate))));
			return true;
		} else if (item.getItemId() == R.id.menu_button_share) {
			Journey journey = null;
			for (Journey j : journeys) {
				if (j.expanded > 0) {
					journey = j;
					break;
				}
			}
			if (journey != null) {
				final String[] descr = new String[3];
				for (int i = 0; i < 3; i++)
					descr[i] = journey.getDescription(i);

				final CharSequence[] items = {
						getString(R.string.send_short) + " ("
								+ descr[0].length() + ")",
						getString(R.string.send_medium) + " ("
								+ descr[1].length() + ")",
						getString(R.string.send_long) + " ("
								+ descr[2].length() + ")" };

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.send_journey);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						Intent sharingIntent = new Intent(Intent.ACTION_SEND);
						sharingIntent.setType("text/*");
						sharingIntent.putExtra(
								android.content.Intent.EXTRA_TEXT, descr[item]);
						startActivity(Intent.createChooser(
								sharingIntent,
								getApplicationContext().getText(
										R.string.share_using)));
					}
				});
				AlertDialog alert = builder.create();
				alert.show();

			} else {
				Toast.makeText(this, R.string.no_journey_selected,
						Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}

	private class Downloader extends ASyncNSDownloader {
		private boolean scrollDown;

		public Downloader(String url, ProgressDialog dialog, boolean scrollDown) {
			super(url, journeyParser, dialog);
			currentlyDownloading = true;
			journeyParser.lastTPWD = new TripPlanWithDeparture(lastTripPlan,
					app.departureOrArrival, lastDate);
			((Button) findViewById(R.id.ButtonMore)).setEnabled(false);
			this.scrollDown = scrollDown;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (dialog != null)
				dialog.dismiss();
			if (!failed)
				refreshJourneys(true);
			done();
			if (scrollDown)
				((ScrollView) findViewById(R.id.ScrollView01))
						.post(new Runnable() {
							@Override
							public void run() {
								((ScrollView) findViewById(R.id.ScrollView01))
										.fullScroll(View.FOCUS_DOWN);
							}
						});
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			done();
		}

		protected void done() {
			((ProgressBar) findViewById(R.id.ImageViewLoading2))
					.setVisibility(View.GONE);
			((Button) findViewById(R.id.ButtonMore)).setEnabled(true);
			currentlyDownloading = false;
		}
	}

	private class GetJourneysFromDBAsync extends
			AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			try {
				loadNewJourneys();
			} catch (Exception e) {
			}
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			refreshJourneys(false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		morePressed = false;
	}
}