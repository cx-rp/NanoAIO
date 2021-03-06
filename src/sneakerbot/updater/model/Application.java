package sneakerbot.updater.model;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = { "name", "licenses", "changelog", "releases" })
public class Application {
	private String name;	
	private URL licenses;
	private URL changelog;	
	private ArrayList<Release> releases;
	
	public Application() {
		releases = new ArrayList<>();
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public URL getLicenses() {
		return licenses;
	}

	public void setLicenses(URL licenses) {
		this.licenses = licenses;
	}

	@XmlAttribute
	public URL getChangelog() {
		return changelog;
	}

	public void setChangelog(URL changelog) {
		this.changelog = changelog;
	}

  @XmlElement(name = "release")
	public ArrayList<Release> getReleases() {
		return releases;
	}
}