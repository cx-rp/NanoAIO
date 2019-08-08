package sneakerbot.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import sneakerbot.controllers.MainController;
import sneakerbot.controllers.TaskController.TaskType;
import sneakerbot.loaders.Profile.ProfileObject;
import sneakerbot.loaders.Proxy.ProxyObject;

public class TaskConfig<V> extends javafx.concurrent.Service<String> {

    public TaskConfig(TaskType type, int id, ProxyObject proxy, String status, String locale, String sku, String size, boolean splash, boolean manual) { // adidas
    	this(type, id, proxy, status);
    	
    	this.locale = locale;
    	this.sku = sku;
    	this.size = size;
    	this.splash = splash;
    }
    
    public TaskConfig(TaskType type, int id, ProxyObject proxy, String status, String host, String product, String size, String profileName) { // shopify
    	this(type, id, proxy, status);
    	
    	this.host = host;
    	this.product = product;
    	this.size = size;
    	this.profileName = profileName;
    	
    	profile = MainController.profiles.get(profileName);
    }
    
    public TaskConfig(TaskType type, int id, ProxyObject proxy, String status, String keyword, String size, String color,
    		String category, String profileName, String releaseTime) { // supreme
    	this(type, id, proxy, status);
    	
    	this.keyword = keyword;
    	this.size = size;
    	this.color = color;
    	this.profileName = profileName;
    	this.releaseTime = releaseTime;
    	this.category = category;
    	
    	profile = MainController.profiles.get(profileName);
    }
    
    TaskConfig(TaskType type, int id, ProxyObject proxy, String status) {
    	this.type = type;
    	this.id = id;
    	this.proxy = proxy;
    	this.status = new SimpleStringProperty(status);
    }
    
	@Override
	protected Task<String> createTask() {		
		System.out.println("Task created " + id);
		Task<String> task = null;
		
		if(type == TaskType.ADIDAS) 
			task = new Adidas();
		 else if(type == TaskType.SHOPIFY) 
			task = new Shopify(host, product, size, profile, proxy);
		 else if(type == TaskType.SUPREME) 
			task = new Supreme(keyword, size, color, releaseTime.equalsIgnoreCase("manual") ? "" : releaseTime, category, profile, proxy);
		
		
		if(task != null)
			statusProperty().bind(task.messageProperty());
		
		return task;
	}
	
	public void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {		         
		}
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public String getColor() {
		return color;
	}
	
	public String getReleaseTime() {
		return releaseTime;
	}
	
	public String getProduct() {
		return product;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getProfileName() {
		return profileName;
	}
	
	public String getHost() {
		return host;
	}
    
	public int getId() {
		return id;
	}

	public TaskType getType() {
		return type;
	}

	public String getLocale() {
		return locale;
	}

	public String getSku() {
		return sku;
	}

	public String getSize() {
		return size;
	}
	
	public boolean isSplash() {
		return splash;
	}
	
	public String getProxy() {
		return proxy == null ? "localhost" : proxy.toString();
	}

	public String getStatus() {
		return status.get();
	}
	
	public void setStatus(String text) {
		status.set(text);
	}
	
    public SimpleStringProperty statusProperty(){
        return status;
    }
    
    private String keyword; // supreme
    private String color;
    private String releaseTime;   
    private String category;
    
    private String product; // shopify
    private String profileName;
    private String host;
    
	private String locale; // adidas
	private String sku;
	
	private int id; // all
	private TaskType type;	
	private String size;
	private boolean splash;
	private ProxyObject proxy;	
	private SimpleStringProperty status;
	private ProfileObject profile;
	
	static {
		Logger.getLogger("org.openqa.selenium.remote").setLevel(Level.OFF);
	}
}  
