package com.waleson.sneltrein.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.waleson.sneltrein.R;
import com.waleson.sneltrein.adapters.LinearLayoutAdapter;
import com.waleson.sneltrein.cornerstones.Disruption;
import com.waleson.sneltrein.cornerstones.Station;
import com.waleson.sneltrein.downloader.ASyncNSDownloader;
import com.waleson.sneltrein.parsers.DisruptionsParser;

public class Disruptions extends Activity implements OnClickListener {

	LayoutInflater inflater;
	LinearLayoutAdapter<Disruption> lla_unplanned;
	LinearLayoutAdapter<Disruption> lla_planned;
	Station station = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disruptions);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		lla_unplanned = new LinearLayoutAdapter<Disruption>(
				(LinearLayout) findViewById(R.id.LinearLayoutDisruptionsUnplanned),
				this);
		lla_planned = new LinearLayoutAdapter<Disruption>(
				(LinearLayout) findViewById(R.id.LinearLayoutDisruptionsPlanned),
				this);

		try {
			station = new Station(getIntent().getExtras());
		} catch (Exception e) {
			station = null;
		}

		if (station != null)
			setTitle(getString(R.string.disruptions) + " - " + station.name);

		ProgressDialog dialog = null;
		dialog = ProgressDialog.show(Disruptions.this, "", "", true);
		dialog.setCancelable(true);
		DisruptionsDownloader d = new DisruptionsDownloader(dialog,
				"http://webservices.ns.nl/ns-api-storingen?"
						+ (station == null ? "unplanned=true&actual=true"
								: "station=" + station.code));
		d.execute();
	}

	private class DisruptionsDownloader extends ASyncNSDownloader {
		public DisruptionsDownloader(ProgressDialog dialog, String url) {
			super(url, new DisruptionsParser(station), dialog);
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (dialog != null)
				dialog.dismiss();
			DisruptionsParser parse = (DisruptionsParser) handler;
			lla_unplanned.refresh(parse.result_unplanned, inflater);
			lla_planned.refresh(parse.result_planned, inflater);
		}
	}

	public void onClick(View v) {
		try {
			for (int i = lla_planned.getCount() - 1; i >= 0; i--) {
				try {
					View v1 = lla_planned.getView(i);
					Disruption d = (Disruption) (v1.getTag());
					if (v1 != v && d.expanded > 0) {
						d.expanded = 0;
						lla_planned.update(i, d, inflater);
					}
				} catch (Exception e) {
				}
			}
			for (int i = lla_unplanned.getCount() - 1; i >= 0; i--) {
				try {
					View v1 = lla_unplanned.getView(i);
					Disruption d = (Disruption) (v1.getTag());
					if (v1 != v && d.expanded > 0) {
						d.expanded = 0;
						lla_unplanned.update(i, d, inflater);
					}
				} catch (Exception e) {
				}
			}

			Disruption d = (Disruption) (v.getTag());
			d.toggle();
			d.updateView(inflater, v, 0);
		} catch (Exception e) {
		}
	}

}
