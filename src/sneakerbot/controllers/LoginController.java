package sneakerbot.controllers;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import sneakerbot.MainUI;
import sneakerbot.animation.FadeOutDownTransition;
import sneakerbot.animation.ShakeTransition;
import sneakerbot.updater.UpdateFX;

public class LoginController implements Initializable {
	
	boolean duplicate;
	LoginDetails details;

    @FXML
    private StackPane rootPane;
    @FXML
    private Label responseLabel;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;
    @FXML
    private CheckBox rememberCkBox;

    @FXML
    void close(ActionEvent event) {
    	save(new LoginDetails(username.getText(), password.getText(), rememberCkBox.isSelected()));
        new FadeOutDownTransition(rootPane)
                .setOnFinish((e) -> {
                    ((Stage) username.getScene().getWindow()).close();
                    Platform.exit();
                })
                .setDelayTime(Duration.ZERO)
                .setDuration(Duration.millis(300))
                .play();

    }

    @FXML
    void help(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Help not available", ButtonType.OK);
        alert.setTitle("Help");
        alert.setHeaderText("Help");
        alert.show();
    }
    
    Thread loginThread = null;

    @FXML
    void login(ActionEvent event) {
    	if(loginThread != null && loginThread.isAlive()) 
    		return;
    	
    	
    	loginThread = new Thread(() -> {
            username.setId(null);
            password.setId(null);
            
            username.setDisable(true);
            password.setDisable(true);
            
            boolean validate = validate(username.getText(), password.getText());
            if (validate/*username.getText().equalsIgnoreCase("") && password.getText().equals("")*/) {
            	save(new LoginDetails(username.getText(), password.getText(), rememberCkBox.isSelected()));
                new FadeOutDownTransition(rootPane)
                        .setOnFinish(e -> ((Stage) username.getScene().getWindow()).close())
                        .setDelayTime(Duration.ZERO)
                        .setDuration(Duration.millis(300))
                        .play();
                
                UpdateFX updater = null;
				try {
					updater = new UpdateFX(MainUI.class);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                updater.checkUpdates();

            } else {

                new ShakeTransition(rootPane)
                        .setDelayTime(Duration.ZERO)
                        .setDuration(Duration.millis(800))
                        .play();

                /*if (!username.getText().equalsIgnoreCase("")) {
                    username.setId("text-field-validation-error");
                }

                if (!password.getText().equals("")) {
                    password.setId("text-field-validation-error");
                }*/

            }
            username.setDisable(false);
            password.setDisable(false);
    	});
    	loginThread.start();
    }
    
    private boolean validate(String user, String password) { 	
    	if(user.isEmpty() || password.isEmpty()) {
    		Platform.runLater(() -> responseLabel.setText("Login details cannot be blank!"));
    		return false;
    	}
    	
		try
		{
    		Platform.runLater(() -> responseLabel.setText("Connecting to auth server.."));
			int port = 7412;
			final double currVersion = 0.05;
			Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), port);
			try {
				socket.setSoTimeout(6000);
				socket.setTcpNoDelay(true);
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());			
				double version = in.readDouble();
				int flags = in.readInt();
				
				if(version != currVersion) {
					System.out.println("version mismatch! OLD: " + currVersion + ", NEW: " + version);
		    		Platform.runLater(() -> responseLabel.setText("Please update your client."));
		    		if(flags == 1) {
			    		out.writeUTF("DerpOS");
			    		out.writeInt(0);
		    		}
		    		
					out.writeUTF(user + ":" + password);
					out.flush();
					
					in.readBoolean();
					return false;					
				}
				
				out.writeUTF(user + ":" + password);
				out.flush();
				
				boolean success = in.readBoolean();
				
				if(success)
		    		Platform.runLater(() -> responseLabel.setText("Login success!"));
				else
		    		Platform.runLater(() -> responseLabel.setText("Login failed! Try again.."));
				
				return success;
				
			} finally { socket.close(); }
		}
		catch (Exception ex) { 
			if(ex.getMessage().contains("Connection timed out") || ex.getMessage().contains("Connection refused:")) {
	    		Platform.runLater(() -> responseLabel.setText("Error connecting to auth server.."));
			}
			System.out.println(ex);
		}
    	
		return false;
    }

    @FXML
    void minimize(ActionEvent event) {
        ((Stage) ((Stage) username.getScene().getWindow()).getOwner()).setIconified(true);
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	details = load("data/login.json");
    	duplicate = false;
    	
    	if(details.rememberMe()) {
    		username.setText(details.getUsername());
    		password.setText(details.getPassword());
    		rememberCkBox.setSelected(details.rememberMe());
    	}
    }
    
    public void save(LoginDetails details) {
		try (FileWriter writer = new FileWriter("data/login.json")) {
			new GsonBuilder().enableComplexMapKeySerialization()
				.setPrettyPrinting().create().toJson(details, writer);
		} catch (IOException e) { e.printStackTrace(); }	  		
    }
    
	public LoginDetails load(String name) {
        File file = new File(name); 
        
        if(!file.exists()) {
        	System.out.println(name + " does not exist; One has been created for you.");
        	return create(name);
        }
        
		try {
			return new GsonBuilder().create().fromJson(new FileReader(name), LoginDetails.class);
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public LoginDetails create(String name) {      
		LoginDetails temp =  new LoginDetails("", "", true);
		try (FileWriter writer = new FileWriter(name)) {
			new GsonBuilder().enableComplexMapKeySerialization()
				.setPrettyPrinting().create().toJson(temp, writer);
		} catch (IOException e) { e.printStackTrace(); }	
		
		return temp;
	}
    
	public static class LoginDetails {

		public LoginDetails(String username, String password, boolean remember) {
			super();
			this.username = username;
			this.password = password;
			this.remember = remember;
		}
		
		public String getUsername() {
			return username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public boolean rememberMe() {
			return remember;
		}
				
		@Override
		public String toString() {
			return "LoginDetails [username=" + username + ", password=" + password + ", remember=" + remember + "]";
		}

		private String username;
		private String password;
		private boolean remember;
		
	}
}
