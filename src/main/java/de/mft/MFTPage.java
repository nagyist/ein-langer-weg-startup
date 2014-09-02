package de.mft;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

import de.mft.similarity.WS4JManager;

public class MFTPage extends WebPage {

	private static final long serialVersionUID = 1L;

	WS4JManager ws4j = new WS4JManager();
	
	
	public MFTPage(final PageParameters parameters) {
		super(parameters);

    	
    	FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
    	add(feedbackPanel);
         
    }
}
