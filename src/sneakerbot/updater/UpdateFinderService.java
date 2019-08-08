package sneakerbot.updater;

import java.util.HashMap;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sneakerbot.updater.model.Application;
import sneakerbot.updater.model.Release;

public class UpdateFinderService extends Service<Release> {

	private Application application;
	private int releaseID;
	private int licenseVersion;

	public UpdateFinderService(Application application, int releaseID, int licenseVersion) {
		this.application = application;
		this.releaseID = releaseID;
		this.licenseVersion = licenseVersion;
	}

	@Override
	protected Task<Release> createTask() {
		System.out.println("CHECKING");
		return new Task<Release>() {

			@Override
			protected Release call() throws Exception {
				HashMap<Integer, Release> releaseMap = new HashMap<>();
				Release newestVersion = null;
				
				for (Release release : application.getReleases()) {
					Release currentNewest = releaseMap.get(release.getLicenseVersion());
					
					if (currentNewest == null || (release.getId() > currentNewest.getId())) {
						releaseMap.put(release.getLicenseVersion(), release);
					}
					
					if (newestVersion == null || (release.getId() > newestVersion.getId())) {
						newestVersion = release;
					}
				}
				
				Release newestForThisLicense = releaseMap.get(licenseVersion);
				
				if ((newestForThisLicense != null) && (newestForThisLicense.getId() > releaseID)) {
					System.out.print("newestForThisLicense: " + newestForThisLicense.getId() + ", Release: " + releaseID);
					
					return newestForThisLicense;
				} else if ((newestVersion != null) && (newestVersion.getId() > releaseID)) {
					System.out.print("newestVersion: " + newestForThisLicense.getId() + ", Release: " + releaseID);
					return newestVersion;
				}
							
				System.out.println("NO UPDATE FOUND");
				throw new NoUpdateException();
			}
		};
	}
}