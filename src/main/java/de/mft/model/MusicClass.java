package de.mft.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import de.mft.interpretation.Interpretation;

public class MusicClass extends ModelClass {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String model = "model1";
	public final String modelPath = pathToModels + "model1/";
	public final String modelName = "model1.model";
	public final String trainPath = pathToData + "model1/train/";
	public final String testPath = pathToData + "model1/test/";
	
	private final String className = "MUSIK_RESSOURCEN"; 
	private boolean locationFound;
	private double similarityToLocationDe;
	private double similarityToFamilyDe;
	private double valueOfAllOtherClasses;
	
	public MusicClass(Interpretation interpretation) {
		super(interpretation);
		fillRestClassAttributes(interpretation);
	}

	private void fillRestClassAttributes(Interpretation interpretation) {
		setLocationFound(interpretation.locationFound());
		setSimilarityToLocationDe(interpretation.getDeSimilarities().get("LAND/ORT"));
		setSimilarityToFamilyDe(interpretation.getDeSimilarities().get("FAMILIE/PRIVATSPHÄRE"));
		double valueOfOthers = 0.0;
		String[] arr = {"NACHRICHTEN/INFORMATION",
				"SPORT/KARRIERE", "KÖRPER/MENSCH",
				"FAMILIE/PRIVATSPHÄRE", "LAND/ORT"};
		for (String str : arr) {
			valueOfOthers += interpretation.getDeSimilarities().get(str);
		}
		valueOfOthers = valueOfOthers / arr.length;
		setValueOfAllOtherClasses(valueOfOthers);
	}

	public boolean locationFound() {
		return locationFound;
	}

	public void setLocationFound(boolean locationFound) {
		this.locationFound = locationFound;
	}

	public double getSimilarityToLocationDe() {
		return similarityToLocationDe;
	}

	public void setSimilarityToLocationDe(double similarityToLocationDe) {
		this.similarityToLocationDe = similarityToLocationDe;
	}

	public double getSimilarityToFamilyDe() {
		return similarityToFamilyDe;
	}

	public void setSimilarityToFamilyDe(double similarityToFamilyDe) {
		this.similarityToFamilyDe = similarityToFamilyDe;
	}

	public double getValueOfAllOtherClasses() {
		return valueOfAllOtherClasses;
	}

	public void setValueOfAllOtherClasses(double valueOfAllOtherClasses) {
		this.valueOfAllOtherClasses = valueOfAllOtherClasses;
	}
	
	@Override
	public Instance getInstance() {
		Instance instance = new Instance(7);
		Instances instances = exampleInstances();
		instance.setDataset(instances);
		instance.setValue(0, String.valueOf(locationFound()));
		instance.setValue(1, getSimilarityToOwnClassEn());
		instance.setValue(2, getSimilarityToOwnClassDe());
		instance.setValue(3, getSimilarityToLocationDe());
		instance.setValue(4, getSimilarityToFamilyDe());
		instance.setValue(5, getValueOfAllOtherClasses());
		instance.setValue(6, getNegativClass());
		return instance;
	}

	@Override
	public AdaBoostM1 loadTrainedModel() {
		AdaBoostM1 cls = null;
		try {
			cls = (AdaBoostM1) SerializationHelper.read(pathToModels + modelName);
		} catch (Exception e1) {
			System.out.println("Loading Algorithm Failed: " + pathToModels + modelName);
			e1.printStackTrace();
		}
		return cls;
	}

	@Override
	public Instances exampleInstances() {
		Instances inst = null;
		try {
			inst = new Instances(new BufferedReader(new FileReader(testPath + testData)));
			inst.setClassIndex(inst.numAttributes() - 1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inst;
	}
	
	public boolean saveFeedbackInstance(Instance instance) {
		return super.saveFeedbackInstance(model, instance);
	}

	@Override
	public String getClassName() {
		return className;
	}
}
