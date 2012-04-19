package com.waleson.sneltrein.adapters;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.waleson.sneltrein.utils.IViewable;

public class LinearLayoutAdapter<T extends IViewable> {

	LinearLayout layout;
	OnClickListener ocl = null;
	OnLongClickListener olcl = null;
	OnTouchListener otl = null;

	public LinearLayoutAdapter(LinearLayout ll) {
		layout = ll;
	}

	public LinearLayoutAdapter(LinearLayout ll, OnClickListener ocl) {
		layout = ll;
		this.ocl = ocl;
	}

	public LinearLayoutAdapter(LinearLayout ll, OnClickListener ocl,
			OnTouchListener otl) {
		layout = ll;
		this.ocl = ocl;
		this.otl = otl;
	}

	public LinearLayoutAdapter(LinearLayout ll, OnClickListener ocl,
			OnLongClickListener olcl) {
		layout = ll;
		this.ocl = ocl;
		this.olcl = olcl;
	}

	public LinearLayoutAdapter(LinearLayout ll, OnClickListener ocl,
			OnLongClickListener olcl, OnTouchListener otl) {
		layout = ll;
		this.ocl = ocl;
		this.olcl = olcl;
		this.otl = otl;
	}

	public int getCount() {
		return layout.getChildCount();
	}

	public void clear() {
		layout.removeAllViews();
	}

	public void update(int i, T object, LayoutInflater inflater) {
		if (layout.getChildCount() > i && i >= 0)
			object.updateView(inflater, layout.getChildAt(i), 1);
	}

	public void refresh(List<T> list, LayoutInflater inflater) {
		int b = Math.min(getCount(), list.size());

		for (int i = 0; i < b; i++) {
			list.get(i).updateView(inflater, layout.getChildAt(i), 1);
		}
		for (int i = layout.getChildCount(); i < list.size(); i++) {
			View v = list.get(i).getView(inflater, 1);
			if (ocl != null)
				v.setOnClickListener(ocl);
			if (olcl != null) {
				v.setOnLongClickListener(olcl);
			}
			if (otl != null) {
				v.setOnTouchListener(otl);
				if (v.getClass() == RelativeLayout.class) {
					RelativeLayout r = (RelativeLayout) v;
					for (int ii = 0; ii < r.getChildCount(); ii++) {
						r.getChildAt(ii).setOnTouchListener(otl);
						r.getChildAt(ii).setOnClickListener(ocl);
					}
				}
			}
			layout.addView(v);

		}
		for (int i = layout.getChildCount() - 1; i >= list.size(); i--) {
			layout.removeViewAt(i);
		}

	}

	public View getView(int i) {
		if (i >= 0 && i < layout.getChildCount()) {
			return layout.getChildAt(i);
		}
		return null;
	}

}
