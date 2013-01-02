package eu.waleson.sneltrein.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import eu.waleson.sneltrein.R;
import eu.waleson.sneltrein.STApplication;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
    }
    @Override
    public void onPause() {
    	super.onPause();
		((STApplication)getApplication()).loadFromPreferences();
    	
    }
}
