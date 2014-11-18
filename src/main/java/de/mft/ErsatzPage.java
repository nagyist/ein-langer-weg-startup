package de.mft;

import org.apache.commons.lang.WordUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.mft.interpretation.Interpretation;
import de.mft.similarity.GNETSimilarity;
import de.mft.similarity.WS4JSimilarity;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
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

public class ErsatzPage extends WebPage {

	private static final long serialVersionUID = 1L;
	private TextField<String> searchQuery;
	private FeedbackPanel feedbackPanel;
	
	private Label persons, locations, intention, instance, result,
			feedback_panel, title, texte, instances;
	
//	private static final List<String> MODELS = Arrays.asList(new String[] {
//			"MUSIK/RESSOURCEN", "LAND/ORT", "SPORT/KARRIERE" }); 
//	private Map<String, Integer> modelsIntanceLength = null;
	

	private String selected = "LAND/ORT";
//	private DropDownChoice<String> listModels;
	
	private String classFileName = "musik_ressourcen";
	
	private Button SUBMIT, TRUE, FALSE, SAVE, TOGGLE;
	private Instances inst = null;
	private AdaBoostM1 cls = null;
	private static GNETSimilarity gnet = GNETSimilarity.getInstance();
	private Map<String, Object> INTERPRETATION_VALUES = null;
	private Instance instanceExample = null;
	private int instanceExampleLength = 0;
	
	private double similarity_to_class_en = 0.0;
	private double similarity_to_class_de = 0.0;
	private String location_found = "?", klassifikation, 
			positiv_class = "LAND_ORT",
			negativ_class = "NO_LAND_ORT";
	

	public ErsatzPage(final PageParameters parameters) throws Exception {
		
		super(parameters);
//		modelsIntanceLength = new HashMap<String, Integer>();
//		modelsIntanceLength.put("MUSIK/RESSOURCEN", 6);
//		modelsIntanceLength.put("LAND/ORT", 4);
//		modelsIntanceLength.put("SPORT/KARRIERE", 4);

		initializeLabels();
		final String arr[] = { "false", "true" };
		initilizeButtons(arr);
		hideInformationText();

		Form<?> searchForm = initializeSearchForm();

		Form<?> feedbackForm = new Form<String>("feedbackForm");
		Form<?> saveForm = new Form<String>("saveForm");
		feedbackForm.add(TRUE);
		feedbackForm.add(FALSE);
		saveForm.add(feedback_panel);
		saveForm.add(SAVE);
		saveForm.add(TOGGLE);
		addLabelToHomepage();
		addFormsToHomepage(searchForm, feedbackForm, saveForm);
			
	}

	private String convertToModelName(String className) {
		String rs = null;
		switch (className) {
			case "MUSIK/RESSOURCEN":
				rs = "musik_ressourcen";
				break;
			case "LAND/ORT":
				rs = "land_ort";
				break;
			case "SPORT/KARRIERE":
				rs = "sport_karriere";
				break;
			default:
				break;
		}
		return rs;
	}
	private void addFormsToHomepage(Form<?> searchForm, Form<?> feedbackForm,
			Form<?> saveForm) {
		add(searchForm);
		add(feedbackForm);
		add(saveForm);
	}

