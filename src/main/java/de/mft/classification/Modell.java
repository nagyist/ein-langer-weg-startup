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

	/*
	 * public static void main(String[] args) { BufferedReader reader; Instances
	 * test1, test2, test3; AdaBoostM1 music_classifier_new,
	 * location_classifier_new, music_classifier_old, location_classifier_old,
	 * sport_classifier_new, sport_classifier_old; String modelName1 =
	 * "models/model1.model"; String modelName2 = "models/model2.model"; String
	 * modelName3 = "models/model3.model"; try { music_classifier_new =
	 * trainAlgorithm("data/model1/train/train_after_feedback.arff");
	 * location_classifier_new =
	 * trainAlgorithm("data/model2/train/train_after_feedback.arff");
	 * sport_classifier_new =
	 * trainAlgorithm("data/model3/train/train_after_feedback.arff");
	 * 
	 * music_classifier_old = loadTrainedModel("models/model1-1.0.0.model");
	 * location_classifier_old = loadTrainedModel("models/model2-1.0.0.model");
	 * sport_classifier_old = loadTrainedModel("models/model3_alt.model");
	 * 
	 * reader = new BufferedReader(new FileReader(
	 * "data/model1/test/test_data.arff")); test1 = new Instances(reader); if
	 * (test1.classIndex() == -1) test1.setClassIndex(test1.numAttributes() -
	 * 1); reader.close();
	 * 
	 * reader = new BufferedReader(new FileReader(
	 * "data/model2/test/test_data.arff")); test2 = new Instances(reader); if
	 * (test2.classIndex() == -1) test2.setClassIndex(test2.numAttributes() -
	 * 1); reader.close();
	 * 
	 * reader = new BufferedReader(new FileReader(
	 * "data/model3/test/test_data.arff")); test3 = new Instances(reader); if
	 * (test3.classIndex() == -1) test3.setClassIndex(test3.numAttributes() -
	 * 1); reader.close();
	 * 
	 * System.out.println("Music Classifiers:"); Evaluation evaluation = new
	 * Evaluation(test1); evaluation.evaluateModel(music_classifier_old, test1);
	 * System.out.print("Old: "); System.out.println("Accuracy: " +
	 * evaluation.pctCorrect()); System.out.println("Precision: " +
	 * evaluation.precision(0)); System.out.println("Recall: " +
	 * evaluation.recall(0)); System.out.println("F-Measure: " +
	 * evaluation.fMeasure(0)); evaluation = new Evaluation(test1);
	 * evaluation.evaluateModel(music_classifier_new, test1);
	 * System.out.print("New: "); System.out.println("Accuracy: " +
	 * evaluation.pctCorrect()); System.out.println("Precision: " +
	 * evaluation.precision(0)); System.out.println("Recall: " +
	 * evaluation.recall(0)); System.out.println("F-Measure: " +
	 * evaluation.fMeasure(0));
	 * 
	 * System.out.println("Location Classifiers:"); evaluation = new
	 * Evaluation(test2); evaluation.evaluateModel(location_classifier_old,
	 * test2); System.out.print("Old: "); System.out.println("Accuracy: " +
	 * evaluation.pctCorrect()); System.out.println("Precision: " +
	 * evaluation.precision(0)); System.out.println("Recall: " +
	 * evaluation.recall(0)); System.out.println("F-Measure: " +
	 * evaluation.fMeasure(0)); evaluation = new Evaluation(test2);
	 * evaluation.evaluateModel(location_classifier_new, test2);
	 * System.out.print("New: "); System.out.println("Accuracy: " +
	 * evaluation.pctCorrect()); System.out.println("Precision: " +
	 * evaluation.precision(0)); System.out.println("Recall: " +
	 * evaluation.recall(0)); System.out.println("F-Measure: " +
	 * evaluation.fMeasure(0));
	 * 
	 * System.out.println("Sport Classifiers:"); evaluation = new
	 * Evaluation(test3); evaluation.evaluateModel(sport_classifier_old, test3);
	 * System.out.print("Old: "); System.out.println("Accuracy: " +
	 * evaluation.pctCorrect()); System.out.println("Precision: " +
	 * evaluation.precision(0)); System.out.println("Recall: " +
	 * evaluation.recall(0)); System.out.println("F-Measure: " +
	 * evaluation.fMeasure(0)); evaluation = new Evaluation(test3);
	 * evaluation.evaluateModel(sport_classifier_new, test3);
	 * System.out.print("New: "); System.out.println("Accuracy: " +
	 * evaluation.pctCorrect()); System.out.println("Precision: " +
	 * evaluation.precision(0)); System.out.println("Recall: " +
	 * evaluation.recall(0)); System.out.println("F-Measure: " +
	 * evaluation.fMeasure(0));
	 * 
	 * Instance i1 = new Instance(7); Instance i2 = new Instance(4); Instance i3
	 * = new Instance(3); i1.setDataset(test1); i2.setDataset(test2);
	 * i3.setDataset(test3);
	 * 
	 * // i1.setValue(0, "true"); // i1.setValue(1, 0); // i1.setValue(2,
	 * 0.2444); // i1.setValue(3, 0.1429); // i1.setValue(4, 0); //
	 * i1.setValue(5, 0.02858); // i1.setValue(6, "NO_MR"); // // i2.setValue(0,
	 * "true"); // i2.setValue(1, 0); // i2.setValue(2, 0.1429); //
	 * i2.setValue(3, "NO_LO");
	 * 
	 * i1.setValue(0, "true"); i1.setValue(1, 1.127); i1.setValue(2, 0);
	 * i1.setValue(3, 0); i1.setValue(4, 0); i1.setValue(5, 0); i1.setValue(6,
	 * "NO_MR");
	 * 
	 * i2.setValue(0, "true"); i2.setValue(1, 1.6124); i2.setValue(2, 0);
	 * i2.setValue(3, "NO_LO");
	 * 
	 * i3.setValue(0, 1.3565); i3.setValue(1, 1.1111); i3.setValue(2, "NO_SK");
	 * 
	 * double musicClassificationIndex = music_classifier_old
	 * .classifyInstance(i1); String musicClassification = test1
	 * .classAttribute() .value((int) musicClassificationIndex);
	 * System.out.println
	 * ("Musik Classification of old Model of Query \"Max Mustermann Essen\": "
	 * + musicClassification);
	 * 
	 * musicClassificationIndex = music_classifier_new .classifyInstance(i1);
	 * musicClassification = test1 .classAttribute() .value((int)
	 * musicClassificationIndex); System.out.println(
	 * "Musik Classification of new Model of Query \"Max Mustermann Essen\": " +
	 * musicClassification); System.out.println(); double
	 * ortungClassificationIndex = location_classifier_old
	 * .classifyInstance(i2); String ortungClassification =
	 * test2.classAttribute() .value((int) ortungClassificationIndex);
	 * System.out.println(
	 * "Ort Classification of old Model of Query \"Max Mustermann Essen\": " +
	 * ortungClassification); ortungClassificationIndex =
	 * location_classifier_new .classifyInstance(i2); ortungClassification =
	 * test2.classAttribute() .value((int) ortungClassificationIndex);
	 * System.out.println(
	 * "Ort Classification of new Model of Query \"Max Mustermann Essen\": " +
	 * ortungClassification);
	 * 
	 * System.out.println(); double sportClassificationIndex =
	 * sport_classifier_old .classifyInstance(i3); String sportClassification =
	 * test3.classAttribute() .value((int) sportClassificationIndex);
	 * System.out.println(
	 * "Sport Classification of old Model of Query \"Max Mustermann Posters\": "
	 * + sportClassification); sportClassificationIndex = sport_classifier_new
	 * .classifyInstance(i3); sportClassification = test3.classAttribute()
	 * .value((int) sportClassificationIndex); System.out.println(
	 * "Ort Classification of new Model of Query \"Max Mustermann Posters\": " +
	 * sportClassification);
	 * 
	 * 
	 * 
	 * // System.out.println(serializeModel(music_classifier_new, modelName1));
	 * // System.out.println(serializeModel(location_classifier_new,
	 * modelName2)); // System.out.println(serializeModel(sport_classifier_new,
	 * modelName3)); } catch (Exception e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 */

	public static void main(String[] args) {
		BufferedReader reader;
		Bagging b_music = new Bagging(), b_ort = new Bagging(), b_sport = new Bagging();
		AdaBoostM1 a_music = new AdaBoostM1(), a_ort = new AdaBoostM1(), a_sport = new AdaBoostM1();
		AdaBoostM1 a_music_af = new AdaBoostM1(), a_ort_af = new AdaBoostM1(), a_sport_af = new AdaBoostM1();
		NaiveBayes n_music = new NaiveBayes(), n_ort = new NaiveBayes(), n_sport = new NaiveBayes();
		Instances m_testInstances, o_testInstances, s_testInstances;
		String train_music = "data/model1/train/train_data.arff";
		String train_music_after_feedback = "data/model1/train/train_after_feedback.arff";
		String test_music = "data/model1/test/test_data.arff";
		String train_ort = "data/model2/train/train_data.arff";
		String train_ort_after_feedback = "data/model2/train/train_after_feedback.arff";
		String test_ort = "data/model2/test/test_data.arff";
		String train_sport = "data/model3/train/train_data.arff";
		String train_sport_after_feedback = "data/model3/train/train_after_feedback.arff";
		String test_sport = "data/model3/test/test_data.arff";

		try {
			a_music_af = (AdaBoostM1) trainAlgorithm(a_music_af,
					train_music_after_feedback);
			a_ort_af = (AdaBoostM1) trainAlgorithm(a_ort_af,
					train_ort_after_feedback);
			a_sport_af = (AdaBoostM1) trainAlgorithm(a_sport_af,
					train_sport_after_feedback);

			a_music = (AdaBoostM1) trainAlgorithm(a_music, train_music);
			b_music = (Bagging) trainAlgorithm(b_music, train_music);
			n_music = (NaiveBayes) trainAlgorithm(n_music, train_music);

			a_ort = (AdaBoostM1) trainAlgorithm(a_ort, train_ort);
			b_ort = (Bagging) trainAlgorithm(b_ort, train_ort);
			n_ort = (NaiveBayes) trainAlgorithm(n_ort, train_ort);

			a_sport = (AdaBoostM1) trainAlgorithm(a_sport, train_sport);
			b_sport = (Bagging) trainAlgorithm(b_sport, train_sport);
			n_sport = (NaiveBayes) trainAlgorithm(n_sport, train_sport);

			reader = new BufferedReader(new FileReader(test_music));
			m_testInstances = new Instances(reader);
			if (m_testInstances.classIndex() == -1)
				m_testInstances
						.setClassIndex(m_testInstances.numAttributes() - 1);
			reader.close();

			reader = new BufferedReader(new FileReader(test_ort));
			o_testInstances = new Instances(reader);
			if (o_testInstances.classIndex() == -1)
				o_testInstances
						.setClassIndex(o_testInstances.numAttributes() - 1);
			reader.close();

			reader = new BufferedReader(new FileReader(test_sport));
			s_testInstances = new Instances(reader);
			if (s_testInstances.classIndex() == -1)
				s_testInstances
						.setClassIndex(s_testInstances.numAttributes() - 1);
			reader.close();

			Evaluation evaluation = new Evaluation(m_testInstances);
			evaluation.evaluateModel(a_music, m_testInstances);
			System.out.print("{Musik} & \\multicolumn{1}{ |c| } {"
					+ String.format("%.2f", evaluation.pctCorrect()) + "} & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(1) * 100)
					+ " & ");
			evaluation = new Evaluation(m_testInstances);
			evaluation.evaluateModel(b_music, m_testInstances);
			System.out.print(String.format("%.2f", evaluation.pctCorrect())
					+ " & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(1) * 100)
					+ " & ");
			evaluation = new Evaluation(m_testInstances);
			evaluation.evaluateModel(n_music, m_testInstances);
			System.out.println(String.format("%.2f", evaluation.pctCorrect())
					+ " & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(1) * 100)
					+ "  \\\\ \\cline{1-10}");
			System.out.println();
			evaluation = new Evaluation(o_testInstances);
			evaluation.evaluateModel(a_ort, o_testInstances);
			System.out.print("{Ort} & \\multicolumn{1}{ |c| } {"
					+ String.format("%.2f", evaluation.pctCorrect()) + "} & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(0) * 100)
					+ " & ");
			evaluation = new Evaluation(o_testInstances);
			evaluation.evaluateModel(b_ort, o_testInstances);
			System.out.print(String.format("%.2f", evaluation.pctCorrect())
					+ " & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(0) * 100)
					+ " & ");
			evaluation = new Evaluation(o_testInstances);
			evaluation.evaluateModel(n_ort, o_testInstances);
			System.out.println(String.format("%.2f", evaluation.pctCorrect())
					+ " & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(0) * 100)
					+ "  \\\\ \\cline{1-10}");
			System.out.println();

			evaluation = new Evaluation(s_testInstances);
			evaluation.evaluateModel(a_sport, s_testInstances);
			System.out.print("{Sport} & \\multicolumn{1}{ |c| } {"
					+ String.format("%.2f", evaluation.pctCorrect()) + "} & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(0) * 100)
					+ " & ");
			evaluation = new Evaluation(s_testInstances);
			evaluation.evaluateModel(b_sport, s_testInstances);
			System.out.print(String.format("%.2f", evaluation.pctCorrect())
					+ " & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(0) * 100)
					+ " & ");
			evaluation = new Evaluation(s_testInstances);
			evaluation.evaluateModel(n_sport, s_testInstances);
			System.out.println(String.format("%.2f", evaluation.pctCorrect())
					+ " & "
					+ String.format("%.2f", evaluation.precision(0) * 100)
					+ " & " + String.format("%.2f", evaluation.recall(0) * 100)
					+ "  \\\\ \\cline{1-10}");

			System.out.println("Feedback Ergebnisse: ");
			evaluation = new Evaluation(m_testInstances);
			evaluation.evaluateModel(a_music, m_testInstances);
			System.out
					.print(String.format("%.2f", evaluation.pctCorrect())
							+ " & "
							+ String.format("%.2f",
									evaluation.precision(0) * 100) + " & "
							+ String.format("%.2f", evaluation.recall(0) * 100)
							+ " & "
							+ String.format("%.2f", evaluation.fMeasure(0) * 100)
							+ " & ");
			evaluation = new Evaluation(m_testInstances);
			evaluation.evaluateModel(a_music_af, m_testInstances);
			System.out
					.println(String.format("%.2f", evaluation.pctCorrect())
							+ " & "
							+ String.format("%.2f",
									evaluation.precision(0) * 100) + " & "
							+ String.format("%.2f", evaluation.recall(0) * 100)
							+ " & "
							+ String.format("%.2f", evaluation.fMeasure(0) * 100));
			
			System.out.println();
			evaluation = new Evaluation(o_testInstances);
			evaluation.evaluateModel(a_ort, o_testInstances);
			System.out
					.print(String.format("%.2f", evaluation.pctCorrect())
							+ " & "
							+ String.format("%.2f",
									evaluation.precision(0) * 100) + " & "
							+ String.format("%.2f", evaluation.recall(0) * 100)
							+ " & "
							+ String.format("%.2f", evaluation.fMeasure(0) * 100)
							+ " & ");
			evaluation = new Evaluation(o_testInstances);
			evaluation.evaluateModel(a_ort_af, o_testInstances);
			System.out
					.println(String.format("%.2f", evaluation.pctCorrect())
							+ " & "
							+ String.format("%.2f",
									evaluation.precision(0) * 100) + " & "
							+ String.format("%.2f", evaluation.recall(0) * 100)
							+ " & "
							+ String.format("%.2f", evaluation.fMeasure(0) * 100));
			
			System.out.println();
			evaluation = new Evaluation(s_testInstances);
			evaluation.evaluateModel(a_sport, s_testInstances);
			System.out
					.print(String.format("%.2f", evaluation.pctCorrect())
							+ " & "
							+ String.format("%.2f",
									evaluation.precision(0) * 100) + " & "
							+ String.format("%.2f", evaluation.recall(0) * 100)
							+ " & "
							+ String.format("%.2f", evaluation.fMeasure(0) * 100)
							+ " & ");
			evaluation = new Evaluation(s_testInstances);
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
