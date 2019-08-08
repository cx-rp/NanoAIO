package sneakerbot.controllers;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.ResourceBundle;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import sneakerbot.animation.FadeOutDownTransition;

public class CaptchaController implements Initializable {

	@Override
	public void initialize(URL location, ResourceBundle resources) {	
		
		titleLabel.setText("DISABLED!");
		final WebEngine engine = webview.getEngine();
		
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		webview.setContextMenuEnabled(false);
		engine.setUserAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
		    public boolean verify(String hostname, SSLSession session) {
		        return true;
		    }
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid); 
		
		//engine.setUserAgent("foo\nHeader: value");
		
		webview.setCache(true);
		engine.setJavaScriptEnabled(true);
		
		//engine.load("http://dev.supremenewyork.com:8080");
		//engine.loadContent(load("6LeWwRkUAAAAAOBsau7KpuC9AV-6J8mhw4AjC3Xz", "http://www.supremenewyork.com/"));
		engine.loadContent(load());
		//engine.load("http://dev.supremenewyork.com:8080");
		//engine.load("https://patrickhlauke.github.io/recaptcha/");
		
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("app", new CaptchaResponse());
	}
	
	public String load(String key, String host) {
		return "<html>\r\n\t                        <style>\r\n\t                        body\r\n\t                        {{\r\n\t                            background-color: #0B132B\r\n\t                        }}\r\n\t                        </style>\r\n                            <script>function _submit(captcha){{ app.send(captcha, '" + host + "'); }};</script>\r\n\t                        <body onclick='grecaptcha.execute();' onload='grecaptcha.execute();'>\r\n                            <div class='g-recaptcha'\r\n                                  data-sitekey='" + key + "'\r\n                                  data-callback='_submit'\r\n                                  data-size='invisible'\r\n\t\t                          data-theme='dark'>\r\n                            </div>\r\n\r\n                            <script src='https://www.google.com/recaptcha/api.js' async defer></script>\r\n                        </html>";
	}
	
	public String load() {
		return "<!DOCTYPE html>\r\n                            <html lang='en' >\r\n\r\n                            <head>\r\n                                <meta charset='UTF-8'>\r\n                                <title>Loading circle</title>\r\n\r\n                            </head>\r\n                            <style>\r\n\r\n                            body{\r\n                                background-color: #0B132B;\r\n                            }\r\n                            #loader-page {\r\n                                position: fixed;\r\n                                top: 0;\r\n                                left: 0;\r\n                                height: 100%;\r\n                                width: 100%;\r\n                                background-color: transparent;\r\n                            }\r\n\r\n                            .loader-name {\r\n                                position: absolute;\r\n                                top: 50%;\r\n                                left: 50%;\r\n                                margin-top: -10px;\r\n                                margin-left: -52px;\r\n                                font-size: 125%;\r\n                                font-family: 'Montserrat', sans-serif;\r\n                                text-transform: uppercase;\r\n                                letter-spacing: 0.1em;\r\n                                color: #fefefe;\r\n                            }\r\n\r\n                            .loader-circle {\r\n                                width: 180px;\r\n                                height: 180px;\r\n                                -webkit-box-sizing: border-box;\r\n                                        box-sizing: border-box;\r\n                                position: fixed;\r\n                                top: 50%;\r\n                                left: 50%;\r\n                                border-top: 5px solid #fefefe;\r\n                                border-bottom: 2px solid transparent;\r\n                                border-left: 2px solid transparent;\r\n                                border-right: 2px solid transparent;\r\n                                border-radius: 50%;\r\n                                margin-top: -90px;\r\n                                margin-left: -90px;\r\n                                -webkit-animation: loader 1s infinite linear;\r\n                                        animation: loader 1s infinite linear;\r\n                            }\r\n\r\n                            @-webkit-keyframes loader {\r\n                                from {\r\n                                -webkit-transform: rotate(0deg);\r\n                                        transform: rotate(0deg);\r\n                                }\r\n                                to {\r\n                                -webkit-transform: rotate(360deg);\r\n                                        transform: rotate(360deg);\r\n                                }\r\n                            }\r\n\r\n                            @keyframes loader {\r\n                                from {\r\n                                -webkit-transform: rotate(0deg);\r\n                                        transform: rotate(0deg);\r\n                                }\r\n                                to {\r\n                                -webkit-transform: rotate(360deg);\r\n                                        transform: rotate(360deg);\r\n                                }\r\n                            }\r\n\r\n                            </style>\r\n                            <body>\r\n\r\n\t                            <div class='loader-name'>Waiting</div>\r\n\t                            <div class='loader-circle'></div>\r\n                            </div>\r\n\r\n                            </body>\r\n\r\n                            </html>\r\n                            ";
	}
	
    @FXML
    void close(ActionEvent event) {
        new FadeOutDownTransition(rootPane)
                .setOnFinish((e) -> {
                    //((Stage) webview.getScene().getWindow()).close();
    				SettingsController.captchaWindow.close();
    				SettingsController.captchaWindow = null;
                })
                .setDelayTime(Duration.ZERO)
                .setDuration(Duration.millis(300))
                .play();
    }
	
    @FXML
    void minimize(ActionEvent event) {
        new FadeOutDownTransition(rootPane)
        .setOnFinish((e) -> {
           // ((Stage) ((Stage) webview.getScene().getWindow())).setIconified(true);
			SettingsController.captchaWindow.setIconified(true);
        })
        .setDelayTime(Duration.ZERO)
        .setDuration(Duration.millis(300))
        .play();
    }

    @FXML
    private WebView webview;
    
    @FXML
    private StackPane rootPane;
    
    @FXML
    private Label titleLabel;
    
    public class CaptchaResponse {
    	
    	public void send(Object captcha, Object host) {
    		System.out.print(captcha.toString() + " - " + host.toString());
    	}
    }
}
