package sneakerbot.updater;

import java.net.URL;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sneakerbot.updater.model.Application;
import sneakerbot.updater.model.Release;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class XMLRetrieverService extends Service<Application> {
	private URL xmlURL;
	
	public XMLRetrieverService(URL xmlURL) {
		this.xmlURL = xmlURL;
	}

	@Override
	protected Task<Application> createTask() {
		System.out.println("DOWNLOADING XML: " + xmlURL.toString());
		
		return new Task<Application>() {

			@Override
			protected Application call() throws Exception {
				JAXBContext ctx = JAXBContext.newInstance(Application.class);
				Unmarshaller um = ctx.createUnmarshaller();
				Application app = (Application) um.unmarshal(xmlURL);
				
				System.out.println("PASSED");
				for (Release release : app.getReleases()) {
					release.setApplication(app);
				}
				
				return app;
			}
		};
	}
}