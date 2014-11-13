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

import de.mft.data.Klasse;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomePage extends WebPage {
	
	private static final long serialVersionUID = 1L;
	
	private static List<Klasse> models = new ArrayList<Klasse>(Arrays.asList(new Klasse[] {new Klasse(Klasse.musicClass), new Klasse(Klasse.landOrtClass), new Klasse(Klasse.sportClass)}));
	
	private AdaBoostM1 cls = null;
	
	private Label persons, locations, intention, instance, result,
	saveFeedbackPanel, title, texte, instances;
	
	private Instance instanceExample = null;
	
	private Form<?> searchForm, feedbackForm, saveForm;
	
	private TextField<String> searchQuery;
	
	private Button submit = null, save = null, toggle = null, trueFeedbackButton = null, falseFeedbackButton = null;
	
	public HomePage(final PageParameters parameters) throws Exception {
		
		super(parameters);
		final String arr[] = { "false", "true" };
		final String klassifikation = "";
		initializeLabels();
		
		initializeInformationText();
		
		initilizeButtons();

		searchForm = initializeSearchForm();
		
		saveForm = initializeSaveForm();
		feedbackForm = initializeFeedbackForm(klassifikation, arr);
		
		addLabelsToHomepage();
		addFormsToHomepage();
			
	}

	private Form<?> initializeSaveForm() {
		Form<?> saveForm = new Form<String>("saveForm");
		saveForm.add(saveFeedbackPanel);
		saveForm.add(save);
		saveForm.add(toggle);

		return saveForm;
	}

	private Form<?> initializeFeedbackForm(final String klassifikation, String[] arr) {
		Form<?> feedbackForm = new Form<String>("feedbackForm");

		trueFeedbackButton = new Button("true") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				if (instanceExample != null) {
					String bool = (int) instanceExample.value(0)== 0 ? "false" : "true";
					saveFeedbackPanel.setDefaultModelObject("Orakel Datansatz: "
							+ bool + ","
							+ instanceExample.value(1) + "," + instanceExample.value(2) + ","
							+ klassifikation);
					instanceExample.setClassValue(klassifikation);
				}
				save.add(new AttributeAppender("class", true,
						new Model<String>("saveButton-make-visible"), " "));
				toggle.add(new AttributeAppender("class", true,
						new Model<String>("toggleButton-make-visible"), " "));
			}
		};

		falseFeedbackButton = new Button("false") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				if (instanceExample != null) {
					String bool = (int) instanceExample.value(0)== 0 ? "false" : "true";
					saveFeedbackPanel.setDefaultModelObject("Orakel Datansatz: "
							+ bool + ","
							+ instanceExample.value(1) + "," + instanceExample.value(2) + ","
							+ contraKlassifikation(klassifikation));
					instanceExample.setClassValue(contraKlassifikation(klassifikation));
				}
				save.add(new AttributeAppender("class", true,
						new Model<String>("saveButton-make-visible"), " "));
				toggle.add(new AttributeAppender("class", true,
						new Model<String>("toggleButton-make-visible"), " "));
			}
		};
		feedbackForm.add(trueFeedbackButton);
		feedbackForm.add(falseFeedbackButton);
		
		return feedbackForm;
	}

	private String contraKlassifikation(String klassifikation) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Form<?> initializeSearchForm() {
		Form<?> searchForm = new Form<String>("searchForm");
		
		searchQuery = new TextField<String>("searchQuery", Model.of(""));
		searchForm.add(searchQuery);
		
		List<String> choices = new ArrayList<String>();
		for(Klasse k : models) choices.add(k.getName());
		String selected = "LAND/ORT";
		 
		final DropDownChoice<String> listModels = new DropDownChoice<String>(
				"models", new PropertyModel<String>(this, "selected"), choices);
		searchForm.add(listModels);
		
		submit = new Button("submitButton") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				try {
					Map<String, Object> interpretation = Interpretation
							.interpretQuery(WordUtils.capitalize(searchQuery
									.getModelObject()));
					
					String model_name = models.get(1).getModelName();
					loadTrainedAlgorithm(model_name);			
					Instances inst = evaluateModell();
					int instanceLength = models.get(1).getInstanceLength();
					
					instanceExample = new Instance(instanceLength);
					instanceExample.setDataset(inst);
					Map<String, Object> results = new HashMap<String, Object>();
					for(String attribute : models.get(1).getAttributeNames()) {
						results.put(attribute, null);
					}
					
					for(int i = 0; i < results.size(); i++) {
						instanceExample.setValue(i, (double) results.get(i));
					}
					
					double p = cls.classifyInstance(instanceExample);
					String klassifikation = inst.classAttribute().value((int) p);
					
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
							+ instanceExample.toString().replace(models.get(1).getName(), "?"));
					result.setDefaultModelObject("Query classified as: "
							+ klassifikation);
					trueFeedbackButton.add(new AttributeAppender("class", true,
							new Model<String>("visible-buttons"), " "));
					falseFeedbackButton.add(new AttributeAppender("class", true,
							new Model<String>("visible-buttons"), " "));
					texte.add(new AttributeAppender("class", true,
							new Model<String>("texte-after-submit"), " "));
					
					
					
				} catch (NullPointerException e) {
					System.out.println("NullPointerException: Submit");
				}
				catch (Exception e) {
					System.out.println("Exception: Submit");
				}
				
				
			}

			

		};
		
		return searchForm;
	}

	private Instances evaluateModell() {
		return null;
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
	
	private void addFormsToHomepage()  {
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


	private void initilizeButtons() {
		save = new Button("save") {
			@SuppressWarnings("deprecation")
			@Override
			public void onSubmit() {
				String feedBack = "Datensatz \"<span style='color:deepPink'>" + instanceExample.toString() + "</span>\" wurde erfolgreich gespeichert";
				saveFeedbackPanel.setEscapeModelStrings(false);
				saveFeedbackPanel.setDefaultModelObject(feedBack);
				add(new AttributeAppender("class", true,
						new Model<String>("hidden-buttons"), ""));
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
		toggle = new Button("toggle") {
			@Override
			public void onSubmit() {
				
			}
		};
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
		saveFeedbackPanel = new Label("feedback_panel", new Model<String>(""));
		instances = new Label("instances", new Model<String>(""));
	}


	private void initializeInformationText() {
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
	
//	public static void main(String[] args) {
//		System.out.println(HomePage.models);
//	}

}
