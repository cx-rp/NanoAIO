package sneakerbot.controllers.tasks;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
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
import sneakerbot.tasks.TaskConfig;

public class AdidasController implements Initializable {
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
    	tasks = new ArrayList<TaskConfig<String>>();
		
    	//taskIdCol.setCellValueFactory(new PropertyValueFactory<TaskConfig, Integer>("id"));
    	taskTypeCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, TaskType>("type"));
    	localeCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("locale"));
    	skuCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("sku"));
    	sizeCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("sizes"));
    	statusCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("status"));
    	splashCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, Boolean>("splash"));
    	proxyCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("proxy"));
    	
    	addColorToRows(new ArrayList<TableColumn<TaskConfig<String>, String>>(Arrays.asList(localeCol, skuCol, 
    			sizeCol, statusCol, proxyCol)));
		
      	taskAmountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));	
    	taskAmountSpinner.focusedProperty().addListener((s, ov, nv) -> {
    	    if (nv) return;
    	    
    	    commitEditorText(taskAmountSpinner);
    	});
    	
    	createButtons();

    	createTaskBtn.setOnAction((e) -> createTasks());
    	startAllBtn.setOnAction((e) -> startTasks());
    	stopAllBtn.setOnAction((e) -> stopTasks());
    	
    	
		System.out.println("Adidas Controller Loaded!");
		
	}
	
	public <T> void addColorToRows(ArrayList<TableColumn<TaskConfig<String>, T>> cellList) {  
		Paint color = Color.WHITE;
		
		for(TableColumn<TaskConfig<String>, T> cellFactory : cellList) {
			cellFactory.setCellFactory(column -> {
	    		return new TableCell<TaskConfig<String>, T>() {
	                @Override
	                public void updateItem(T item, boolean empty) {
	                    super.updateItem(item, empty);

	                    if (!isEmpty() && item != null) {
	   	                 
	                        if(((String) item).contains("@gre@")) {
	                        	setTextFill(Color.GREEN);
	                        	setText(((String) item).replaceAll("@gre@", ""));
	                        } else if(((String) item).contains("@red@")) {
	                        	setTextFill(Color.RED);
	                        	setText(((String) item).replaceAll("@red@", ""));
	                        } else if(((String) item).contains("@yel@")) {
	                        	setTextFill(Color.YELLOW);
	                        	setText(((String) item).replaceAll("@yel@", ""));
	                        } else if(((String) item).contains("@org@")) {
	                        	setTextFill(Color.ORANGE);
	                        	setText(((String) item).replaceAll("@org@", ""));
	                        } else {
	                            setTextFill(color);
	                            setText((String) item);
	                        }
	                    } else 
                            setText("");
	                    
	                }
	            };
	        });
		}
	}
	
    public void createTasks() {
    	if(itemTxt.getText().isEmpty() || sizesTxt.getText().isEmpty())
    		return;
    	
    	int count = taskAmountSpinner.getValue();
    	
    	if(count < 1)
    		return;
    	
    	for(int amt = 0; amt < count; amt++) {
    		TaskConfig<String> config = new TaskConfig<String>(TaskType.ADIDAS, table.getItems().size() + 1, MainController.getRandomProxy(), "Ready!", "US",
        				itemTxt.getText(), sizesTxt.getText(), splashCheckBox.isSelected(), manualCheckBox.isSelected());
	

    		tasks.add(config);   		
    		table.getItems().add(config);   		
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
            				} else 
            					task.cancel();
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
    		if(t.isRunning())
    			t.cancel();
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
    private TextField itemTxt;
    @FXML
    private TextField sizesTxt;   
    @FXML  
    private CheckBox splashCheckBox;
    @FXML  
    private CheckBox manualCheckBox;
	
    @FXML    
    public TableView<TaskConfig<String>> table;       
    @FXML 
    private TableColumn<TaskConfig<String>, TaskType> taskTypeCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> localeCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> skuCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> sizeCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> statusCol;
    @FXML 
    private TableColumn<TaskConfig<String>, Boolean> splashCol;
    @FXML 
    private TableColumn<TaskConfig<String>, String> proxyCol;
    @FXML 
    private TableColumn<TaskConfig<String>, Void> actionCol;

}
