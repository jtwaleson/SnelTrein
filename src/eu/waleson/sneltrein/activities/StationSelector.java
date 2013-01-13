package eu.waleson.sneltrein.activities;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.STApplication;
import eu.waleson.sneltrein.adapters.LinearLayoutAdapter;
import eu.waleson.sneltrein.cornerstones.Station;
import eu.waleson.sneltrein.utils.ILocationActivity;

public class StationSelector extends Activity implements OnClickListener,
		OnItemClickListener, ILocationActivity {

	STApplication app = null;
	int stationCount = 1;
	int curStation = 1;
	Bundle returnBundle;
	private boolean locationForced = false;

	// I like naming variables that do something slightly intelligent
	//   after Microsoft's intelliMouse
	ArrayList<Station> intelliStations = null;

	LinearLayoutAdapter<Station> lla;

	private LayoutInflater inflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_selector);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		app = (STApplication) getApplication();
		stationCount = getIntent().getExtras().getInt("StationCount");
		returnBundle = new Bundle();

		lla = new LinearLayoutAdapter<Station>(
				(LinearLayout) findViewById(R.id.LinearLayoutStations), this);
		lla.refresh(app.myStationsAndNearStations, inflater);
		if (lla.getCount() > 4)
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		prepareAutoComplete();
		updateTexts();
	}
	private class DiacreticStationArrayAdapter extends ArrayAdapter<Station> {
		private ArrayList<Station> mItems;
        private DiacreticsFilter mFilter = null;

		public DiacreticStationArrayAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}
		@Override
		public Filter getFilter() {
			if (mFilter == null)
				return new DiacreticsFilter();
			else
				return mFilter;
		}
        @Override
        public int getCount() {
            return mItems.size();
        }
        @Override
        public Station getItem(int position) {
            return mItems.get(position);
        }
        @Override
        public int getPosition(Station item) {
            return mItems.indexOf(item);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
		private class DiacreticsFilter extends Filter {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults fr = new FilterResults();
				ArrayList<Station> results = new ArrayList<Station>();
				
				if (constraint != null && constraint.length() > 0) {
					String lowerConstraint = " " + constraint.toString().toLowerCase().trim();
					
					for (Station s : app.allStations) {
						if (s.normalizedName.contains(lowerConstraint))
							results.add(s);
					}
				}
				fr.values = results;
				fr.count = results.size();
				return fr;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				mItems = (ArrayList<Station>)results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
			}
			
		}
	}

	public void prepareAutoComplete() {
		DiacreticStationArrayAdapter adapter = new DiacreticStationArrayAdapter(this,	R.layout.autocomplete_item_station);
		AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextViewStationSelector);
		actv.setThreshold(1);
		actv.setAdapter(adapter);
		actv.setOnItemClickListener(this);
	}

	public void AddStation(Station s) {
		((ScrollView) findViewById(R.id.ScrollView)).scrollTo(0, 0);

		s.addToBundle(returnBundle, curStation);
		Toast.makeText(this,
				getString(R.string.station_selected) + ": " + s.name,
				Toast.LENGTH_SHORT).show();
		curStation++;
		if (curStation > stationCount) {
			sendAndFinish();
		} else {
			if (curStation == 2) {
				intelliStations = app.database.getDestinationStations(s);
			} else {
				intelliStations = null;
			}
			updateTexts();
		}
	}

	public void sendAndFinish() {
		Intent returnIntent = new Intent();
		returnBundle.putInt("Count", stationCount);
		returnIntent.putExtras(returnBundle);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private void updateTexts() {
		AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextViewStationSelector);
		actv.setText("");
		TextView tv = (TextView) findViewById(R.id.TextViewStationSelectorStationName);
		if (stationCount == 1)
			tv.setText(R.string.station_dots);
		else {
			if (curStation == 1)
				tv.setText(R.string.from_station);
			else if (curStation == 2)
				tv.setText(R.string.to_station);
			else
				tv.setText(R.string.via_station);
		}
		actv.requestFocus();
		refreshStations();
	}

	@Override
	public void onClick(View v) {
		AddStation((Station) v.getTag());
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AddStation((Station) parent.getItemAtPosition(position));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			curStation--;
			if (curStation == 1)
				intelliStations = null;
			if (curStation > 0) {
				updateTexts();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void refreshStations() {
		if (intelliStations != null) {
			ArrayList<Station> st = new ArrayList<Station>();
			ArrayList<String> codes = new ArrayList<String>();
			st.addAll(intelliStations);
			for (Station s : intelliStations)
				codes.add(s.code);
			for (Station s : app.myStations)
				if (!codes.contains(s.code))
					st.add(s);
			lla.refresh(st, inflater);
		} else {
			lla.refresh((curStation == 1 ? app.myStationsAndNearStations
					: app.myStations), inflater);
		}
	}

	@Override
	public void processLocation() {
		refreshStations();
		if (locationForced) {
			Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT)
					.show();
			locationForced = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		app.locationActivity = this;
	}

	@Override
	public void onPause() {
		super.onPause();
		app.locationActivity = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh_position) {
			locationForced = true;
			app.getNewFix();
			return true;
		}
		return false;
	}

}
