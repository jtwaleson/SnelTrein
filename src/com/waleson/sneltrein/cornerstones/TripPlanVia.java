package com.waleson.sneltrein.cornerstones;

import android.content.ContentValues;
import android.os.Bundle;

public class TripPlanVia extends TripPlan {
	Station stationVia;

	public TripPlanVia(Station from, Station to, Station via) {
		super(from, to);
		stationVia = via;
	}

	@Override
	public TripPlan getReverse() {
		return new TripPlanVia(stationTo, stationFrom, stationVia);
	}

	@Override
	public String getDBCacheClause() {
		return String
				.format("stationFrom = '%1$s' AND stationTo = '%2$s' AND stationVia = '%3$s'",
						stationFrom.code, stationTo.code, stationVia.code);
	}

	@Override
	public String getDBClause() {
		return String.format(
				"S1Code = '%1$s' AND S2Code = '%2$s' AND S3Code = '%3$s'",
				stationFrom.code, stationTo.code, stationVia.code);
	}

	@Override
	public boolean hasCodes() {
		return super.hasCodes() && stationVia.hasCode();
	}

	@Override
	public String getDBInsertionString() {
		return String.format("'%s','%s','%s','%s','%s','%s'", stationFrom.name,
				stationFrom.code, stationTo.name, stationTo.code,
				stationVia.name, stationVia.code);
	}

	@Override
	public void addToContenValues(ContentValues values) {
		super.addToContenValues(values);
		values.put("stationVia", stationVia.code);
	}

	@Override
	public void addToContenValues2(ContentValues values) {
		super.addToContenValues2(values);
		values.put("S3Code", stationVia.code);
		values.put("S3Name", stationVia.name);
	}

	@Override
	public Bundle getBundle() {
		Bundle bu = super.getBundle();
		stationVia.addToBundle(bu, 3);
		return bu;
	}

	public TripPlanVia(Bundle b) {
		super(b);
		stationVia = new Station(b, 3);
	}

	@Override
	public String toString() {
		return super.toString() + " (via " + stationVia.name + ")";
	}

	@Override
	public String toNiceString() {
		return super.toNiceString() + "\n   (via " + stationVia.name + ")";
	}

	@Override
	public Station[] getStations() {
		return new Station[] { stationFrom, stationTo, stationVia };
	}

	@Override
	public String getSearchTerms() {
		return super.getSearchTerms() + "&viaStation=" + stationVia.code;
	}

	@Override
	public String getSearchTerms2(boolean withAmp) {
		return super.getSearchTerms2(withAmp) + "&via=" + stationVia.code;
	}

}
