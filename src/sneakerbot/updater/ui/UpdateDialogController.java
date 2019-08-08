package sneakerbot.updater.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import sneakerbot.animation.FadeInUpTransition;
import sneakerbot.animation.FadeOutDownTransition;
import sneakerbot.updater.model.Release;

public class UpdateDialogController {
    @FXML
    private StackPane rootPane;
	@FXML
	private Label infoLabel;
	@FXML
	private ResourceBundle resources;
	
	private Release release;
	private String currentVersion;
	private int currentLicenseVersion;
	private URL css;
	
	public static void showUpdateDialog(Release release, int currentReleaseID, String currentVersion, int currentLicenseVersion, URL css) {
		try {
			ResourceBundle i18nBundle = ResourceBundle.getBundle("updater.ui.i18n.UpdateDialog");
			
			FXMLLoader loader = new FXMLLoader(UpdateDialogController.class.getResource("/updater/ui/UpdateDialog.fxml"), i18nBundle);
			loader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent page = loader.load();
			UpdateDialogController controller = loader.getController();
			controller.release = release;
			controller.currentVersion = currentVersion;
			controller.currentLicenseVersion = currentLicenseVersion;
			controller.css = css;
			controller.initialize();
			
			
	        Scene sceneLogin = new Scene(page);

	        Stage stageLogin = new Stage(StageStyle.TRANSPARENT);
	        
	        stageLogin.initModality(Modality.APPLICATION_MODAL);
	        //stageLogin.initOwner(stage);
	        stageLogin.setScene(sceneLogin);
	        stageLogin.getScene().getRoot().setStyle("-fx-background-color: transparent;");
	        stageLogin.getScene().setFill(null);
	       // stageLogin.setOnHiding((e) -> root.setEffect(null));
	        stageLogin.show();

	        page.setOpacity(0);

	        new FadeInUpTransition(page)
	                .setDelayTime(Duration.millis(200))
	                .setDuration(Duration.millis(500))
	                .play();
			
		} catch (Throwable ex) {
			ex.printStackTrace();
		}		
	}

	private void initialize() {
		release.getApplication().getChangelog();
		
	    Object[] messageArguments = { release.getApplication().getName(), currentVersion, release.getVersion() };
	    MessageFormat formatter = new MessageFormat("");
	    formatter.setLocale(resources.getLocale());
	    
		if (release.getLicenseVersion() != currentLicenseVersion) {
			formatter.applyPattern(resources.getString("infotext.paidupgrade"));
		} else {
			formatter.applyPattern(resources.getString("infotext.freeupgrade"));			
		}
		
		infoLabel.setText(formatter.format(messageArguments));
		infoLabel.autosize();
	}
	
	private void close() {		
        new FadeOutDownTransition(rootPane)
        .setOnFinish((e) -> {
    		((Stage) infoLabel.getScene().getWindow()).close();	
        })
        .setDelayTime(Duration.ZERO)
        .setDuration(Duration.millis(300))
        .play();
		
	}
	
	@FXML
	public void performUpdate(ActionEvent event) {
		UpdateController.performUpdate(release, css);
		close();
	}

	@FXML
	public void cancel(ActionEvent event) {
		close();
	}
	
	@FXML
	public void ignoreVersion(ActionEvent event) {
		//TODO: implement this feature
		close();
	}
}