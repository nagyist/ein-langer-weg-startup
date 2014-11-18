package de.mft.classification;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream.GetField;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;
import weka.core.SerializationHelper;


public class Modell {
	
	private final static String dataPath = "trainTestData/";
	
	private static final String modelsPath = "models/";
	
//	private static final String feedbackPath = "feedbackInstances/";


	private static void serializeModell(String className){
		Classifier cls = new AdaBoostM1();
		String trainData = dataPath + getModelName(className) + "_train_data.arff";
		System.out.println(trainData);
		String model_path = null;
		
		try {
			Instances inst = new Instances(new BufferedReader(new FileReader(trainData))); 
			inst.setClassIndex(inst.numAttributes() - 1);
			cls.buildClassifier(inst);
			model_path = modelsPath + getModelName(className) + ".model";
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(model_path));
			oos.writeObject(cls);
			oos.flush();
			oos.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Model serialized: " + model_path);
	}

	
	private static String getModelName(String name) {
		String modelName = null;
		switch (name) {
		case "MUSIK_RESSOURCEN":
			modelName = "musik_ressourcen";
			break;
		case "LAND_ORT":
			modelName = "land_ort";
			break;
		case "SPORT_KARRIERE":
			modelName = "sport_career";
			break;
		default:
			break;
		}
		return modelName;
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Modell.serializeModell("SPORT_KARRIERE");
		System.out.println("Finished!");
	}
}
