package de.mft.classification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.ObjectInputStream.GetField;

import de.mft.model.Klasse;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;
import weka.core.SerializationHelper;


public class Modell {
	
	private final static String dataPath = "trainTestData/";
	
	private static final String modelsPath = "models/";
	
	private static final String feedbackPath = "feedbackInstances/";
	
	public final static String algorithmOptions = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.J48 -- -C 0.25 -M 2";

//	private static final String feedbackPath = "feedbackInstances/";


	public static void serializeModell(String className){
		Classifier cls = new AdaBoostM1();
		String trainData = dataPath + Klasse.getModelName(className) + "_train_data.arff";
		System.out.println(trainData);
		String model_path = null;
		
		try {
			Instances inst = new Instances(new BufferedReader(new FileReader(trainData))); 
			inst.setClassIndex(inst.numAttributes() - 1);
			cls.buildClassifier(inst);
			model_path = modelsPath + Klasse.getModelName(className) + ".model";
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(model_path));
			oos.writeObject(cls);
			oos.flush();
			oos.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Model serialized: " + model_path);
	}

	public static boolean serializeModell(String trainData, String className){
		Classifier cls = new AdaBoostM1();
		String model_path = null;
		
		try {
			Instances inst = new Instances(new BufferedReader(new FileReader(trainData))); 
			inst.setClassIndex(inst.numAttributes() - 1);
			cls.buildClassifier(inst);
			model_path = modelsPath + Klasse.getModelName(className) + ".model";
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(model_path));
			oos.writeObject(cls);
			oos.flush();
			oos.close();
			System.out.println("Model serialized: " + model_path);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean actualizeTrainData(String modellName) {
		String trainData = dataPath + modellName + "_train_data.arff";
		String feedbackData = feedbackPath + modellName + "_feedback_instances.arff";
		
		BufferedReader br;
		Writer out;
		String line;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(new File(trainData)));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br = new BufferedReader(new FileReader(new File(feedbackData)));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(trainData), "UTF8"));
			out.append(sb.toString());
			out.flush();
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	public static boolean clearFeedbackData(String className) {
		String feedbackData = feedbackPath + Klasse.getModelName(className)  + "_feedback_instances.arff";
		
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(feedbackData), "UTF8"));
			out.append("");
			out.flush();
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private static String serializeModel(Classifier cls, Instances inst,
			String modelPath) {

		// serialize model
		String attributes = "_";
		for (int i = 0; i < inst.numAttributes(); i++) {
			attributes += inst.attribute(i).name() + "_";
		}
		attributes = attributes.substring(0, attributes.length() - 7);
		String modelName = cls.getClass().getSimpleName();
		modelName = modelPath + modelName + attributes + ".model";
		try {
			SerializationHelper.write(modelName, cls);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return modelName;
	}

	public static AdaBoostM1 trainAlgorithm(Instances inst, String[] options)
			throws Exception {
		AdaBoostM1 cls = null;
		if (inst != null) {
			cls = new AdaBoostM1();
			cls.setOptions(options);
			cls.buildClassifier(inst);
			return cls;
		}
		return cls;
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
