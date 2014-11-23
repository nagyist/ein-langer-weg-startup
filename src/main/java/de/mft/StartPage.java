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
import org.apache.wicket.markup.html.panel.FeedbackPanel;
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

	private final static Logger LOGGER = Logger.getLogger(StartPage.class
			.getName());
	static {
		LOGGER.setLevel(Level.INFO);
	}

	private TextField<String> searchQuery;

	private Button musicFeedback, ortungFeedback, sportFeedback;

	private MusicClass musicClass = null;

	private OrtungClass ortungClass = null;

	private SportClass sportClass = null;

	private Interpretation interpretation = null;

	private Label noResults;
	
	private static boolean searched = false;
	
	@SuppressWarnings("serial")
	public StartPage(final PageParameters parameters) {
		super(parameters);

		Label titlePanel = new Label("title", new Model<String>(""));
		titlePanel
				.setDefaultModelObject("Startseite - AdaBoost Classification with User-Feedback");
		add(titlePanel);

		noResults = new Label("noResults", new Model<String>(""));
		noResults.setEscapeModelStrings(false);
		add(noResults);

		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);
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

				if (musicClass.saveFeedbackInstance(musicInstance)
						&& ortungClass.saveFeedbackInstance(ortungClass
								.getInstance())
						&& sportClass.saveFeedbackInstance(sportClass
								.getInstance()))
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
				if (musicClass.saveFeedbackInstance(musicClass.getInstance())
						&& ortungClass.saveFeedbackInstance(ortungInstance)
						&& sportClass.saveFeedbackInstance(sportClass
								.getInstance()))
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

				if (musicClass.saveFeedbackInstance(musicClass.getInstance())
						&& ortungClass.saveFeedbackInstance(ortungClass
								.getInstance())
						&& sportClass.saveFeedbackInstance(sportInstance))
					noResults.setDefaultModelObject(dankSagung);
				searched = false;
			}
		};

		Form<?> feedbackForm = new Form<String>("feedbackForm");
		feedbackForm.add(musicFeedback);
		feedbackForm.add(ortungFeedback);
		feedbackForm.add(sportFeedback);

		add(feedbackForm);

		Form<?> searchForm = initializeSearchForm(titlePanel, noResults);
		add(searchForm);
		
		add(new Link<Object>("userguides") {
			@Override
			public void onClick() {
				setResponsePage(UserGuides.class, getPageParameters());
			}

		});

	}

	@SuppressWarnings("serial")
	private Form<?> initializeSearchForm(final Label titlePanel,
			final Label noResults) {
		Form<?> searchForm = new Form<String>("searchForm");
		searchQuery = new TextField<String>("searchQuery", Model.of(""));
		searchForm.add(searchQuery);

		searchForm.add(new Button("submitButton") {
			@Override
			public void onSubmit() {
				if (searched && musicClass != null && ortungClass != null && sportClass != null) {
					musicClass.saveFeedbackInstance(musicClass.getInstance());
					ortungClass.saveFeedbackInstance(ortungClass.getInstance());
					sportClass.saveFeedbackInstance(sportClass.getInstance());
				}
				musicFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				ortungFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				sportFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				noResults.setDefaultModelObject("");
				String query = searchQuery.getModelObject();
				if (titlePanel != null && query != null && !"".equals(query)) {
					titlePanel.setDefaultModelObject(query + " - Results");
					interpretation = new Interpretation(Start.gnet, Start.ws4j, query);
					if (interpretation.personFound()) {
						musicClass = new MusicClass("MUSIK_RESSOURCEN",
								interpretation);
						ortungClass = new OrtungClass("LAND_ORT",
								interpretation);
						sportClass = new SportClass("SPORT_KARRIERE",
								interpretation);

						Instance musicInstance = musicClass.getInstance();
						Instance ortungInstance = ortungClass.getInstance();
						Instance sportInstance = sportClass.getInstance();
						AdaBoostM1 musicModel = musicClass.loadTrainedModel();
						AdaBoostM1 ortungModel = ortungClass.loadTrainedModel();
						AdaBoostM1 sportModel = sportClass.loadTrainedModel();

						double musicClassificationDouble = 0, ortungClassificationDouble = 0, sportClassificationDouble = 0;
						String musicClassification = null, ortungClassification = null, sportClassification = null;
						try {
							musicClassificationDouble = musicModel
									.classifyInstance(musicInstance);
							musicClassification = musicClass.exampleInstances()
									.classAttribute()
									.value((int) musicClassificationDouble);
							ortungClassificationDouble = ortungModel
									.classifyInstance(ortungInstance);
							ortungClassification = ortungClass
									.exampleInstances().classAttribute()
									.value((int) ortungClassificationDouble);
							sportClassificationDouble = sportModel
									.classifyInstance(sportInstance);
							sportClassification = sportClass.exampleInstances()
									.classAttribute()
									.value((int) sportClassificationDouble);

							if (musicClassification.equals(musicClass
									.getClassName()))
								musicFeedback.add(new AttributeModifier(
										"class", musicClass.getClassName()));
							if (ortungClassification.equals(ortungClass
									.getClassName()))
								ortungFeedback.add(new AttributeModifier(
										"class", ortungClass.getClassName()));
							if (sportClassification.equals(sportClass
									.getClassName()))
								sportFeedback.add(new AttributeModifier(
										"class", sportClass.getClassName()));
							searched = true;
							
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else {
						String interString = "<span style='color: red'> Person konnte nicht gefunden werden </span><br /> Versuchen Sie bitte eine andere Suchanfrage";
						noResults.setDefaultModelObject(interString);
					}

				} else {
					info("NullPointer..");
				}
			}
		});
		return searchForm;
	}

}
