package de.mft.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class TrainedModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String testData = "data/{0}/test/test_data.arff";

	private final String modelName = "models/{0}.model";
	
	private String name;

	private double accuaracy;
	
	private double precision;
	
	private double recall;

	private double fMeasure;
	
	public TrainedModel(String name) {
		setName(name);
		getModelInfo();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Name of Model: " + getName() + "\n");
		sb.append("Accuaracy: " + getAccuaracy() + "\n");
		sb.append("Precision: " + getPrecision() + "\n");
		sb.append("Recall: " + getRecall() + "\n");
		sb.append("F-Measure: " + getfMeasure());
		return sb.toString();
	}

	private void getModelInfo() {
		AdaBoostM1 cls = loadTrainedModel();
		Instances data = testInstances();
		Evaluation eval = null;	
		try {
			eval = new Evaluation(data);
			Random rand = new Random(1); // using seed = 1
			int folds = 10;
			eval.crossValidateModel(cls, data, folds, rand);
			setAccuaracy(Math.rint(eval.pctCorrect()*10000)/10000);
			setPrecision(Math.rint(eval.precision(0)*10000)/10000);
			setRecall(Math.rint(eval.recall(0)*10000)/10000);
			setfMeasure(Math.rint(eval.fMeasure(0)*10000)/10000);
		} catch (Exception e) {
			// TODO 
			System.out.println("Evaluation " + getClass().getName());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public AdaBoostM1 loadTrainedModel() {
		AdaBoostM1 cls = null;
		try {
			cls = (AdaBoostM1) SerializationHelper.read(MessageFormat.format(modelName, name));
		} catch (Exception e1) {
			System.out.println("Loading Algorithm Failed: " + MessageFormat.format(modelName, name));
			e1.printStackTrace();
		}
		return cls;
	}
	
	public Instances testInstances() {
		Instances inst = null;
		try {
			inst = new Instances(new BufferedReader(new FileReader(MessageFormat.format(testData, name))));
			inst.setClassIndex(inst.numAttributes() - 1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
				e.printStackTrace();
		}
		return inst;
	}

	public double getAccuaracy() {
		return accuaracy;
	}

	public void setAccuaracy(double accuaracy) {
		this.accuaracy = accuaracy;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getfMeasure() {
		return fMeasure;
	}

	public void setfMeasure(double fMeasure) {
		this.fMeasure = fMeasure;
	}

	public static void main(String[] args) {
		TrainedModel m = new TrainedModel("model1");
		System.out.println(m.toString());
		m = new TrainedModel("model2");
		System.out.println();
		System.out.println(m.toString());
		m = new TrainedModel("model3");
		System.out.println();
		System.out.println(m.toString());
	}
	
}
