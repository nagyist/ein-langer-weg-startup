package de.mft.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eml.sir.gn.GermaNetObject;
import org.eml.sir.gn.GermaNetParser;
import org.eml.sir.gn.StemmingGermaNetObject;
import org.eml.sir.gn.Synset;
import org.eml.sir.gn.WordSense;
import org.eml.sir.util.SnowballStemmer;

public class GNETSimilarity {
	private static List<ClassStructure> CLASSES = new ArrayList<ClassStructure>();

	
	/**
	 * The GermaNet Object
	 */
	private static GermaNetObject gno = null;
	
	private static final int NORMALISER = 10; 

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
	private StemmingGermaNetObject sgno = null;

	/**
	 * The instance of GermaNet Extractor
	 */
	private static GNETSimilarity instance = null;

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

	private GNETSimilarity() {
		init();
	}

	/**
	 * obtains all hyponyms of a term queried
	 * 
	 * @param word
	 * @return the list of all hyponyms
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private List<String> getAllHyponyms(String word) {
		List<String> results = new ArrayList<String>();
		List<WordSense> senses = sgno.getWordSenses(word);
		if (senses != null) {
			for (int i = 0; i < senses.size(); i++) {
				WordSense ws = (WordSense) senses.get(i);
				Synset ss = ws.getSynset();
				if (ss.toHyponymsString().length() > 0) {
					String[] tmp = ss.toHyponymsString().split(" ");
					for (String h : tmp)
						results.add(h);
				}
			}
		}
		return results;
	}

	/**
	 * obtains all hypernyms of a term queried
	 * 
	 * @param word
	 * @return the list of all hypernyms
	 */
	@SuppressWarnings("unchecked")
	private List<String> getAllHypernyms(String word) {
		List<String> results = new ArrayList<String>();
		List<WordSense> senses = sgno.getWordSenses(word);
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
	
	public static GNETSimilarity getInstance() {
		if (instance == null) {
			instance = new GNETSimilarity();
		}
		return instance;
	}
	
	private static Set<String> calculate3thDepthHypernyms(String word){
		List<String> temp = new ArrayList<String>();
		Map<String, Integer> wordHypernyms = new HashMap<String, Integer>();
		wordHypernyms.put(word, 1);
		int j = 2;
		for(int i = 0; i < 3; i++) {
			for(String str : wordHypernyms.keySet()) {
				temp.addAll(instance.getAllHypernyms(str));
			}
			for(String s : temp) {
				wordHypernyms.put(s, j);
				j++;
			}
			temp.clear();
		}
		return wordHypernyms.keySet();
	}
	
	private static double calculateWordSimilarityToClass(String word, ClassStructure struct){
		Set<String> thirdDepthHypernymsOfWord  = calculate3thDepthHypernyms(word);
		Set<String> thirdDepthHypernymsOfClass = new HashSet<String>();
		for(String str : struct.list) {
			thirdDepthHypernymsOfClass.addAll(calculate3thDepthHypernyms(str));
		}
		Set<String> intersection = new HashSet<String>(thirdDepthHypernymsOfWord); // use the copy constructor
		intersection.retainAll(thirdDepthHypernymsOfWord);
		return (double) intersection.size() / NORMALISER;
	}
	
	private static double calculateQuerySimilarityToClass(String query, ClassStructure struct){
		double result = 0.0;
		String[] arr = query.split("\\s+");
		for(String str : arr) {
			result += calculateWordSimilarityToClass(str, struct);
		}
		return (double) (result / arr.length);
	} 

	public Map<String, Double> calculateSimilarityToAllClasses(String query) {
		Map<String, Double> querySimilarityMap = new HashMap<String, Double>();
		for (ClassStructure klasse : CLASSES) {
			querySimilarityMap.put(klasse.name, calculateQuerySimilarityToClass(query, klasse));
		}
		return querySimilarityMap;
	}

		
	public static void main(String args[]) {
		String word = "Kunst";
		String word2 = "Hund";
		GNETSimilarity gnet = GNETSimilarity.getInstance();
		
		System.out.println(calculateWordSimilarityToClass(word, CLASSES.get(0)));
		System.out.println(calculateWordSimilarityToClass(word2, CLASSES.get(0)));
	}
	
	private void initClasses() {
		List<String> music = new ArrayList<String>();
		music.add("Kunst");
		music.add("Musik");
		music.add("Musiker");
		music.add("Künstler");
		music.add("Künstlerin");
		music.add("Kunstwerk");

		List<String> family = new ArrayList<String>();
		family.add("altersspezifisches_Lebewesen");
		family.add("Familienangehöriger");
		family.add("Familienangehörige");
		family.add("Familienmitglied");
		family.add("Verwandter");
		family.add("Verwandte");

		List<String> news = new ArrayList<String>();
		news.add("Mitteilung");
		news.add("Sendung");
		news.add("Information");
		news.add("Info");
		news.add("Kommunikationseinheit");
		news.add("Programm");

		List<String> sport = new ArrayList<String>();
		sport.add("Sport");
		sport.add("Spiel");
		sport.add("Sportart");
		sport.add("Wettspiel");
		sport.add("Disziplin");
		sport.add("Match");

		List<String> human = new ArrayList<String>();
		human.add("Körperbereich");
		human.add("Körperstruktur");
		human.add("körperspezifisch");
		human.add("chemiespezifisch");
		human.add("Leitmotiv");
		human.add("leben");
		
		
		List<String> location = new ArrayList<String>();
		location.add("Territorium");
		location.add("Wohnort");
		location.add("Verwaltungsgebiet");
		location.add("Amtsgebiet");
		location.add("Verwaltungsbezirk");
		location.add("Pionier");

		CLASSES.add(new ClassStructure("MUSIK/RESSOURCEN", music, music.size()));
		CLASSES.add(new ClassStructure("NACHRICHTEN/INFORMATION", news, news.size()));
		CLASSES.add(new ClassStructure("SPORT/KARRIERE", sport, sport.size()));
		CLASSES.add(new ClassStructure("KÖRPER/MENSCH", human, human.size()));
		CLASSES.add(new ClassStructure("FAMILIE/PRIVATSPHÄRE", family, family.size()));
		CLASSES.add(new ClassStructure("LAND/ORT", location, location.size()));
	}
	
}
