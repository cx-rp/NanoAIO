package sneakerbot.updater;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javafx.application.Platform;
import sneakerbot.updater.ui.UpdateDialogController;

public class UpdateFX {
	private URL updateXML;
	private int releaseID;
	private String version;
	private int licenseVersion;
	private URL css;
	
	/**
	 * Creates and initializes an instance of the UpdateFX class.
	 * 
	 * @param updateXML the URL to the XML file describing the updates
	 * @param releaseID the ID of the current release
	 * @param version the human readable version string
	 * @param licenseVersion the version of the license
	 * @param css the css theme
	 */
	public UpdateFX(URL updateXML, int releaseID, String version, int licenseVersion, URL css) {
		this.updateXML = updateXML;
		this.releaseID = releaseID;
		this.version = version;
		this.licenseVersion = licenseVersion;
		this.css = css;
	}
	
	/**
	 * Creates and initializes an instance of the UpdateFX class.
	 * 
	 * @param propertyFile the property file containing the options
	 * @param css the css theme
	 * @throws IOException malformed URL
	 */
	public UpdateFX(Properties propertyFile, URL css) throws IOException {
		this(new URL(propertyFile.getProperty("app.updatefx.url")), 
				Integer.parseInt(propertyFile.getProperty("app.release")), 
				propertyFile.getProperty("app.version"), 
				Integer.parseInt(propertyFile.getProperty("app.licenseVersion")),
				css
		);
	}
	
	/**
	 * Creates and initializes an instance of the UpdateFX class.
	 * 
	 * @param applicationMain the main class of the application, where app-info.properties will be looked for
	 * @throws IOException error reading property or css file
	 */
	public UpdateFX(Class<?> applicationMain) throws IOException {
		this(getPropertiesForApp(applicationMain), getCSSForApp(applicationMain));
	}
	
	private static Properties getPropertiesForApp(Class<?> applicationMain) throws IOException {
		Properties properties = new Properties();
		properties.load(applicationMain.getResourceAsStream("app-info.properties"));
		return properties;
	}
	
	private static URL getCSSForApp(Class<?> applicationMain) throws IOException {
		return applicationMain.getResource("/style.css");
	}
	
	public URL getUpdateXML() {
		return updateXML;
	}

	public int getReleaseID() {
		return releaseID;
	}
	
	public int getLicenseVersion() {
		return licenseVersion;
	}

	public String getVersion() {
		return version;
	}

	/**
	 * Checks for updates and prompts the user to eventually install them.
	 */
	
	public void checkUpdates() {
		System.out.println("CHECKING FOR UPDATES");
		XMLRetrieverService xmlRetriever = new XMLRetrieverService(getUpdateXML());
		
		
		xmlRetriever.valueProperty().addListener((observable, oldValue, application) -> {
			UpdateFinderService service = new UpdateFinderService(application, getReleaseID(), getLicenseVersion());
			service.valueProperty().addListener((obs, oldVal, release) -> {
				Platform.runLater(() -> UpdateDialogController.showUpdateDialog(release, getReleaseID(), getVersion(), getLicenseVersion(), css));
			});
			
			service.start();
		});
		
		xmlRetriever.start();
	}
}
