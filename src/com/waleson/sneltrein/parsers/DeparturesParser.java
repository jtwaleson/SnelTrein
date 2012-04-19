package com.waleson.sneltrein.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.waleson.sneltrein.cornerstones.CDate;
import com.waleson.sneltrein.cornerstones.Station;
import com.waleson.sneltrein.cornerstones.TrainStop;
import com.waleson.sneltrein.cornerstones.TrainTravelDeparture;

public class DeparturesParser extends DefaultHandler {

	private boolean in_VertrekkendeTrein = false;
	private boolean checkChars = false;

	private String currentNode;
	private String currentString = "";

	private Station depStation;

	public DeparturesParser(Station s) {
		depStation = s;
	}

	public ArrayList<TrainTravelDeparture> result = new ArrayList<TrainTravelDeparture>();
	TrainTravelDeparture currentTrainTravel = null;

	@Override
	public void startElement(String namespacesURI, String localName,
			String qName, Attributes atts) throws SAXException {
		currentString = "";
		currentNode = localName;

		checkChars = false;
		if (localName.equals("VertrekkendeTrein") && !in_VertrekkendeTrein) {
			currentTrainTravel = new TrainTravelDeparture(new TrainStop(),
					new TrainStop());
			currentTrainTravel.trainstopA.station = depStation;
			in_VertrekkendeTrein = true;
		} else
			checkChars = true;
	}

	@Override
	public void endElement(String namespaceURL, String localName, String qName)
			throws SAXException {
		currentNode = localName;
		finalString();
		if (localName.equals("VertrekkendeTrein") && in_VertrekkendeTrein) {
			in_VertrekkendeTrein = false;
			if (currentTrainTravel != null)
				result.add(currentTrainTravel);
			currentTrainTravel = null;
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

	private void finalString() {
		if (!checkChars || currentString.length() == 0)
			return;

		if (in_VertrekkendeTrein) {
			if (currentNode.equals("RitNummer"))
				currentTrainTravel.ritNummer = currentString;
			else if (currentNode.equals("VertrekTijd")) {
				currentTrainTravel.trainstopA.date = CDate
						.parseNSTime(currentString);
			} else if (currentNode.equals("VertrekSpoor"))
				currentTrainTravel.trainstopA.track = currentString;
			else if (currentNode.equals("EindBestemming"))
				currentTrainTravel.trainstopB.station = new Station("",
						currentString);
			else if (currentNode.equals("TreinSoort"))
				currentTrainTravel.kindOfTrain = currentString;
			else if (currentNode.equals("RouteTekst"))
				currentTrainTravel.viaStations = currentString;
			else if (currentNode.equals("VertrekVertragingTekst"))
				currentTrainTravel.delay = currentString;
		}
	}
}