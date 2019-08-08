package sneakerbot.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class TaskController implements Initializable {
	
	public enum TaskType {
	    ADIDAS,
	    SHOPIFY,
	    SUPREME
	} 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	AnchorPane panel = null;
    	String[] data = new String[] {"Adidas", "Shopify", "Supreme"};
    	
    	for(String tabName : data) {
    		try {
    			panel = new FXMLLoader(getClass().getResource("/config/" + tabName.toLowerCase() + ".fxml")).load();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		if(panel != null) {
    			Tab t = new Tab(tabName);
    			t.setContent(panel);
    			taskPane.getTabs().add(t);
    		} else
    			System.out.println(tabName + " fxml could not be loaded!");
    		
    		panel = null;
    	}
    }
    
    @FXML
    private AnchorPane mainPanel;   
    @FXML
    private TabPane taskPane;        
       
    @FXML
    private AnchorPane taskTypePane;
    
    @FXML
    private ChoiceBox<TaskType> taskChoiceBox;   
    @FXML    
    private Spinner<Integer> taskAmountSpinner;  
    @FXML    
    private Button createTaskBtn;
    @FXML    
    private Button startAllBtn;
    @FXML    
    private Button stopAllBtn;
    
    @FXML  
    private ChoiceBox<String> profileBox;
    @FXML
    private TextField earlyLinkTxt; 
}
