package de.mft.interpretation;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.apache.commons.lang.WordUtils; 
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mft.similarity.GNETSimilarity;
import de.mft.similarity.WS4JSimilarity;


public class Interpretation {

	private static final String SOLR_URL = "http://localhost:8983/solr/collection1/select?q={0}&sort={1}+asc&rows=1000&fl={1}&wt=xml&indent=true";

	public static Map<String, Object> interpretQuery(String query)
			throws IOException {
		Map<String, Object> interpretation = null;
		List<String> person_names = extractEntitiesFromXML(query, "person_name_lc", permutations(query));
		if (person_names.size() > 0) {
			interpretation = new HashMap<String, Object>();
			interpretation.put("query", query);
			String lang = LangDetector.detectLang(query);
			interpretation.put("language", lang);
			query = StopWordsRemover.removeStopWords(query);
			HashSet<String> namesHS = new HashSet<String>();
			namesHS.addAll(person_names);
			person_names.clear();
			person_names.addAll(namesHS);
			interpretation.put("names", person_names);
			query = removeEntitiesFromQuery(query, person_names);
			List<String> locations = new ArrayList<String>();
			for(String str : query.split("\\s+")) locations.add(str);
			List<String> extractLocations = extractEntitiesFromXML(query, "location_name", locations);
			HashSet<String> locationHS = new HashSet<String>();
			locationHS.addAll(extractLocations);
			extractLocations.clear();
			extractLocations.addAll(locationHS);
			interpretation.put("location_found",
					extractLocations.size() > 0);
			interpretation.put("locations", extractLocations);
			interpretation.put("intention", query);
//			Map<String, Double> de_similarities = gnet.calculateSimilarityToAllClasses(query);
//			Map<String, Double> en_similarities = (new WS4JSimilarity()).calculateSimilarityToAllClasses(query);
//			interpretation.put("en_similarities", en_similarities);
//			interpretation.put("de_similarities", de_similarities);
		}
		return interpretation;
	}

	private static String removeEntitiesFromQuery(String query,
			List<String> nList) {
		String line, intention = query.toLowerCase();

		for (String str : nList) {
			line = str.toLowerCase();
			intention = removeStringFromAnOther(intention, line);
		}
		return WordUtils.capitalize(intention);
	}

	private static String removeStringFromAnOther(String src, String toBeRemoved) {
		String rs = "";
		List<String> names = Arrays.asList(toBeRemoved.split("\\s+"));
		for (String str : src.split("\\s+")) {
			if (!names.contains(str))
				rs += str + " ";
		}
		return rs.trim();
	}

	private static List<String> permutations(String query) {
		List<String> rs = new ArrayList<String>();
		String[] split = query.split("\\s+");
		for (int i = 0; i < split.length - 1; i++)
			for (int j = i + 1; j < split.length; j++) {
				rs.add(split[i].toLowerCase() + " " + split[j].toLowerCase());
				rs.add(split[j].toLowerCase() + " " + split[i].toLowerCase());
			}
		return rs;
	}

	private static List<String> extractEntitiesFromXML(String query, String label, List<String> qList)
			throws IOException {
		String solr_query = "";
		for (String str : qList)
			solr_query += label + "%3A\""
					+ URLEncoder.encode(str, "UTF-8") + "\"%20OR%20";
		solr_query = solr_query.substring(0, solr_query.length() - 8);
		String url = MessageFormat.format(SOLR_URL, solr_query, label);
		URL link = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) link
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.connect();
		List<String> entitiesList = new ArrayList<String>();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputStream in = connection.getInputStream();
			Document doc = dBuilder.parse(in);

			NodeList nList = doc.getElementsByTagName("doc");
			Node node = null, innerNode = null;
			int limitDocs = nList.getLength();
			for (int i = 0; i < limitDocs; i++) {
				node = nList.item(i);
				NodeList nList2 = node.getChildNodes();
				for (int j = 0; j < nList2.getLength(); j++) {
					innerNode = nList2.item(j);
					if (innerNode.hasAttributes())
						entitiesList.add(WordUtils.capitalize(node.getTextContent()
								.trim()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entitiesList;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws IOException, SQLException {

		String search = "Michael Jackson Chicago";
		GNETSimilarity gnet = GNETSimilarity.getInstance();
		try {
			Map<String, Object> map = interpretQuery(search);
			System.out.println(map);
		} catch (Exception e) {
			System.out.println("Exception...");
		}

		System.out.println("queries bearbeitet");
	}

}
