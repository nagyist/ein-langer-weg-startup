package de.mft;


import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;












import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MFTPage extends WebPage {

	private static final long serialVersionUID = 1L;
	private TextField<String> searchQuery;
	private FeedbackPanel feedbackPanel;
    private Label query, evalResultLabel1, evalResultLabel2, evalResultLabel3, instance, result;
    private Instances inst = null;
    private AdaBoostM1 cls = null;
    private Evaluation  eval;
    
	public MFTPage(final PageParameters parameters) throws Exception {
		super(parameters);
    	
		
    	// Add a FeedbackPanel for displaying our messages
    	feedbackPanel = new FeedbackPanel("feedback");
    	
    	// Initialize Labels
    	query = new Label("query", new Model<String>(""));
    	evalResultLabel1 = new Label("evalResultLabel1", new Model<String>(""));
    	evalResultLabel2 = new Label("evalResultLabel2", new Model<String>(""));
    	evalResultLabel3 = new Label("evalResultLabel3", new Model<String>(""));
    	instance = new Label("instance", new Model<String>(""));
    	result = new Label("result", new Model<String>(""));
    	
    	// Add a form with an onSubmit implementation that sets a message
    	Form<?> form = new Form<String>("searchForm");
		searchQuery = new TextField<String>("searchQuery", Model.of(""));
		form.add(searchQuery);
		

		String modelPath = "models/AdaBoostM1_location_found_location_class_location_klasse.model";
		try {
			cls = (AdaBoostM1) SerializationHelper.read(modelPath);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String testingsdata = "java_files/data_492014_062715.arff";
		inst = createInstancesFromData(testingsdata);
		inst.setClassIndex(inst.numAttributes() - 1);
		eval = new Evaluation(inst);
		eval.evaluateModel(cls, inst);
//		final WebMarkupContainer wmc = new WebMarkupContainer("texte");
//		wmc.setOutputMarkupPlaceholderTag(true);
//		form.add(wmc);
		
		form.add(new Button("submitButton") {
			@Override
            public void onSubmit() {
            	
//				wmc.add(new AttributeAppender("class", true, new Model<String>("texte_after_submit"), " "));

                Instance iExample = new Instance(4);
        		iExample.setDataset(inst);
        		
        		iExample.setValue(0, "true");
        		iExample.setValue(1, 2.89);
        		iExample.setValue(2, 1.14);
        		iExample.setValue(3, "NACHRICHTEN/INFORMATION");
        		double p = -1;
				try {
					p = cls.classifyInstance(iExample);
					query.setDefaultModelObject("Query: " + searchQuery.getModelObject());
					evalResultLabel2.setDefaultModelObject(eval.toClassDetailsString());
					evalResultLabel1.setDefaultModelObject(eval.toSummaryString());
					evalResultLabel3.setDefaultModelObject(eval.toMatrixString());
					instance.setDefaultModelObject("Dataset: " + iExample.toString());
					result.setDefaultModelObject("Query is classified as: " + inst.classAttribute().value((int) p));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
            }
		});
		

        feedbackPanel.setOutputMarkupId(true);
    	add(feedbackPanel);
    	
    	// Add Labels
    	add(query);
    	add(evalResultLabel1);
    	add(evalResultLabel2);
    	add(evalResultLabel3);
    	add(instance);
    	add(result);
    	
    	add(form);
    }
	
	
    private static void evaluateActualModell(AdaBoostM1 cls, Instances data, Label label1, Label label2, Label label3) throws Exception {
		data.setClassIndex(data.numAttributes() - 1);
		Evaluation eval = new Evaluation(data);
		eval.evaluateModel(cls, data);
		label1.setDefaultModelObject(eval.toSummaryString());
		label2.setDefaultModelObject(eval.toClassDetailsString());
		label3.setDefaultModelObject(eval.toMatrixString());
    }
    
    private static AdaBoostM1 loadActualModel() throws Exception {
    	String algorithmOptions = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.J48 -- -C 0.25 -M 2";
    	String[] args = algorithmOptions.split("\\s+");
    	String rootPath = "java_files/", modelsPath = "models/";
    	String trainingsdata = rootPath + "data_492014_062713.arff", testingsdata = rootPath + "data_492014_062715.arff";

    	Instances inst = createInstancesFromData(trainingsdata);
		AdaBoostM1 cls = trainAlgorithm(inst, args);
		System.out.println("\nCreated Model name " + serializeModel(cls, inst, modelsPath));
		return cls;
		
	}
    
	private static AdaBoostM1 trainAlgorithm(Instances inst, String[] options) throws Exception {
		AdaBoostM1 cls = null;
		if (inst != null) {
			 cls = new AdaBoostM1();
			 cls.setOptions(options);
			 cls.buildClassifier(inst);
			 return cls;
		} 
		return cls;
	}
	
	private static Instances createInstancesFromData(String trainData) {
		Instances inst = null;
		if(trainData == null) {
			System.err.println("Data are NULL please check your Input!");
			System.exit(1);
		}
		try {
			inst = new Instances(new BufferedReader(new
			FileReader(trainData)));
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
	
	private static String serializeModel(Classifier cls, Instances inst, String modelPath) {
		
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
}
