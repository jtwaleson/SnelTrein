package com.waleson.sneltrein.cornerstones;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.waleson.sneltrein.R;
import com.waleson.sneltrein.utils.IViewable;

public class Disruption implements IViewable {
	public Station station;

	public int expanded = 0;

	public boolean planned;
	public String id = "", traject = "", periode = "", reden = "", advies = "",
			bericht = "";
	public CDate datum = null;

	public Disruption(Station station, boolean planned) {
		this.station = station;
		this.planned = planned;
	}

	public View getView(LayoutInflater inflater, int number) {
		View view = inflater.inflate(R.layout.list_item_disruption, null);
		updateView(inflater, view, 1);
		return view;
	}

	public void updateView(LayoutInflater inflater, View view, int number) {
		// s += bericht;
		((TextView) view.findViewById(R.id.TVTitle)).setText(traject);
		if (datum != null) {
			((TextView) view.findViewById(R.id.TVDescription1))
					.setText("DATUM: " + datum.getDayMonth() + "  "
							+ datum.getHourMinute());
		} else if (periode.length() > 0) {
			((TextView) view.findViewById(R.id.TVDescription1))
					.setText("PERIODE: " + periode);
		}
		((TextView) view.findViewById(R.id.TVDescription2)).setText("OORZAAK: "
				+ reden);

		((TextView) view.findViewById(R.id.TVDescription3)).setText("ADVIES: "
				+ advies);
		if (expanded == 0) {
			((ImageView) view.findViewById(R.id.IVExpand))
					.setImageResource(R.drawable.arrow_down_float);
			((TextView) view.findViewById(R.id.TVDescription3))
					.setVisibility(View.GONE);
		} else {
			((ImageView) view.findViewById(R.id.IVExpand))
					.setImageResource(R.drawable.arrow_up_float);
			((TextView) view.findViewById(R.id.TVDescription3))
					.setVisibility(View.VISIBLE);
		}
		view.setTag(this);
	}

	public void toggle() {
		expanded = (expanded + 1) % 2;
	}
}
