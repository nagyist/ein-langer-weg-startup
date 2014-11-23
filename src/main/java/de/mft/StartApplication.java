package de.mft;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

public class StartApplication extends WebApplication {

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return StartPage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
		mount(new MountedMapperWithoutPageComponentInfo("/startseite/", StartPage.class));
		mount(new MountedMapperWithoutPageComponentInfo("/benutzereinleitung/", UserGuides.class));
	}
}
