package sneakerbot.tasks;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

import sneakerbot.controllers.MainController;

public class Adidas extends javafx.concurrent.Task<String> {
	
	public Adidas() {		
        loadCondition = new ExpectedCondition<Boolean>() {
        	public Boolean apply(WebDriver driver) {
        		return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
        	}
        };
        
		initDriver();		
	}

	protected boolean isElementPresent(By by){
        try{
            driver.findElement(by);
            return true;
        }
        catch(NoSuchElementException e){
            return false;
        }
    }

	@Override
	protected String call() throws Exception {
		try {
			while(driver == null)
				sleep(1500L);
			
			driver.get("http://adidas.cartchefs.co.uk/splash_test");
			JavascriptExecutor js = (JavascriptExecutor)driver;
			
			
			
			boolean passed = false;
			
			while(!passed) {
				boolean found = (boolean) js.executeScript("return typeof CAPTCHA_KEY !== 'undefined';");
				boolean found_window = (boolean) js.executeScript("return typeof window.CAPTCHA_KEY !== 'undefined';");				
				
				if(found || found_window ) {
					updateMessage("Passed Splash!");
					passed = true;
					System.out.println("found: " + found + ", found_window: " + found_window);
				} else 
					System.out.println("Not passed!");
				
				sleep(1500L);
			}
						

		} catch (Exception ex) { ex.printStackTrace(); }
        
		sleep(30000L);
		
		if(isCancelled()) {
	        updateMessage("cancelled!");
		} else
			updateMessage("Sleeping 1");
		
		return null;
	}
	
	public void sendKeys(WebElement element, String value) {
		wait.until(new Function<WebDriver, Boolean>() {
		    @Override
		    public Boolean apply(WebDriver input) {
		        element.clear();
		        element.sendKeys(value);
		        return element.getAttribute("value").equals(value);
		    }
		});
	}
	
	public void initDriver() {
		new Thread(() ->  {
	        ChromeOptions chromeOptions= new ChromeOptions();
	        
	        if(MainController.CHROME_PATH != null)
	        	chromeOptions.setBinary(MainController.CHROME_PATH+ "\\chrome.exe");
	        
	        chromeOptions.addArguments();
	        chromeOptions.addArguments("--disable-gpu", "--no-sandbox", "--incognito", "--disable-accelerated-2d-canvas");
	        chromeOptions.addArguments("--window-size=571,428");
	       // chromeOptions.addArguments("--proxy-server=");
	       // chromeOptions.addArguments("--headless");
	        
	        driver = new ChromeDriver(chromeOptions);	
	        driver.setLogLevel(Level.OFF);
	        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
	        wait = new WebDriverWait(driver, 15);
		}).start();
	}
	
    @Override protected void succeeded() {
		new Thread(() ->  {
	    	if(driver != null) {
	    		driver.close();
	    		driver.quit();    		
	    	}
		}).start();
    	
        super.succeeded();
        updateMessage("Done!");
    }

    @Override protected void cancelled() {
		new Thread(() ->  {
	    	if(driver != null) {
	    		driver.close();
	    		driver.quit();    		
	    	}
		}).start();
    	
        super.cancelled();
        updateMessage("Cancelled!");
    }

    @Override protected void failed() {
		new Thread(() ->  {
	    	if(driver != null) {
	    		driver.close();
	    		driver.quit();    		
	    	}
		}).start();
    	
    	
        super.failed();
        updateMessage("Failed!");
    }

	public void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {		
            if (isCancelled()) 
                updateMessage("Thread " +  Thread.currentThread().getId() + " Stopped!");
            
		}
	}
	
	private ChromeDriver driver;
	WebDriverWait wait;
    ExpectedCondition<Boolean> loadCondition;
}
