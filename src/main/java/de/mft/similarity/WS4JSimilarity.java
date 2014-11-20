package de.mft.similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.sf.snowball.ext.EnglishStemmer;

import org.eml.sir.util.Stemmer;

public class WS4JSimilarity {
	
	private static List<ClassStructure> CLASSES = new ArrayList<ClassStructure>();
	
	private ClassStructure music_ressourcen, human_body, news_information, land_location, family_privacy, sport_carrer;
		
	public WS4JSimilarity() {
		initClasses();
	}
	
	public static void main( String[] args )
    {
		String query = "city angels";
		WS4JSimilarity ws4 = new WS4JSimilarity();
		System.out.println(ws4.calculateSimilarityToAllClasses(query));
    }
	
	private void initClasses() {
		
		List<String> music_class = new ArrayList<String>();
		music_class.add("music");
		music_class.add("song");
		music_class.add("art");
		music_class.add("lyric");
		music_class.add("picture");
		music_class.add("film");
		music_class.add("instrument");
		music_class.add("band");
		music_ressourcen = new ClassStructure("MUSIK/RESSOURCEN", music_class, music_class.size());
		
		List<String> carrer_class = new ArrayList<String>();
		carrer_class.add("work");
		carrer_class.add("sport");
		carrer_class.add("ball");
		carrer_class.add("player");
		carrer_class.add("game");
		carrer_class.add("job");
		carrer_class.add("career");
		sport_carrer = new ClassStructure("SPORT/KARRIERE", carrer_class, carrer_class.size());
		
		List<String> body_class = new ArrayList<String>();
		body_class.add("body");
		body_class.add("hand");
		body_class.add("foot");
		body_class.add("skin");
		body_class.add("weight");
		body_class.add("height");
		human_body = new ClassStructure("MENSCH/KÖRPER", body_class, body_class.size());
		
		List<String> news_class = new ArrayList<String>();
		news_class.add("information");
		news_class.add("tv");
		news_class.add("news");
		news_class.add("article");
		news_class.add("report");
		news_class.add("society");
		news_information = new ClassStructure("NACHRICHTEN/INFORMATION", news_class, news_class.size());
		
		List<String> location_class = new ArrayList<String>();
		location_class.add("location");
		location_class.add("country");
		location_class.add("continent");
		location_class.add("city");
		location_class.add("village");
		location_class.add("town");
		location_class.add("home");
		location_class.add("house");
		location_class.add("address");
		land_location = new ClassStructure("LAND/ORT", location_class, location_class.size());
		
		List<String> family_class = new ArrayList<String>();
		family_class.add("family");
		family_class.add("father");
		family_class.add("mother");
		family_class.add("son");
		family_class.add("friend");
		family_class.add("daughter");
		family_class.add("personality");
		family_class.add("life");
		family_privacy = new ClassStructure("FAMILIE/PRIVATSPHÄRE", family_class, family_class.size());
		
		CLASSES.add(music_ressourcen);
		CLASSES.add(human_body);
		CLASSES.add(news_information);
		CLASSES.add(land_location);
		CLASSES.add(family_privacy);
		CLASSES.add(sport_carrer);
	}

	public String autoQueryClassification(String query) {
		Map<String, Double> map = calculateSimilarityToAllClasses(query);
		MapValueComparator bvc =  new MapValueComparator(map);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
        sorted_map.putAll(map);
        return sorted_map.firstKey();
	}
	
	public Map<String, Double> calculateSimilarityToAllClasses(String query) {
		Map<String, Double> querySimilarityMap = new HashMap<String, Double>();
		
		for (ClassStructure klasse : CLASSES) {
			querySimilarityMap.put(klasse.name, calculateQuerySimilarityToClass(query, klasse));
		}
//		querySimilarityMap.put("NAVIGATIONAL/SOCIAL", countNavigationalSitesOfQuery(query));
		return querySimilarityMap;
	}
	
	private double calculateQuerySimilarityToClass(String query, ClassStructure klasse) {
		String arr[] = query.split("\\s+");
		double value = 0.0;
		for(String str : arr) {
			value += calculateWordSimilarityToClass(str, klasse);
		}
		return Math.rint(((value/klasse.length)/arr.length)*10000)/10000;
	}
	
	private double calculateWordSimilarityToClass(String word, ClassStructure klasse) {
		if (word.endsWith("s")) {
			EnglishStemmer stemmer = new EnglishStemmer();
			stemmer.setCurrent(word);
			if (stemmer.stem()) word = stemmer.getCurrent();
		}
		double value = 0.0;
		for(String str : klasse.list) {
			value += WS4JCalculator.runLCH(word.toLowerCase(), str);
		}
		return value;
	}
	
	public static double countNavigationalSitesOfQuery(String query) {
		String[] arr = query.split("\\s+");
		int score, maxScore = -1;
		for (String str : arr) {
			score = countWordInNavigationalSites(str.toLowerCase());
			if (score > maxScore) maxScore = score;
		}
		return (double) maxScore/10;
	}
	
	private static int countWordInNavigationalSites(String word) {
		word = word.toLowerCase();
		if (word.length() > 3) {
			List<String> list = new ArrayList<String>();
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(
						"nlp/social_media.txt")));
				String line;
				while ((line = br.readLine()) != null) {
					if (line.contains(word))
						list.add(line);
				}
				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return list.size();			
		}
		return 0;
	}

}

class ClassStructure {
	public String name;
    public List<String> list;
    public int length;
    public ClassStructure(String name, List<String> list, int length) {
    	this.name = name;
    	this.list = list;
    	this.length = length;
    }
}

class MapValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public MapValueComparator(Map<String, Double> base) {
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

