package sneakerbot.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.swing.text.MaskFormatter;

import java.util.Map.Entry;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import sneakerbot.captcha.TwoCaptcha;
import sneakerbot.loaders.Profile;
import sneakerbot.loaders.Profile.CreditCard;
import sneakerbot.loaders.Profile.ProfileObject;
import sneakerbot.loaders.Profile.Shipping;
import sneakerbot.loaders.Proxy;
import sneakerbot.loaders.Proxy.ProxyObject;

public class SettingsController implements Initializable {

	@Override
	public void initialize(URL location, ResourceBundle resources) {	
		
		for(Entry<String, ProfileObject> profiles : MainController.profiles.entrySet()) 
			profileList.getItems().add(profiles.getKey());
		
		for (Sitekeys key : Sitekeys.values()) 
			sitekeyBox.getItems().add(key.name() + " (" + key.getSitekey() + ")");
		
		sitekeyBox.getSelectionModel().selectFirst();
		
		initTextField();
		
    	stateBox.getItems().setAll(State.values());
    	monthBox.getItems().setAll(Month.values());
    	monthBox.getSelectionModel().select(0);
    	yearBox.getItems().setAll(year); 
    	
		if(MainController.proxies != null) {
			String text = "";
			
			for(ProxyObject proxy : MainController.proxies) 
				text += proxy.toString() + "\n";
					
			proxyTxt.setText(text);
		}

		saveProxyBtn.setOnAction((e) -> saveProxies());
		saveProfileBtn.setOnAction((e) -> saveProfile());
		deleteBtn.setOnAction((e) -> deleteProfile());
		harvesterBtn.setOnAction((e) -> toggleHarvester());	
		captchaWindowBtn.setOnAction((e) -> openCaptchaWindow());	
		profileList.setOnMouseClicked(e -> loadProfile(e));
	}
	
