package eu.waleson.sneltrein.cornerstones;

import java.util.ArrayList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.utils.IViewable;

public class TrainTravel implements IViewable {
	public TrainStop trainstopA = null;
	public TrainStop trainstopB = null;
	public boolean cancelled = false;
	public String delay = "";
	public String kindOfTrain = "";
	public ArrayList<TrainStop> allstops;
	public String ritNummer = "";
	public Journey journey;

	public TripPlan getTripPlan() {
		TripPlan t = new TripPlan(trainstopA.station, trainstopB.station);
		t.date = new CDate(trainstopA.date);
		return t;
	}

	public void print() {
		Log.v("TrainTravel", "cancelled: " + (cancelled ? "yes" : "no"));
		Log.v("TrainTravel", "delay: " + delay);
		Log.v("TrainTravel", "kindOfTrain: " + kindOfTrain);
		Log.v("TrainTravel", "TrainStop A");
		trainstopA.print();
		Log.v("TrainTravel", "TrainStop B");
		trainstopB.print();
	}

	public TrainTravel(TrainStop a, TrainStop b, Journey journey) {
		this.journey = journey;
		trainstopA = a;
		trainstopB = b;
	}

	public TrainTravel(Journey journey) {
		this.journey = journey;
		allstops = new ArrayList<TrainStop>();
	}

	public void addStop(TrainStop ts) {
		if (trainstopA == null) {
			trainstopA = ts;
			delay = ts.delay;
		} else {
			if (trainstopB != null) {
				allstops.add(trainstopB);
			}
			trainstopB = ts;
		}
	}

	@Override
	public View getView(LayoutInflater inflater, int number) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(
				R.layout.list_item_traintravel, null);
		updateView(inflater, view, 1);
		return view;
	}

	@Override
	public void updateView(LayoutInflater inflater, View view, int number) {
		RelativeLayout v = (RelativeLayout) view;
		((TextView) v.findViewById(R.id.TVTimeA)).setText(trainstopA.date
				.getHourMinute());
		((TextView) v.findViewById(R.id.TVTimeB)).setText(trainstopB.date
				.getHourMinute());
		((TextView) v.findViewById(R.id.TVTrackA)).setText(trainstopA.track);
		((TextView) v.findViewById(R.id.TVTrackB)).setText(trainstopB.track);
		((TextView) v.findViewById(R.id.TVStationA))
				.setText(trainstopA.station.name);
		((TextView) v.findViewById(R.id.TVStationB)).setText(" > "
				+ trainstopB.station.name);

		if (journey.expanded == 2) {
			String via = "";
			boolean first = true;
			for (TrainStop ts : allstops) {
				if (!first)
					via += "\n";
				else
					first = false;
				via += ts.date.getHourMinute() + " ";
				via += ts.station.name;
				via += (ts.delay != null && ts.delay.length() > 0 ? " ("
						+ ts.delay + ")" : "");
			}
			((TextView) v.findViewById(R.id.TVViaStations)).setText(via);
			((TextView) v.findViewById(R.id.TVViaStations)).setTextAppearance(
					inflater.getContext(), R.style.Extra);
		} else {
			((TextView) v.findViewById(R.id.TVViaStations)).setText("(+ "
					+ allstops.size() + " stops)");
			((TextView) v.findViewById(R.id.TVViaStations)).setTextAppearance(
					inflater.getContext(), R.style.ExtraItalic);
		}

		((TextView) v.findViewById(R.id.TVExtra1)).setText(kindOfTrain);
		((TextView) v.findViewById(R.id.TVDelay1)).setText(trainstopA.delay);
		if (trainstopB.delay != null && trainstopB.delay.length() > 0) {
			((TextView) v.findViewById(R.id.TVDelay2))
					.setText(trainstopB.delay);
			((TextView) v.findViewById(R.id.TVDelay2))
					.setVisibility(View.VISIBLE);
		} else {
			((TextView) v.findViewById(R.id.TVDelay2)).setVisibility(View.GONE);
		}

	}

	public String getDescription(int size) {
		// size 0: compressed, 1: normal, 2: extensive
		String s = "";
		s += trainstopA.getDescription();
		if (size > 1) {
			for (TrainStop ts : allstops) {
				if (ts != trainstopA && ts != trainstopB) {
					s += " - " + ts.getDescription();
				}
			}
		}
		s += ">> " + trainstopB.getDescription();
		if (size == 0) {
			s = s.replaceAll("[aeoiyu]", "");
		}
		return s;
	}
}
