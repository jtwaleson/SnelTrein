package com.waleson.sneltrein.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.waleson.sneltrein.R;
import com.waleson.sneltrein.STApplication;
import com.waleson.sneltrein.adapters.LinearLayoutAdapter;
import com.waleson.sneltrein.cornerstones.Station;
import com.waleson.sneltrein.utils.ILocationActivity;

public class StationSelector extends Activity implements OnClickListener,
		OnItemClickListener, ILocationActivity {

	STApplication app = null;
	int stationCount = 1;
	int curStation = 1;
	Bundle returnBundle;
	private boolean locationForced = false;

	// I like naming variables that do something slightly intelligent
	//   after Microsoft's intelliMouse
	ArrayList<Station> intelliStations = null;

	public static final String[] stationIds = { "gvc", "ht", "hto", "hde",
			"ahbf", "are", "asch", "aw", "atn", "ac", "eahs", "aime", "aixtgv",
			"akm", "albert", "amr", "amrn", "aml", "amri", "almb", "alm",
			"almm", "almo", "almp", "alms", "apn", "apn", "eabg", "amf",
			"amfs", "avat", "shl", "asa", "asdar", "asb", "asd", "ashd",
			"asdl", "asdm", "rai", "assp", "ass", "asdz", "ana", "atwlb",
			"andd", "atw", "apd", "apdm", "apdo", "apg", "akl", "arn", "ah",
			"ahpr", "ahp", "ahz", "asn", "ma", "avtgv", "brn", "bh", "oeynh",
			"badenz", "rbb", "bf", "brd", "bnc", "bnn", "baselb", "basels",
			"bdm", "bk", "bsd", "bl", "bgn", "gsb", "berhbl", "bhf", "bspd",
			"bet", "bv", "bielef", "bhv", "br", "bll", "bchz", "bdg", "kboi",
			"bonn", "ebok", "bn", "bsk", "bourg", "bkf", "bkg", "bmr", "btl",
			"bd", "bdpb", "bremen", "bressx", "bkl", "kbry", "brig", "fr",
			"bmn", "brusc", "brusn", "brusz", "bp", "bde", "bnk", "bsmz",
			"ebeo", "buende", "cps", "cas", "chamb", "cvm", "ecmf", "co",
			"como", "ck", "cl", "da", "dln", "dl", "ese", "dvnk", "dei", "ddn",
			"dt", "dtz", "dz", "dzw", "ht", "hto", "dld", "gvc", "gv", "laa",
			"gvm", "gvmw", "ypb", "hdr", "hdrz", "dn", "dv", "dvc", "dvt",
			"did", "dmn", "dmnz", "dr", "dtc", "dtch", "ddr", "ddrs", "ddzd",
			"edd", "edo", "edkd", "db", "drh", "drp", "duisb", "dvn", "dvd",
			"kdul", "edulh", "kdr", "dussel", "ec", "edc", "ed", "edn", "edf",
			"ehv", "ehb", "ehst", "lkr", "est", "emn", "emnz", "em", "ekz",
			"eenp", "es", "ese", "esd", "eepe", "eml", "escht", "eschn",
			"eweis", "esn", "etn", "egh", "eghm", "fn", "fffm", "franko",
			"fnaf", "rf", "frutig", "gdr", "gdk", "geisl", "gdm", "gp", "gln",
			"lut", "fgsp", "gz", "gbr", "gs", "go", "goppen", "gr", "gd",
			"gdg", "gbg", "gk", "g", "gn", "gerp", "gnn", "gw", "tgo", "mgzb",
			"hlm", "hlms", "hagen", "hamh", "hamm", "hann", "hde", "hdb", "hd",
			"bzm", "gnd", "hrn", "hlg", "hlgh", "hk", "had", "hr", "hry",
			"hwd", "hrl", "hrlk", "hrlw", "hze", "mid", "hlo", "hno", "hm",
			"hmh", "hmbv", "hmbh", "hmn", "hgl", "fkb", "hglo", "ht", "hto",
			"hz", "hil", "hvs", "hvsn", "hvsp", "hnp", "hld", "hlds", "hb",
			"hor", "hon", "holt", "ehzw", "hfd", "hgv", "hgz", "hks", "hn",
			"hnk", "hrt", "htn", "htnc", "sgl", "hdg", "ijt", "innsb",
			"jenbac", "kn", "fkth", "kpn", "kander", "bzl", "lkp", "karlsr",
			"krd", "krw", "ktr", "koln", "gkt", "kirchb", "kbk", "kmr", "kko",
			"kohl", "konin", "kbw", "kzd", "kmw", "kbd", "kma", "kw", "krg",
			"kufst", "kutno", "koln", "kkd", "zlw", "lg", "landry", "lang",
			"leer", "ldm", "lw", "lwc", "elgd", "ledn", "ldl", "lls", "elet",
			"leuven", "ltv", "flb", "lis", "luik", "lc", "lp", "ltn", "eldh",
			"elue", "mz", "mrn", "mas", "mss", "msw", "mt", "mtr", "mannhe",
			"mg", "mrb", "marne", "mars", "mth", "mech", "mes", "mp", "emte",
			"mdb", "minden", "kmb", "mmlh", "mout", "mcgb", "munchh", "munst",
			"enhf", "enbe", "ndb", "neuss", "na", "nvp", "nwk", "nsch", "nkk",
			"nm", "nmd", "nmh", "nml", "nvd", "nvdw", "enow", "ns", "nh",
			"obd", "oberh", "eop", "ro", "ot", "odz", "ost", "omn", "olv",
			"fsd", "otb", "op", "osnh", "o", "ow", "odb", "ovn", "paris",
			"paris", "tp", "pozn", "eprn", "pmr", "pmo", "pmw", "pt", "rat",
			"rvs", "rv", "rh", "rheine", "rhn", "rsn", "rsw", "rb", "rm", "rd",
			"rsd", "ehw", "rs", "rta", "rtb", "rtd", "rlb", "rtn", "rtst",
			"rtz", "rtdzpl", "rl", "rzepin", "sptn", "sptz", "spm", "ssh",
			"swd", "sgn", "sda", "sdm", "nwl", "sog", "sn", "shl", "kswe",
			"esrt", "esem", "eseb", "ksb", "spv", "std", "sdt", "sdtb", "sk",
			"sknd", "st", "stz", "sd", "sbk", "utm", "maria", "stv", "stm",
			"swk", "egrk", "ebgo", "ebft", "ls", "stola", "stolb", "stolmb",
			"stolr", "stolsm", "ts", "srn", "sm", "wta", "tg", "tbg", "thun",
			"tl", "tpsw", "tb", "tbr", "tbu", "twl", "utg", "uhz", "uhm", "tu",
			"eun", "ust", "ut", "utl", "utm", "utm", "uto", "utt", "utwr",
			"utzl", "valtgv", "vk", "vsv", "vdm", "vndc", "vndw", "klp", "vwd",
			"vp", "vl", "vry", "vlb", "viers", "fvs", "vdg", "vdo", "vdw",
			"vtn", "vs", "vss", "vdl", "vb", "vh", "vst", "vem", "vd", "vz",
			"vhp", "vg", "wad", "wadn", "wfm", "warszc", "warsaw", "wr", "wt",
			"wp", "wl", "wtv", "wz", "wienw", "wdn", "wc", "wh", "gwd", "ws",
			"wsm", "ww", "www", "wd", "wf", "hwob", "wv", "wk", "wm", "kwba",
			"wupph", "kwo", "wuppv", "worgl", "zd", "zdk", "zbm", "zvt", "za",
			"zv", "zvb", "ztm", "ztmo", "zb", "zh", "zp", "zww", "zwd", "zl",
			"zue" };
	public static final String[] stationNames = { "'s-Gravenhage",
			"'s-Hertogenbosch", "'s-Hertogenbosch Oost", "'t Harde",
			"Aachen Hbf", "Aachen Rothe Erde", "Aachen Schanz", "Aachen West",
			"Aalten", "Abcoude", "Ahaus", "Aime-la-Plagne", "Aix-en-Provence",
			"Akkrum", "Albertville", "Alkmaar", "Alkmaar Noord", "Almelo",
			"Almelo de Riet", "Almere Buiten", "Almere Centrum",
			"Almere Muziekwijk", "Almere Oostvaarders", "Almere Parkwijk",
			"Almere Strand", "Alphen a/d Rijn", "Alphen aan den Rijn",
			"Altenberge", "Amersfoort", "Amersfoort Schothorst",
			"Amersfoort Vathorst", "Amsterdam Airport", "Amsterdam Amstel",
			"Amsterdam Arena", "Amsterdam Bijlmer ArenA", "Amsterdam Centraal",
			"Amsterdam Holendrecht", "Amsterdam Lelylaan",
			"Amsterdam Muiderpoort", "Amsterdam RAI", "Amsterdam Science Park",
			"Amsterdam Sloterdijk", "Amsterdam Zuid", "Anna Paulowna",
			"Antwerpen Luchtbal", "Antwerpen Noorderdokken",
			"Antwerpen-Centraal", "Apeldoorn", "Apeldoorn De Maten",
			"Apeldoorn Osseveld", "Appingedam", "Arkel", "Arnemuiden",
			"Arnhem", "Arnhem Presikhaaf", "Arnhem Velperpoort", "Arnhem Zuid",
			"Assen", "Augsburg Hbf", "Avignon TGV", "Baarn", "Bad Bentheim",
			"Bad Oeynhausen", "Baden", "Baden-Baden", "Baflo", "Barendrecht",
			"Barneveld Centrum", "Barneveld Noord", "Basel Bad Bf",
			"Basel SBB", "Bedum", "Beek-Elsloo", "Beesd", "Beilen",
			"Bergen op Zoom", "Berlin Gesundbrunnen", "Berlin Hbf",
			"Berlin Ostbahnhof", "Berlin-Spandau", "Best", "Beverwijk",
			"Bielefeld Hbf", "Bilthoven", "Blerick", "Bloemendaal", "Bocholtz",
			"Bodegraven", "Boisheim", "Bonn Hbf", "Bork", "Borne", "Boskoop",
			"Bourg-St-Maurice", "Bovenkarspel Flora",
			"Bovenkarspel-Grootebroek", "Boxmeer", "Boxtel", "Breda",
			"Breda-Prinsenbeek", "Bremen Hbf", "Bressoux", "Breukelen",
			"Breyell", "Brig", "Brugge", "Brummen", "Brussel-Centraal",
			"Brussel-Noord", "Brussel-Zuid/Midi", "Buitenpost", "Bunde",
			"Bunnik", "Bussum Zuid", "Bonen", "Bunde (Westfalen)",
			"Capelle Schollevaar", "Castricum", "Chambery", "Chevremont",
			"Coesfeld (Westf)", "Coevorden", "Como", "Cuijk", "Culemborg",
			"Daarlerveen", "Dalen", "Dalfsen", "De Eschmarke", "De Vink",
			"Deinum", "Delden", "Delft", "Delft Zuid", "Delfzijl",
			"Delfzijl West", "Den Bosch", "Den Bosch Oost", "Den Dolder",
			"Den Haag Centraal", "Den Haag HS", "Den Haag Laan v NOI",
			"Den Haag Mariahoeve", "Den Haag Moerwijk", "Den Haag Ypenburg",
			"Den Helder", "Den Helder Zuid", "Deurne", "Deventer",
			"Deventer Colmschate", "Deventer De Scheg", "Didam", "Diemen",
			"Diemen Zuid", "Dieren", "Doetinchem", "Doetinchem De Huet",
			"Dordrecht", "Dordrecht Stadspolders", "Dordrecht Zuid",
			"Dortmund Derne", "Dortmund Hbf", "Dortmund Kirchderne",
			"Driebergen-Zeist", "Driehuis", "Dronrijp", "Duisburg Hbf",
			"Duiven", "Duivendrecht", "Dulken", "Dulmen", "Duren",
			"Dusseldorf Hbf", "Echt", "Ede Centrum", "Ede-Wageningen",
			"Eijsden", "Eilendorf", "Eindhoven", "Eindhoven Beukenlaan",
			"Eindhoven Stadion", "Ekeren", "Elst", "Emmen", "Emmen Zuid",
			"Emmerich", "Enkhuizen", "Ennepetal (Gevelsberg)", "Enschede",
			"Enschede De Eschmarke", "Enschede Drienerlo", "Epe (Westf)",
			"Ermelo", "Eschweiler Talbahnhof", "Eschweiler-Nothberg",
			"Eschweiler-Weisweiler", "Essen (Belgie)", "Etten-Leur",
			"Eygelshoven", "Eygelshoven Markt", "Franeker",
			"Frankfurt (M) Hbf", "Frankfurt (Oder)",
			"Frankfurt Flughafen Fernb", "Freiburg (Breisgau) Hbf", "Frutigen",
			"Gaanderen", "Geerdijk", "Geislingen", "Geldermalsen", "Geldrop",
			"Geleen Oost", "Geleen-Lutterade", "Gent Sint Pieters",
			"Gilze-Rijen", "Glanerbrug", "Goes", "Goor", "Goppenstein",
			"Gorinchem", "Gouda", "Gouda Goverwelle", "Gramsbergen",
			"Grijpskerk", "Gronau Westf", "Groningen", "Groningen Europapark",
			"Groningen Noord", "Grou-Jirnsum", "Goppingen", "Gunzburg",
			"Haarlem", "Haarlem Spaarnwoude", "Hagen Hbf", "Hamburg Hbf",
			"Hamm (Westf.)", "Hannover Hbf", "Harde ('t)", "Hardenberg",
			"Harderwijk", "Hardinxveld Blauwe Zoom", "Hardinxveld-Giessendam",
			"Haren", "Harlingen", "Harlingen Haven", "Heemskerk",
			"Heemstede-Aerdenhout", "Heerenveen", "Heerenveen IJsstadion",
			"Heerhugowaard", "Heerlen", "Heerlen De Kissel",
			"Heerlen Woonboulevard", "Heeze", "Heide", "Heiloo", "Heino",
			"Helmond", "Helmond 't Hout", "Helmond Brandevoort",
			"Helmond Brouwhuis", "Hemmen-Dodewaard", "Hengelo",
			"Hengelo FBK stadion", "Hengelo Oost", "Hertogenbosch ('s)",
			"Hertogenbosch Oost ('s)", "Herzogenrath", "Hillegom", "Hilversum",
			"Hilversum Noord", "Hilversum Sportpark", "Hindeloopen",
			"Hoek van Holland Haven", "Hoek van Holland Strand", "Hoensbroek",
			"Hollandsche Rading", "Holten", "Holtwick", "Holzwickede",
			"Hoofddorp", "Hoogeveen", "Hoogezand-Sappemeer", "Hoogkarspel",
			"Hoorn", "Hoorn Kersenboogerd", "Horst-Sevenum", "Houten",
			"Houten Castellum", "Houthem-St Gerlach", "Hurdegaryp", "IJlst",
			"Innsbruck Hbf", "Jenbach", "Kaldenkirchen", "Kalmthout", "Kampen",
			"Kandersteg", "Kapelle-Biezelinge", "Kapellen", "Karlsruhe Hbf",
			"Kerkrade Centrum", "Kerkrade West", "Kesteren", "Keulen",
			"Kijkuit", "Kirchberg in Tirol", "Klarenbeek", "Klimmen-Ransdaal",
			"Koblenz Hbf", "Kohlscheid", "Konin", "Koog Bloemwijk",
			"Koog-Zaandijk", "Koudum-Molkwerum", "Krabbendijke",
			"Krommenie-Assendelft", "Kropswolde", "Kruiningen-Yerseke",
			"Kufstein", "Kutno", "Koln Hbf", "Koln-Deutz", "Lage Zwaluwe",
			"Landgraaf", "Landry", "Langerwehe", "Leer (Ostfriesland)",
			"Leerdam", "Leeuwarden", "Leeuwarden Camminghaburen", "Legden",
			"Leiden Centraal", "Leiden Lammenschans", "Lelystad Centrum",
			"Lette (Kr Coersfeld)", "Leuven", "Lichtenvoorde-Groenlo",
			"Limburg Sud", "Lisse", "Liege-Guillemins", "Lochem", "Loppersum",
			"Lunteren", "Ludinghausen", "Lunen Hbf", "Maarheeze", "Maarn",
			"Maarssen", "Maassluis", "Maassluis West", "Maastricht",
			"Maastricht Randwyck", "Mannheim Hbf", "Mantgum", "Marienberg",
			"Marne-la-Vallee-Chessy", "Marseille-St-Charles", "Martenshoek",
			"Mechelen", "Meerssen", "Meppel", "Metelen land", "Middelburg",
			"Minden (Westf.)", "Montabaur", "Mook Molenhoek",
			"Moutiers-Salins-Brides", "Monchengladbach Hbf", "Munchen Hbf",
			"Munster (Westf) Hbf", "Munster Zentrum Nord", "Munster-Hager",
			"Naarden-Bussum", "Neuss Hbf", "Nieuw Amsterdam", "Nieuw Vennep",
			"Nieuwerkerk a/d IJssel", "Nieuweschans", "Nijkerk", "Nijmegen",
			"Nijmegen Dukenburg", "Nijmegen Heyendaal", "Nijmegen Lent",
			"Nijverdal", "Nijverdal West", "Nordwalde", "Nunspeet", "Nuth",
			"Obdam", "Oberhausen Hbf", "Ochtrup", "Offenburg", "Oisterwijk",
			"Oldenzaal", "Olst", "Ommen", "Onze Lieve Vrouwe t. Nood",
			"Oostende", "Oosterbeek", "Opheusden", "Osnabruck Hbf", "Oss",
			"Oss West", "Oudenbosch", "Overveen", "Parijs", "Paris-Nord",
			"Plochingen", "Poznan Gl", "Preussen", "Purmerend",
			"Purmerend Overwhere", "Purmerend Weidevenne", "Putten", "Raalte",
			"Ravenstein", "Reuver", "Rheden", "Rheine", "Rhenen", "Rijssen",
			"Rijswijk", "Rilland-Bath", "Roermond", "Roodeschool",
			"Roosendaal", "Rosendahl-Holtwick", "Rosmalen",
			"Rotterdam Alexander", "Rotterdam Blaak", "Rotterdam Centraal",
			"Rotterdam Lombardijen", "Rotterdam Noord", "Rotterdam Stadion",
			"Rotterdam Zuid", "Rotterdam Zuidplein", "Ruurlo", "Rzepin",
			"Santpoort Noord", "Santpoort Zuid", "Sappemeer Oost",
			"Sassenheim", "Sauwerd", "Schagen", "Scheemda", "Schiedam Centrum",
			"Schiedam Nieuwland", "Schin op Geul", "Schinnen", "Schiphol",
			"Schwelm", "Schwerte (Ruhr)", "Selm", "Selm-Beifang",
			"Siegburg/Bonn", "Simpelveld", "Sittard", "Sliedrecht",
			"Sliedrecht Baanhoek", "Sneek", "Sneek Noord", "Soest",
			"Soest Zuid", "Soestdijk", "Spaubeek", "Spoorwegmuseum",
			"St Mariaburg", "Stavoren", "Stedum", "Steenwijk",
			"Steinfurt Grottenkamp", "Steinfurt-Borghorst",
			"Steinfurt-Burgsteinfurt", "Stendal", "Stolberg Altstadt",
			"Stolberg Hbf", "Stolberg Muhlener Bahnhof", "Stolberg Rathaus",
			"Stolberg Schneidmuhle", "Stuttgart Hbf", "Susteren", "Swalmen",
			"Tantow", "Tegelen", "Terborg", "Thun", "Tiel", "Tiel Passewaaij",
			"Tilburg", "Tilburg Reeshof", "Tilburg Universiteit", "Twello",
			"Uitgeest", "Uithuizen", "Uithuizermeeden", "Ulm Hbf", "Unna",
			"Usquert", "Utrecht Centraal", "Utrecht Lunetten",
			"Utrecht Maliebaan", "Utrecht Maliebaan", "Utrecht Overvecht",
			"Utrecht Terwijde", "Utrecht Westraven", "Utrecht Zuilen",
			"Valence TGV", "Valkenburg", "Varsseveld", "Veendam",
			"Veenendaal Centrum", "Veenendaal West", "Veenendaal-De Klomp",
			"Veenwouden", "Velp", "Venlo", "Venray", "Vierlingsbeek",
			"Viersen", "Vise", "Vlaardingen Centrum", "Vlaardingen Oost",
			"Vlaardingen West", "Vleuten", "Vlissingen", "Vlissingen Souburg",
			"Voerendaal", "Voorburg", "Voorhout", "Voorschoten", "Voorst-Empe",
			"Vorden", "Vriezenveen", "Vroomshoop", "Vught", "Waddinxveen",
			"Waddinxveen Noord", "Warffum", "Warszawa Centralna",
			"Warszawa Wschodnia", "Weener", "Weert", "Weesp", "Wehl",
			"Westervoort", "Wezep", "Wien Westbahnhof", "Wierden", "Wijchen",
			"Wijhe", "Wildert", "Winschoten", "Winsum", "Winterswijk",
			"Winterswijk West", "Woerden", "Wolfheze", "Wolfsburg", "Wolvega",
			"Workum", "Wormerveer", "Wuppertal Barmen", "Wuppertal Hbf",
			"Wuppertal Oberbarmen", "Wuppertal-Vohwinkel", "Worgl", "Zaandam",
			"Zaandam Kogerveld", "Zaltbommel", "Zandvoort aan Zee",
			"Zetten-Andelst", "Zevenaar", "Zevenbergen", "Zoetermeer",
			"Zoetermeer Oost", "Zuidbroek", "Zuidhorn", "Zutphen",
			"Zwaagwesteinde", "Zwijndrecht", "Zwolle", "Zurich HB" };
	LinearLayoutAdapter<Station> lla;

	private class StationID {
		int id;

		public StationID(int id) {
			this.id = id;
		}

		public String toString() {
			return stationNames[id];
		}

		public Station getStation() {
			return new Station(stationIds[id], stationNames[id]);
		}
	}

	private ArrayList<StationID> stations;
	private LayoutInflater inflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_selector);

		stations = new ArrayList<StationID>();
		for (int i = 0; i < stationNames.length; i++) {
			stations.add(new StationID(i));
		}
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		app = (STApplication) getApplication();
		stationCount = getIntent().getExtras().getInt("StationCount");
		returnBundle = new Bundle();

		lla = new LinearLayoutAdapter<Station>(
				(LinearLayout) findViewById(R.id.LinearLayoutStations), this);
		lla.refresh(app.myStationsAndNearStations, inflater);
		if (lla.getCount() > 4)
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		prepareAutoComplete();
		updateTexts();
	}

	public void prepareAutoComplete() {
		ArrayAdapter<StationID> adapter = new ArrayAdapter<StationID>(this,
				R.layout.autocomplete_item_station, stations);
		AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextViewStationSelector);
		actv.setThreshold(1);
		actv.setAdapter(adapter);
		actv.setOnItemClickListener(this);
	}

	public void AddStation(Station s) {
		((ScrollView) findViewById(R.id.ScrollView)).scrollTo(0, 0);

		s.addToBundle(returnBundle, curStation);
		Toast.makeText(this,
				getString(R.string.station_selected) + ": " + s.name,
				Toast.LENGTH_SHORT).show();
		curStation++;
		if (curStation > stationCount) {
			sendAndFinish();
		} else {
			if (curStation == 2) {
				intelliStations = app.database.getDestinationStations(s);
			} else {
				intelliStations = null;
			}
			updateTexts();
		}
	}

	public void sendAndFinish() {
		Intent returnIntent = new Intent();
		returnBundle.putInt("Count", stationCount);
		returnIntent.putExtras(returnBundle);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	private void updateTexts() {
		AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextViewStationSelector);
		actv.setText("");
		TextView tv = (TextView) findViewById(R.id.TextViewStationSelectorStationName);
		if (stationCount == 1)
			tv.setText(R.string.station_dots);
		else {
			if (curStation == 1)
				tv.setText(R.string.from_station);
			else if (curStation == 2)
				tv.setText(R.string.to_station);
			else
				tv.setText(R.string.via_station);
		}
		actv.requestFocus();
		refreshStations();
	}

	public void onClick(View v) {
		AddStation((Station) v.getTag());
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AddStation(((StationID) parent.getItemAtPosition(position))
				.getStation());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			curStation--;
			if (curStation == 1)
				intelliStations = null;
			if (curStation > 0) {
				updateTexts();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void refreshStations() {
		if (intelliStations != null) {
			ArrayList<Station> st = new ArrayList<Station>();
			ArrayList<String> codes = new ArrayList<String>();
			st.addAll(intelliStations);
			for (Station s : intelliStations)
				codes.add(s.code);
			for (Station s : app.myStations)
				if (!codes.contains(s.code))
					st.add(s);
			lla.refresh(st, inflater);
		} else {
			lla.refresh((curStation == 1 ? app.myStationsAndNearStations
					: app.myStations), inflater);
		}
	}

	public void processLocation() {
		refreshStations();
		if (locationForced) {
			Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT)
					.show();
			locationForced = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		app.locationActivity = this;
	}

	@Override
	public void onPause() {
		super.onPause();
		app.locationActivity = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_location, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh_position) {
			locationForced = true;
			app.getNewFix();
			return true;
		}
		return false;
	}

}
