package eu.waleson.sneltrein.cornerstones;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.utils.IViewable;

public class TrainStop implements IViewable {
	public Station station;
	public CDate date;
	public String track;
	public String delay = "";

	public void print() {
		Log.v("TrainStop",
				"Date: " + date.getDayMonth() + " (" + date.getHourMinute()
						+ ") ");
		Log.v("TrainStop", "Station: " + station.name + " (" + station.code
				+ ")");
		Log.v("TrainStop", "Delay: " + (delay != null ? delay : ""));
		Log.v("TrainStop", "Track: " + (track != null ? track : ""));

	}

	public TrainStop() {
		this.date = CDate.now();
		this.track = "";
		this.station = new Station("", "");
		this.delay = "";
	}

	public TrainStop(Station station, CDate datetime, String track) {
		this.track = track;
		this.date = datetime;
		this.station = station;
	}

	@Override
	public View getView(LayoutInflater inflater, int number) {
		View v = inflater.inflate(R.layout.list_item_track, null);
		updateView(inflater, v, number);
		return v;
	}

	@Override
	public void updateView(LayoutInflater inflater, View view, int number) {
		TextView tv;
		tv = (TextView) view.findViewById(R.id.TVTime);
		tv.setText(date.getHourMinute());
		tv = (TextView) view.findViewById(R.id.TVTrack);
		tv.setText(track);
		tv = (TextView) view.findViewById(R.id.TVDirection);
		tv.setText(station.name);
	}

	public void insertIntoCache(SQLiteDatabase db, long trainTravelId,
			long tripPlanId, boolean isDeparture) {
		ContentValues cv = new ContentValues();
		cv.put("TrainTravelId", trainTravelId);
		cv.put("TripPlanId", tripPlanId);
		cv.put("Station", station.name);
		cv.put("Track", track);
		cv.put("Time", date.getDBFormat());
		cv.put("Delay", delay);
		cv.put("IsDeparture", (isDeparture ? 1 : 0));
		db.insert("TrainStop_Cache", "", cv);
	}

	public void storeInDB(SQLiteDatabase sampleDB, long journeyid,
			long trainstopid) {
		ContentValues values = new ContentValues();
		values.put("journeyid", journeyid);
		values.put("trainstopid", trainstopid);
		values.put("StationName", station.name);
		values.put("StationCode", station.code);
		values.put("Delay", delay);
		values.put("Track", track);
		values.put("TrackChange", false);
		values.put("Date", date.getISOString(false));
		sampleDB.insert("TrainStops", null, values);
	}

	public String getDescription() {
		String s = "";
		s += date.getHourMinute();
		s += (delay != null && delay.length() > 0 ? "(" + delay + ") " : " ");
		s += station.name;
		s += track != null ? " [" + track + "]" : "";
		s += "\n";
		return s;
	}
}
