package eu.waleson.sneltrein.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.waleson.sneltrein.cornerstones.CDate;
import eu.waleson.sneltrein.cornerstones.Disruption;
import eu.waleson.sneltrein.cornerstones.Station;


public class DisruptionsParser extends DefaultHandler {

	private boolean in_storing = false;
	private boolean in_planned = false;
	private boolean checkChars = false;

	private String currentNode;
	private String currentString = "";

	private Station depStation = null;

	public DisruptionsParser(Station s) {
		depStation = s;
	}

	public ArrayList<Disruption> result_planned = new ArrayList<Disruption>();
	public ArrayList<Disruption> result_unplanned = new ArrayList<Disruption>();
	Disruption currentDisruption = null;

	@Override
	public void startElement(String namespacesURI, String localName,
			String qName, Attributes atts) throws SAXException {
		currentString = "";
		currentNode = localName;

		checkChars = false;
		if (localName.equals("Ongepland")) {
			in_planned = false;
		} else if (localName.equals("Gepland")) {
			in_planned = true;
		} else if (localName.equals("Storing") && !in_storing) {
			currentDisruption = new Disruption(depStation, in_planned);
			in_storing = true;
		} else
			checkChars = true;
	}

	@Override
	public void endElement(String namespaceURL, String localName, String qName)
			throws SAXException {
		currentNode = localName;
		finalString();
		if (localName.equals("Storing") && in_storing) {
			in_storing = false;
			if (currentDisruption != null) {
				if (in_planned)
					result_planned.add(currentDisruption);
				else
					result_unplanned.add(currentDisruption);
			}
			currentDisruption = null;
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

		if (in_storing) {
			if (currentNode.equals("id"))
				currentDisruption.id = currentString;
			else if (currentNode.equals("Traject")) {
				currentDisruption.traject = currentString;
			} else if (currentNode.equals("Periode"))
				currentDisruption.periode = currentString;
			else if (currentNode.equals("Reden"))
				currentDisruption.reden = currentString;
			else if (currentNode.equals("Advies"))
				currentDisruption.advies = currentString;
			else if (currentNode.equals("Bericht"))
				currentDisruption.bericht = currentString;
			else if (currentNode.equals("Datum"))
				currentDisruption.datum = CDate.parseNSTime(currentString);
		}
	}
}