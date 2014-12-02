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

import de.mft.model.TrainedModel;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;
import weka.core.SerializationHelper;


public class Modell {
	
	private final static String dataPath = "trainTestData/";
	
	private static final String modelsPath = "models/";
	
	private static final String feedbackPath = "feedbackInstances/";
	
	public final static String algorithmOptions = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.J48 -- -C 0.25 -M 2";

//	private static final String feedbackPath = "feedbackInstances/";


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
	
	public static String serializeModel(Classifier cls, String modelPath) {
		try {
			SerializationHelper.write(modelPath, cls);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return modelPath;
	}

	public static AdaBoostM1 trainAlgorithm(String pathToTrainData) {
		BufferedReader reader;
		Instances data = null;
		try {
			reader = new BufferedReader(new FileReader(pathToTrainData));
			data = new Instances(reader);
			 if (data.classIndex() == -1)
				   data.setClassIndex(data.numAttributes() - 1);
			reader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		AdaBoostM1 cls = null;
		if (data != null) {
			cls = new AdaBoostM1();
			try {
				cls.setOptions(algorithmOptions.split("\\s+"));
				cls.buildClassifier(data);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cls;
		}
		return cls;
	}
	
	public static void main(String[] args) {
		BufferedReader reader;
		Instances test;
		AdaBoostM1 cls;
		String modelName = "models/model1.model";
		try {
			cls = trainAlgorithm("data/model1/train/train_data.arff");
			reader = new BufferedReader(new FileReader(
					"data/model1/test/test_data.arff"));
			test = new Instances(reader);
			 if (test.classIndex() == -1)
				   test.setClassIndex(test.numAttributes() - 1);
			reader.close();
			Evaluation evaluation = new Evaluation(test);
			evaluation.evaluateModel(cls, test);
			System.out.println(evaluation.toSummaryString());
			System.out.println(serializeModel(cls, modelName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
