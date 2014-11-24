package de.mft.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import weka.core.Instances;
import de.mft.interpretation.Interpretation;
import de.mft.similarity.GNETManager;
import de.mft.similarity.WS4JSimilarity;

public abstract class ModelClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String pathToModels = "models/"; 

	public final static String pathToFeedback = "feedback/"; 
	
	public final static String pathToData = "data/"; 
	
	public final static String trainData = "train_data.arff"; 
	
	public final static String testData = "test_data.arff"; 
	
	private double similarityToOwnClassEn;
	
	private double similarityToOwnClassDe;

	
	public ModelClass(Interpretation interpretation) {
		setSimilarityToOwnClassEn(interpretation.getEnSimilarities().get(getClassName().replace("_", "/")));
		setSimilarityToOwnClassDe(interpretation.getDeSimilarities().get(getClassName().replace("_", "/")));
	}
	
	public abstract Instance getInstance();
	
	public abstract AdaBoostM1 loadTrainedModel();
	
	public abstract Instances exampleInstances();
	
	public abstract String getClassName();

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
	
	public boolean saveFeedbackInstance(String model, String query, Instance instance) {
			BufferedReader br;
			Writer out;
			String line;
			StringBuffer sb = new StringBuffer();
			try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					pathToFeedback + model + "/feedback.arff"), "UTF8"));
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				sb.append(query+ "," + instance.toString() + "\n");
				br.close();
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(pathToFeedback
								+ model + "/feedback.arff"), "UTF8"));
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
	}
	
	public static void main(String[] args){
		Interpretation i = new Interpretation(GNETManager.getInstance(), new WS4JSimilarity(), "Thami Bouchnafa Songs in Kenitra balls");
		MusicClass music = new MusicClass(i);
		Instance in = music.getInstance();
		in.setClassValue(music.getClassName());
		System.out.println(in.toString());
	}
}
