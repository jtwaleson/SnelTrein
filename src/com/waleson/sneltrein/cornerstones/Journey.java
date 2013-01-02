package com.waleson.sneltrein.cornerstones;

import java.util.ArrayList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.waleson.sneltrein.R;
import com.waleson.sneltrein.adapters.LinearLayoutAdapter;
import com.waleson.sneltrein.utils.Database;
import com.waleson.sneltrein.utils.IViewable;

public class Journey implements IViewable, Comparable<Journey> {
	private ArrayList<TrainTravel> traintravels = null;

	public boolean cancelled = false;
	public int expanded = 0;
	public int transfers = 0;
	long fetchedOn = 0;
	public long currentTime = 0;
	public String delayArrival = "";
	public String delayDeparture = "";
	public String plannedDuration = "";
	public String actualDuration = "";
	public CDate plannedDeparture = null;
	public CDate plannedArrival = null;
	public String extraInfo = "";

	LinearLayoutAdapter<TrainTravel> traintravelsadapter = null;

	private Database db;

	public long id;

	public Journey(long fetchedOn, Database db) {
		this.fetchedOn = fetchedOn;
		this.db = db;
	}

	public ArrayList<TrainTravel> getTrainTravels() {
		if (traintravels == null || traintravels.size() == 0)
			traintravels = db.getTrainTravels(this, id);
		return traintravels;
	}

	public void setExpanded(int expanded) {
		this.expanded = expanded;
	}

	public void toggleExpanded() {
		this.expanded++;
		this.expanded = this.expanded % 3;
	}

	public void clear() {
		traintravels.clear();
	}

	public void print() {
		Log.v("Journey", "printing......");
		Log.v("Journey", "cancelled = " + (cancelled ? "true" : "false"));
		for (TrainTravel tt : traintravels)
			tt.print();
	}

	public void addTrainTravel(TrainTravel tt) {
		traintravels.add(tt);
	}

	public void checkTimes() {

	}

	@Override
	public View getView(LayoutInflater inflater, int number) {
		View view = inflater.inflate(R.layout.list_item_journeysummary, null);
		updateView(inflater, view, number);
		return view;
	}

	@Override
	public void updateView(LayoutInflater inflater, View view, int number) {
		((TextView) view.findViewById(R.id.TVDepartureDate))
				.setText(plannedDeparture.getDayMonth());
		((TextView) view.findViewById(R.id.TVDepartureTime)).setText("D "
				+ plannedDeparture.getHourMinute());
		((TextView) view.findViewById(R.id.TVArrivalDate))
				.setText(plannedArrival.getDayMonth());
		((TextView) view.findViewById(R.id.TVArrivalTime)).setText("A "
				+ plannedArrival.getHourMinute());
		((TextView) view.findViewById(R.id.TVTransfers))
				.setText(transfers > 0 ? transfers + " x" : "direct");
		((TextView) view.findViewById(R.id.TVLength)).setText(actualDuration);
		((TextView) view.findViewById(R.id.TVDelay1)).setText(delayDeparture);
		((TextView) view.findViewById(R.id.TVDelay2)).setText(delayArrival);
		if (extraInfo != null && extraInfo.length() > 0) {
			((TextView) view.findViewById(R.id.TVExtraInfo))
					.setVisibility(View.VISIBLE);
			((TextView) view.findViewById(R.id.TVExtraInfo)).setText(extraInfo);
		} else {
			((TextView) view.findViewById(R.id.TVExtraInfo))
					.setVisibility(View.GONE);
		}
		long diff = currentTime - fetchedOn;
		// if (diff < 5000)
		// ((ImageView)view.findViewById(R.id.IVCache)).setVisibility(ImageView.INVISIBLE);
		// else
		ImageView iv = (ImageView) view.findViewById(R.id.IVCache);
		if (iv.getClass() == ImageView.class) {
			if (diff < 60000)
				iv.setImageResource(R.drawable.floppygr2);
			else if (diff < 600000)
				iv.setImageResource(R.drawable.floppyor);
			else
				iv.setImageResource(R.drawable.floppyrd);
		}

		if (traintravelsadapter == null && expanded > 0) {
			traintravelsadapter = new LinearLayoutAdapter<TrainTravel>(
					(LinearLayout) view.findViewById(R.id.LLDetails));
		}
		if (expanded > 0) {
			traintravelsadapter.refresh(getTrainTravels(), inflater);
		} else if (traintravelsadapter != null) {
			traintravelsadapter.clear();
			traintravelsadapter = null;
		} else if (((LinearLayout) view.findViewById(R.id.LLDetails))
				.getChildCount() > 0)
			((LinearLayout) view.findViewById(R.id.LLDetails)).removeAllViews();
		view.setTag(this);
	}

	@Override
	public int compareTo(Journey another) {
		int res = this.plannedDeparture.compareTo(another.plannedDeparture);
		if (res == 0)
			res = this.plannedArrival.compareTo(another.plannedArrival);
		return res;
	}

	public String getDescription(int size) {
		String s = "";
		for (TrainTravel tt : traintravels) {
			s += tt.getDescription(size);
		}
		return s;
	}
}
