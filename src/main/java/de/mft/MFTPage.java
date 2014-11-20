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

import de.mft.classification.Modell;
import de.mft.interpretation.Interpretation;
import de.mft.model.Klasse;
import de.mft.similarity.GNETManager;
import de.mft.similarity.WS4JSimilarity;

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

public class MFTPage extends WebPage {

	private static final long serialVersionUID = 1L;
	private TextField<String> searchQuery;

	private Label persons, locations, intention, instance, result,
				title, texte, instances;

	private RadioChoice<String> radio;

	private Button submitButton, positivFeedbackButton;
	private Instances inst = null;
	private AdaBoostM1 cls = null;
	private static GNETManager gnet = GNETManager.getInstance();
	private static WS4JSimilarity ws4j = new WS4JSimilarity();

	private Map<String, Object> interpretation = null;
	private Instance instanceExample = null;

	private String klassifikation, positiv_class;
	private static String negativ_class;
	private String selected = Klasse.musicClass;

	private static boolean positiverFeedBack = false;

	private Form<?> searchForm, feedbackForm;

	public MFTPage(final PageParameters parameters) throws Exception {

		super(parameters);
		initializeLabels();
		final String arr[] = { "false", "true" };
		initilizeButtons(arr);
		hideInformationText();
		searchForm = initializeSearchForm();
		add(persons);
		add(locations);
		add(intention);
		add(instance);
		add(result);
		add(title);
		add(texte);
		add(instances);
		feedbackForm = new Form<String>("feedbackForm");
		feedbackForm.add(positivFeedbackButton);
		add(searchForm);
		add(feedbackForm);
	}

