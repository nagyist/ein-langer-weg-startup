package de.mft.interpretation;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



import org.apache.commons.lang.WordUtils; 
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.mft.model.Klasse;
import de.mft.similarity.GNETManager;
import de.mft.similarity.WS4JSimilarity;


public class Interpretation {

	private String query;
	
	private String intention;
	
	private List<String>  personNames;
	
	private List<String>  locationNames;
	
	private boolean personFound;
	
	private boolean locationFound;
	
	private Map<String, Double> deSimilarities;
	
	private Map<String, Double> enSimilarities;
	
	private static final String SOLR_URL = "http://localhost:8985/solr/collection1/select?q={0}&sort={1}+asc&rows=1000&fl={1}&wt=xml&indent=true";
	
	public Interpretation(GNETManager gnet, WS4JSimilarity ws4j, String query) {
		this.setQuery(query);
		interprete(gnet, ws4j, query);
	}
	
	private void interprete(GNETManager gnet, WS4JSimilarity ws4j, String query) {
		List<String> personNames = null, locationNames = null;
		try {
			query = StopWordsRemover.removeStopWords(query);

			personNames = extractEntitiesFromXML(query, "person_name_lc", permutations(query));
			HashSet<String> namesHS = new HashSet<String>();
			namesHS.addAll(personNames);
			personNames.clear();
			personNames.addAll(namesHS);
			this.setPersonNames(personNames);
			this.setPersonFound(this.getPersonNames().size() > 0);
			
			query = removeEntitiesFromQuery(query, personNames);
			List<String> locations = new ArrayList<String>();
			for(String str : query.split("\\s+")) locations.add(str);
			locationNames = extractEntitiesFromXML(query, "location_name", locations);
			HashSet<String> locationHS = new HashSet<String>();
			locationHS.addAll(locationNames);
			locationNames.clear();
			locationNames.addAll(locationHS);
			this.setLocationNames(locationNames);
			this.setLocationFound(this.getLocationNames().size() > 0);
			
			this.setIntention(query);
			
			setDeSimilarities(removeInfinityValues(gnet.calculateSimilarityToAllClasses(getIntention())));
			setEnSimilarities(removeInfinityValues(ws4j.calculateSimilarityToAllClasses(getIntention())));
			
		} catch (IOException e) {
			System.out.println("Solr Query Failed!! ");
			e.printStackTrace();
		}
	}

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
		}
		return interpretation;
	}

	private Map<String, Double> removeInfinityValues(Map<String, Double> similarities) {
		Map<String, Double> rs = new HashMap<String, Double>();
		for (Entry<String, Double> e : similarities.entrySet()) {
			double newValue = "Infinity".equals(String.valueOf(e.getValue())) ? new Double(4.9878) : e.getValue();
			newValue = Math.round(10000.0 * newValue) / 10000.0; 
			rs.put(e.getKey(), newValue);
		}
		return rs;
	}
	
	@SuppressWarnings("unused")
	private static Map<String, Object> getResultsForClass(String selected,
			Map<String, Object> interpretation) {
		Map<String, Object> rs = new HashMap<String, Object>();
		List<String> attris = (new Klasse(selected)).getAttributeNames();
		GNETManager gnet = GNETManager.getInstance();
		WS4JSimilarity ws4j = new WS4JSimilarity();
		
		Map<String, Double> de = gnet
				.calculateSimilarityToAllClasses((String) interpretation
						.get("intention"));
		Map<String, Double> en = ws4j
				.calculateSimilarityToAllClasses((String) interpretation
						.get("intention"));
		for (String attr : attris) {
			if (attr.endsWith("_de")) {
				rs.put(attr, de.get(Klasse.attributesOriginalNames.get(attr)));
			} else if (attr.endsWith("_en")) {
				rs.put(attr, en.get(Klasse.attributesOriginalNames.get(attr)));
			} else if (attr.equals("class")) {
				rs.put(attr, Klasse.getNegativClass(selected));
			} else if (attr.equals("valueOfAllOtherClasses")) {
				String[] arr = { "MUSIK/RESSOURCEN", "NACHRICHTEN/INFORMATION",
						"SPORT/KARRIERE", "KÖRPER/MENSCH",
						"FAMILIE/PRIVATSPHÄRE", "LAND/ORT" };
				double temp = 0;
				Map<String, Double> de_similarities = gnet
						.calculateSimilarityToAllClasses((String) interpretation
								.get("intention"));
				for (String str : arr) {
					temp += de_similarities.get(str);
				}
				rs.put(attr, temp / arr.length);
			} else {
				rs.put(attr, interpretation.get(attr));
			}
		}
		return rs;
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

	public String toString() {
		String rs  = "";
		rs += "Persons Found: " + getPersonNames() + "\n";
		rs += "Locations Found: " + getLocationNames() + "\n";
		rs += "Intention of the Searcher: " + getIntention() + "\n";
		
		return rs;
	}
	

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getIntention() {
		return intention;
	}

	public void setIntention(String intention) {
		this.intention = intention;
	}

	public List<String> getPersonNames() {
		return personNames;
	}

	public void setPersonNames(List<String> personNames) {
		this.personNames = personNames;
	}

	public boolean personFound() {
		return personFound;
	}

	public void setPersonFound(boolean person_found) {
		this.personFound = person_found;
	}

	public boolean locationFound() {
		return locationFound;
	}

	public void setLocationFound(boolean location_found) {
		this.locationFound = location_found;
	}

	public List<String> getLocationNames() {
		return locationNames;
	}

	public void setLocationNames(List<String> locationNames) {
		this.locationNames = locationNames;
	}

	public void setDeSimilarities(Map<String, Double> de_similarities) {
		this.deSimilarities = de_similarities;
	}

	public Map<String, Double> getDeSimilarities() {
		return deSimilarities;
	}
	
	public void setEnSimilarities(Map<String, Double> en_similarities) {
		this.enSimilarities = en_similarities;
	}

	public Map<String, Double> getEnSimilarities() {
		return enSimilarities;
	}

}
