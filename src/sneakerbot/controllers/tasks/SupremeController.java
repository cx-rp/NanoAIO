package sneakerbot.controllers.tasks;

import java.net.URL;
import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.Random;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import javafx.util.StringConverter;
import sneakerbot.controllers.MainController;
import sneakerbot.controllers.TaskController.TaskType;
import sneakerbot.loaders.Profile.ProfileObject;
import sneakerbot.tasks.TaskConfig;

public class SupremeController implements Initializable {

	@Override
	public void initialize(URL url, ResourceBundle rb) {
    	tasks = new ArrayList<TaskConfig<String>>();
		
    	refreshProfiles();	
    	
    	releaseTimeTxt.setText(grabThursday());
		
    	table.setPlaceholder(new Label(""));
    	sizeBox.getItems().addAll(new String[] {"Random (S-XL)", "Random (8.0-13.0)", "N/A", "Small", "Medium", "Large", "XLarge",
    			"8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "13"});
    	sizeBox.getSelectionModel().selectFirst();
    	
    	categoryBox.getItems().addAll(new String[] {"Jackets", "Shirts", "Tops/Sweaters", "Sweatshirts", "Pants", "Shorts",
    			"T-Shirts", "Hats", "Bags", "Accessories", "Skate", "Shoes"});
    	categoryBox.getSelectionModel().selectFirst();

    	productCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("keyword"));
    	sizeCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("size"));
    	colorCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("color"));  	
    	profileCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("profileName"));  
    	categoryCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("category"));  
    	proxyCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("proxy"));
    	statusCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("status"));   	
    	
    	addColorToRows(new ArrayList<TableColumn<TaskConfig<String>, String>>(Arrays.asList(productCol, sizeCol, 
    			colorCol, profileCol, categoryCol, proxyCol, statusCol)));
		
      	taskAmountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));	
    	taskAmountSpinner.focusedProperty().addListener((s, ov, nv) -> {
    	    if (nv) return;
    	    
    	    commitEditorText(taskAmountSpinner);
    	});
    	
    	createButtons();

    	createTaskBtn.setOnAction((e) -> createTasks());
    	startAllBtn.setOnAction((e) -> startTasks());
    	stopAllBtn.setOnAction((e) -> stopTasks());
    	refreshBtn.setOnAction((e) -> refreshProfiles());
		System.out.println("Supreme Controller loaded!");
	}
	
	public void addColorToRows(ArrayList<TableColumn<TaskConfig<String>, String>> cellList) {  
		Paint color = Color.WHITE;
		
		for(TableColumn<TaskConfig<String>, String> cellFactory : cellList) {
			cellFactory.setCellFactory(column -> {
	    		return new TableCell<TaskConfig<String>, String>() {
	                @Override
	                public void updateItem(String item, boolean empty) {
	                    super.updateItem(item, empty);
	                    
	                    setAlignment(Pos.CENTER);
	                    if (!isEmpty()) {
	                 
	                        if(item.contains("@gre@")) {
	                        	setTextFill(Color.GREEN);
	                        	setText(item.replaceAll("@gre@", ""));
	                        } else if(item.contains("@red@")) {
	                        	setTextFill(Color.RED);
	                        	setText(item.replaceAll("@red@", ""));
	                        } else if(item.contains("@yel@")) {
	                        	setTextFill(Color.YELLOW);
	                        	setText(item.replaceAll("@yel@", ""));
	                        } else if(item.contains("@gold@")) {
	                        	setTextFill(Color.GOLD);
	                        	setText(item.replaceAll("@gold@", ""));
	                        } else if(item.contains("@org@")) {
	                        	setTextFill(Color.ORANGE);
	                        	setText(item.replaceAll("@org@", ""));
	                        } else if(item.contains("@dorg@")) {
	                        	setTextFill(Color.DARKORANGE);
	                        	setText(item.replaceAll("@dorg@", ""));
	                        } else {
	                            setTextFill(color);
	                            setText(item);
	                        }
	                    } else 
                            setText("");
	                    
	                }
	            };
	        });
		}
	}
	
    public void createTasks() {
    	if(keywordTxt.getText().isEmpty() || profileBox.getSelectionModel().getSelectedIndex() == -1)
    		return;
    	
    	int count = taskAmountSpinner.getValue();
    	
    	if(count < 1)
    		return;
    	
    	for(int amt = 0; amt < count; amt++) {
    		String size = sizeBox.getSelectionModel().getSelectedItem();
    		if(size.equalsIgnoreCase("Random (S-XL)")) {
    			String[] temp = new String[] { "Small", "Medium", "Large", "XLarge" };
    			int index = new Random().nextInt(temp.length);
    			size = temp[index];
    		} else if(size.equalsIgnoreCase("Random (8.0-13.0)")) {
    			String[] temp = new String[] { "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "13" };
    			int index = new Random().nextInt(temp.length);
    			size = temp[index];
    		}
    		
    		TaskConfig<String> config = new TaskConfig<String>(TaskType.SUPREME, table.getItems().size() + 1, MainController.getRandomProxy(), "Ready!",
    				keywordTxt.getText(), size, colorTxt.getText().isEmpty() ? "N/A" : colorTxt.getText(), categoryBox.getSelectionModel().getSelectedItem(),
    						profileBox.getItems().size() == 0 ? "No profiles" : profileBox.getSelectionModel().getSelectedItem(), 
    								releaseTimeTxt.getText().isEmpty() ? "Manual" : releaseTimeTxt.getText());
    		
    		tasks.add(config);   		
    		table.getItems().add(config);   
    		
    		//config.setOnSucceeded(event -> event.getTarget().);
    	}
    }
    
    public void createButtons() {
    	actionCol.setCellFactory(new Callback<TableColumn<TaskConfig<String>, Void>, TableCell<TaskConfig<String>, Void>>() {
    		@Override
    		public TableCell<TaskConfig<String>, Void> call(TableColumn<TaskConfig<String>, Void> column) {
                final TableCell<TaskConfig<String>, Void> cell = new TableCell<TaskConfig<String>, Void>() {
                    
                	final Button addButton = new Button();
                	final Button stopButton = new Button();
                	final HBox pane = new HBox();
                	final DoubleProperty buttonY = new SimpleDoubleProperty();
                    
                    {
            			//pane.setSpacing(5);
            			pane.getChildren().addAll(addButton, stopButton);
            			pane.setAlignment(Pos.CENTER);
            	        
            			//addButton.setStyle("-fx-background-color: #4D6B53");
            			addButton.setPadding(Insets.EMPTY);
            			addButton.setStyle("-fx-graphic: url('icons/start.png');");
            			addButton.getStyleClass().add("btn-menu");
            			addButton.setOnMousePressed((e) -> buttonY.set(e.getScreenY()));
            	        
            			addButton.setOnAction((e) -> {
            				TaskConfig<String> task = tasks.get(getTableRow().getIndex());
            				
               				//System.out.println("index: " + getTableRow().getIndex() + " Task running: " + task.isRunning()); 
               				
            		    	if(!stopAllBtn.getText().contains("Stop")) 
            		        	stopAllBtn.setText("Stop All Tasks");
               				
            				if(task.isRunning()) 
            					return;
            				
            				if(task.getState() == Worker.State.CANCELLED || task.getState() == Worker.State.FAILED || task.getState() == Worker.State.SUCCEEDED)
            					task.reset();
            				
            				task.start();
            				
            			});
            			
            			//removeButton.setStyle("-fx-background-color: #4f0000");
            			stopButton.setPadding(Insets.EMPTY);
            			stopButton.setStyle("-fx-graphic: url('icons/stop.png');");
            			stopButton.getStyleClass().add("btn-menu");
            			stopButton.setOnMousePressed((e) -> buttonY.set(e.getScreenY()));
            	      
            			stopButton.setOnAction((e) -> {
            				int index = getTableRow().getIndex();
            				TaskConfig<String> task = tasks.get(index); 
            				
            				if(!task.isRunning()) {
            					task = tasks.remove(index);    
                				table.getItems().remove(index);
            				} else {
            					task.cancel();
            					task.reset();
            				}
            			});
                    }
                    
            		@Override
            		protected void updateItem(Void item, boolean empty) {
            			super.updateItem(item, empty);
            			if (!empty) {
            				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            				setGraphic(pane);
            			} else {
            				setGraphic(null);
            			}
            		}
                };                
                return cell;
    		}   		
    	});
    }
    
    public void startTasks() {
    	if(tasks.isEmpty())
    		return;
    	
    	tasks.stream().forEach(t -> {
			if(t.isRunning()) 
				return;
			
			if(t.getState() == Worker.State.CANCELLED || t.getState() == Worker.State.FAILED || t.getState() == Worker.State.SUCCEEDED)
				t.reset();
    				
    		t.start();
    	});
    	
    	stopAllBtn.setText("Stop All Tasks");
    }
    
    public void stopTasks() { 		
    	if(tasks.isEmpty())
    		return;
    	
    	tasks.stream().forEach(t -> {
    		if(t.isRunning()) {
    			t.cancel();
				t.reset();
    		}
    	});
    	
    	if(!stopAllBtn.getText().contains("Stop")) {
    		tasks.clear();
        	table.getItems().clear();
    	} else
    		stopAllBtn.setText("Remove All Tasks");  
    }
    
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }
    
    public void refreshProfiles() {
    	profileBox.getItems().clear();
    	
		for(Entry<String, ProfileObject> profiles : MainController.profiles.entrySet()) 
			profileBox.getItems().add(profiles.getKey());
    }
    
    public String grabThursday() {
    	int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    	LocalDate currThurs = dow == Calendar.THURSDAY ? LocalDate.now() : LocalDate.now().with( TemporalAdjusters.next( DayOfWeek.THURSDAY ) ) ;
    	
    	String month = new DateFormatSymbols().getShortMonths()[currThurs.getMonth().getValue() - 1];
    	String day = Integer.toString(currThurs.getDayOfMonth());
    	String year = Integer.toString(currThurs.getYear());
    	
    	return "Thu, " + day + " " + month + " " + year + " 15:00:00 GMT";
    }
    
	public static ArrayList<TaskConfig<String>> tasks; 
	
    @FXML    
    private Spinner<Integer> taskAmountSpinner;  
    @FXML    
    private Button createTaskBtn;
    @FXML    
    private Button startAllBtn;
    @FXML    
    private Button stopAllBtn;

	@FXML
	private TextField keywordTxt;
	@FXML
	private ChoiceBox<String> sizeBox;
	@FXML
	private TextField colorTxt;
	@FXML
	private TextField releaseTimeTxt;
	@FXML
	private ChoiceBox<String> profileBox;
	@FXML
	private ChoiceBox<String> categoryBox;
	@FXML
	private CheckBox browserCkBox;	
	@FXML
	private CheckBox headlessCkBox;	
	@FXML
	private Button refreshBtn;	
		
    @FXML    
    public TableView<TaskConfig<String>> table;       
    @FXML 
    private TableColumn<TaskConfig<String>, String> productCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> sizeCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> colorCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> profileCol;
    @FXML 
    private TableColumn<TaskConfig<String>, String> categoryCol;
    @FXML 
    private TableColumn<TaskConfig<String>, String> proxyCol;
    @FXML 
    private TableColumn<TaskConfig<String>, String> statusCol;
    @FXML 
    private TableColumn<TaskConfig<String>, Void> actionCol;
}