	@SuppressWarnings("serial")
	private Form<?> initializeSearchForm() {
		Form<?> searchForm = new Form<String>("searchForm");
		searchQuery = new TextField<String>("searchQuery", Model.of(""));
		searchForm.add(searchQuery);

		radio = new RadioChoice<String>("radioGroup",
				new PropertyModel<String>(this, "selected"),
				Arrays.asList(new String[] { Klasse.musicClass,
						Klasse.landOrtClass, Klasse.sportClass }));
		searchForm.add(radio);

		submitButton = new Button("submitButton") {
			@Override
			public void onSubmit() {
				
				fillFeedbackInstancesField();
				positiv_class = selected;
				negativ_class = Klasse.getNegativClass(positiv_class);
				int feedbackInstancesCount = countFeedbackInstances();
				if (feedbackInstancesCount == 30) {
					boolean dataActualized = false ;
					try {
						dataActualized = Modell.actualizeTrainData(Klasse.getModelName(positiv_class));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("Actualizing train Data failed");
						e.printStackTrace();
					} 
					if (dataActualized) {
						try {
							boolean newModellSerialized = Modell.serializeModell(Klasse.getTrainDataName(positiv_class), positiv_class);
							if (newModellSerialized) Modell.clearFeedbackData(positiv_class);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println("Retrain modell failed");
							e.printStackTrace();
						} 
					}
				}
				loadTrainedAlgorithm(getModelName(selected));
				evaluateModell(getModelName(selected));
				
				if (!positiverFeedBack && instanceExample != null) {
					instanceExample.setClassValue(contraKlassifikation());
					saveFeedbackInstance();
				}
				
				try {
					getPageParameters().set("q", searchQuery.getModelObject());
					String query = getPageParameters().get("q").toString();
					interpretation = Interpretation.interpretQuery(query);
					searchQuery.setDefaultModelObject(query);
					
					Map<String, Object> results = getResultsForClass(selected,
							interpretation);
					for (String key : results.keySet()) {
						if ("Infinity".equals(results.get(key).toString()))
							results.put(key, new Double(4.5656));
					}
					int sizeOfInstance = results.size();
					instanceExample = new Instance(sizeOfInstance);
					instanceExample.setDataset(inst);
					initializeInstanceExample(results);
					
					double p = cls.classifyInstance(instanceExample);
					klassifikation = inst.classAttribute().value((int) p);
				} catch (IOException e1) {
					System.out.println("WordNet v GermaNet Fehler...");
				} catch (NullPointerException e1) {
					noPersonFoundCase();
				} catch (Exception e) {
					System.out.println("Classification Fehler: 1");
				}

				try {
					writeSubmitResultsToFeedbackPannel();
				} catch (NullPointerException e) {
					System.out
							.println("Interpretation Fehler: NullPointer ...");
				} catch (Exception e) {
					System.out.println("Classification Fehler: 2");
				}

			}
		};
		searchForm.add(submitButton);
		return searchForm;
	}

	
	private void initializeInstanceExample(Map<String, Object> results) {
		if (Klasse.musicClass.equals(selected)) {
			instanceExample.setValue(0,
					results.get("location_found").toString());
			instanceExample.setValue(1,
					(double) results.get("similarity_to_music_en"));
			instanceExample.setValue(2,
					(double) results.get("similarity_to_music_de"));
			instanceExample.setValue(3, (double) results
					.get("similarity_to_location_de"));
			instanceExample
					.setValue(4, (double) results
							.get("similarity_to_family_de"));
			instanceExample.setValue(5,
					(double) results.get("valueOfAllOtherClasses"));
			instanceExample.setClassValue(results.get("class")
					.toString());
		} else if (Klasse.landOrtClass.equals(selected)) {
			instanceExample.setValue(0,
					results.get("location_found").toString());
			instanceExample.setValue(1, (double) results
					.get("similarity_to_location_en"));
			instanceExample.setValue(2, (double) results
					.get("similarity_to_location_de"));
			instanceExample.setClassValue(results.get("class")
					.toString());
		} else if (Klasse.sportClass.equals(selected)) {
			instanceExample.setValue(0,
					(double) results.get("similarity_to_sport_en"));
			instanceExample.setValue(1,
					(double) results.get("similarity_to_sport_de"));
			instanceExample.setClassValue(results.get("class")
					.toString());
		}
	}
	@SuppressWarnings({ "deprecation" })	
	private void noPersonFoundCase() {
		locations.setEscapeModelStrings(false);
		locations
				.setDefaultModelObject("<center><span style='text-align:center;font-size:50px;color:#009682;font-weight: bold;'>Person not found 404, Please try another Person</span></center>");
		texte.add(new AttributeAppender("class", true,
				new Model<String>("texte-after-submit"), " "));
		title.setDefaultModelObject("No Results Found for Query "
				+ searchQuery.getModelObject());
		instance.setDefaultModelObject("");
		result.setDefaultModelObject("");
		persons.setDefaultModelObject("");
		intention.setDefaultModelObject("");
		System.out
				.println("Interpretation Fehler: No Person Found or Solr is not started ...");
	}
	@SuppressWarnings({ "deprecation" })
	private void writeSubmitResultsToFeedbackPannel() {
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
				+ instanceExample.toString().replace(negativ_class,
						"?"));
		result.setDefaultModelObject("Query classified as: "
				+ klassifikation);
		positivFeedbackButton.add(new AttributeAppender("class",
					true, new Model<String>(getModelName(selected)), " "));
		texte.add(new AttributeAppender("class", true,
				new Model<String>("texte-after-submit"), " "));
	}
	
	private static Map<String, Object> getResultsForClass(String selected,
			Map<String, Object> interpretation) {
		Map<String, Object> rs = new HashMap<String, Object>();
		List<String> attris = (new Klasse(selected)).getAttributeNames();
		Map<String, Double> de = gnet
				.calculateSimilarityToAllClasses((String) interpretation
						.get("intention"));
		Map<String, Double> en = ws4j
				.calculateSimilarityToAllClasses((String) interpretation
						.get("intention"));
		for (String attr : attris) {
			if (attr.endsWith("_de")) {
				rs.put(attr, de.get(Klasse.attributesOriginalNames.get(attr)));
			} else if (attr.endsWith("_en")) {
				rs.put(attr, en.get(Klasse.attributesOriginalNames.get(attr)));
			} else if (attr.equals("class")) {
				rs.put(attr, negativ_class);
			} else if (attr.equals("valueOfAllOtherClasses")) {
				String[] arr = { "MUSIK/RESSOURCEN", "NACHRICHTEN/INFORMATION",
						"SPORT/KARRIERE", "KÖRPER/MENSCH",
						"FAMILIE/PRIVATSPHÄRE", "LAND/ORT" };
				double temp = 0;
				Map<String, Double> de_similarities = gnet
						.calculateSimilarityToAllClasses((String) interpretation
								.get("intention"));
				for (String str : arr) {
					temp += de_similarities.get(str);
				}
				rs.put(attr, temp / arr.length);
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
		String testingsdata = "trainTestData/" + modelName + "_test_data.arff";
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
		String modelPath = "models/" + model + ".model";
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

		positivFeedbackButton = new Button("true") {
			@Override
			public void onSubmit() {
				instanceExample.setClassValue(klassifikation);
				saveFeedbackInstance();
				positiverFeedBack = true;
			}

		};
	}

	private void saveFeedbackInstance() {
		BufferedReader br;
		Writer out;
		String line, insts = "";
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(new File(""
					+ "feedbackInstances/" + getModelName(selected)
					+ "_feedback_instances.arff")));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			sb.append(instanceExample.toString() + "\n");
			insts = sb.toString();
			br.close();
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("feedbackInstances/"
							+ getModelName(selected)
							+ "_feedback_instances.arff"), "UTF8"));
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
		String header = Klasse.fileHeader.get(selected);
		insts = "<p  class=\"results\" style='text-align:left;margin-top:10px;margin-buttom:10px'>"
				+ header.replaceAll("\n", "<br />")  + insts.replaceAll("\n", "<br />") + "</p>";
		instances.setEscapeModelStrings(false);
		instances.setDefaultModelObject(insts);
	}
	
	private void fillFeedbackInstancesField() {
		BufferedReader br;
		Writer out;
		String line, insts = "";
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new FileReader(new File(""
					+ "feedbackInstances/" + getModelName(selected)
					+ "_feedback_instances.arff")));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			insts = sb.toString();
			br.close();
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("feedbackInstances/"
							+ getModelName(selected)
							+ "_feedback_instances.arff"), "UTF8"));
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
		String header = Klasse.fileHeader.get(selected);
		insts = "<p  class=\"results\" style='text-align:left;margin-top:10px;margin-buttom:10px'>"
				+ header.replaceAll("\n", "<br />") + insts.replaceAll("\n", "<br />") + "</p>";
		instances.setEscapeModelStrings(false);
		instances.setDefaultModelObject(insts);
	}
	
	private int countFeedbackInstances() {
		int rs = 0;
		BufferedReader br;
		@SuppressWarnings("unused")
		String line;
		try {
			br = new BufferedReader(new FileReader(new File(""
					+ "feedbackInstances/" + getModelName(selected)
					+ "_feedback_instances.arff")));
			while ((line = br.readLine()) != null) {
				rs++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	private String contraKlassifikation() {
		return (klassifikation.equals(negativ_class)) ? positiv_class : negativ_class;
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
		instances = new Label("instances", new Model<String>(""));
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

}