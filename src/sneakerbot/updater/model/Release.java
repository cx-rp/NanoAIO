package sneakerbot.updater.model;

import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


@XmlType(propOrder = { "id", "version", "licenseVersion", "releaseDate", "binaries" })
public class Release {
	private int id;	
	private String version;	
	private int licenseVersion;	
	private Date releaseDate;
	private ArrayList<Binary> binaries;
	private Application application;
	
	public Release() {
		binaries = new ArrayList<>();
	}

	@XmlAttribute(required = true)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@XmlAttribute(required = true)
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@XmlAttribute
	public int getLicenseVersion() {
		return licenseVersion;
	}

	public void setLicenseVersion(int licenseVersion) {
		this.licenseVersion = licenseVersion;
	}

	@XmlAttribute
	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	@XmlElement(name = "binary")
	public ArrayList<Binary> getBinaries() {
		return binaries;
	}
	
	@XmlTransient
	public Application getApplication() {
		return application;
	}
	
	public void setApplication(Application application) {
		this.application = application;
	}
}