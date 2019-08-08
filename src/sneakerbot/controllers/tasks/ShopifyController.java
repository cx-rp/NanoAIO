package sneakerbot.controllers.tasks;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import sneakerbot.loaders.Profile.ProfileObject;
import sneakerbot.tasks.TaskConfig;

public class ShopifyController implements Initializable {
	
	public enum Hosts {
		_12AmRun("12amrun.com"),
		_18Montrose("18montrose.com"),
		AbovetheCloudsStore("abovethecloudsstore.com"),
		AddictMiami("addictmiami.com"),
		AlifeNYC("alifenewyork.com"),
		Alumni("tha-alumni.com"),
		A_ma_maniere("a-ma-maniere.com"),
		Amongst_Few("amongstfew.com"),
		Anti_Social_Social_Club("shop.antisocialsocialclub.com"),
		APB_Store("apbstore.com"),
		Astoreisgood("astoreisgood.com"),
		Atmosny("atmosny.com"),
		Bape("bape.com"),
		BB_Branded("bbbranded.com"),
		Beatnic("beatniconline.com"),
		Bianca_Chandon("biancachandon.com"),
		Billionaire_Boys_Club("bbcicecream.com"),
		Blends("blendsus.com"),
		Blkmkt("blkmkt.us"),
		Bodega("shop.bdgastore.com"),
		Bows_and_Arrows_Berkeley("bowsandarrowsberkeley.com"),
		Burn_Rubber_Sneakers("burnrubbersneakers.com"),
		Capsule_Toronto("capsuletoronto.com"),
		Centre("centre214.com"),
		Champs_Sports("champssports.com"),
		City_Blue("cityblueshop.com"),
		Cncpts("cncpts.com"),
		Commonwealth_ftgg("commonwealth-ftgg.com"),
		Concrete("concretecult.myshopify.com"),
		Courtside_Sneakers("courtsidesneakers.com"),
		Darkside("thedarksideinitiative.com"),
		Deadstock("deadstock.ca"),
		Doomsday_store("doomsday-store.com"),
		Dope_factory("dope-factory.com"),
		Drip("dripboutique.com"),
		DSM_Eflash_US("eflash-us.doverstreetmarket.com"),
		DSM_Eflash_UK("eflash.doverstreetmarket.com"),
		Eastbay("eastbay.com"),
		Exclucity("shop.exclucitylife.com"),
		Extrabutterny("shop.extrabutterny.com"),
		Fear_Of_God("fearofgod.com"),
		Fresh_Rags_FL("freshragsfl.com"),
		Good_as_Gold("goodasgoldshop.com"),
		Hanon("hanon-shop.com"),
		Haven_Shop("havenshop.com"),
		Highs_and_Lows("highsandlows.net.au"),
		Hombre_Amsterdam("hombreamsterdam.com"),
		Hunting_Lodge("hombreamsterdam.com"),
		Justdon("justdon.com"),
		Kith_("kith.com"),
		Kong_Online("kongonline.co.uk"),
		Lapstone_and_Hammer("lapstoneandhammer.com"),
		Leaders_1354("leaders1354.com"),
		Let_Us_Prosper("letusprosper.com"),
		Machus_Online("machusonline.com"),
		Manorphx("manorphx.com"),
		Marathon_Sports("marathonsports.com"),
		Mini_Shop_Madrid("marathonsports.com"),
		Noirfonce("noirfonce.eu"),
		Nojo_Kicks("noirfonce.eu"),
		Notre_shop("notre-shop.com"),
		Nrml("nrml.ca"),
		Offthehook_CA("offthehook.ca"),
		Oipolloi("oipolloi.com"),
		Oneness_287("oneness287.com"),
		OVO_USA("us.octobersveryown.com"),
		Packer_Shoes("packershoes.com"),
		Palace_USA("palaceskateboards.com"),
		Philip_Browne("philipbrownemenswear.co.uk"),
		Places_Faces("placesplusfaces.com"),
		Proper_Lbc("properlbc.com"),
		Reigning_Champ("reigningchamp.com"),
		Renarts("renarts.com"),
		Rime_NYC("rimenyc.com"),
		Rise_45("rise45.com"),
		Rock_City_Kicks("rockcitykicks.com"),
		Rooney_Shop("rooneyshop.com"),
		Rsvp_Gallery("rsvpgallery.com"),
		Saint_Alfred("saintalfred.com"),
		Samtabak("samtabak.com"),
		Shoe_Gallery_Miami("shoegallerymiami.com"),
		Shop_Nice_Kicks("shopnicekicks.com"),
		Sneaker_Junkies_USA("sneakerjunkiesusa.com"),
		Sneaker_Politics("sneakerpolitics.com"),
		Sneaker_World_Shop("sneakerworldshop.com"),
		Social_Status("socialstatuspgh.com"),
		Solefly("solefly.com"),
		Sole_Classics("soleclassics.com"),
		Stampd("stampd.com"),
		Suede_Store("suede-store.com"),
		The_Chimp_Store("thechimpstore.com"),
		The_Premier_Store("thepremierstore.com"),
		The_Sports_Edit("thesportsedit.com"),
		The_Sure_Store("thesurestore.com"),
		The_Hip_Store("thehipstore.co.uk"),
		Travis_Scott("travisscott.com"),
		Trophy_Room_Store("trophyroomstore.com"),
		Undefeated("undefeated.com"),
		Unknwn("unknwn.com"),
		Urbanindustry ("urbanindustry.co.uk"),
		Westnyc("westnyc.com"),
		Wishatl("wishatl.com"),
		Xhibition("xhibition.co"),
		YeezySupply("yeezysupply.com");
	    
	    
		Hosts(String host) {
	    	this.host = host;
	    }
	    
