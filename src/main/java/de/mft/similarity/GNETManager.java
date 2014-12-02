package de.mft.similarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.sf.snowball.ext.EnglishStemmer;

import org.eml.sir.gn.GermaNetObject;
import org.eml.sir.gn.GermaNetParser;
import org.eml.sir.gn.StemmingGermaNetObject;
import org.eml.sir.gn.Synset;
import org.eml.sir.gn.WordSense;
import org.eml.sir.util.SnowballStemmer;

public class GNETManager {

	private static Map<String, List<String>> CLASSES = new HashMap<String, List<String>>();

	/**
	 * The GermaNet Object
	 */
	private static GermaNetObject gno = null;

	/**
	 * The GermaNet Parser
	 */
	private static GermaNetParser myParser = null;

	/**
	 * The Snowball Stemmer
	 */
	private static SnowballStemmer snowStemmer = null;

	/**
	 * The Stemming GermaNet Object
	 */
	private static StemmingGermaNetObject sgno = null;

	/**
	 * The instance of GermaNet Extractor
	 */
	private static GNETManager instance = null;

	/**
	 * initializes the source of GermaNet resource files and the stemming object
	 */
	private void init() {

		gno = new GermaNetObject();
		myParser = new GermaNetParser();
		snowStemmer = new SnowballStemmer();
		try {
			gno = myParser.parse("germanet/V5_UTF");
		} catch (Throwable t) {
			t.printStackTrace();
		}

		sgno = new StemmingGermaNetObject(gno, snowStemmer);
		sgno.setStemming(true);
		initClasses();
	}

	private GNETManager() {
		init();
	}
	
	
	public static GNETManager getInstance() {
		if (instance == null) {
			instance = new GNETManager();
		}
		return instance;
	}
	
	private void initClasses() {
		List<String> music = new ArrayList<String>();
		music.add("Kunst");
		music.add("Musik");
		music.add("Musiker");
		music.add("Künstler");
		music.add("Komposition");
		music.add("Kunstwerk");
		music.add("Lied");
		music.add("Gesangsstück");
		music.add("Musikstück");
		music.add("Film");
		music.add("Filmkassette");
		music.add("Medium");
		music.add("Speichermedium");
		music.add("Datenträger");
		music.add("Tonträger");
		music.add("Zugriff");
		music.add("Akteur");
		music.add("Hardware");
		music.add("Akteurin");
		music.add("Charakterbeschaffener");
		music.add("Bildträger");
		music.add("Filmvorführung");
		music.add("Regisseur");
		music.add("Filmpreis");
		music.add("Regisseurin");
		music.add("Aufnahme");
		music.add("Aufzeichnung");
		music.add("Mitschnitt");
		music.add("Computerterm");
		music.add("Griff");
		music.add("Handgriff");
		music.add("Greifen");
		music.add("Computertätigkeit");
		music.add("Druckwerk");
		music.add("Printmedium");
		music.add("Druckerzeugnis");
		music.add("aufnehmen");
		music.add("Kulturveranstaltung");
		music.add("Musikveranstaltung");
		music.add("Veranstaltung");
		music.add("Zerstreuung");
		music.add("Unterhaltung");
		music.add("kaufen");
		music.add("Schauspieler");
		music.add("Schauspielerin");

		List<String> family = new ArrayList<String>();
		family.add("altersspezifisches_Lebewesen");
		family.add("Familienangehöriger");
		family.add("Familienangehörige");
		family.add("Familienmitglied");
		family.add("Verwandter");
		family.add("Verwandte");
		family.add("Mitmensch");
		family.add("verwandter_Mensch");
		family.add("Zustand");
		family.add("institutionsspezifisch");
		family.add("Leitmotiv");
		family.add("Motiv");

		List<String> news = new ArrayList<String>();
		news.add("Mitteilung");
		news.add("Sendung");
		news.add("Information");
		news.add("Info");
		news.add("Kommunikationseinheit");
		news.add("Programm");
		news.add("Text");
		news.add("Report");
		news.add("Bericht");

		List<String> sport = new ArrayList<String>();
		sport.add("Sport");
		sport.add("Spiel");
		sport.add("Sportart");
		sport.add("Wettspiel");
		sport.add("Disziplin");
		sport.add("Match");
		sport.add("Berufstätiger");
		sport.add("Berufstätige");
		sport.add("Aktivität");

		List<String> human = new ArrayList<String>();
		human.add("Körperbereich");
		human.add("Körperstruktur");
		human.add("körperspezifisch");
		human.add("chemiespezifisch");
		human.add("Leitmotiv");
		human.add("Körperteil");
		human.add("Sinnesorgan");
		
		
		List<String> location = new ArrayList<String>();
		location.add("Territorium");
		location.add("Wohnort");
		location.add("Verwaltungsgebiet");
		location.add("Amtsgebiet");
		location.add("Verwaltungsbezirk");
		location.add("Pionier");
		location.add("Stadt");
		location.add("Staat");
		location.add("Land");
		location.add("Grundbesitz");
		location.add("Grundstück");
		location.add("Region");
		location.add("Ort");
		location.add("Aufenthaltsort");

		CLASSES.put("MUSIK/RESSOURCEN", music);
		CLASSES.put("NACHRICHTEN/INFORMATION", news);
		CLASSES.put("SPORT/KARRIERE", sport);
		CLASSES.put("KÖRPER/MENSCH", human);
		CLASSES.put("FAMILIE/PRIVATSPHÄRE", family);
		CLASSES.put("LAND/ORT", location);
	}
	
