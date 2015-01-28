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
import java.text.MessageFormat;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class Modell {

	private final static String dataPath = "trainTestData/";

	private static final String modelsPath = "models/";

	private static final String feedbackPath = "feedbackInstances/";

	public final static String adaboostM1Options = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.J48 -- -C 0.25 -M 2";

	public final static String baggingOptions = "-P 100 -S 1 -num-slots 1 -I 10 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1";

	public final static String naiveBayesOptions = "-P 100 -S 1 -num-slots 1 -I 10 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";

	// private static final String feedbackPath = "feedbackInstances/";

	public static boolean actualizeTrainData(String modellName) {
		String trainData = dataPath + modellName + "_train_data.arff";
		String feedbackData = feedbackPath + modellName
				+ "_feedback_instances.arff";

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

	public static Classifier trainAlgorithm(Classifier cls,
			String pathToTrainData) {
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
		if (data != null) {
			try {
				if (cls instanceof AdaBoostM1)
					cls.setOptions(adaboostM1Options.split("\\s+"));
				else if (cls instanceof Bagging)
					cls.setOptions(baggingOptions.split("\\s+"));
				cls.buildClassifier(data);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cls;
		}
		return cls;
	}

	public static AdaBoostM1 loadTrainedModel(String modelPath) {
		AdaBoostM1 cls = null;
		try {
			cls = (AdaBoostM1) SerializationHelper.read(modelPath);
		} catch (Exception e1) {
			System.out.println("Loading Algorithm Failed: " + modelPath);
			e1.printStackTrace();
		}
		return cls;
	}

	public static void main(String[] args) {
		BufferedReader reader;
		AdaBoostM1 a_sport_af = new AdaBoostM1();
		Instances s_testInstances;
		String train_sport_after_feedback = "data/model3/train/train_after_feedback.arff";
		String test_sport = "data/model3/test/test_data.arff";

		try {
			a_sport_af = (AdaBoostM1) trainAlgorithm(a_sport_af,
					train_sport_after_feedback);


			 // serialize model
			 ObjectOutputStream oos = new ObjectOutputStream(
			                            new FileOutputStream("models/model3-af.model"));
			 oos.writeObject(a_sport_af);
			 oos.flush();
			 oos.close();
			 
			reader = new BufferedReader(new FileReader(test_sport));
			s_testInstances = new Instances(reader);
			if (s_testInstances.classIndex() == -1)
				s_testInstances
						.setClassIndex(s_testInstances.numAttributes() - 1);
			reader.close();

			Evaluation evaluation = new Evaluation(s_testInstances);
			evaluation.evaluateModel(a_sport_af, s_testInstances);
			System.out
					.println(String.format("%.2f", evaluation.pctCorrect())
							+ " & "
							+ String.format("%.2f",
									evaluation.precision(0) * 100) + " & "
							+ String.format("%.2f", evaluation.recall(0) * 100)
							+ " & "
							+ String.format("%.2f", evaluation.fMeasure(0) * 100));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
