package com.waleson.sneltrein.cornerstones;

import com.waleson.sneltrein.R;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TrainTravelDeparture extends TrainTravel {

	public String viaStations = "";

	public TrainTravelDeparture(TrainStop a, TrainStop b) {
		super(a, b, null);
	}

	public TrainTravelDeparture(TrainStop a, TrainStop b, String viaStations) {
		super(a, b, null);
		this.viaStations = viaStations;
	}

	@Override
	public View getView(LayoutInflater inflater, int number) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(
				R.layout.list_item_traintraveldeparture, null);
		updateView(view, 1);
		return view;
	}

	public void updateView(View view, int number) {
		RelativeLayout v = (RelativeLayout) view;

		((TextView) v.findViewById(R.id.TVTime)).setText(trainstopA.date
				.getHourMinute());
		((TextView) v.findViewById(R.id.TVTrack)).setText(trainstopA.track);
		((TextView) v.findViewById(R.id.TVStation))
				.setText(trainstopB.station.name);
		((TextView) v.findViewById(R.id.TVDelay)).setText(delay);
		((TextView) v.findViewById(R.id.TVExtra1)).setText("");

		// ((TextView)v.findViewById(R.id.TVExtra2)).setText("");
		((TextView) v.findViewById(R.id.TVExtra1)).setText(viaStations);
		((TextView) v.findViewById(R.id.TVExtra2)).setText(kindOfTrain);

		if (cancelled) {
			((TextView) v.findViewById(R.id.TVExtra2))
					.setText(R.string.journey_is_cancelled);
			((TextView) v.findViewById(R.id.TVExtra2)).setTextColor(Color.RED);
		}
		v.setTag(this);
	}

}