	    public String getHost() {
	    	return host;
	    }
	    
	    private String host;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
    	tasks = new ArrayList<TaskConfig<String>>();
		
		for(Entry<String, ProfileObject> profiles : MainController.profiles.entrySet()) 
			profileBox.getItems().add(profiles.getKey());
		
		for (Hosts h : Hosts.values()) 
			siteBox.getItems().add(h.name().replace("_", " "));
		
		//siteBox.getItems().setAll(Hosts.values());
		siteBox.getSelectionModel().selectFirst();
		
    	hostCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, TaskType>("host"));
    	productCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("product"));
    	sizeCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("size"));
    	profileCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("profileName"));  	
    	proxyCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("proxy"));
    	statusCol.setCellValueFactory(new PropertyValueFactory<TaskConfig<String>, String>("status"));   	
		
    	addColorToRows(new ArrayList<TableColumn<TaskConfig<String>, String>>(Arrays.asList(productCol, 
    			sizeCol, profileCol, proxyCol, statusCol)));
    	
      	taskAmountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));	
    	taskAmountSpinner.focusedProperty().addListener((s, ov, nv) -> {
    	    if (nv) return;
    	    
    	    commitEditorText(taskAmountSpinner);
    	});
    	
    	createButtons();

    	createTaskBtn.setOnAction((e) -> createTasks());
    	startAllBtn.setOnAction((e) -> startTasks());
    	stopAllBtn.setOnAction((e) -> stopTasks());
		System.out.println("Shopify Controller loaded!");		
	}
	
    public void createTasks() {
    	if(keywordTxt.getText().isEmpty() || sizesTxt.getText().isEmpty() || profileBox.getSelectionModel().getSelectedIndex() == -1)
    		return;
    	
    	int count = taskAmountSpinner.getValue();
    	
    	if(count < 1)
    		return;
    	
    	for(int amt = 0; amt < count; amt++) {
    		TaskConfig<String> config = new TaskConfig<String>(TaskType.SHOPIFY, table.getItems().size() + 1, MainController.getRandomProxy(), "Ready!",
    				Hosts.valueOf(siteBox.getSelectionModel().getSelectedItem().replace(" ", "_")).getHost(), keywordTxt.getText(), sizesTxt.getText(), 
    				profileBox.getItems().size() == 0 ? "No profiles" : profileBox.getSelectionModel().getSelectedItem());
	

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
    
	public void addColorToRows(ArrayList<TableColumn<TaskConfig<String>, String>> cellList) {  
		Paint color = Color.WHITE;
		
		for(TableColumn<TaskConfig<String>, String> cellFactory : cellList) {
			cellFactory.setCellFactory(column -> {
	    		return new TableCell<TaskConfig<String>, String>() {
	                @Override
	                public void updateItem(String item, boolean empty) {
	                    super.updateItem(item, empty);

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
	                        } else if(item.contains("@org@")) {
	                        	setTextFill(Color.ORANGE);
	                        	setText(item.replaceAll("@org@", ""));
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
	private TextField sizesTxt;
	@FXML
	private TextField keywordTxt;
	@FXML
	private ChoiceBox<String> siteBox;
	@FXML
	private ChoiceBox<String> profileBox;
		
    @FXML    
    public TableView<TaskConfig<String>> table;       
    @FXML 
    private TableColumn<TaskConfig<String>, TaskType> hostCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> productCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> sizeCol; 
    @FXML 
    private TableColumn<TaskConfig<String>, String> profileCol;
    @FXML 
    private TableColumn<TaskConfig<String>, String> proxyCol;
    @FXML 
    private TableColumn<TaskConfig<String>, String> statusCol;
    @FXML 
    private TableColumn<TaskConfig<String>, Void> actionCol;
}
