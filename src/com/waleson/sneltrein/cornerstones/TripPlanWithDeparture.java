package com.waleson.sneltrein.cornerstones;

import android.content.ContentValues;

public class TripPlanWithDeparture {

	public boolean basedOnDeparture = true;
	public CDate datetime;
	public TripPlan orig;

	public TripPlanWithDeparture(TripPlan orig, boolean basedOnDeparture,
			CDate datetime) {
		this.orig = orig;
		this.basedOnDeparture = basedOnDeparture;
		this.datetime = datetime;
	}

	public void addToContentValues(ContentValues cv) {
		orig.addToContenValues(cv);
	}
}
