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

public class SportClass extends ModelClass {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String model = "model3";
	public final String modelPath = pathToModels + "model3/";
	public final String modelName = "model3.model";
	public final String trainPath = pathToData + "model3/train/";
	public final String testPath = pathToData + "model3/test/";
	
	private final String className = "SPORT_KARRIERE";
	
	public SportClass(Interpretation interpretation) {
		super(interpretation);
	}

	@Override
	public Instance getInstance() {
		Instance instance = new Instance(3);
		Instances instances = exampleInstances();
		instance.setDataset(instances);
		instance.setValue(0, getSimilarityToOwnClassEn());
		instance.setValue(1, getSimilarityToOwnClassDe());
		instance.setValue(2, getNegativClass());
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
