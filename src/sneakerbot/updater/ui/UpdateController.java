package sneakerbot.updater.ui;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import sneakerbot.animation.FadeInUpTransition;
import sneakerbot.animation.FadeOutDownTransition;
import sneakerbot.updater.InstallerService;
import sneakerbot.updater.UpdateDownloadService;
import sneakerbot.updater.model.Release;

public class UpdateController {
    @FXML
    private StackPane rootPane;
	@FXML
	private Label stepLabel;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label progressLabel;
	@FXML
	private Button actionButton;
	@FXML
	private ResourceBundle resources;

	private Release release;
	private UpdateDownloadService service;

	public static void performUpdate(Release release, URL css) {
		try {

			ResourceBundle i18nBundle = ResourceBundle.getBundle("updater.ui.i18n.UpdateProgressDialog");
			
			FXMLLoader loader = new FXMLLoader(UpdateDialogController.class.getResource("/updater/ui/UpdateProgressDialog.fxml"), i18nBundle);
			loader.setBuilderFactory(new JavaFXBuilderFactory());
			Parent page = loader.load();
			UpdateController controller = loader.getController();
			controller.release = release;
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
		stepLabel.setText(resources.getString("label.downloading"));
		service = new UpdateDownloadService(release);

		service.workDoneProperty().addListener((observable, oldValue, newValue) -> {
			progressBar.setProgress(service.getWorkDone() / service.getTotalWork());
			updateProgressLabel();
		});
		
		service.setOnSucceeded((event) -> Platform.runLater(() -> {
            actionButton.setDefaultButton(true);
            actionButton.setOnAction(this::install);
            actionButton.setText(resources.getString("button.install"));
            actionButton.autosize();
            stepLabel.setText(resources.getString("label.downloaded"));
        }));
		
		service.setOnFailed((event) -> Platform.runLater(() -> {
            actionButton.setDefaultButton(true);
            stepLabel.setText(resources.getString("label.downloadfailed"));
        }));
		
		service.start();
	}
	
	private String byte2Str(double bytes) {
		double unit = 1024.0;
		
		if (bytes < unit) {
			return bytes + " B";
		}
		
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "kMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	private void updateProgressLabel() {
		MessageFormat mf = new MessageFormat(resources.getString("label.progress"), resources.getLocale());
		Object[] params = { byte2Str(service.getWorkDone()), byte2Str(service.getTotalWork())};
		
		progressLabel.setText(mf.format(params));
	}
	
	private void close() {			
        new FadeOutDownTransition(rootPane)
        .setOnFinish((e) -> {
        	((Stage) progressLabel.getScene().getWindow()).close();	
        })
        .setDelayTime(Duration.ZERO)
        .setDuration(Duration.millis(300))
        .play();

	}

	@FXML
	public void cancel(ActionEvent event) {
		service.cancel();
		close();
	}

	@FXML
	public void install(ActionEvent event) {
		actionButton.setDisable(true);
		progressBar.setProgress(-1.0);
		progressLabel.setText("");
		stepLabel.setText(resources.getString("label.installing"));
		
		InstallerService installService = new InstallerService(service.getValue());
		
		installService.setOnFailed((evt) -> Platform.runLater(() -> {
            actionButton.setDisable(false);
            actionButton.setOnAction((clickEvent) -> close());
            actionButton.setText(resources.getString("button.cancel"));
            actionButton.autosize();
            progressBar.setProgress(1.0);
            stepLabel.setText(resources.getString("label.installfailed"));
        }));
		
		installService.start();
	}
}