	/**
	 * obtains all hypernyms of a term queried
	 * 
	 * @param word
	 * @return the list of all hypernyms
	 */
	@SuppressWarnings("unchecked")
	private static ArrayList<String> getAllHypernyms(String word) {
		ArrayList<String> results = new ArrayList<String>();
		ArrayList<WordSense> senses = sgno.getWordSenses(word);
		if (senses != null) {
			for (int i = 0; i < senses.size(); i++) {
				WordSense ws = (WordSense) senses.get(i);
				Synset ss = ws.getSynset();
				if (ss.toHyperonymsString().length() > 0) {
					String[] tmp = ss.toHyperonymsString().split("\\s+");
					for (String h : tmp)
						results.add(h);
				}
			}
		}
		return results;
	}

	private static double calculateScoreOfClass(String word, List<String> list) {
		List<String> hypernymsList = new ArrayList<String>();
		List<String> hypernyms2DepthList;
		if (word.endsWith("s")) {
			List<String> hypers = getAllHypernyms(word);
			if (hypers.size() > 0) {
				hypernymsList.addAll(hypers);
			} else {
				hypers = getAllHypernyms(word.substring(0, word.length()-1));
				hypernymsList.addAll(hypers);
			}
		} else {
			hypernymsList.addAll(getAllHypernyms(word));
		}
		double score = 0;
		for (String hypernym : hypernymsList) {
			if (list.contains(hypernym)) score++;
			hypernyms2DepthList = getAllHypernyms(hypernym);
			for (String str : hypernyms2DepthList) {
				if (list.contains(str))	score++;
			}
		}
		return score/Double.valueOf(list.size());
	}
	
	private static Map<String, Double> calculateScoreToAllClasses(String word) {
		Map<String, Double> result = new HashMap<String, Double>();
		for(Entry<String, List<String>> entry : CLASSES.entrySet()) {
			String key = entry.getKey();
			double value = calculateScoreOfClass(word, entry.getValue());
			result.put(key, value);
		}
		return result;
	}
	private Map<String, Double> calculateScoreToAllClasses(String[] arr) {
		Map<String, Double> result = new HashMap<String, Double>();
		Map<String, Double> temp;
		double music_class = 0.0;
		double body_class = 0.0;
		double location_class = 0.0;
		double news_class = 0.0;
		double family_class = 0.0;
		double career_class = 0.0;
		for (String str : arr) {
			temp = calculateScoreToAllClasses(str);
			music_class += temp.get("MUSIK/RESSOURCEN");
			body_class += temp.get("KÖRPER/MENSCH");
			news_class += temp.get("NACHRICHTEN/INFORMATION");
			location_class += temp.get("LAND/ORT");
			family_class += temp.get("FAMILIE/PRIVATSPHÄRE");
			career_class += temp.get("SPORT/KARRIERE");
		}
		result.put("MUSIK/RESSOURCEN", music_class);
		result.put("KÖRPER/MENSCH", body_class);
		result.put("NACHRICHTEN/INFORMATION", news_class);
		result.put("LAND/ORT", location_class);
		result.put("FAMILIE/PRIVATSPHÄRE", family_class);
		result.put("SPORT/KARRIERE", career_class);
		return result;
	}
	
	public Map<String, Double> calculateSimilarityToAllClasses(String query) {
		return calculateScoreToAllClasses(query.split("\\s+"));
	}
	
	private static boolean couldClassifyString(Map<String, Double> map) {
	    int i = 0;
		for (double value : map.values()) {
			if (value == 0.0) i++;
		}
		return i != map.size();
	}

	private String calculateHypernymsClass(String query) {
		String[] arr = query.split("\\s+");
		Map<String, Double> map = calculateScoreToAllClasses(arr);
	    ValueComparator bvc =  new ValueComparator(map);
	    TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
	    sorted_map.putAll(map);
	    return sorted_map.firstKey();
	}

	public static void main(String args[]) {
		GNETManager gnet = GNETManager.getInstance();
		WS4JSimilarity ws4j;
		String word = "";
        Scanner scanner = new Scanner(System.in);
        while (!word.equals("q") && !word.equals("quit") && !word.equals("exit")) {
        	System.out.print("4> ");
        	ws4j = new WS4JSimilarity();
        	word  = scanner.next();
        	System.out.println("GNET " + gnet.calculateSimilarityToAllClasses(word));
        	System.out.println("WS4J " + ws4j.calculateSimilarityToAllClasses(word));
        	System.out.println("Hypernyms: " + gnet.getAllHypernyms(word));
        	for (String str : gnet.getAllHypernyms(word)) {
        		if (str.indexOf("_") == -1) System.out.println("HyperHypernyms: " + gnet.getAllHypernyms(str));
        	}
        }        
	}
}

class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}
