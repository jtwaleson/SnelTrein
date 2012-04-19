package com.waleson.sneltrein;

import java.util.ArrayList;
import java.util.EventListener;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.waleson.sneltrein.activities.StationSelector;
import com.waleson.sneltrein.cornerstones.CDate;
import com.waleson.sneltrein.cornerstones.Station;
import com.waleson.sneltrein.cornerstones.TripPlan;
import com.waleson.sneltrein.utils.Database;
import com.waleson.sneltrein.utils.ILocationActivity;

public class STApplication extends Application implements
		OnSharedPreferenceChangeListener {

	public ArrayList<Station> myStations;
	public ArrayList<TripPlan> myTrips;
	public ArrayList<Station> nearStations;
	public ArrayList<Station> myStationsAndNearStations;

	public int numberOfStations = 10;
	public int numberOfTrips = 10;
	public boolean tripsAboveStations = true;
	public boolean startSearching = true;
	public boolean sortStationsByUsage = false;
	public boolean sortTripsByUsage = true;
	public boolean departureOrArrival = true;
	public boolean userHasAnnualPass = false;
	public boolean takeHiSpeedTrains = false;
	public boolean fallBackOnBrowser = true;
	public int numberOfNearStations = 3;

	public ILocationActivity locationActivity = null;

	public TripPlan lastUsedTripPlan = null;

	public EventListener eventListener;

	public boolean settingsChanged = false;

	private LocationManager locationManager;
	public boolean locationWaiting = false;
	public long lastLocationTime = 0;

	public Database database;

	@Override
	public void onCreate() {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		loadFromPreferences();
		myStationsAndNearStations = new ArrayList<Station>();
		database = new Database(getApplicationContext(), this);
		database.refreshStations(sortStationsByUsage);
		refreshTrips();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (numberOfNearStations > 0) {
			registerLocation(locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
			getNewFix();
		}

	}

	public void updateStations() {
		myStationsAndNearStations.clear();
		myStationsAndNearStations.addAll(nearStations);
		myStationsAndNearStations.addAll(myStations);
	}

	public void getNewFix() {
		if (locationWaiting == false) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 50000, 500,
					new LocationListener() {
						public void onStatusChanged(String provider,
								int status, Bundle extras) {
						}

						public void onProviderEnabled(String provider) {
						}

						public void onProviderDisabled(String provider) {
						}

						public void onLocationChanged(Location location) {
							locationManager.removeUpdates(this);
							registerLocation(location);
						}
					});
		}

	}

	public void registerLocation(Location location) {
		locationWaiting = false;
		if (location != null) {
			lastLocationTime = CDate.now().getTimeinMillis();
			database.getNearStations(location);
			updateStations();
			if (locationActivity != null)
				locationActivity.processLocation();
		}
	}

	public void loadFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		numberOfStations = Integer.parseInt(prefs.getString(
				"number_of_fav_stations", "10"));
		numberOfTrips = Integer.parseInt(prefs.getString("number_of_fav_trips",
				"10"));
		tripsAboveStations = prefs.getBoolean("trips_above_stations_in_main",
				true);
		startSearching = prefs.getBoolean("auto_load_nearest_departure", true);
		sortStationsByUsage = prefs.getBoolean("sort_stations_by_hits", false);
		sortTripsByUsage = prefs.getBoolean("sort_trips_by_hits", true);
		userHasAnnualPass = prefs.getBoolean("has_ov_card", false);
		takeHiSpeedTrains = prefs.getBoolean("take_hispeed_trains", false);
		fallBackOnBrowser = prefs.getBoolean("revert_to_browser", true);
		numberOfNearStations = Integer.parseInt(prefs.getString(
				"number_of_near_stations", "3"));
	}

	public void refreshTrips() {
		myTrips = database.getTrips(numberOfTrips, sortTripsByUsage);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		loadFromPreferences();
		if (numberOfNearStations > 0)
			registerLocation(locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		else
			nearStations.clear();

		database.refreshStations(sortStationsByUsage);
		refreshTrips();
		settingsChanged = true;
	}

	public String findStationCodeFromName(String name) {
		name = name.trim();
		int count = StationSelector.stationNames.length;
		for (int i = 0; i < count; i++)
			if (StationSelector.stationNames[i].equalsIgnoreCase(name))
				return StationSelector.stationIds[i];
		return "";
	}

	public String findStationNameFromCode(String code) {
		code = code.trim();
		int count = StationSelector.stationIds.length;
		for (int i = 0; i < count; i++)
			if (StationSelector.stationIds[i].equalsIgnoreCase(code))
				return StationSelector.stationNames[i];
		return "";
	}
}