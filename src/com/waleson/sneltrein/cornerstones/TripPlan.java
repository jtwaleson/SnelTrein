package com.waleson.sneltrein.cornerstones;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.waleson.sneltrein.R;
import com.waleson.sneltrein.utils.IViewable;

public class TripPlan implements IViewable, Comparable<TripPlan> {
	public Station stationFrom;
	public Station stationTo;
	public CDate date;
	public int userColor = 0;
	public int hits = 0;
	public int special = 0;

	public TripPlan(Station from, Station to) {
		stationFrom = from;
		stationTo = to;
	}

	public TripPlan getReverse() {
		return new TripPlan(stationTo, stationFrom);
	}

	public static TripPlan fromCursor(Cursor c) {
		TripPlan result = null;
		Station s1 = null, s2 = null, s3 = null;
		int hits = c.getInt(6);
		for (int i = 0; i < 3; i++) {
			if (c.isNull(i * 2) || c.isNull(i * 2 + 1))
				break;
			else {
				Station s = new Station(c.getString(i * 2 + 1),
						c.getString(i * 2));
				if (s1 == null)
					s1 = s;
				else if (s2 == null)
					s2 = s;
				else
					s3 = s;
			}
		}
		if (s3 == null) {
			result = new TripPlan(s1, s2);
		} else {
			result = new TripPlanVia(s1, s2, s3);
		}
		if (result != null)
			result.userColor = c.getInt(c.getColumnIndex("UserColor"));
		result.hits = hits;
		return result;
	}

	public static TripPlan fromResultBundle(Bundle b) {
		if (b.getInt("Count") == 3) {
			return new TripPlanVia(b);
		} else {
			return new TripPlan(b);
		}
	}

	public TripPlan(Bundle b) {
		stationFrom = new Station(b, 1);
		stationTo = new Station(b, 2);
	}

	public static TripPlan createFromBundle(Bundle b) {
		if (b.containsKey("stationCode3"))
			return new TripPlanVia(b);
		else
			return new TripPlan(b);
	}

	public boolean hasCodes() {
		return stationFrom.hasCode() && stationTo.hasCode();
	}

	public String getDBCacheClause() {
		return String
				.format("stationFrom = '%1$s' AND stationTo = '%2$s' AND stationVia IS NULL",
						stationFrom.code, stationTo.code);
	}

	public String getDBClause() {
		return String.format(
				"S1Code = '%1$s' AND S2Code = '%2$s' AND S3Code IS NULL",
				stationFrom.code, stationTo.code);
	}

	public String getDBInsertionString() {
		return String.format("'%s','%s','%s','%s',NULL,NULL", stationFrom.name,
				stationFrom.code, stationTo.name, stationTo.code);
	}

	public void addToContenValues(ContentValues values) {
		values.put("stationFrom", stationFrom.code);
		values.put("stationTo", stationTo.code);
		values.putNull("stationVia");
	}

	public void addToContenValues2(ContentValues values) {
		values.put("S1Code", stationFrom.code);
		values.put("S1Name", stationFrom.name);
		values.put("S2Code", stationTo.code);
		values.put("S2Name", stationTo.name);
	}

	public Bundle getBundle() {
		Bundle bu = new Bundle();
		stationFrom.addToBundle(bu, 1);
		stationTo.addToBundle(bu, 2);
		return bu;
	}

	@Override
	public String toString() {
		return stationFrom.name + " > " + stationTo.name;
	}

	public String toNiceString() {
		return stationFrom.name + "\n  > " + stationTo.name;
	}

	@Override
	public View getView(LayoutInflater inflater, int number) {
		LinearLayout view = (LinearLayout) inflater.inflate(
				R.layout.list_item_trip, null);
		updateView(inflater, view, 1);
		return view;
	}

	@Override
	public void updateView(LayoutInflater inflater, View v, int number) {
		v.setTag(this);
		TextView tv = (TextView) v.findViewById(R.id.TextViewTrip);
		ImageView iv = (ImageView) v.findViewById(R.id.ImageView);
		int drawable = R.drawable.jog_tab_target_gray;
		switch (userColor) {
		case (1):
			drawable = R.drawable.jog_tab_target_green;
			break;
		case (2):
			drawable = R.drawable.jog_tab_target_red;
			break;
		case (3):
			drawable = R.drawable.jog_tab_target_yellow;
			break;
		}
		iv.setImageResource(drawable);
		tv.setText(this.toNiceString());
		if (special == 1) {
			((ImageView) v.findViewById(R.id.ImageView2))
					.setVisibility(View.VISIBLE);
			((TextView) v.findViewById(R.id.TVHits2)).setVisibility(View.GONE);
		} else {
			((ImageView) v.findViewById(R.id.ImageView2))
					.setVisibility(View.GONE);
			((TextView) v.findViewById(R.id.TVHits2))
					.setVisibility(View.VISIBLE);
			if (hits > 0)
				((TextView) v.findViewById(R.id.TVHits2)).setText("" + hits);
			else
				((TextView) v.findViewById(R.id.TVHits2)).setText("");
		}
	}

	public Station[] getStations() {
		return new Station[] { stationFrom, stationTo };
	}

	@Override
	public int compareTo(TripPlan another) {
		return stationFrom.compareTo(another.stationFrom);
	}

	public String getSearchTerms() {
		return "&fromStation=" + stationFrom.code + "&toStation="
				+ stationTo.code;
	}

	public String getSearchTerms2(boolean withAmp) {
		return (withAmp ? "&" : "") + "from=" + stationFrom.code + "&to="
				+ stationTo.code;
	}
}