	private void addLabelToHomepage() {
		add(feedbackPanel);
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

//		listModels = new DropDownChoice<String>(
//				"models", new PropertyModel<String>(this, "selected"), MODELS);
//		searchForm.add(listModels);
//		
//		String model_name = convertToModelName(searchForm.get("models").getDefaultModelObjectAsString());
//		instanceExampleLength = modelsIntanceLength.get(searchForm.get("models").getDefaultModelObjectAsString());
//		selected = model_name;
		try {
			loadTrainedAlgorithm("land_ort");			
			evaluateModell();
		} catch (Exception e) {
			System.out.println(selected);
			System.exit(0);
		}
		
		SUBMIT = new Button("submitButton") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				
				try {
					INTERPRETATION_VALUES = Interpretation
							.interpretQuery(WordUtils.capitalize(searchQuery
									.getModelObject()));
					
					similarity_to_class_en = (new WS4JSimilarity())
							.calculateSimilarityToAllClasses(
									(String) INTERPRETATION_VALUES
											.get("intention")).get("LAND/ORT");
					similarity_to_class_de = gnet
							.calculateSimilarityToAllClasses(
									(String) INTERPRETATION_VALUES
											.get("intention")).get("LAND/ORT");
					location_found = INTERPRETATION_VALUES
							.get("location_found").toString();
				} catch (IOException e1) {
					System.out.println("WordNet v GermaNet Fehler...");
				} catch (NullPointerException e1) {
					locations.setEscapeModelStrings(false);
					locations.setDefaultModelObject("<center><span style='text-align:center;font-size:50px;color:#009682;font-weight: bold;'>Person not found 404, Please try another Person</span></center>");
					texte.add(new AttributeAppender("class", true,
							new Model<String>("texte-after-submit"), " "));
					System.out.println("Interpretation Fehler: NullPointer ...");
				}
				
				try {
					instanceExample = new Instance(4);
					instanceExample.setDataset(inst);
					instanceExample.setValue(0, location_found);
					instanceExample.setValue(1, similarity_to_class_en);
					instanceExample.setValue(2, similarity_to_class_de);
					instanceExample.setValue(3, negativ_class);
					double p = cls.classifyInstance(instanceExample);
					klassifikation = inst.classAttribute().value((int) p);
				} catch (Exception e) {
					System.out.println("Classification Fehler: 1");
				}	
				try {
					persons.setDefaultModelObject("Persons Found: "
							+ INTERPRETATION_VALUES.get("names"));
					intention
							.setDefaultModelObject("The intention of the Searcher: "
									+ INTERPRETATION_VALUES.get("intention"));
					locations.setDefaultModelObject("Locations Found: "
							+ INTERPRETATION_VALUES.get("locations"));
					title.setDefaultModelObject(searchQuery.getModelObject()
							+ " - Results");
					instance.setDefaultModelObject("Dataset: "
							+ instanceExample.toString().replace(negativ_class, "?"));
					result.setDefaultModelObject("Query classified as: "
							+ klassifikation);
					TRUE.add(new AttributeAppender("class", true,
							new Model<String>("visible-buttons"), " "));
					FALSE.add(new AttributeAppender("class", true,
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
		searchForm.add(SUBMIT);
		
		return searchForm;
	}

	private void evaluateModell() throws Exception {
		// Evaluate Algorithm
		String testingsdata = "java_files/data_492014_062715.arff";
		inst = createInstancesFromData(testingsdata);
		inst.setClassIndex(inst.numAttributes() - 1);
		Evaluation eval = new Evaluation(inst);
		eval.evaluateModel(cls, inst);
	}

	private void loadTrainedAlgorithm(String model) {
		String modelPath = "models/"+model+".model";
		try {
			cls = (AdaBoostM1) SerializationHelper.read(modelPath);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
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
		TOGGLE = new Button("toggle") {
			@Override
			public void onSubmit() {
				
			}
		};
		SAVE = new Button("save") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				String feedBack = "Datensatz \"<span style='color:deepPink'>" + instanceExample.toString() + "</span>\" wurde erfolgreich gespeichert";
				feedback_panel.setEscapeModelStrings(false);
				feedback_panel.setDefaultModelObject(feedBack);
				SAVE.add(new AttributeAppender("class", true,
						new Model<String>("hidden-buttons"), " "));
				BufferedReader br;
				Writer out;
				String line, insts = "";
				StringBuffer sb = new StringBuffer();
				try {
					br = new BufferedReader(new FileReader(new File("java_files/feedback_instances.arff")));
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
		
		TRUE = new Button("true") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				if (instanceExample != null) {
					feedback_panel.setDefaultModelObject("Orakel Datansatz: "
							+ arr[(int) instanceExample.value(0)] + ","
							+ instanceExample.value(1) + "," + instanceExample.value(2) + ","
							+ klassifikation);
					instanceExample.setClassValue(klassifikation);
				}
				SAVE.add(new AttributeAppender("class", true,
						new Model<String>("saveButton-make-visible"), " "));
				TOGGLE.add(new AttributeAppender("class", true,
						new Model<String>("toggleButton-make-visible"), " "));
			}
		};

		FALSE = new Button("false") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				if (instanceExample != null) {
					feedback_panel.setDefaultModelObject("Orakel Datansatz: "
							+ arr[(int) instanceExample.value(0)] + ","
							+ instanceExample.value(1) + "," + instanceExample.value(2) + ","
							+ contraKlassifikation(klassifikation));
					instanceExample.setClassValue(contraKlassifikation(klassifikation));
				}
				SAVE.add(new AttributeAppender("class", true,
						new Model<String>("saveButton-make-visible"), " "));
				TOGGLE.add(new AttributeAppender("class", true,
						new Model<String>("toggleButton-make-visible"), " "));
			}
		};
	}
	
	private String contraKlassifikation(String klasse) {
		if (klasse == null) return negativ_class;
		if (klasse.equals(negativ_class)) return positiv_class;
		else return negativ_class;
	}

	private void initializeLabels() {
		// Initialize Title
		title = new Label("title", new Model<String>(""));
		title.setDefaultModelObject("Startseite - bouchnafa.de");
		// Add a FeedbackPanel for displaying our messages
		feedbackPanel = new FeedbackPanel("feedback");

		// Initialize Labels
		persons = new Label("persons", new Model<String>(""));
		locations = new Label("locations", new Model<String>(""));
		intention = new Label("intention", new Model<String>(""));
		instance = new Label("instance", new Model<String>(""));
		result = new Label("result", new Model<String>(""));
		feedback_panel = new Label("feedback_panel", new Model<String>(""));
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