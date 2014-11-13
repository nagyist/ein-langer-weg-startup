package de.mft.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Klasse {

	private String name;
	private String modelName;
	private int instanceLength;
	private List<String> attributeNames;
	
	public static String musicClass = "MUSIK/RESSOURCEN";
	public static String landOrtClass = "LAND/ORT";
	public static String sportClass = "SPORT/KARRIERE";
	
	public enum ClassName {MUSIK_RESSOURCEN, LAND_ORT, SPORT_KARRIERE};

	
	private static Map<String, List<String>> CLASSES = null;
	private static Map<String, String> ModelNames = null;

	static {
		CLASSES = new HashMap<String, List<String>>();

		List<String> musicNameList = null;
		List<String> sportNameList = null;
		List<String> landOrtNameList = null;
		musicNameList = new ArrayList<String>(Arrays.asList(new String[] {
				"location_found", "similarity_to_music_class_en",
				"similarity_to_music_class_de",
				"similarity_to_location_class_de",
				"similarity_to_family_class_de", "valueOfAllOtherClasses",
				"class" }));
		sportNameList = new ArrayList<String>(Arrays.asList(new String[] {
				"similarity_to_sport_class_en", "similarity_to_sport_class_de",
				"class" }));
		landOrtNameList = new ArrayList<String>(Arrays.asList(new String[] {
				"location_found", "similarity_to_location_class_en",
				"similarity_to_location_class_de", "class" }));

		CLASSES.put(musicClass, musicNameList);
		CLASSES.put(landOrtClass, landOrtNameList);
		CLASSES.put(sportClass, sportNameList);
		
		ModelNames = new HashMap<String, String>();
		ModelNames.put(musicClass, "musik_ressourcen");
		ModelNames.put(landOrtClass, "land_ort");
		ModelNames.put(sportClass, "sport_karriere");
	}

	public Klasse(String name) {
		this.setName(name);
		this.setModelName(name);
		this.setInstanceLength(name);
		this.setAttributeNames(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String name) {
		String modelName = null;
		switch (name) {
		case "MUSIK/RESSOURCEN":
			modelName = ModelNames.get(name);
			break;
		case "LAND/ORT":
			modelName = ModelNames.get(name);
			break;
		case "SPORT/KARRIERE":
			modelName = ModelNames.get(name);
			break;
		default:
			break;
		}
		this.modelName = modelName;
	}

	public int getInstanceLength() {
		return instanceLength;
	}

	public void setInstanceLength(String name) {
		int instanceLength = 0;
		switch (name) {
		case "MUSIK/RESSOURCEN":
			instanceLength = CLASSES.get(name).size();
			break;
		case "LAND/ORT":
			instanceLength = CLASSES.get(name).size();
			break;
		case "SPORT/KARRIERE":
			instanceLength = CLASSES.get(name).size();
			break;
		default:
			break;
		}
		this.instanceLength = instanceLength;
	}

	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public void setAttributeNames(String name) {
		List<String> attrList = null;
		switch (name) {
		case "MUSIK/RESSOURCEN":
			attrList = CLASSES.get(name);
			break;
		case "LAND/ORT":
			attrList = CLASSES.get(name);
			break;
		case "SPORT/KARRIERE":
			attrList = CLASSES.get(name);
			break;
		default:
			break;
		}
		this.attributeNames = attrList;
	}
	
	public static void main(String[] args) {
		Klasse music = new Klasse(Klasse.musicClass);
		System.out.println(music.getInstanceLength());
	}
}
