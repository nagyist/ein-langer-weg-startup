package de.mft;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class UserGuides extends WebPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public UserGuides(final PageParameters pageParameters) {
		add(new Link<Object>("backtohomepage") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				PageParameters pageParameters = new PageParameters();
				setResponsePage(StartPage.class, pageParameters);
			}

		});
	}

}
