package de.mft;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import de.mft.interpretation.Interpretation;
import de.mft.model.MusicClass;
import de.mft.model.OrtungClass;
import de.mft.model.SportClass;
import de.mft.similarity.GNETManager;
import de.mft.similarity.WS4JSimilarity;

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

	private final static GNETManager gnet = GNETManager.getInstance();

	private final static WS4JSimilarity ws4j = new WS4JSimilarity();

	@SuppressWarnings("serial")
	public StartPage(final PageParameters parameters) {
		super(parameters);

		Label titlePanel = new Label("title", new Model<String>(""));
		titlePanel
				.setDefaultModelObject("Startseite - AdaBoost Classification with User-Feedback");
		add(titlePanel);

		Label noResults = new Label("noResults", new Model<String>(""));
		noResults.setEscapeModelStrings(false);
		add(noResults);
		
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);
		final Button musicFeedback = new Button("musicFeedback") {
			@Override
			public void onSubmit() {
				
			}
		};
		final Button ortungFeedback = new Button("ortungFeedback") {
			@Override
			public void onSubmit() {
				
			}
		};
		final Button sportFeedback = new Button("sportFeedback") {
			@Override
			public void onSubmit() {
				
			}
		};
		
		Form<?> feedbackForm = new Form<String>("feedbackForm");
		feedbackForm.add(musicFeedback);
		feedbackForm.add(ortungFeedback);
		feedbackForm.add(sportFeedback);
		
		add(feedbackForm);
		
		Form<?> searchForm = initializeSearchForm(musicFeedback, ortungFeedback, sportFeedback, titlePanel, noResults);
		add(searchForm);
		
	}

	@SuppressWarnings("serial")
	private Form<?> initializeSearchForm(final Button musicFeedback, final Button ortungFeedback, final Button sportFeedback, final Label titlePanel, final Label noResults) {
		Form<?> searchForm = new Form<String>("searchForm");
		final TextField<String> searchQuery = new TextField<String>(
				"searchQuery", Model.of(""));
		searchForm.add(searchQuery);

		
		searchForm.add(new Button("submitButton") {
			@Override
			public void onSubmit() {
				musicFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				ortungFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				sportFeedback.add(new AttributeModifier("class", "hidden-buttons"));
				String query = searchQuery.getModelObject();
				if (titlePanel != null || query == null || "".equals(query)) {
					titlePanel.setDefaultModelObject(query + " - Results");
					Interpretation interpretation = new Interpretation(gnet,
							ws4j, query);
					if (interpretation.personFound()) {
						MusicClass musicClass = new MusicClass(
								"MUSIK_RESSOURCEN", interpretation);
						OrtungClass ortungClass = new OrtungClass("LAND_ORT",
								interpretation);
						SportClass sportClass = new SportClass(
								"SPORT_KARRIERE", interpretation);

						Instance musicInstance = musicClass.getInstance();
						Instance ortungInstance = ortungClass.getInstance();
						Instance sportInstance = sportClass.getInstance();
						AdaBoostM1 musicModel = musicClass.loadTrainedModel();
						AdaBoostM1 ortungModel = ortungClass.loadTrainedModel();
						AdaBoostM1 sportModel = sportClass.loadTrainedModel();
						
						double musicClassificationDouble = 0, ortungClassificationDouble = 0, sportClassificationDouble = 0;
						String musicClassification = null, ortungClassification = null, sportClassification = null;
						try {
							musicClassificationDouble = musicModel.classifyInstance(musicInstance);
							musicClassification = musicClass.exampleInstances().classAttribute().value((int) musicClassificationDouble);
							ortungClassificationDouble = ortungModel.classifyInstance(ortungInstance);
							ortungClassification = ortungClass.exampleInstances().classAttribute().value((int) ortungClassificationDouble);
							sportClassificationDouble = sportModel.classifyInstance(sportInstance);
							sportClassification = sportClass.exampleInstances().classAttribute().value((int) sportClassificationDouble);
							
							if (musicClassification.equals(musicClass.getClassName()))
								musicFeedback.add(new AttributeModifier("class", musicClass.getClassName()));
							if (ortungClassification.equals(ortungClass.getClassName()))
								ortungFeedback.add(new AttributeModifier("class", ortungClass.getClassName()));
							if (sportClassification.equals(sportClass.getClassName()))
								sportFeedback.add(new AttributeModifier("class", sportClass.getClassName()));
						} catch (Exception e) {
							e.printStackTrace();
						}						

					} else {
						String interString = "Person could not be Found <br /> Please try another Search Query";
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
