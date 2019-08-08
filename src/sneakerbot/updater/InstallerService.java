package sneakerbot.updater;

import java.nio.file.Path;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sneakerbot.util.PIDUtil;
import sneakerbot.util.ScriptUtil;

public class InstallerService extends Service<Void> {
	private Path installer;
	public InstallerService(Path installer) {
		this.installer = installer;
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				String[] nameComponents = installer.getFileName().toString().split("\\.");
				
				if (nameComponents.length < 2) {
					throw new IllegalArgumentException("Files without extensions are not supported yet");
				}
				
				String extension = nameComponents[nameComponents.length - 1].toLowerCase();
				
				switch(extension) {
				case "dmg":
					handleDMGInstallation();
					break;
				case "exe":
					handleEXEInstallation();
					break;
				case "msi":
					handleMSIInstallation();
					break;
				default:
					throw new IllegalArgumentException(String.format("installers with extension %s are not supported", extension));
				}
				
				Platform.exit();
				return null;
			}
		};
	}

	private void handleDMGInstallation() throws Exception {
		Path tmpScript = ScriptUtil.copyScript("installdmg.sh");
		new ProcessBuilder("/bin/sh", tmpScript.toAbsolutePath().toString(), installer.toAbsolutePath().toString(), String.format("%d", PIDUtil.getPID())).start();
	}
	
	private void handleEXEInstallation() throws Exception {
		Path tmpScript = ScriptUtil.copyScript("insnan.bat");
		System.out.println("Script path: " + tmpScript.toAbsolutePath().toString() + ", Installer Path: " + installer.toAbsolutePath().toString() + ", Exe Path: " + System.getProperty("user.dir") + "/NanoAIO.exe");
		new ProcessBuilder("cmd", "/c", tmpScript.toAbsolutePath().toString(), installer.toAbsolutePath().toString(), System.getProperty("user.dir") + "/NanoAIO.exe").start();
	     	
		//new ProcessBuilder(installer.toAbsolutePath().toString(), "/SILENT", "/SP-", "/SUPPRESSMSGBOXES").start();
	}
	
	private void handleMSIInstallation() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
