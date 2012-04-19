package com.waleson.sneltrein.cornerstones;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.waleson.sneltrein.R;
import com.waleson.sneltrein.utils.IViewable;

public class Station implements IViewable, Comparable<Station> {
	public String code = "";
	public String name = "";
	public int hits = 0;
	public double distance = 0;
	public int special = 0;
	
	public Station(String aCode, String aName) {
		code = aCode;
		name = aName;
	}
	public View getView(LayoutInflater inflater, int number) {
		if (number == 1)
			return getView1(inflater);
		else if (number == 2)
			return getView2(inflater);
		return null;
			
	}
	public void print() {
		Log.v("Station","code: "+code);
		Log.v("Station","name: "+name);
	}
	public View getView1(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.list_item_station, null);
		updateView(inflater,view,1);
		return view;				
	}
	public View getView2(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.autocomplete_item_station, null);
		updateView(inflater,view,2);
		return view;				
	}
	public void updateView(LayoutInflater inflater, View view, int number) {
		((TextView)view.findViewById(R.id.TVName)).setText(name);
		if (hits > 0)
			((TextView)view.findViewById(R.id.TVHits)).setText(""+hits);
		else
			((TextView)view.findViewById(R.id.TVHits)).setText("");
		
		if (special == 1) {
			((ImageView)view.findViewById(R.id.IVSpecial)).setImageResource(R.drawable.ic_menu_mylocation);
			((ImageView)view.findViewById(R.id.IVSpecial)).setVisibility(View.VISIBLE);
			((TextView)view.findViewById(R.id.TVHits)).setText(String.format("%.1f", distance/1000)+"km");
		} else if (special == 2) {
			((ImageView)view.findViewById(R.id.IVSpecial)).setImageResource(R.drawable.btn_star_big_on);
			((ImageView)view.findViewById(R.id.IVSpecial)).setVisibility(View.VISIBLE);
		} else
			((ImageView)view.findViewById(R.id.IVSpecial)).setVisibility(View.INVISIBLE);
		view.setTag(this);
	}
	public Station(Cursor c) {
		this.name = c.getString(0);
		this.code = c.getString(1);
		this.hits = c.getInt(2);
	}
	public boolean hasCode() {
		return code.length() > 0;
	}
	public Bundle getBundle() {
		Bundle bu = new Bundle();
		bu.putString("stationCode", code);
		bu.putString("stationName", name);
		return bu;
	}
	public Station(Bundle b) {
		name = b.getString("stationName");
		code = b.getString("stationCode");
	}
	public Station(Bundle b, int i) {
		name = b.getString("stationName"+i);
		code = b.getString("stationCode"+i);
	}
	public void addToBundle(Bundle bu, int i) {
		bu.putString("stationCode"+i, code);
		bu.putString("stationName"+i, name);
	}
	@Override
	public String toString() {
		return name;
	}
	public int compareTo(Station another) {
		if (special == 1)
			return (distance > another.distance ? 1 : (distance < another.distance ? -1 : 0));
		else
			return name.compareTo(another.name);
	}
}