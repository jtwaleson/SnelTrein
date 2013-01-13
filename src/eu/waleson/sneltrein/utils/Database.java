package eu.waleson.sneltrein.utils;

import java.util.ArrayList;
import java.util.Collections;

import org.xmlpull.v1.XmlPullParser;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;
import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.STApplication;
import eu.waleson.sneltrein.cornerstones.CDate;
import eu.waleson.sneltrein.cornerstones.Journey;
import eu.waleson.sneltrein.cornerstones.Station;
import eu.waleson.sneltrein.cornerstones.TrainStop;
import eu.waleson.sneltrein.cornerstones.TrainTravel;
import eu.waleson.sneltrein.cornerstones.TripPlan;

public class Database {

	public final String DB_NAME = "MyStations";
	public final String STATIONS_TABLE_NAME = "Stations";
	public final String TRIPS_TABLE_NAME = "Trips";

	public SQLiteDatabase sampleDB;

	STApplication app;

	/** Called when the activity is first created. */

	public Database(Context context, STApplication app) {
		this.app = app;
		sampleDB = null;

		app.nearStations = new ArrayList<Station>();

		DatabaseHelper dbHelper = new DatabaseHelper(context, DB_NAME);
		sampleDB = dbHelper.getWritableDatabase();

		try {

		} catch (SQLiteException se) {
			Log.e(getClass().getSimpleName(),
					"Could not create or Open the database");
		}
	}

	public void CloseDB() {
		if (sampleDB != null) {
			sampleDB.close();
		}
	}

