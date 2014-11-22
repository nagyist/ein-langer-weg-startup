package de.mft.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import de.mft.interpretation.Interpretation;

public class OrtungClass extends ModelClass {

	public final String model = "model2";
	public final String modelPath = pathToModels + "model2/";
	public final String modelName = "model2.model";
	public final String trainPath = pathToData + "model2/train/";
	public final String testPath = pathToData + "model2/test/";
	
	private boolean locationFound;
	
	public OrtungClass(String class_, Interpretation interpretation) {
		super(class_, interpretation);
		setLocationFound(interpretation.locationFound());
	}

	public boolean locationFound() {
		return locationFound;
	}

	public void setLocationFound(boolean locationFound) {
		this.locationFound = locationFound;
	}

	@Override
	public Instance getInstance() {
		Instance instance = new Instance(4);
		Instances instances = exampleInstances();
		instance.setDataset(instances);
		instance.setValue(0, String.valueOf(locationFound()));
		instance.setValue(1, getSimilarityToOwnClassEn());
		instance.setValue(2, getSimilarityToOwnClassDe());
		instance.setValue(3, getNegativClass());
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

}
