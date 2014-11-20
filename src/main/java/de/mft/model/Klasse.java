package de.mft.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Klasse {

	private static final long serialVersionUID = 1L;

	private String name;
	private String modelName;
	private int instanceLength;
	private List<String> attributeNames;
	
	public final static String musicClass = "MUSIK_RESSOURCEN";
	public final static String landOrtClass = "LAND_ORT";
	public final static String sportClass = "SPORT_KARRIERE";
	
	private static Map<String, List<String>> CLASSES = null;
	private static Map<String, String> ModelNames = null;
	public static Map<String, String> attributesOriginalNames = null;
	
	public static Map<String, String> fileHeader = null;
	
	static {
		CLASSES = new HashMap<String, List<String>>();

		List<String> musicNameList = null;
		List<String> sportNameList = null;
		List<String> landOrtNameList = null;
		musicNameList = new ArrayList<String>(Arrays.asList(new String[] {
				"location_found", "similarity_to_music_en",
				"similarity_to_music_de",
				"similarity_to_location_de",
				"similarity_to_family_de", "valueOfAllOtherClasses",
				"class" }));
		sportNameList = new ArrayList<String>(Arrays.asList(new String[] {
				"similarity_to_sport_en", "similarity_to_sport_de",
				"class" }));
		landOrtNameList = new ArrayList<String>(Arrays.asList(new String[] {
				"location_found", "similarity_to_location_en",
				"similarity_to_location_de", "class" }));
		
		attributesOriginalNames = new HashMap<String, String>();
		attributesOriginalNames.put("location_found", "location_found");
		attributesOriginalNames.put("similarity_to_music_en", "MUSIK/RESSOURCEN");
		attributesOriginalNames.put("similarity_to_music_de", "MUSIK/RESSOURCEN");
		attributesOriginalNames.put("similarity_to_location_en", "LAND/ORT");
		attributesOriginalNames.put("similarity_to_location_de", "LAND/ORT");
		attributesOriginalNames.put("class", "class");
		attributesOriginalNames.put("similarity_to_family_de", "FAMILIE/PRIVATSPHÄRE");
		attributesOriginalNames.put("similarity_to_sport_en", "SPORT/KARRIERE");
		attributesOriginalNames.put("similarity_to_sport_de", "SPORT/KARRIERE");

		CLASSES.put(musicClass, musicNameList);
		CLASSES.put(landOrtClass, landOrtNameList);
		CLASSES.put(sportClass, sportNameList);
		
		ModelNames = new HashMap<String, String>();
		ModelNames.put(musicClass, "musik_ressourcen");
		ModelNames.put(landOrtClass, "land_ort");
		ModelNames.put(sportClass, "sport_career");
		
		fileHeader = new HashMap<String, String>();
		fileHeader.put(landOrtClass, "@relation "+(new Klasse(landOrtClass)).getModelName()+"_feedback_instances\n\n"+
				"@attribute location_found {false, true}\n" +
				"@attribute similarity_to_location_en numeric\n"+
				"@attribute similarity_to_location_de numeric\n"+
				"@attribute class {"+landOrtClass+", "+getNegativClass(landOrtClass)+"}\n\n"+
				"@data\n\n");
		
		fileHeader.put(musicClass, "@relation "+(new Klasse(musicClass)).getModelName()+"_feedback_instances\n\n"+
				"@attribute location_found {false, true}\n" +
				"@attribute similarity_to_music_en numeric\n" +
				"@attribute similarity_to_music_de numeric\n" +
				"@attribute similarity_to_location_de numeric\n" +
				"@attribute similarity_to_family_de numeric\n" +
				"@attribute valueOfAllOtherClasses numeric\n" +
				"@attribute class {"+musicClass+", "+getNegativClass(musicClass)+"}\n\n"+
				"@data\n\n");
		
		fileHeader.put(sportClass, "@relation "+(new Klasse(sportClass)).getModelName()+"_feedback_instances\n\n"+
				"@attribute similarity_to_sport_en numeric\n"+
				"@attribute similarity_to_sport_de numeric\n"+
				"@attribute class {"+sportClass+", "+getNegativClass(sportClass)+"}\n\n"+
				"@data\n\n");
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
	
	public static String getNegativClass(String _class) {
		return "NO_" + String.valueOf(_class.charAt(0)) + String.valueOf(_class.charAt(_class.indexOf("_") +1));
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModelName() {
		return modelName;
	}
	
	public static String getModelName(String name) {
		String modelName = null;
		switch (name) {
		case musicClass:
			modelName = "musik_ressourcen";
			break;
		case landOrtClass:
			modelName = "land_ort";
			break;
		case sportClass:
			modelName = "sport_career";
			break;
		default:
			break;
		}
		return modelName;
	}
	
	public static String getTrainDataName(String className) {
		return "trainTestData/" + Klasse.getModelName(className)+ "_train_data.arff";
	}
	
	public static String getTestDataName(String className) {
		return "trainTestData/" + Klasse.getModelName(className)+ "_test_data.arff";
	}
	

	public void setModelName(String name) {
		String modelName = null;
		switch (name) {
		case "MUSIK_RESSOURCEN":
			modelName = ModelNames.get(name);
			break;
		case "LAND_ORT":
			modelName = ModelNames.get(name);
			break;
		case "SPORT_KARRIERE":
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
		case "MUSIK_RESSOURCEN":
			instanceLength = CLASSES.get(name).size();
			break;
		case "LAND_ORT":
			instanceLength = CLASSES.get(name).size();
			break;
		case "SPORT_KARRIERE":
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
		case "MUSIK_RESSOURCEN":
			attrList = CLASSES.get(name);
			break;
		case "LAND_ORT":
			attrList = CLASSES.get(name);
			break;
		case "SPORT_KARRIERE":
			attrList = CLASSES.get(name);
			break;
		default:
			break;
		}
		this.attributeNames = attrList;
	}
	
	public String toString() {
		String rs = "";
		rs += "Name of Class " + this.getName() + "\n";
		rs += "Name of Modell " + this.getModelName() + "\n";
		rs += "Class Attributes " + this.getAttributeNames() + "\n";
		return rs;
	}
	
	public static void main(String[] args) {
		Klasse music = new Klasse(Klasse.musicClass);
		System.out.println(music.toString());
		System.out.println(Klasse.fileHeader);
	}
}
