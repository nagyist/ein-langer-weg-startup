package de.mft.interpretation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;




import org.apache.commons.lang.StringUtils;

import de.mft.similarity.WS4JSimilarity;

import edu.cmu.lti.ws4j.WS4J;

public class LangDetector {

	public static void main(String[] args) throws SQLException {
		DBController dbc = new DBController();
		ResultSet rs = dbc.executeQuery("SELECT intention,lang from labled_datensatz_1 WHERE 1 LIMIT 100");
		String query;
		double lang;
	}

	private static int levenshteinDistance(String str_1, String str_2) {
		return StringUtils.getLevenshteinDistance(str_1.toLowerCase(),
				str_2.toLowerCase());
	}

	private static Map<String, Integer> levenshteinNeighbours(String token,
			String lang) {
		Map<String, Integer> list = new HashMap<String, Integer>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					"nlp/" + lang + ".txt")));
			String line;
			int i;
			while ((line = br.readLine()) != null) {
				i = levenshteinDistance(token, line);
				if (i <= 2)
					list.put(line, i);
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}
	private static double levenshteinScoreOfQuery(String query, String lang) {
		List<Integer> scores = new ArrayList<Integer>();
		String[] arr = query.split("\\s+");
		double value = 0.0, k = 1.0;
		for (String token : arr) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(
						"nlp/" + lang + ".txt")));
				String line;
				int i;
				while ((line = br.readLine()) != null) {
					i = levenshteinDistance(token, line);
					if (i < 2) {
						int temp = (i == 0) ? 100 : 5; 
						scores.add(temp);
						value += Double.valueOf(temp);
						k++;
					}
				}
				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		value = value / k;
		return value;
	}
	
	public static String detectLanguage(String query) {
		double score_de = levenshteinScoreOfQuery(query, "german");
		double score_en = levenshteinScoreOfQuery(query, "english");
		String rs = (score_en > score_de) ? "en" : "de";
		return rs;
	}
	
	@SuppressWarnings("unused")
	private static int levenshteinScore(Map<String, Map<String, Integer>> map) {
		int value = 0;
		for (Entry<String, Map<String, Integer>> entry : map.entrySet()) {
			Map<String, Integer> temp = entry.getValue();
			for(Entry<String, Integer> e : temp.entrySet()) value += e.getValue();
		}
		return value;
	}

	private static boolean hasLevenshteinNeighbours(String str) {
		Map<String, Integer> map = levenshteinNeighbours(str, "german");
		return map.containsValue(0);
	}

	private static HashMap<String, Boolean> levenshteinNeighbours(String query) {
		String[] arr = query.split("\\s+");
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		try {
			for (String s : arr)
				map.put(s, hasLevenshteinNeighbours(s));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	public static String detect(String query) {
		String lang = "en";
		HashMap<String, Boolean> de = levenshteinNeighbours(query);
		for (Boolean bool : de.values()) {
			if (bool)
				lang = "de";
			return lang;
		}
		return lang;
	}
	
	
	public static String detectLang(String query) {
		String lang = "de";

		String[] arr = query.split("\\s+");
		
		for (String token : arr) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(
						"nlp/english.txt")));
				String line;
				int i;
				while ((line = br.readLine()) != null) {
					i = levenshteinDistance(token, line);
					if (i == 0) return "en";
				}
				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return lang;
	}

}

