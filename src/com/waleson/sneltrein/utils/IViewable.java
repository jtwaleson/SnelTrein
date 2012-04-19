package com.waleson.sneltrein.utils;

import android.view.LayoutInflater;
import android.view.View;

public interface IViewable {
	public View getView(LayoutInflater inflater, int number);
	public void updateView(LayoutInflater inflater, View view, int number);
}