	public void loadProfile(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
        	String name = profileList.getSelectionModel().getSelectedItem();
        	
        	if(name == null || name.isEmpty())
        		return;
        	
        	ProfileObject profile = MainController.profiles.get(name);
        	
        	if(profile == null)
        		return;
        	
        	try {
            	firstnameTxt.setText(profile.getShipping().getFirstName());
            	lastnameTxt.setText(profile.getShipping().getLastName());
            	addressTxt.setText(profile.getShipping().getAddress());
            	address2Txt.setText(profile.getShipping().getAddress2());
            	cityTxt.setText(profile.getShipping().getCity());
            	zipcodeTxt.setText(profile.getShipping().getZip());
            	emailTxt.setText(profile.getEmail());
            	phoneTxt.setText(profile.getShipping().getPhone().replaceAll("-", ""));
            	cardTxt.setText(profile.getCard().getNumber().replaceAll(" ", ""));
            	cvvTxt.setText(profile.getCard().getCode());
            	profilenameTxt.setText(name);
            	
            	if(profile.getShipping().getState() != null)
            		stateBox.getSelectionModel().select(profile.getShipping().getState());
            	
            	if(profile.getCard().getMonth() != null)
            		monthBox.getSelectionModel().select(Month.values()[Integer.parseInt(profile.getCard().getMonth()) - 1]);
            	
            	if(profile.getCard().getYear() != null)
            		yearBox.getSelectionModel().select(profile.getCard().getYear());
        	} catch (Exception e) { System.out.println("Error setting profile text."); e.printStackTrace();}    	       	
        }
	}
	
	
	public void saveProfile() {
		if(profilenameTxt.getText().isEmpty()) 
			return;			

		stateBox.getSelectionModel().getSelectedItem();
		ProfileObject profile = new ProfileObject(emailTxt.getText(), new Shipping(firstnameTxt.getText(), lastnameTxt.getText(), addressTxt.getText(),
				address2Txt.getText(), cityTxt.getText(), stateBox.getSelectionModel().getSelectedItem(), zipcodeTxt.getText(),
				formatPhone(phoneTxt.getText())), new CreditCard(formatCard(cardTxt.getText()), monthBox.getSelectionModel().getSelectedItem().getMonth(),
						yearBox.getSelectionModel().getSelectedItem(), cvvTxt.getText()));
		
		
		if(!profileList.getItems().contains(profilenameTxt.getText()))
			profileList.getItems().add(profilenameTxt.getText());
		
		Profile.save(profilenameTxt.getText(), profile);
	}
	
	public void deleteProfile() {
		if(profileList.getSelectionModel().getSelectedItem() == null) 
			return;
		
		File file = new File("data/profiles/" + profileList.getSelectionModel().getSelectedItem() + ".json");
		
		if(file.exists())
			file.delete();
		
		MainController.profiles.remove(profileList.getSelectionModel().getSelectedItem());
		profileList.getItems().remove(profileList.getSelectionModel().getSelectedItem());
	}

	public void saveProxies() {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/proxies.txt"), StandardCharsets.UTF_8))) {
		    writer.write(proxyTxt.getText().replaceAll("\n", System.lineSeparator()));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}  
		System.out.println("saved!");
		MainController.proxies.clear();
		MainController.proxies = Proxy.load("data/proxies.txt");
	}
	
    public boolean addHost(String host) {
        String osName = System.getProperty("os.name");
        String file = null;

        if (osName.contains("Windows")) 
        	file = System.getenv("WinDir") + "/system32/drivers/etc/hosts";
         else 
        	file = "/etc/hosts";   
        
        try {    
        	
        	if(search(Paths.get(file), host))
        		return true;
        
        	String toAdd = "127.0.0.1\t" + host + "\t#NanoAIO server\r\n\r\n";

        	return Files.write(Paths.get(file), toAdd.getBytes(), StandardOpenOption.APPEND) != null;
		} catch (Exception e) { 
			if(e.getClass().getName().contains("AccessDeniedException")) {
				System.out.println("Unable to modify host file! Please run as administrator.");
			} else
				e.printStackTrace();
		}
        
        return false;
    }
    
    
    public static boolean search(Path path, String search) throws IOException {
    	search = search.trim();
        try {
            for(String line : Files.readAllLines(path)) {
                if (line.contains(search))
                    return true;
            }
        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }
	
	public void toggleHarvester() {
		if(apiKeyTxt.getText().isEmpty()) {
	        Alert alert = new Alert(
	                Alert.AlertType.INFORMATION,
	                "Please enter your 2Captcha API key.",
	                ButtonType.OK);
	        alert.setHeaderText(null);
	        alert.setTitle("Captcha Window");      
	        alert.show();
			return;
		}
		harvesterBtn.setDisable(true);
		
		if(twocaptcha == null) {
			Sitekeys key = Sitekeys.values()[sitekeyBox.getSelectionModel().getSelectedIndex()];
			if(addHost(key.getHost())) {
				twocaptcha = new TwoCaptcha(apiKeyTxt.getText(), key.getSitekey(), key.getWebsite());
				twocaptcha.start();
				harvesterBtn.setText("Stop Harvester");				
			} else 
				showAdminAlert();
		} else {
			if(captchaWindow != null) {
				captchaWindow.close();			
				captchaWindow = null;
			}
			
			twocaptcha.stopHarvester();
			harvesterBtn.setText("Start Harvester");
			twocaptcha = null;
		}
		
		final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(5),
				new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent actionEvent) {
					harvesterBtn.setDisable(false);
				}
		}));
		animation.setCycleCount(1);
		animation.play();
	}
	
	public void openCaptchaWindow() {
		
		if(captchaWindow == null) {
			try {
		        StackPane captcha = new FXMLLoader(getClass().getResource("/captcha.fxml")).load();
		        captchaWindow = new Stage(StageStyle.TRANSPARENT);
		        
		        captcha.setOnMousePressed(new EventHandler<MouseEvent>() {
		            @Override
		            public void handle(MouseEvent event) {
		                xOffset = event.getSceneX();
		                yOffset = event.getSceneY();
		            }
		        });
		         
		         captcha.setOnMouseDragged(new EventHandler<MouseEvent>() {
		            @Override
		            public void handle(MouseEvent event) {
		            	captchaWindow.setX(event.getScreenX() - xOffset);
		            	captchaWindow.setY(event.getScreenY() - yOffset);
		            }
		        });
		         
		         captchaWindow.initStyle(StageStyle.TRANSPARENT);
		         captchaWindow.setScene(new Scene(captcha));  
		         captchaWindow.getScene().getRoot().setStyle("-fx-background-color: transparent;");
		         captchaWindow.getScene().setFill(null);
		         captchaWindow.show();
			} catch (Exception e) { e.getMessage(); }
		}
		/*if(twocaptcha != null) {
			Sitekeys key = Sitekeys.values()[sitekeyBox.getSelectionModel().getSelectedIndex()];
	        Alert alert = new Alert(
	                Alert.AlertType.INFORMATION,
	                "Disabled! Please open http://" + key.getHost() + ":8080 in your browser.",
	                ButtonType.OK);
	        alert.setHeaderText(null);
	        alert.setTitle("Captcha Window");      
	        alert.show();
		}*/
	}
	
	public void initTextField() {
    	cardTxt.textProperty().addListener((o, ov, n) -> {
            if (!n.matches("\\d*"))    	
                cardTxt.setText(n.replaceAll("[^\\d]", ""));
            
            if(n.length() > 16)
            	cardTxt.setText(n.substring(0, 16));
            
    	});
    	
    	cvvTxt.textProperty().addListener((o, ov, n) -> {
            if (!n.matches("\\d*"))    	
            	cvvTxt.setText(n.replaceAll("[^\\d]", ""));
            
            if(n.length() > 3)
            	cvvTxt.setText(n.substring(0, 3));
            
    	});
    	
    	zipcodeTxt.textProperty().addListener((o, ov, n) -> {
            if (!n.matches("\\d*"))    	
            	zipcodeTxt.setText(n.replaceAll("[^\\d]", ""));
            
            if(n.length() > 5)
            	zipcodeTxt.setText(n.substring(0, 5));
            
    	});
		
    	phoneTxt.textProperty().addListener((o, ov, n) -> {
            if (!n.matches("\\d*"))    	
            	phoneTxt.setText(n.replaceAll("[^\\d]", ""));
            
            if(n.length() > 10)
            	phoneTxt.setText(n.substring(0, 10));
            
    	});
	}
	
	public void showAdminAlert() {
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Unable to start the captcha harvester. Please run as administrator.",
                ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Captcha Harvester");      
        alert.show();
	}
	
	public String formatCard(String cardNumber) {
	    if (cardNumber == null) return null;
	    char delimiter = ' ';
	    return cardNumber.replaceAll(".{4}(?!$)", "$0" + delimiter);
	}
	
	public String formatPhone(String phone) {
		try {
			MaskFormatter maskFormatter= new MaskFormatter("###-###-####");
			maskFormatter.setValueContainsLiteralCharacters(false);
			
			return maskFormatter.valueToString(phone);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return phone;
	}
	
    private double xOffset = 0;
    private double yOffset = 0;
	public static Stage captchaWindow;
	public static TwoCaptcha twocaptcha;
	//public static Harvester harvester;
	
    @FXML    
    private ChoiceBox<String> sitekeyBox;
    @FXML    
    private TextField apiKeyTxt;
    @FXML    
    private Button harvesterBtn;
    @FXML    
    private Button captchaWindowBtn;
    
    @FXML    
    private TextArea proxyTxt;
    @FXML    
    private Button saveProxyBtn;
    
    @FXML    
    private TextArea accountTxt;
    @FXML    
    private Button saveAccBtn; 
    @FXML    
    private Button createBtn; 
    
    
    @FXML    
    private ListView<String> profileList;
    @FXML    
    private Button deleteBtn; 
    
    @FXML    
    private Button saveProfileBtn;
    @FXML    
    private TextField firstnameTxt;
    @FXML    
    private TextField lastnameTxt;
    @FXML    
    private TextField addressTxt;
    @FXML    
    private TextField address2Txt;
    @FXML    
    private TextField cityTxt;
    @FXML    
    private ComboBox<State> stateBox;
    @FXML    
    private TextField zipcodeTxt;
    @FXML    
    private TextField emailTxt;
    @FXML    
    private TextField phoneTxt;
    @FXML    
    private TextField cardTxt;
    @FXML    
    private ComboBox<Month> monthBox;
    @FXML    
    private ComboBox<String> yearBox;
    @FXML    
    private TextField cvvTxt;
    @FXML    
    private TextField profilenameTxt;    
	
    public enum State {
        AK("Alaska"),
        AL("Alabama"),
        AR("Arkansas"),
        AZ("Arizona"),
        CA("California"),
        CO("Colorado"),
        CT("Connecticut"),
        DE("Delaware"),
        FL("Florida"),
        GA("Georgia"),
        HI("Hawaii"),
        IA("Iowa"),
        ID("Idaho"),
        IL("Illinois"),
        IN("Indiana"),
        KS("Kansas"),
        KY("Kentucky"),
        LA("Louisiana"),
        MA("Massachusetts"),
        MD("Maryland"),
        ME("Maine"),
        MI("Michigan"),
        MN("Minnesota"),
        MO("Missouri"),
        MS("Mississippi"),
        MT("Montana"),
        NC("North Carolina"),
        ND("North Dakota"),
        NE("Nebraska"),
        NH("New Hampshire"),
        NJ("New Jersey"),
        NM("New Mexico"),
        NV("Nevada"),
        NY("New York"),
        OH("Ohio"),
        OK("Oklahoma"),
        OR("Oregon"),
        PA("Pennsylvania"),
        RI("Rhode Island"),
        SC("South Carolina"),
        SD("South Dakota"),
        TN("Tennessee"),
        TX("Texas"),
        UT("Utah"),
        VA("Virginia"),
        VT("Vermont"),
        WA("Washington"),
        WI("Wisconsin"),
        WV("West Virginia"),
        WY("Wyoming");
    	
    	
    	private String state;
    	
	    private State(String state) {
	    	this.state = state;
	    }
	    
	    public String getFullName() {
	    	return this.state;
	    }
    }
    
    public enum Sitekeys {
        Supreme("6LeWwRkUAAAAAOBsau7KpuC9AV-6J8mhw4AjC3Xz", "nano.supremenewyork.com", "http://www.supremenewyork.com/"),
        Adidas("6LdC0iQUAAAAAOYmRv34KSLDe-7DmQrUSYJH8eB_", "nano.adidas.com", "http://www.adidas.com/");
    	
    	
    	private String sitekey;
    	private String host;
    	private String website;
    	
	    private Sitekeys(String sitekey, String host, String website) {
	    	this.sitekey = sitekey;
	    	this.host = host;
	    }
	    
	    public String getSitekey() {
	    	return this.sitekey;
	    }
	    
	    public String getHost() {
	    	return this.host;
	    }
	    
	    public String getWebsite() {
	    	return this.website;
	    }
    }
    
    public enum Month {
        January("01"),
        Febuary("02"),
        March("03"),
        April("04"),
        May("05"),
        June("06"),
        July("07"),
        August("08"),
        September("09"),
        October("10"),
        November("11"),
        December("12");
    	
    	
    	private String month;
    	
	    private Month(String month) {
	    	this.month = month;
	    }
	    
	    public String getMonth() {
	    	return this.month;
	    }
    }
    private String[] year = new String[] {"18", "19", "20", "21", "22", "23", "24", "25", "26", "27"};
}
