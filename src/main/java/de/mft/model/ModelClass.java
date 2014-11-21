package de.mft.model;

import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import weka.core.Instances;
import de.mft.interpretation.Interpretation;

public abstract class ModelClass {

	public final static String pathToModels = "model/"; 
	
	public final static String pathToData = "data/"; 
	
	public final static String trainData = "train_data.arff"; 
	
	public final static String testData = "test_data.arff"; 
	
	private String className;

	private double similarityToOwnClassEn;
	
	private double similarityToOwnClassDe;

	
	public ModelClass(String class_, Interpretation interpretation) {
		setClassName(class_);
		setSimilarityToOwnClassEn(interpretation.getEnSimilarities().get(getClassName().replace("_", "/")));
		setSimilarityToOwnClassDe(interpretation.getDeSimilarities().get(getClassName().replace("_", "/")));
	}
	
	public abstract Instance getInstance();
	
	public abstract AdaBoostM1 loadTrainedModel();
	
	public abstract Instances exampleInstances();

	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public double getSimilarityToOwnClassEn() {
		return similarityToOwnClassEn;
	}

	public void setSimilarityToOwnClassEn(double similarityToOwnClassEn) {
		this.similarityToOwnClassEn = similarityToOwnClassEn;
	}

	public double getSimilarityToOwnClassDe() {
		return similarityToOwnClassDe;
	}

	public void setSimilarityToOwnClassDe(double similarityToOwnClassDe) {
		this.similarityToOwnClassDe = similarityToOwnClassDe;
	}
	
	public String getNegativClass() {
		return "NO_"
				+ String.valueOf(getClassName().charAt(0))
				+ String.valueOf(getClassName().charAt(
						getClassName().indexOf("_") + 1));
	}
	
	public String toString() {
		return getInstance().toString();
	}
	
}
