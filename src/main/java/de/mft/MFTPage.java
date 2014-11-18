package de.mft;

import org.apache.commons.lang.WordUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.mft.interpretation.Interpretation;
import de.mft.model.Klasse;
import de.mft.similarity.GNETSimilarity;
import de.mft.similarity.WS4JSimilarity;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MFTPage extends WebPage {

	private static final long serialVersionUID = 1L;
	private TextField<String> searchQuery;
	
	private Label persons, locations, intention, instance, result,
			feedback_instance, title, texte, instances;
	
	private RadioChoice<String> radio;

	private Button submitButton, positivFeedbackButton, negativFeedbackButton, saveButton, toggleButton;
	private Instances inst = null;
	private AdaBoostM1 cls = null;
	private static GNETSimilarity gnet = GNETSimilarity.getInstance();
	private static WS4JSimilarity ws4j = new WS4JSimilarity();
	
	private Map<String, Object> interpretation = null;
	private Instance instanceExample = null;
	
	private double similarity_to_class_en = 0.0;
	private double similarity_to_class_de = 0.0;
	private String location_found = "?", klassifikation, 
			positiv_class;
	private static String negativ_class = "NO_";
	private String selected = Klasse.landOrtClass;
	
	private Map<String, Object> results; 
	
	
	private Form<?> searchForm, saveForm, feedbackForm;

	public MFTPage(final PageParameters parameters) throws Exception {
		
		super(parameters);
		initializeLabels();
		final String arr[] = { "false", "true" };
		initilizeButtons(arr);
		hideInformationText();

		searchForm = initializeSearchForm();

		feedbackForm = new Form<String>("feedbackForm");
		saveForm = new Form<String>("saveForm");
		feedbackForm.add(positivFeedbackButton);
		feedbackForm.add(negativFeedbackButton);
		saveForm.add(feedback_instance);
		saveForm.add(saveButton);
		saveForm.add(toggleButton);
		addLabelsToHomepage();
		addFormsToHomepage();
			
	}

	private void addFormsToHomepage() {
		add(searchForm);
		add(feedbackForm);
		add(saveForm);
	}

	private void addLabelsToHomepage() {
		add(persons);
		add(locations);
		add(intention);
		add(instance);
		add(result);
		add(title);
		add(texte);
		add(instances);
	}

	private Form<?> initializeSearchForm() {
		// Add a form with an onSubmit implementation that sets a message
		Form<?> searchForm = new Form<String>("searchForm");
		searchQuery = new TextField<String>("searchQuery", Model.of(""));
		searchForm.add(searchQuery);

		radio = new RadioChoice<String>("radioGroup", new PropertyModel<String>(
				this, "selected"), Arrays.asList(new String[] {
				Klasse.musicClass, Klasse.landOrtClass, Klasse.sportClass }));
		searchForm.add(radio);
		
		positiv_class = selected;
		negativ_class += String.valueOf(positiv_class.charAt(0)) + String.valueOf(positiv_class.charAt(positiv_class.indexOf("_") + 1));
		
		loadTrainedAlgorithm(getModelName(selected));			
		evaluateModell(getModelName(selected));
		
		submitButton = new Button("submitButton") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				String indexClass = positiv_class.replace("_", "/");
				try {
					interpretation = Interpretation
							.interpretQuery(WordUtils.capitalize(searchQuery
									.getModelObject()));
					
					results = getResultsForClass(selected, interpretation);
					for(String key : results.keySet()) {
						if ("Infinity".equals(results.get(key).toString()))
							results.put(key, new Double(10));
					}
//					similarity_to_class_en = ws4j
//							.calculateSimilarityToAllClasses(
//									(String) interpretation
//											.get("intention")).get(indexClass);
//					similarity_to_class_de = gnet
//							.calculateSimilarityToAllClasses(
//									(String) interpretation
//											.get("intention")).get(indexClass);
//					location_found = interpretation
//							.get("location_found").toString();
				} catch (IOException e1) {
					System.out.println("WordNet v GermaNet Fehler...");
				} catch (NullPointerException e1) {
					locations.setEscapeModelStrings(false);
					locations.setDefaultModelObject("<center><span style='text-align:center;font-size:50px;color:#009682;font-weight: bold;'>Person not found 404, Please try another Person</span></center>");
					texte.add(new AttributeAppender("class", true,
							new Model<String>("texte-after-submit"), " "));
					title.setDefaultModelObject("No Results Found for Query " + searchQuery.getModelObject());
					instance.setDefaultModelObject("");
					result.setDefaultModelObject("");
					persons.setDefaultModelObject("");
					intention.setDefaultModelObject("");
					System.out.println("Interpretation Fehler: No Person Found or Solr is not started ...");
				}
				
				try {
//					int sizeOfInstance = 4;
					int sizeOfInstance = results.size();
					instanceExample = new Instance(sizeOfInstance);
					List<String> attris = (new Klasse(selected)).getAttributeNames();

					for(String str : attris) {
						instanceExample.setValue(new Attribute(str), results.get(str).toString());							
					}
					System.out.println(instanceExample.toString());
//					instanceExample.setDataset(inst);
//					instanceExample.setValue(0, location_found);
//					instanceExample.setValue(1, similarity_to_class_en);
//					instanceExample.setValue(2, similarity_to_class_de);
//					instanceExample.setValue(3, negativ_class);
					double p = cls.classifyInstance(instanceExample);
					klassifikation = inst.classAttribute().value((int) p);
				} catch (Exception e) {
					System.out.println(instanceExample.numAttributes());
					System.out.println(instanceExample.toString());
					System.out.println("Classification Fehler: 1");
				}	
				try {
					persons.setDefaultModelObject("Persons Found: "
							+ interpretation.get("names"));
					intention
							.setDefaultModelObject("The intention of the Searcher: "
									+ interpretation.get("intention"));
					locations.setDefaultModelObject("Locations Found: "
							+ interpretation.get("locations"));
					title.setDefaultModelObject(searchQuery.getModelObject()
							+ " - Results");
					instance.setDefaultModelObject("Dataset: "
							+ instanceExample.toString().replace(negativ_class, "?"));
					result.setDefaultModelObject("Query classified as: "
							+ klassifikation);
					positivFeedbackButton.add(new AttributeAppender("class", true,
							new Model<String>("visible-buttons"), " "));
					negativFeedbackButton.add(new AttributeAppender("class", true,
							new Model<String>("visible-buttons"), " "));
					texte.add(new AttributeAppender("class", true,
							new Model<String>("texte-after-submit"), " "));
				} catch (NullPointerException e) {
					System.out.println("Interpretation Fehler: NullPointer ...");
				} catch (Exception e) {
					System.out.println("Classification Fehler: 2");
				}

			}

		};
		searchForm.add(submitButton);
		
		return searchForm;
	}

	private static Map<String, Object> getResultsForClass(String selected,
			Map<String, Object> interpretation) {
		Map<String, Object> rs = new HashMap<String, Object>();
		List<String> attris = (new Klasse(selected)).getAttributeNames();
		for(String attr : attris)  {
			if (attr.endsWith("_de")) {
				rs.put(attr, gnet
							.calculateSimilarityToAllClasses(
									(String) interpretation
											.get("intention")).get(Klasse.attributesOriginalNames.get(attr)));				
			} else if(attr.endsWith("_en")) {
				rs.put(attr, ws4j
						.calculateSimilarityToAllClasses(
								(String) interpretation
										.get("intention")).get(Klasse.attributesOriginalNames.get(attr)));				

			} else if (attr.equals("class")) {
				rs.put(attr, negativ_class);
			} else {
				rs.put(attr, interpretation.get(attr));
			}
		}
		return rs;
	}
	
	private String getModelName(String name) {
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

	private void evaluateModell(String modelName) {
		// Evaluate Algorithm
		String testingsdata = "trainTestData/"+modelName+"_test_data.arff";
		inst = createInstancesFromData(testingsdata);
		inst.setClassIndex(inst.numAttributes() - 1);
		Evaluation eval;
		try {
			eval = new Evaluation(inst);
			eval.evaluateModel(cls, inst);
		} catch (Exception e) {
			System.out.println("Evaluation of Model Failed: " + testingsdata);
			e.printStackTrace();
		}
	}

	private void loadTrainedAlgorithm(String model) {
		String modelPath = "models/"+model+".model";
		try {
			cls = (AdaBoostM1) SerializationHelper.read(modelPath);
		} catch (Exception e1) {
			System.out.println("Loading Algorithm Failed: " + modelPath);
			e1.printStackTrace();
		}
	}

	private void hideInformationText() {
		texte = new Label("texte", new Model<String>(""));
		texte.setEscapeModelStrings(false);
		texte.setDefaultModelObject("Die Suchmachine verwendet einen schon trainierten Klassifikator "
				+ "namens AdaBoost <br /> Der Algorithmus klassifiziert personbezogene "
				+ "Suchanfragen <br /> Mögliche Klassen sind beispielsweise \"Musik\", "
				+ "\"Sport\", \"Ortung\" etc. <br /> Hier wird der Feedback Ansatz "
				+ "getestet <br /> Wir geben mehrere Suchanfragen ein, die von unserem "
				+ "Algorithmus falsch klassifiziert werden. Diese Suchanfragen werden "
				+ "vom Orakel neu klassifiziert. Neue Datensätze werden erzeugt und zu "
				+ "den Trainigsdaten hinzugefügt. Die Genauigkeit des Algorithmuses "
				+ "wird dann beobachtet.");
	}

	@SuppressWarnings("serial")
	private void initilizeButtons(final String[] arr) {
		toggleButton = new Button("toggle") {
			@Override
			public void onSubmit() {
				
			}
		};
		saveButton = new Button("save") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				String feedBack = "Datensatz \"<span style='color:deepPink'>" + instanceExample.toString() + "</span>\" wurde erfolgreich gespeichert";
				feedback_instance.setEscapeModelStrings(false);
				feedback_instance.setDefaultModelObject(feedBack);
				saveButton.add(new AttributeAppender("class", true,
						new Model<String>("hidden-buttons"), " "));
				BufferedReader br;
				Writer out;
				String line, insts = "";
				StringBuffer sb = new StringBuffer();
				try {
					br = new BufferedReader(new FileReader(new File("" +
							"feedbackInstances/" + getModelName(selected) + "_feedback_instances.arff")));
					while((line = br.readLine()) != null) {
						sb.append(line+"\n");
					}
					sb.append(instanceExample.toString()+"\n");
					insts = sb.toString();
					br.close();
					out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("java_files/feedback_instances.arff"), "UTF8"));
					out.append(sb.toString());
					out.flush();
					out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				insts = "<p  class=\"results\" style='text-align:left;margin-top:10px;margin-buttom:10px'>" + insts.replaceAll("\n", "<br />") + "</p>";
				instances.setEscapeModelStrings(false);
				instances.setDefaultModelObject(insts);
			}
		};
		
		positivFeedbackButton = new Button("true") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				if (instanceExample != null) {
					feedback_instance.setDefaultModelObject("Orakel Datansatz: "
							+ arr[(int) instanceExample.value(0)] + ","
							+ instanceExample.value(1) + "," + instanceExample.value(2) + ","
							+ klassifikation);
					instanceExample.setClassValue(klassifikation);
				}
				saveButton.add(new AttributeAppender("class", true,
						new Model<String>("saveButton-make-visible"), " "));
				toggleButton.add(new AttributeAppender("class", true,
						new Model<String>("toggleButton-make-visible"), " "));
			}
		};

		negativFeedbackButton = new Button("false") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				if (instanceExample != null) {
					feedback_instance.setDefaultModelObject("Orakel Datansatz: "
							+ arr[(int) instanceExample.value(0)] + ","
							+ instanceExample.value(1) + "," + instanceExample.value(2) + ","
							+ contraKlassifikation(klassifikation));
					instanceExample.setClassValue(contraKlassifikation(klassifikation));
				}
				saveButton.add(new AttributeAppender("class", true,
						new Model<String>("saveButton-make-visible"), " "));
				toggleButton.add(new AttributeAppender("class", true,
						new Model<String>("toggleButton-make-visible"), " "));
			}
		};
	}
	
	private String contraKlassifikation(String klasse) {
		return (klasse.equals(negativ_class)) ? positiv_class : negativ_class;
	}

	private void initializeLabels() {
		// Initialize Title
		title = new Label("title", new Model<String>(""));
		title.setDefaultModelObject("Startseite - bouchnafa.de");

		// Initialize Labels
		persons = new Label("persons", new Model<String>(""));
		locations = new Label("locations", new Model<String>(""));
		intention = new Label("intention", new Model<String>(""));
		instance = new Label("instance", new Model<String>(""));
		result = new Label("result", new Model<String>(""));
		feedback_instance = new Label("feedback_instance", new Model<String>(""));
		instances = new Label("instances", new Model<String>(""));
	}

	private static void evaluateActualModell(AdaBoostM1 cls, Instances data,
			Label label1, Label label2, Label label3) throws Exception {
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
		String trainingsdata = rootPath + "data_492014_062713.arff", testingsdata = rootPath
				+ "data_492014_062715.arff";

		Instances inst = createInstancesFromData(trainingsdata);
		AdaBoostM1 cls = trainAlgorithm(inst, args);
		System.out.println("\nCreated Model name "
				+ serializeModel(cls, inst, modelsPath));
		return cls;

	}

	private static AdaBoostM1 trainAlgorithm(Instances inst, String[] options)
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

	private static Instances createInstancesFromData(String trainData) {
		Instances inst = null;
		if (trainData == null) {
			System.err.println("Data are NULL please check your Input!");
			System.exit(1);
		}
		try {
			inst = new Instances(new BufferedReader(new FileReader(trainData)));
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
	
}