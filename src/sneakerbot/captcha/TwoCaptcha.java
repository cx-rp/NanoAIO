package sneakerbot.captcha;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import sneakerbot.captcha.Harvester.CaptchaResponse;
import sneakerbot.controllers.MainController;

public class TwoCaptcha extends Thread {
	
	public TwoCaptcha(String apiKey, String siteKey, String host) {
		this.apiKey = apiKey;
		this.siteKey = siteKey;
		this.host = host;
	}
	
	@Override
	public void run() {
        Runnable task = () -> {
	    	while(running) {
	        	int count = 0;
	        	if(MainController.captchas != null) {
				    synchronized(MainController.captchas) {
				    	for(int index = 0; index < MainController.captchas.size(); index++) {
				    		CaptchaResponse response = MainController.captchas.get(index);
				    		
				    		if(response.isExpired()) {
				    			MainController.captchas.remove(index);
				    			count++;
				    		}
				    	}
				    }
	        	}
			    
			    if(count > 0)
			    	print("Removed " + count + " expired captchas from bank.");   
			    
			    try {
					Thread.sleep(30000L);
				} catch (InterruptedException e) { e.printStackTrace(); }
	    	}
        };
		
        remCaptchaThread = new Thread(task);
        remCaptchaThread.start();
		
		while (running) {
			String captcha = solve();

			if(captcha != null && MainController.captchas != null) {
			    synchronized(MainController.captchas) {
	                MainController.captchas.add(new CaptchaResponse(captcha, System.currentTimeMillis()));
	                print("Captcha Tokens Harvested: " + MainController.captchas.size());
			    }
			}
			
			sleep(15000L);
		}
	}
	
	public void stopHarvester() {
		running = false;
        print("Harvester stopped.");
	}
	
	public String solve() {
		if(apiKey == null || siteKey == null || host == null)
			return null; 
		
		String id = getCaptchaId(apiKey, siteKey, host);
		
		while(id == null) {
			id = getCaptchaId(apiKey, siteKey, host);
			sleep(500L);
		}
		
		String answer = getCaptchaAnswer(apiKey, id);
		
		while(answer == null) {
			answer = getCaptchaAnswer(apiKey, id);
			sleep(5000L);
		}
		
		return answer;
	}
	
	public String getCaptchaId(String apiKey, String siteKey, String host) {
        HttpClient client =  HttpClientBuilder.create().build();
        
		HttpPost request = new HttpPost("http://2captcha.com/in.php");
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		
		data.add(new BasicNameValuePair("key", apiKey));
		data.add(new BasicNameValuePair("method", "userrecaptcha"));
		data.add(new BasicNameValuePair("googlekey", siteKey));
		data.add(new BasicNameValuePair("pageurl", host));
		
		try {
			request.setEntity(new UrlEncodedFormEntity(data));
			response = client.execute(request);
			Document doc = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "http://2captcha.com");			       

			print(doc.toString());
			if(response.getStatusLine().getStatusCode() == 200 && doc.toString().contains("OK|")) {
				return doc.toString().split("|")[1];
			} else 
				print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + doc.toString());
				
			//sleep(new Random().nextInt((int) (3500L - 1500L) + 1) + 1500L); // sleep random time 1.5-3 secs
		} catch (Exception e) {
			if(debug) 
				e.printStackTrace();
			else 		
				print("[Exception - getCaptchaId()] -> " + e.getClass().getName());
		} finally {
			if(request != null)
				request.releaseConnection();
			try {
				if(response != null && response.getEntity() != null)
					EntityUtils.consume(response.getEntity());
			} catch (Exception e) { e.printStackTrace(); }
		}
		
		return null;
	}
	
	
	public String getCaptchaAnswer(String apiKey, String captchaId) {
        HttpClient client =  HttpClientBuilder.create()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(CookieSpecs.STANDARD)
						.setConnectTimeout(15000)
						.setConnectionRequestTimeout(15000)
						.setSocketTimeout(15000)
						.build())
				.build();
        
		HttpPost request = new HttpPost("http://2captcha.com/res.php");
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		
		data.add(new BasicNameValuePair("key", apiKey));
		data.add(new BasicNameValuePair("action", "get"));
		data.add(new BasicNameValuePair("id", captchaId));
		
		try {
			request.setEntity(new UrlEncodedFormEntity(data));
			response = client.execute(request);
			Document doc = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "http://2captcha.com");			       

			print(doc.toString());
			if(response.getStatusLine().getStatusCode() == 200 && !doc.toString().contains("CAPCHA_NOT_READY")) {
				return doc.toString().split("|")[1];
			} else if(debug)
				print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + doc.toString());
				
			//sleep(new Random().nextInt((int) (3500L - 1500L) + 1) + 1500L); // sleep random time 1.5-3 secs
		} catch (Exception e) {
			if(debug) 
				e.printStackTrace();
			else 		
				print("[Exception - getCaptchaId()] -> " + e.getClass().getName());
		} finally {
			if(request != null)
				request.releaseConnection();
			try {
				if(response != null && response.getEntity() != null)
					EntityUtils.consume(response.getEntity());
			} catch (Exception e) { e.printStackTrace(); }
		}
		
		return null;
	}
	
	
	
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {		}
	}
	
	public void print(Object text) {
		System.out.println("[" + new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime()) + "][TwoCaptcha][" + Thread.currentThread().getId() + "] " + text.toString());
	}
	
	String apiKey;
	String host;
	String siteKey;
	Thread remCaptchaThread;
	boolean debug;
	boolean running;
	
}
