package sneakerbot.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import sneakerbot.animation.FadeInTransition;
import sneakerbot.animation.FadeInUpTransition;
import sneakerbot.captcha.Harvester.CaptchaResponse;
import sneakerbot.loaders.Profile;
import sneakerbot.loaders.Profile.ProfileObject;
import sneakerbot.loaders.Proxy;
import sneakerbot.loaders.Proxy.ProxyObject;
import sneakerbot.util.WinRegistry;

public class MainController implements Initializable {
	
    @FXML
    private AnchorPane mainPanel;

    @FXML
    private StackPane rootPane;
    
    @FXML
    private Button btnTaskView;
    
    @FXML
    private Button btnProfile;

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnExit;

    private HashMap<String, AnchorPane> panels;
	static ArrayList<ProxyObject> proxies;
	static ArrayList<ProxyObject> usedProxies;
	public static List<CaptchaResponse> captchas;
	public static HashMap<String, ProfileObject> profiles;
    public static HashMap<String, AnchorPane> configPanels;
    public static String CHROME_PATH = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	panels = new HashMap<String, AnchorPane>();
    	configPanels = new HashMap<String, AnchorPane>();
		proxies = Proxy.load("data/proxies.txt");
		usedProxies = new ArrayList<ProxyObject>();
		captchas = new ArrayList<CaptchaResponse>();
		profiles = Profile.load();
		CHROME_PATH = getChromePath();
		
    	String[] anchorNames = new String[] { "task_view.fxml", "settings.fxml" };
		new Thread(() -> {
		    synchronized(panels) {
				for(String name : anchorNames) {
					try {					
						panels.put(name, FXMLLoader.load(getClass().getResource("/" + name)));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				Platform.runLater(() -> loadAnchor("task_view.fxml"));
				
		    }
		}).start();

		btnProfile.setOnAction((e) -> {
        	
        });        
        btnTaskView.setOnAction((e) -> loadAnchor("task_view.fxml"));
        btnSettings.setOnAction((e) -> loadAnchor("settings.fxml"));
        btnExit.setOnAction((e) -> exit());
        
    }
    
	public static ProxyObject getRandomProxy() {
		int proxyCount = proxies.size();
		int usedCount = usedProxies.size();
		
		if(proxyCount == 0 && usedCount == 0)
			return null; 
		
		int index = new Random().nextInt(proxyCount != 0 ? proxyCount : usedCount);
		if(proxyCount != 0) {
			ProxyObject proxy = proxies.remove(index);
			usedProxies.add(proxy);
			return proxy;
		} else 
			return usedProxies.get(index);

	}
	
    public String getChromePath() {
		try {
			return WinRegistry.readString(
				    WinRegistry.HKEY_LOCAL_MACHINE,                             //HKEY
				   "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe",           //Key
				   "Path");
		} catch (Exception e) { System.out.println("Chrome path not found. -> " + e.getMessage()); }   
		
		return null;
    }
    
    public void loadAnchor(String name) {
    	try {
    		AnchorPane newPanel = panels.get(name); 		
            new FadeInTransition(mainPanel)
            .setDelayTime(Duration.millis(0))
            .setDuration(Duration.millis(300))
            .play();
            mainPanel.getChildren().setAll(newPanel);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}  	
    }

    private void exit() {
    	try {
            StackPane rootLogin = new FXMLLoader(getClass().getResource("/exit.fxml")).load();
            Scene sceneLogin = new Scene(rootLogin);

            Stage stageLogin = new Stage(StageStyle.TRANSPARENT);
            
            stageLogin.initModality(Modality.APPLICATION_MODAL);
            stageLogin.initOwner(btnExit.getScene().getWindow());
            stageLogin.setScene(sceneLogin);
            stageLogin.getScene().getRoot().setStyle("-fx-background-color: transparent;");
            stageLogin.getScene().setFill(null);
            stageLogin.setOnHiding((e) -> btnExit.getScene().getRoot().setEffect(null));
            stageLogin.show();

            rootLogin.setOpacity(0);

            new FadeInUpTransition(rootLogin)
                    .setDelayTime(Duration.millis(200))
                    .setDuration(Duration.millis(500))
                    .play();
    	} catch (Exception e) { e.printStackTrace(); }
    	
        /*Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to exit?",
                ButtonType.YES, ButtonType.NO);

        alert.setHeaderText("Confirm exit.");

        alert.showAndWait()
                .ifPresent((btn) -> {
                    if (btn.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                    	if(SettingsController.twocaptcha != null) {
                    		SettingsController.twocaptcha.stopHarvester();
                    		SettingsController.twocaptcha = null;
                    	}
                        ((Stage) btnExit.getScene().getWindow()).close();
                        Platform.exit();
                    }
                });*/
    }
}
