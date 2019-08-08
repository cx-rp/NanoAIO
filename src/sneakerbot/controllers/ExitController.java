package sneakerbot.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import sneakerbot.animation.FadeOutDownTransition;

public class ExitController implements Initializable {

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		confirmBtn.setOnAction((e) -> exit());
		cancelBtn.setOnAction((e) -> cancel());
	}
	
	
	private void exit() {
    	if(SettingsController.twocaptcha != null) {
    		SettingsController.twocaptcha.stopHarvester();
    		SettingsController.twocaptcha = null;
    	}
    	
    	Platform.exit();
	}
	
	
	private void cancel() {
        new FadeOutDownTransition(rootPane)
        .setOnFinish(e -> ((Stage) cancelBtn.getScene().getWindow()).close())
        .setDelayTime(Duration.ZERO)
        .setDuration(Duration.millis(300))
        .play();
	}
	
	@FXML
	private StackPane rootPane;
	@FXML
	private Button confirmBtn;
	@FXML
	private Button cancelBtn;

}
