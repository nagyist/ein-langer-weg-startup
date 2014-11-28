package de.mft;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import de.mft.interpretation.Interpretation;
import de.mft.model.MusicClass;
import de.mft.model.OrtungClass;
import de.mft.model.SportClass;

public class StartPage extends WebPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger LOGGER = Logger.getLogger(StartPage.class.getName());

	private TextField<String> searchQuery;

	private Button musicFeedback, ortungFeedback, sportFeedback;

	private String musicClassification = null, ortungClassification = null, sportClassification = null;

	private MusicClass musicClass = null;

	private OrtungClass ortungClass = null;

	private SportClass sportClass = null;

	private Interpretation interpretation = null;

	private Label noResults, infos;
	
	private static boolean searched = false;
	
	@SuppressWarnings("serial")
	public StartPage(final PageParameters parameters) {
		super(parameters);
		LOGGER.setLevel(Level.INFO);

		Label titlePanel = new Label("title", new Model<String>(""));
		titlePanel
				.setDefaultModelObject("Startseite - AdaBoost Classification with User-Feedback");
		add(titlePanel);

		infos = new Label("infos", new Model<String>(""));
		infos.setEscapeModelStrings(false);
		add(infos);
		
		noResults = new Label("noResults", new Model<String>(""));
		noResults.setEscapeModelStrings(false);
		add(noResults);

		Form<?> feedbackForm = new Form<String>("feedbackForm");
		initializeFeedbackButtons();
		feedbackForm.add(musicFeedback);
		feedbackForm.add(ortungFeedback);
		feedbackForm.add(sportFeedback);

		add(feedbackForm);

		Form<?> searchForm = initializeSearchForm(titlePanel);
		add(searchForm);
		
		add(new Link<Object>("userguides") {
			@Override
			public void onClick() {
				setResponsePage(UserGuides.class, getPageParameters());
			}

		});

	}

	@SuppressWarnings("serial")
	private Form<?> initializeSearchForm(final Label titlePanel) {
		Form<?> searchForm = new Form<String>("searchForm");
		searchQuery = new TextField<String>("searchQuery", Model.of(""));
		searchForm.add(searchQuery);
	
		searchForm.add(new Button("submitButton") {
			@Override
			public void onSubmit() {
				if (searched && musicClass != null && ortungClass != null && sportClass != null) {
					
					if (musicClassification != null && musicClassification.equals(musicClass.getClassName())) 
						musicClass.saveFeedbackInstance(interpretation.getQuery(), musicClass.getInstance());
					if (ortungClassification != null && ortungClassification.equals(ortungClass.getClassName()))
						ortungClass.saveFeedbackInstance(interpretation.getQuery(), ortungClass.getInstance());
					if (sportClassification != null && sportClassification.equals(sportClass.getClassName()))
						sportClass.saveFeedbackInstance(interpretation.getQuery(), sportClass.getInstance());
				}
				musicFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				ortungFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				sportFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				noResults.setDefaultModelObject("");
				infos.setDefaultModelObject("");
				String query = searchQuery.getModelObject();
				if (titlePanel != null && query != null && !"".equals(query)) {
					titlePanel.setDefaultModelObject(query + " - Results");
					interpretation = new Interpretation(Start.gnet, Start.ws4j, query);
					if (interpretation.personFound() && interpretation.intentionFound()) {
						musicClass = new MusicClass(interpretation);
						ortungClass = new OrtungClass(interpretation);
						sportClass = new SportClass(interpretation);
						
						infos.setDefaultModelObject("<ul style='list-style-type: none;'><li><strong>Persons Found: </strong>"+
								interpretation.getPersonNames().toString() + "</li>" + 
								"<li><strong style='text-align:inherit'>Intention: </strong>" + interpretation.getIntention() + "</li></ul>");
						
						Instance musicInstance = musicClass.getInstance();
						Instance ortungInstance = ortungClass.getInstance();
						Instance sportInstance = sportClass.getInstance();
						AdaBoostM1 musicModel = musicClass.loadTrainedModel();
						AdaBoostM1 ortungModel = ortungClass.loadTrainedModel();
						AdaBoostM1 sportModel = sportClass.loadTrainedModel();
	
						double musicClassificationIndex = 0, ortungClassificationIndex = 0, sportClassificationIndex = 0;
						try {
							musicClassificationIndex = musicModel
									.classifyInstance(musicInstance);
							musicClassification = musicClass.exampleInstances()
									.classAttribute()
									.value((int) musicClassificationIndex);
							ortungClassificationIndex = ortungModel
									.classifyInstance(ortungInstance);
							ortungClassification = ortungClass
									.exampleInstances().classAttribute()
									.value((int) ortungClassificationIndex);
							sportClassificationIndex = sportModel
									.classifyInstance(sportInstance);
							sportClassification = sportClass.exampleInstances()
									.classAttribute()
									.value((int) sportClassificationIndex);
							boolean a = false, b = false, c = false;
							if (musicClassification.equals(musicClass
									.getClassName())) {
								musicFeedback.add(new AttributeModifier(
										"class", musicClass.getClassName()));
								a = true;
							}
							if (ortungClassification.equals(ortungClass
									.getClassName())) {
								ortungFeedback.add(new AttributeModifier(
										"class", ortungClass.getClassName()));
								b = true;
							}
							if (sportClassification.equals(sportClass
									.getClassName())) {
								sportFeedback.add(new AttributeModifier(
										"class", sportClass.getClassName()));
								c = true;
							}
							if (!a&&!b&&!c) 
								noResults.setDefaultModelObject("<span style='color: red'> Absicht konnte nicht klassifiziert werden</span><br /> Versuchen Sie bitte eine andere Suchanfrage" +
										"<br ><span style='font-size:11px; color:black'><strong>Empfolene Formulierungen: </strong> [Vorname + Nachname + Absicht] oder [Absicht + Vorname + Nachname] <br> Vorname und Nachname können vertauscht werden</span>");
							
							searched = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
	
					} else if (!interpretation.intentionFound()) {
						infos.setDefaultModelObject("<ul style='list-style-type: none;'><li><strong>Persons Found: </strong>"+
								interpretation.getPersonNames().toString() + "</li>" + 
								"<li><strong style='text-align:inherit'>Intention: </strong>" + interpretation.getIntention() + "</li></ul>");
						
						String interString = "<span style='color: red'> Absicht konnte nicht interpretiert werden </span><br /> Versuchen Sie bitte eine andere Suchanfrage" +
								"<br ><span style='font-size:11px; color:black'><strong>Empfolene Formulierungen: </strong> [Vorname + Nachname + Absicht] oder [Absicht + Vorname + Nachname] <br> Vorname und Nachname können vertauscht werden</span>";
						noResults.setDefaultModelObject(interString);
					} else {
						infos.setDefaultModelObject("<ul style='list-style-type: none;'><li><strong>Persons Found: </strong>"+
								interpretation.getPersonNames().toString() + "</li>" + 
								"<li><strong style='text-align:inherit'>Intention: </strong>" + interpretation.getIntention() + "</li></ul>");
						
						String interString = "<span style='color: red'> Person konnte nicht gefunden werden </span><br /> Versuchen Sie bitte eine andere Suchanfrage" +
								"<br ><span style='font-size:11px; color:black'><strong>Empfolene Formulierungen: </strong> [Vorname + Nachname + Absicht] oder [Absicht + Vorname + Nachname] <br> Vorname und Nachname können vertauscht werden</span>";
						noResults.setDefaultModelObject(interString);
					}
	
				} else {
					info("NullPointer..");
				}
			}
		});
		return searchForm;
	}

	@SuppressWarnings("serial")
	private void initializeFeedbackButtons() {
		final String dankSagung = "Vielen Dank für Ihren Feedback <br > Sie können gerne weitere Suchanfragen ausführen";

		musicFeedback = new Button("musicFeedback") {
			@Override
			public void onSubmit() {
				musicFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				ortungFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				sportFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				Instance musicInstance = musicClass.getInstance();
				musicInstance.setClassValue(musicClass.getClassName());

				musicClass.saveFeedbackInstance(interpretation.getQuery(), musicInstance);
				if (ortungClassification != null && ortungClassification.equals(ortungClass.getClassName())) 
					ortungClass.saveFeedbackInstance(interpretation.getQuery(), ortungClass
								.getInstance());

				if (sportClassification != null && sportClassification.equals(sportClass.getClassName())) {
					sportClass.saveFeedbackInstance(interpretation.getQuery(), sportClass
								.getInstance());}
				noResults.setDefaultModelObject(dankSagung);
				searched = false;
			}
		};
		ortungFeedback = new Button("ortungFeedback") {
			@Override
			public void onSubmit() {
				musicFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				ortungFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				sportFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				Instance ortungInstance = ortungClass.getInstance();
				ortungInstance.setClassValue(ortungClass.getClassName());
				ortungClass.saveFeedbackInstance(interpretation.getQuery(), ortungInstance);
				if (musicClassification != null && musicClassification.equals(musicClass.getClassName())) 
					musicClass.saveFeedbackInstance(interpretation.getQuery(), musicClass
								.getInstance());

				if (sportClassification != null && sportClassification.equals(sportClass.getClassName())) {
					sportClass.saveFeedbackInstance(interpretation.getQuery(), sportClass
								.getInstance());}
					noResults.setDefaultModelObject(dankSagung);
				searched = false;
			}
		};
		sportFeedback = new Button("sportFeedback") {
			@Override
			public void onSubmit() {
				musicFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				ortungFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				sportFeedback.add(new AttributeModifier("class",
						"hidden-buttons"));
				Instance sportInstance = sportClass.getInstance();
				sportInstance.setClassValue(sportClass.getClassName());

				sportClass.saveFeedbackInstance(interpretation.getQuery(), sportInstance);
				if (musicClassification != null && musicClassification.equals(musicClass.getClassName())) 
					musicClass.saveFeedbackInstance(interpretation.getQuery(), musicClass
								.getInstance());

				if (ortungClassification != null && ortungClassification.equals(ortungClass.getClassName())) {
					ortungClass.saveFeedbackInstance(interpretation.getQuery(), ortungClass
								.getInstance());}
				noResults.setDefaultModelObject(dankSagung);
				searched = false;
			}
		};
	}

}
