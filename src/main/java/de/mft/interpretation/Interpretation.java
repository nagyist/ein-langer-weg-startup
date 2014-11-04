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


public class Interpretation {

	private static final String SOLR_URL = "http://localhost:8983/solr/collection1/select?q={0}&sort={1}+asc&rows=1000&fl={1}&wt=xml&indent=true";

	public static Map<String, Object> interpretQuery(String query)
			throws IOException {
		Map<String, Object> INTERPRETATION_VALUES = new HashMap<String, Object>();
		List<String> person_names = extractAllPersonNamesFromXML(query);
		if (person_names.size() > 0) {
			INTERPRETATION_VALUES.put("query", query);
			String lang = LangDetector.detectLang(query);
			INTERPRETATION_VALUES.put("language", lang);
			query = StopWordsRemover.removeStopWords(query);
			HashSet<String> namesHS = new HashSet<String>();
			namesHS.addAll(person_names);
			person_names.clear();
			person_names.addAll(namesHS);
			INTERPRETATION_VALUES.put("names", person_names);
			query = removeEntitiesFromQuery(query, person_names);

			List<String> extractLocations = extractAllLocationNamesFromXML(query);
			HashSet<String> locationHS = new HashSet<String>();
			locationHS.addAll(extractLocations);
			extractLocations.clear();
			extractLocations.addAll(locationHS);

			INTERPRETATION_VALUES.put("location_found",
					extractLocations.size() > 0);
			INTERPRETATION_VALUES.put("locations", extractLocations);

			INTERPRETATION_VALUES.put("intention", query);
			

		}
		return INTERPRETATION_VALUES;
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

	private static List<String> extractAllPersonNamesFromXML(String query)
			throws IOException {
		String solr_query = "";
		List<String> split = permutations(query);
		for (String str : split)
			solr_query += "person_name_lc%3A\""
					+ URLEncoder.encode(str, "UTF-8") + "\"%20OR%20";
		solr_query = solr_query.substring(0, solr_query.length() - 8);
		String url = MessageFormat.format(SOLR_URL, solr_query,
				"person_name_lc");
		URL link = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) link
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.connect();
		List<String> persons = new ArrayList<String>();

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
						persons.add(WordUtils.capitalize(node.getTextContent()
								.trim()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return persons;
	}

	public static List<String> extractAllLocationNamesFromXML(String query)
			throws IOException {
		String solr_query = "";
		String[] split = query.split("\\s+");
		for (String str : split)
			solr_query += "location_name%3A\""
					+ URLEncoder.encode(str, "UTF-8") + "\"%20OR%20";
		solr_query = solr_query.substring(0, solr_query.length() - 8);
		String url = MessageFormat
				.format(SOLR_URL, solr_query, "location_name");
		URL link = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) link
				.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.connect();
		List<String> locations = new ArrayList<String>();

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
						locations.add(node.getTextContent().trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return locations;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws IOException, SQLException {

		String search = "Michael Jackson Chicago";

		try {
			Map<String, Object> map = interpretQuery(search);
			System.out.println(map);
		} catch (Exception e) {
			System.out.println("Exception...");
		}

		System.out.println("queries bearbeitet");
	}

}
