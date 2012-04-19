package com.waleson.sneltrein.parsers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.waleson.sneltrein.cornerstones.CDate;
import com.waleson.sneltrein.cornerstones.TripPlanWithDeparture;
import com.waleson.sneltrein.utils.Database;

public class JourneyParser extends DefaultHandler {

	private boolean in_ReisMogelijkheid = false;
	private boolean in_ReisDeel = false;
	private boolean in_ReisStop = false;
	private boolean in_Melding = false;
	private boolean checkChars = false;

	private String currentNode;
	private String currentString;

	// Journey values
	long journeyId = -1;
	ContentValues journeyContentValues;

	// TrainTravel values
	long traintravelID = -1;
	ContentValues traintravelContentValues;

	// TrainStop values
	ContentValues trainstopContentValues;

	long fetchedOn = 0;
	public TripPlanWithDeparture lastTPWD;
	private SQLiteDatabase db;
	private Database appdb;
	String melding;

	public JourneyParser(Database db) {
		this.db = db.sampleDB;
		this.appdb = db;
	}

	String firstDepTime;
	String lastDepTime;

	@Override
	public void startDocument() {
		fetchedOn = CDate.now().getTimeinMillis();
		firstDepTime = null;
		lastDepTime = null;
		// result.clear();
	}

	@Override
	public void endDocument() {
		appdb.deleteJourneys("SELECT id FROM Journeys WHERE "
				+ lastTPWD.orig.getDBCacheClause() + " AND deptime >= '"
				+ firstDepTime + "' AND deptime <= '" + lastDepTime
				+ "' AND fetchedon < '" + fetchedOn + "'");
	}

	@Override
	public void startElement(String namespacesURI, String localName,
			String qName, Attributes atts) throws SAXException {
		currentNode = localName;

		checkChars = false;
		if (localName.equals("ReisMogelijkheid") && !in_ReisMogelijkheid) {
			journeyId = -1;
			journeyContentValues = new ContentValues();
			if (this.lastTPWD != null)
				lastTPWD.addToContentValues(journeyContentValues);
			journeyContentValues.put("fetchedon", fetchedOn);
			journeyContentValues.put(
					"id",
					Long.parseLong(fetchedOn + ""
							+ ((long) (Math.random() * 100000))));
			melding = "";
			in_ReisMogelijkheid = true;
		} else if (localName.equals("Melding") && !in_Melding) {
			in_Melding = true;
		} else if (localName.equals("ReisDeel") && !in_ReisDeel) {
			traintravelID = -1;

			journeyContentValues.put("extrainfo", melding);
			if (journeyId == -1)
				journeyId = db.insert("Journeys", null, journeyContentValues);

			traintravelContentValues = new ContentValues();
			traintravelContentValues.put("ritnummer", "");
			traintravelContentValues.put("delay", "");
			traintravelContentValues.put("journeyid", journeyId);

			in_ReisDeel = true;
		} else if (localName.equals("ReisStop") && !in_ReisStop) {
			if (traintravelID == -1)
				traintravelID = db.insert("TrainTravels", null, traintravelContentValues);

			trainstopContentValues = new ContentValues();
			trainstopContentValues.put("journeyid", journeyId);
			trainstopContentValues.put("trainstopid", traintravelID);
			trainstopContentValues.put("StationCode", "");
			trainstopContentValues.put("TrackChange", false);

			in_ReisStop = true;
		} else
			checkChars = true;

		currentString = "";
	}

	@Override
	public void endElement(String namespaceURL, String localName, String qName)
			throws SAXException {
		currentNode = localName;
		finalString();

		if (localName.equals("ReisMogelijkheid") && in_ReisMogelijkheid) {
			in_ReisMogelijkheid = false;
		} else if (localName.equals("Melding") && in_Melding) {
			in_Melding = false;
		} else if (localName.equals("ReisDeel") && in_ReisDeel) {
			in_ReisDeel = false;
		} else if (localName.equals("ReisStop") && in_ReisStop) {
			in_ReisStop = false;

			db.insert("TrainStops", null, trainstopContentValues);
		}
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (!checkChars)
			return;

		String value = new String(ch, start, length);
		value = value.trim();
		currentString += value;
	}

	public void finalString() {
		if (currentString.length() == 0)
			return;

		if (in_ReisStop) {
			if (currentNode.equals("Naam"))
				trainstopContentValues.put("StationName", currentString);
			else if (currentNode.equals("Tijd")) {
				trainstopContentValues.put("Date", currentString);
			} else if (currentNode.equals("Spoor"))
				trainstopContentValues.put("Track", currentString);
			else if (currentNode.equals("VertrekVertraging"))
				trainstopContentValues.put("Delay", currentString);
		} else if (in_ReisDeel) {
			if (currentNode.equals("VervoerType"))
				traintravelContentValues.put("traintype", currentString);
		} else if (in_Melding) {
			if (currentNode.equals("Text")) {
				if (melding.length() > 0)
					melding += "\n";
				melding += currentString;
			}
		} else if (in_ReisMogelijkheid) {

			if (currentNode.equals("VertrekVertraging"))
				journeyContentValues.put("depDelay", currentString);
			else if (currentNode.equals("AankomstVertraging"))
				journeyContentValues.put("arrDelay", currentString);
			else if (currentNode.equals("GeplandeReisTijd"))
				journeyContentValues.put("plannedDuration", currentString);
			else if (currentNode.equals("ActueleReisTijd"))
				journeyContentValues.put("actualDuration", currentString);
			else if (currentNode.equals("GeplandeVertrekTijd")) {
				journeyContentValues.put("deptime", currentString);
				if (firstDepTime == null)
					firstDepTime = new String(currentString);
				lastDepTime = new String(currentString);
			} else if (currentNode.equals("GeplandeAankomstTijd"))
				journeyContentValues.put("arrtime", currentString);
			else if (currentNode.equals("AantalOverstappen"))
				journeyContentValues.put("transfers", currentString);
		}
	}

}