	public ArrayList<Station> getDestinationStations(Station s) {
		ArrayList<Station> result = new ArrayList<Station>();

		Cursor c = sampleDB.rawQuery("SELECT S2Name, S2Code, Hits FROM "
				+ TRIPS_TABLE_NAME + " WHERE S1Code = '" + s.code
				+ "' ORDER BY Hits DESC LIMIT " + app.numberOfNearStations,
				null);

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					Station n = new Station(c);
					n.special = 2;
					result.add(n);
				} while (c.moveToNext());
			}
			c.close();
		}

		return result;
	}

	public void refreshStations(boolean sortByUsage) {
		app.myStations = new ArrayList<Station>();
		Cursor c = sampleDB.rawQuery("SELECT Name,Code,Hits FROM "
				+ STATIONS_TABLE_NAME + " ORDER BY Hits DESC LIMIT "
				+ app.numberOfStations, null);

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					app.myStations.add(new Station(c));
				} while (c.moveToNext());
			}
			c.close();
		}
		if (!sortByUsage)
			Collections.sort(app.myStations);
		app.updateStations();
	}

	public ArrayList<TripPlan> getTrips(int limit, boolean sortByUsage) {
		ArrayList<TripPlan> result = new ArrayList<TripPlan>();
		Cursor c = sampleDB.rawQuery("SELECT * FROM " + TRIPS_TABLE_NAME +
		// (app.lastused != null ? " WHERE NOT ("+app.lastused.getDBClause()+")"
		// : "")
		// +
				" ORDER BY Hits DESC LIMIT " + limit, null);
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					result.add(TripPlan.fromCursor(c));
				} while (c.moveToNext());
			}
			c.close();
		}
		if (!sortByUsage)
			Collections.sort(result);
		if (app.lastUsedTripPlan != null) {
			app.lastUsedTripPlan.special = 1;
			result.add(0, app.lastUsedTripPlan);
		}
		return result;
	}

	public void AddStationHit(Station s) {
		if (!s.hasCode())
			return;

		Cursor c = sampleDB.rawQuery("SELECT Name FROM " + STATIONS_TABLE_NAME
				+ " where Code = '" + s.code + "'", null);
		if (c != null) {
			if (c.moveToFirst()) {
				c.close();
				sampleDB.execSQL("UPDATE " + STATIONS_TABLE_NAME
						+ " SET Hits = Hits + 1 WHERE Code = '" + s.code + "';");
				app.database.refreshStations(app.sortStationsByUsage);
				return;
			}
		}
		ContentValues cv = new ContentValues();
		cv.put("Name", s.name);
		cv.put("Code", s.code);
		sampleDB.insert(STATIONS_TABLE_NAME, null, cv);
		app.database.refreshStations(app.sortStationsByUsage);
	}

	public void AddTripHit(TripPlan t) {
		if (!t.hasCodes())
			return;

		app.lastUsedTripPlan = t;

		Station[] stations = t.getStations();
		for (Station s : stations)
			AddStationHit(s);

		Cursor c = sampleDB.rawQuery("SELECT S1Name FROM " + TRIPS_TABLE_NAME
				+ " WHERE " + t.getDBClause() + ";", null);
		if (c != null) {
			if (c.moveToFirst()) {
				c.close();
				sampleDB.execSQL("UPDATE " + TRIPS_TABLE_NAME
						+ " SET Hits = Hits + 1 WHERE " + t.getDBClause() + ";");
				app.refreshTrips();
				return;
			}

		}
		ContentValues cv = new ContentValues();
		t.addToContenValues2(cv);
		sampleDB.insert(TRIPS_TABLE_NAME, null, cv);
		app.refreshTrips();
	}

	public void UpdateColor(TripPlan t) {
		sampleDB.execSQL("UPDATE " + TRIPS_TABLE_NAME + " SET UserColor = '"
				+ t.userColor + "' WHERE " + t.getDBClause() + ";");
	}

	public void Remove(TripPlan t) {
		sampleDB.execSQL("DELETE FROM " + TRIPS_TABLE_NAME + " WHERE "
				+ t.getDBClause() + ";");
	}

	public String strLim(String s, int i) {
		if (s.length() > i)
			return s.substring(0, i - 2) + "...";
		return s;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context ctx, String db_name) {
			super(ctx, db_name, null, 13);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + STATIONS_TABLE_NAME
					+ " (Name VARCHAR, Code VARCHAR, Hits INT DEFAULT 1);");
			db.execSQL("CREATE TABLE IF NOT EXISTS "
					+ TRIPS_TABLE_NAME
					+ " (S1Name VARCHAR, S1Code VARCHAR, S2Name VARCHAR, S2Code VARCHAR, S3Name VARCHAR, S3Code VARCHAR, Hits INT DEFAULT 1, UserColor INT DEFAULT 1);");
			upgrade6to7(db);
			upgrade7to8(db);
			upgrade8to9(db);
			upgrade9to10(db);
			updateLocationsFromFile(db);
		}

		private void upgrade4to5(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE TrainStop_Cache ADD COLUMN TripPlanId INTEGER;");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion <= 1) {
				db.execSQL("ALTER TABLE " + TRIPS_TABLE_NAME
						+ " ADD COLUMN UserColor INT DEFAULT 1;");
				oldVersion++;
			}
			if (oldVersion == 2) {
				upgrade2to3(db);
				oldVersion++;
			}
			if (oldVersion == 3) {
				oldVersion++;
			}
			if (oldVersion == 4) {
				upgrade4to5(db);
				oldVersion++;
			}
			if (oldVersion == 5) {
				db.execSQL("ALTER TABLE TrainStop_Cache ADD COLUMN IsDeparture INTEGER");
				oldVersion++;
			}
			if (oldVersion == 6) {
				dropFormerCache(db);
				upgrade6to7(db);
				oldVersion++;
			}
			if (oldVersion == 7) {
				upgrade7to8(db);
				oldVersion++;
			}
			if (oldVersion == 8) {
				upgrade8to9(db);
				oldVersion++;
			}
			if (oldVersion == 9) {
				upgrade9to10(db);
				oldVersion++;
			}
			if (oldVersion == 10) {
				updateLocationsFromFile(db);
				oldVersion++;
			}
			if (oldVersion == 11) {
				updateLocationsFromFile(db);
				oldVersion++;
			}
			if (oldVersion == 12) {
				updateLocationsFromFile(db);
				oldVersion++;
			}

		}

		private void upgrade8to9(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE Journeys (" + " id INTEGER PRIMARY KEY,"
					+ " fetchedon INTEGER, " + " deptime VARCHAR,"
					+ " arrtime VARCHAR," + " depDelay VARCHAR,"
					+ " arrDelay VARCHAR," + " stationFrom VARCHAR,"
					+ " stationTo VARCHAR," + " stationVia VARCHAR,"
					+ " plannedDuration VARCHAR," + " actualDuration VARCHAR,"
					+ " ExtraInfo VARCHAR)");

			db.execSQL("CREATE TABLE TrainTravels ("
					+ " id INTEGER PRIMARY KEY," + " journeyid INTEGER, "
					+ " traintype VARCHAR," + " ritnummer VARCHAR,"
					+ " delay VARCHAR)");

			db.execSQL("CREATE TABLE TrainStops (" + " id INTEGER PRIMARY KEY,"
					+ " journeyid INTEGER, " + " trainstopid INTEGER,"
					+ " StationName VARCHAR," + " StationCode VARCHAR,"
					+ " Delay VARCHAR," + " Track VARCHAR,"
					+ " TrackChange BOOLEAN," + " Date VARCHAR)");
			db.execSQL("CREATE INDEX journey_fetch ON Journeys (fetchedon)");
		}

		private void upgrade9to10(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE Journeys ADD COLUMN transfers INTEGER DEFAULT '0'");
		}

		private void upgrade2to3(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS TripPlan_Cache (id INTEGER PRIMARY KEY, s1 VARCHAR,s2 VARCHAR,s3 VARCHAR,`datetime` INTEGER,departure INTEGER,refreshed INTEGER);");
			db.execSQL("CREATE TABLE IF NOT EXISTS TrainTravel_Cache (id INTEGER PRIMARY KEY, TripPlanId INTEGER, KindOfTrain TEXT, ViaStations TEXT,Cancelled INTEGER);");
			db.execSQL("CREATE TABLE IF NOT EXISTS TrainStop_Cache (TrainTravelId INTEGER, Station TEXT, Time TEXT, Track TEXT,Delay TEXT);");
		}

		private void upgrade7to8(SQLiteDatabase db) {
			db.execSQL("CREATE INDEX station_lat ON StationsFull (Lat);");
			db.execSQL("CREATE INDEX station_lon ON StationsFull (Lon);");
		}

		private void dropFormerCache(SQLiteDatabase db) {
			db.execSQL("DROP TABLE TripPlan_Cache");
			db.execSQL("DROP TABLE TrainTravel_Cache");
			db.execSQL("DROP TABLE TrainStop_Cache");
		}

		private void upgrade6to7(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE StationsFull (Name TEXT, Code VARCHAR, Alias BOOLEAN, Abroad BOOLEAN, Lon DOUBLE, Lat DOUBLE);");
			db.execSQL("CREATE UNIQUE INDEX station_name ON StationsFull (Name, Code);");
		}

		private void updateLocationsFromFile(SQLiteDatabase db) {
			db.execSQL("DELETE FROM StationsFull");
			XmlResourceParser p = app.getApplicationContext().getResources()
					.getXml(R.xml.ns_api_stations);
			try {
				p.next();
				int eventType = p.getEventType();
				String NodeValue = "";

				String s_name = "";
				String s_code = "";
				boolean s_abroad = false;
				boolean s_alias = false;
				double s_lon = 0;
				double s_lat = 0;
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						NodeValue = p.getName();// Start of a Node
					} else if (eventType == XmlPullParser.END_TAG) {
						if (p.getName().equalsIgnoreCase("Station")) {
							ContentValues cv = new ContentValues();
							cv.put("Name", s_name);
							cv.put("Code", s_code);
							cv.put("Alias", s_alias);
							cv.put("Abroad", s_abroad);
							cv.put("Lon", s_lon);
							cv.put("Lat", s_lat);
							db.insert("StationsFull", null, cv);
						}
					} else if (eventType == XmlPullParser.TEXT) {
						String t = p.getText();
						if (NodeValue.equalsIgnoreCase("name")) {
							s_name = t;
						} else if (NodeValue.equalsIgnoreCase("code")) {
							s_code = t.toLowerCase();
						} else if (NodeValue.equalsIgnoreCase("country")) {
							s_abroad = !(t.equals("NL"));
						} else if (NodeValue.equalsIgnoreCase("lat")) {
							s_lat = Double.parseDouble(t);
						} else if (NodeValue.equalsIgnoreCase("long")) {
							s_lon = Double.parseDouble(t);
						} else if (NodeValue.equalsIgnoreCase("alias")) {
							s_alias = t.equalsIgnoreCase("TRUE");
						}
					}

					eventType = p.next(); // Get next event from xml parser
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public void getNearStations(Location l) {
		app.nearStations.clear();
		if (l != null) {
			Double lat = l.getLatitude();
			Double lon = l.getLongitude();
			String[] s = (lon + "," + lon + "," + lat + "," + lat + ","
					+ (lon - .25) + "," + (lon + 0.25) + "," + (lat - .25)
					+ "," + (lat + .25)).split(",");
			Cursor c = sampleDB
					.rawQuery(
							"SELECT code, name, lat, lon, (lon - ?) * (lon -?) + (lat-?) * (lat-?) distance FROM StationsFull WHERE alias = 0 AND lon > ? AND lon < ? AND lat > ? AND lat < ? ORDER BY distance ASC LIMIT "
									+ app.numberOfNearStations * 2, s);
			while (c.moveToNext()) {
				Station s1 = new Station(c.getString(0), c.getString(1));
				Location l1 = new Location(l);
				l1.setLongitude(c.getDouble(3));
				l1.setLatitude(c.getDouble(2));
				s1.distance = l1.distanceTo(l);
				s1.special = 1;
				app.nearStations.add(s1);
			}
			c.close();
			Collections.sort(app.nearStations);
			while (app.nearStations.size() > app.numberOfNearStations)
				app.nearStations.remove(app.numberOfNearStations);
		}
		refreshStations(app.sortStationsByUsage);
	}

	public void deleteJourneys(String sql) {
		Cursor c1 = sampleDB.rawQuery(sql, null);
		String idsin = null;
		while (c1.moveToNext()) {
			if (idsin != null)
				idsin += ",";
			else
				idsin = "";
			idsin += c1.getString(0);
		}
		if (idsin != null) {
			sampleDB.execSQL("DELETE FROM Journeys WHERE id IN (" + idsin + ")");
			sampleDB.execSQL("DELETE FROM TrainTravels WHERE journeyid IN ("
					+ idsin + ")");
			sampleDB.execSQL("DELETE FROM TrainStops WHERE journeyid IN ("
					+ idsin + ")");
		}
		c1.close();
	}

	public ArrayList<Journey> getJourneys(TripPlan tp, CDate lastDate) {
		deleteJourneys("SELECT id FROM Journeys WHERE fetchedon < "
				+ (CDate.now().getTimeinMillis() - 10800000) + ";");
		ArrayList<Journey> result = new ArrayList<Journey>();
		String s = "SELECT fetchedon, deptime, arrtime, depDelay, arrDelay, plannedDuration, actualDuration, id, extrainfo, transfers FROM Journeys WHERE "
				+ tp.getDBCacheClause() + " ORDER BY deptime ASC";
		Cursor c = sampleDB.rawQuery(s, null);
		while (c.moveToNext()) {
			Journey j = new Journey(c.getLong(0), this);
			try {
				j.plannedDeparture = CDate.parseNSTime(c.getString(1)
						.substring(0, 20) + "00:00");
				j.plannedArrival = CDate.parseNSTime(c.getString(2).substring(
						0, 20)
						+ "00:00");
				j.delayDeparture = c.getString(3);
				j.delayArrival = c.getString(4);
				j.plannedDuration = c.getString(5);
				j.actualDuration = c.getString(6);
				j.id = c.getLong(7);
				j.extraInfo = c.getString(8);
				j.transfers = c.getInt(9);
				result.add(j);
			} catch (Exception e) {

			}
		}
		c.close();
		return result;
	}

	public ArrayList<TrainTravel> getTrainTravels(Journey journey,
			long journeyid) {
		ArrayList<TrainTravel> result = new ArrayList<TrainTravel>();
		String s = "SELECT traintype, ritnummer, delay, id FROM TrainTravels WHERE journeyid = '"
				+ journeyid + "'";
		Cursor c = sampleDB.rawQuery(s, null);
		while (c.moveToNext()) {
			TrainTravel tt = new TrainTravel(journey);
			tt.kindOfTrain = c.getString(0);
			tt.ritNummer = c.getString(1);
			tt.delay = c.getString(2);
			for (TrainStop ts : getTrainStops(c.getLong(3))) {
				tt.addStop(ts);
			}

			result.add(tt);
		}
		c.close();
		return result;
	}

	private ArrayList<TrainStop> getTrainStops(long traintravelid) {
		ArrayList<TrainStop> result = new ArrayList<TrainStop>();
		String s = "SELECT StationName, StationCode, Delay, Track, TrackChange, Date, id FROM TrainStops WHERE trainstopid = '"
				+ traintravelid + "'";
		Cursor c = sampleDB.rawQuery(s, null);
		while (c.moveToNext()) {
			TrainStop ts = new TrainStop();
			ts.station = new Station(c.getString(1), c.getString(0));
			ts.delay = c.getString(2);
			ts.track = c.getString(3);
			if (!c.isNull(5))
				ts.date = CDate.parseNSTime(c.getString(5).substring(0, 20)
						+ "00:00");
			result.add(ts);
		}
		c.close();
		return result;
	}

	public ArrayList<Station> getAllStations() {
		ArrayList<Station> result = new ArrayList<Station>();
		String s = "SELECT code, name FROM StationsFull WHERE name NOT IN ('Almere', 'Amsterdam', 'Alphen aan den Rijn', 'Bunde', 'Bonen', 'Dulmen', 'Ludinghausen', 'Lunen', 'Munster', 'De Eschmarke', 'Eschmarke', 'Dusseldorf', 'Koln', 'Den Haag', 'Duren', 'Dulken', 'Koln', 'Leiden', 'Monchengladbach', 'Gunzburg', 'Munchen', 'Otztal', 'Rotterdam', 'Goppingen', 'Utrecht', 'Worgl', 'Zurich') ORDER BY code ASC";
		Cursor c = sampleDB.rawQuery(s, null);
		while (c.moveToNext()) {
			result.add(new Station(c.getString(0), c.getString(1)));
		}
		c.close();
		return result;
	}
}
