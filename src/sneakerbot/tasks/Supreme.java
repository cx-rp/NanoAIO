package sneakerbot.tasks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.GsonBuilder;

import sneakerbot.loaders.Profile.ProfileObject;
import sneakerbot.loaders.Proxy.ProxyObject;
import sneakerbot.captcha.TwoCaptcha;
import sneakerbot.controllers.MainController;
import sneakerbot.controllers.SettingsController;
import sneakerbot.captcha.Harvester.CaptchaResponse;

public class Supreme extends javafx.concurrent.Task<String>  {
	
	public Supreme(String keyword, String size, String color, String releaseTime, String category, 
			ProfileObject profile, ProxyObject proxy) {
		super();	
		
		BasicCredentialsProvider proxyCredentials = null;
		cookies = new BasicCookieStore();
		
		final int timeout = 15000;
		
		if(proxy != null && proxy.getUsername() != null) {
			proxyCredentials = new BasicCredentialsProvider();
			proxyCredentials.setCredentials(
					new AuthScope(proxy.getAddress(), proxy.getPort()),
					new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));			
		}
		
	    SSLContext sslContext = null;
		try {
			sslContext = new SSLContextBuilder()
			        .loadTrustMaterial(null, (certificate, authType) -> true).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
		
		client = HttpClientBuilder.create()
				.setDefaultCookieStore(cookies)
				.setRoutePlanner(proxy != null ? new DefaultProxyRoutePlanner(new HttpHost(proxy.getAddress(), proxy.getPort())) : null)
				.setConnectionReuseStrategy( (response, context) -> false )
				.setSSLContext(sslContext)
	            .setSSLHostnameVerifier(new NoopHostnameVerifier())
				.setDefaultCredentialsProvider(proxyCredentials)
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(CookieSpecs.STANDARD)
						.setProxy(proxy != null ? new HttpHost(proxy.getAddress(), proxy.getPort()) : null)
						.setConnectTimeout(timeout)
						.setConnectionRequestTimeout(timeout)
						.setSocketTimeout(timeout)
						.build())
				.build();
		
        this.keywordList = new ArrayList<String>();         
		this.size = size;
        this.styleList = new ArrayList<String>(); 
		this.releaseTime = releaseTime;
		this.category = category;
		this.profile = profile;
		debug = true;
		USE_CAPTCHA = false;
		sleep = 0L;
		
        if(!keyword.isEmpty())
        	keywordList = Arrays.asList(keyword.split(","));
        
        if(!color.equalsIgnoreCase("N/A"))
        	styleList = Arrays.asList(color.split(","));
	}
	
	@Override
	protected String call() throws Exception {
		if(profile == null) {
	        updateMessage("@red@No checkout profile loaded!");
	        return null;
		}
		
		if(keywordList.isEmpty()) {
	        updateMessage("@red@Please enter the keywords for the product!");
	        return null;			
		}
		
		int retry = 0;		
        updateMessage("@yel@Attempting to find product..");
		article = findProduct(false);
		
		while (sleep > 0L && !isCancelled()) {
			int totalSecs = (int) sleep;
			int hours = totalSecs / 3600;
			int minutes = (totalSecs % 3600) / 60;
			int seconds = totalSecs % 60;
			String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
			updateMessage("@yel@Sleeping for " + timeString);
			sleep(1000L); // sleep til 5 seconds before release time :)	
			sleep -= 1;
			
			if(USE_CAPTCHA) {
				synchronized(SettingsController.twocaptcha) {
					if(sleep <= 60 && SettingsController.twocaptcha == null) {
						SettingsController.twocaptcha = new TwoCaptcha("", "6LeWwRkUAAAAAOBsau7KpuC9AV-6J8mhw4AjC3Xz", "https://www.supremenewyork.com/");
						SettingsController.twocaptcha.start();
					}
				}
			}
		}
		
		return !isCancelled() ? request(retry) : null; 
	}
	
	public String request(int retry) {	
		boolean checkout = false;
	    long start = System.currentTimeMillis();
	    
		if(article == null)
	        updateMessage("@yel@Waiting for product");
		
		while (article == null && !isCancelled()) {
			article = findProduct(false);
			
			if(article == null) 
				sleep(new Random().nextInt((int) (1500L - 500L) + 1) + 500L);
		}

	    if(isCancelled() || article == null)
	    	return null;
	    
	    
		
        updateMessage("@yel@Grabbing product data..");
	    article = grabProductData(false);
	    
	    while(!checkout) {
	    	
			while((article.getCartLink().isEmpty() || article.getVariant().equalsIgnoreCase("SOLD")) && !isCancelled()) {
				updateMessage("@org@(" + retry + ") OOS, waiting for restock");		
				retry++;
				sleep = 0L;
				
			    article = grabProductData(false);
			    
				if(article.getVariant().equalsIgnoreCase("SOLD"))
					sleep(new Random().nextInt((int) (1500L - 500L) + 1) + 500L);
			}
		    
		    if(isCancelled() || article.getProductId().isEmpty() || article.getVariant().isEmpty())
		    	return null;
		    
		    start = System.currentTimeMillis();
		    String cartToken = null;
		    boolean USE_MOBILE = true;
			boolean carted = false;			
		    retry = 0;
		    
		    if(!USE_MOBILE) {
			    updateMessage("@yel@Grabbing Csrf Token...");	 
			    cartToken = getCartToken();	
		    }
		    	    
			updateMessage("@org@Attempting to cart...");
			while(!carted && !isCancelled() && !article.getVariant().equalsIgnoreCase("SOLD")) {
				carted = addToCart(USE_MOBILE, cartToken, retry);
				sleep(250L);
				retry ++;
			}
			
			if(article.getVariant().equalsIgnoreCase("SOLD"))
				continue;
			
		    if(isCancelled())
		    	return null;

			synchronized(MainController.captchas) {
				while(token == null && !isCancelled() && USE_CAPTCHA) {
					if(MainController.captchas.size() >= 1) 
						token = MainController.captchas.remove(new Random().nextInt(MainController.captchas.size()));
					else {
						updateMessage("@red@Please fill captcha bank.");
						sleep(5000L);
					}
				}  	
			}	
			
		    if(isCancelled())
		    	return null;
		    
			long delay = new Random().nextInt((int) (2000L - 1500L) + 1) + 1500L;
			updateMessage("@org@Sleeping for " + delay + "ms..");			
			sleep(delay);
			
		    if(isCancelled())
		    	return null;
		    
			updateMessage("@org@Grabbing to checkout data...");	
			String checkoutToken = getCheckoutCsrf(USE_MOBILE);
			
		    if(isCancelled())
		    	return null;
		    
			updateMessage("@org@Verifying checkout data...");
			verifyEmail(profile.getEmail(), USE_MOBILE);
			
		    if(isCancelled())
		    	return null;
		    
			updateMessage("@org@Attempting to checkout...");	
			CookieStore temp = cookies;
			while(!checkout && !isCancelled() && !article.getVariant().equalsIgnoreCase("SOLD")) {
				checkout = checkout(USE_MOBILE, checkoutToken);
				
				if(article.getVariant().equalsIgnoreCase("SOLD")) {
					cookies = temp;
					break;
				}
			}	
		    
		    if(isCancelled())
		    	return null;
	    }

		if(debug)
			print("cart & checkout time: " + (double) (System.currentTimeMillis() - start) / 1000L + "s.");
		
		return null;
	}
	
	private Article findProduct(boolean mobile) {
		if(mobile)
			return findProductMobile();
		
		return findProduct();
	}
	
	private Article grabProductData(boolean mobile) {
		if(mobile)
			return grabProductDataMobile();
		
		return grabProductData();
	}
	
	private boolean addToCart(boolean mobile, String token, int retry) {
		if(mobile)
			return addToCartMobile(retry);
		
		return addToCart(token, retry);
	}
	
	private boolean checkout(boolean mobile, String token) {
		if(mobile)
			return checkoutMobile(token);
			
		return checkout(token);
	}
	
	public Article findProduct() {
		HttpGet request = new HttpGet("https://www.supremenewyork.com/shop/all/" + category.toLowerCase().replaceAll("/", "_"));
		HttpResponse response = null;
        List<Article> matches = new ArrayList<Article>();
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		request.setHeader("Accept", "text/html, application/xhtml+xml, application/xml");
		request.setHeader("Accept-Encoding", "gzip, deflate");
		request.setHeader("Accept-Language", "en-US,en;q=0.9");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Host", "www.supremenewyork.com");;
		request.setHeader("Referer", "https://www.supremenewyork.com/shop/all/" + category.toLowerCase().replaceAll("/", "_"));
		request.setHeader("X-XHR-Referer", "https://www.supremenewyork.com/shop/all");
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);		
				
				if(!releaseTime.isEmpty()) {
					long currTime = ZonedDateTime.parse(response.getFirstHeader("Date").getValue(), DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond();
					long release_time = ZonedDateTime.parse(releaseTime, DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond();
					
					sleep = (release_time - currTime) - 120; // Is this a thing?
				} else 
					sleep = 0L;
				
				Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
				Elements elements = document.select("article");
				
				for(Element e : elements) {
	            	String name = e.select("h1").text().replaceAll("[^a-zA-Z0-9]", ""); 
	            	String color = e.select("p").text();      		
	            	String link = e.select("a").attr("href"); 
	            	
	            	int kwMatches = (int) keywordList.stream().filter(k -> name.toLowerCase().indexOf(k.toLowerCase()) != -1).count();
	            	int styleMatches = (int) styleList.stream().filter(s -> color.toLowerCase().indexOf(s.toLowerCase()) != -1).count();
	            	
	            	if(kwMatches > 0) {
	            		//if(debug)
	            		//	print("Name: " + name + ", Color: " + color + ", Link: " + link + ", matches: " + kwMatches + ", styles: " + styleMatches);
		            	
	            		matches.add(new Article(name, color, link, kwMatches, styleMatches));
	            	}
				}
			
		    	if(matches.size() == 0 ) 
		    		return null;
		    	        
		    	matches.sort((a, b) -> Integer.compare(b.getKwMatch(), a.getKwMatch()));
		    	
		    	Article temp = matches.get(0);
		    	
		    	matches = matches.stream().filter(a ->  (temp.getKwMatch() == a.getKwMatch()) && (a.getStyleMatch() > 0 || styleList.size() == 0)).collect(Collectors.toList());
		
				if(matches.size() > 0) 
					return matches.get(0);
				else {
					updateMessage("@red@Color not found!");
					print("could not find color...");
					return null;
				}
			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - getProductId(keyword)] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		
		return null;
	}
	
	public Article grabProductData() {
		HttpGet request = new HttpGet("https://www.supremenewyork.com" + article.getLink());
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		request.setHeader("Accept", "text/html, application/xhtml+xml, application/xml");
		request.setHeader("Accept-Encoding", "gzip, deflate");
		request.setHeader("Accept-Language", "en-US,en;q=0.9");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Host", "www.supremenewyork.com");;
		request.setHeader("Referer", "https://www.supremenewyork.com" + article.getLink());
		request.setHeader("X-XHR-Referer", "https://www.supremenewyork.com/shop/all/" + category.toLowerCase().replaceAll("/", "_"));
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);			 
				
				Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
				boolean outOfStock = document.select("b[class=\"button sold-out\"]").size() > 0;
				
				if(outOfStock) {
					//print("Sold out?: " + outOfStock);
					
					article.setCartLink("");
					article.setProductId("");
					article.setVariant("SOLD");
					
					return article;
				}
				
				article.setCartLink(document.select("form[id=cart-addf]").attr("action"));
				article.setProductId(document.select("input[id=\"st\"]").attr("value"));

				Elements options = document.select("select[name=\"s\"] > option");
				if(options.size() > 0) {
					for(Element option : options) {
						if(!option.text().equalsIgnoreCase(size))
							continue;
						
						article.setVariant(option.attr("value"));
					}	
				} else 
					article.setVariant(document.select("input[id=\"s\"]").attr("value"));
				
				return article;
				
			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - grabProductData(article, category)] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry++ + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		return article;
	}
	
	
	public String getCartToken() {
		HttpGet request = new HttpGet("https://www.supremenewyork.com/shop");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, sdch");
		request.setHeader("Accept-Language", "en-US,en;q=0.8");
		request.setHeader("Cache-Control", "max-age=0");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Host", "www.supremenewyork.com");
		request.setHeader("Upgrade-Insecure-Requests", "1");
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);
				
				Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
				String token = document.select("meta[name=csrf-token]").attr("content");	
				
				//if(debug)
				//	print("cart csrf: " + token);			

				return token;
			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - getCartToken()] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry++ + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		return "";
	}
	
	public boolean addToCart(String csrfToken, int retry) {
		print(article.toString());
		HttpPost request = new HttpPost("https://www.supremenewyork.com" + article.getCartLink());
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		request.setHeader("Accept", "*/*;q=0.5, text/javascript, application/javascript, application/ecmascript, application/x-ecmascript");
		request.setHeader("Accept-Encoding", "gzip, deflate");
		request.setHeader("Accept-Language", "en-us");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Origin", "https://www.supremenewyork.com");
		request.setHeader("Referer", "https://www.supremenewyork.com" + article.getLink());
		request.setHeader("Host", "www.supremenewyork.com");
		request.setHeader("X-Requested-With", "XMLHttpRequest");
		request.setHeader("X-CSRF-Token", csrfToken);
		
		data.add(new BasicNameValuePair("utf8", "✓"));
		data.add(new BasicNameValuePair("st", article.getProductId()));
		data.add(new BasicNameValuePair("s", article.getVariant()));
		//data.add(new BasicNameValuePair("qty", "1"));
		data.add(new BasicNameValuePair("commit", "add to cart"));
		
		try {
			request.setEntity(new UrlEncodedFormEntity(data));
			response = client.execute(request);
			       
			Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
				
			if(response.getStatusLine().getStatusCode() == 200 && !document.text().equalsIgnoreCase("[]")) {
				String jsonTxt = document.text();
				jsonTxt = jsonTxt.substring(1, jsonTxt.length() - 1);

				JSONObject result = new JSONObject(jsonTxt);
				
				if(debug)
					print("cart reponse: " + document.text());
				
				if(result.has("in_stock") && result.getBoolean("in_stock"))
					return true;
				else {
					updateMessage("@red@OOS, waiting for restock");			
					article.setCartLink("");
					article.setProductId("");
					article.setVariant("SOLD");
					return false;
				}
			} else 
				print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + document.text());
				
			//sleep(new Random().nextInt((int) (3500L - 1500L) + 1) + 1500L); // sleep random time 1.5-3 secs
		} catch (Exception e) {
			if(!e.getClass().getName().contains("SocketTimeoutException")) {
				if(debug) 
					e.printStackTrace();
				else 		
					print("[Exception - addToCart(productId, variant)] -> " + e.getClass().getName());
			} else
				updateMessage("@red@Connection timeout... retrying (" + retry + ")");
		} finally {
			if(request != null)
				request.releaseConnection();
			try {
				if(response != null && response.getEntity() != null)
					EntityUtils.consume(response.getEntity());
			} catch (Exception e) { e.printStackTrace(); }
		}
		return false;
	}
	
	public boolean addToCartMobile(int retry) {
		HttpPost request = new HttpPost("https://www.supremenewyork.com" + article.getCartLink() + ".json");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
		request.setHeader("Accept", "application/json");
		request.setHeader("Accept-Encoding", "gzip, deflate");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Origin", "https://www.supremenewyork.com");		
		request.setHeader("Referer", "https://www.supremenewyork.com/mobile");
		request.setHeader("X-Requested-With", "XMLHttpRequest");
		
		String payload = "size=" + article.getVariant() + "&style=" + article.getProductId() + "&qty=1";
		//String payload = "s=53643&st=19465&qty=1";
		
		try {
			request.setEntity(new StringEntity(payload));
			response = client.execute(request);
			       
			Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
			
			if(response.getStatusLine().getStatusCode() == 200 && !document.text().equalsIgnoreCase("[]")) {
				String jsonTxt = document.text();
				jsonTxt = jsonTxt.substring(1, jsonTxt.length() - 1);

				JSONObject result = new JSONObject(jsonTxt);
				
				if(debug)
					print("cart reponse: " + document.text());
				
				if(result.has("in_stock") && result.getBoolean("in_stock"))
					return true;
				else {
					updateMessage("@red@OOS, waiting for restock");			
					article.setCartLink("");
					article.setProductId("");
					article.setVariant("SOLD");
					return false;
				}
			} else 
				print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + document.text());
				
			//sleep(new Random().nextInt((int) (3500L - 1500L) + 1) + 1500L); // sleep random time 1.5-3 secs
		} catch (Exception e) {
			if(!e.getClass().getName().contains("SocketTimeoutException")) {
				if(debug) 
					e.printStackTrace();
				else 		
					print("[Exception - mobileATC(productId, variant)] -> " + e.getClass().getName());
			} else
				updateMessage("@red@Connection timeout... retrying (" + retry + ")");
		} finally {
			if(request != null)
				request.releaseConnection();
			try {
				if(response != null && response.getEntity() != null)
					EntityUtils.consume(response.getEntity());
			} catch (Exception e) { e.printStackTrace(); }
		}
		return false;
	}
	
	public String getCheckoutCsrf(boolean mobile) {
		HttpGet request = new HttpGet("https://www.supremenewyork.com/checkout");
		HttpResponse response = null;
		
		if(mobile) {
			request.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
			request.setHeader("Accept", "*");
			request.setHeader("Accept-Encoding", "gzip, deflate");
		} else {
			request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			request.setHeader("Accept-Encoding", "gzip, deflate, sdch");
			request.setHeader("Accept-Language", "en-US,en;q=0.8");
			request.setHeader("Cache-Control", "max-age=0");
			request.setHeader("Connection", "keep-alive");
			request.setHeader("Referer", "https://www.supremenewyork.com" + article.getCartLink());
			request.setHeader("Host", "www.supremenewyork.com");
			request.setHeader("Upgrade-Insecure-Requests", "1");
		}
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);
				
				Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
				String token = document.select("meta[name=csrf-token]").attr("content");	
				
				//if(debug)
				//	print("checkout csrf: " + token);
				
				return token;
			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - getCheckoutCsrf()] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry++ + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		return null;
	}
	
	public void grabPrecheckout() {
		HttpGet request = new HttpGet("https://teleus.supremenewyork.com/pooky");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		request.setHeader("Content-Type", "text/plain;charset=UTF-8");
		request.setHeader("Cache-Control", "max-age=0");
		request.setHeader("Origin", "https://www.supremenewyork.com");
		request.setHeader("Referer", "https://www.supremenewyork.com" + article.getCartLink());
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);

				if(response.getStatusLine().getStatusCode() != 200 && debug)
					print("Code: " + response.getStatusLine().getStatusCode() + "request: https://teleus.supremenewyork.com/pooky");
			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - checkoutPooky(ref)] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry++ + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
	}
	
	public void verifyEmail(String email, boolean mobile) {
		try {
			email = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e1) { e1.printStackTrace(); }
		HttpGet request = new HttpGet("https://www.supremenewyork.com/store_credits/verify?email=" + email);
		HttpResponse response = null;
		
		if(mobile) {
			request.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
		} else {
			request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			request.setHeader("Content-Type", "text/plain;charset=UTF-8");
			request.setHeader("Cache-Control", "max-age=0");
			request.setHeader("Origin", "https://www.supremenewyork.com");
			request.setHeader("Referer", "https://www.supremenewyork.com/checkout");
		}
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);

			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - verifyEmail(email)] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry++ + ")");
				
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
	}
	
	public boolean checkout(String csrfToken) {
		HttpPost request = new HttpPost("https://www.supremenewyork.com/checkout.json");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		request.setHeader("Accept", "*/*");
		request.setHeader("Accept-Encoding", "gzip, deflate, br");
		request.setHeader("Accept-Language", "en-US,en;q=0.9");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		request.setHeader("Origin", "https://www.supremenewyork.com");
		request.setHeader("Referer", "https://www.supremenewyork.com/checkout");
		request.setHeader("X-Requested-With", "XMLHttpRequest");
		request.setHeader("x-csrf-token", csrfToken);
		
		List<NameValuePair> data = generateCheckout(csrfToken);
		try {
				
			request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			response = client.execute(request);
			       
			Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
			
			long delay = new Random().nextInt((int) (3500L - 1500L) + 1) + 1500L;
			print(document.text());
			if(response.getStatusLine().getStatusCode() == 200) {
				JSONObject checkoutJson = new JSONObject(document.text());
				String status = checkoutJson.getString("status");
				
				if(debug)
					print("checkout delay: " + delay + " checkout reponse: " + document.text());
				
				if(status.equalsIgnoreCase("queued")) {
					updateMessage("@org@Processing payment");	
					String slug = checkoutJson.getString("slug");
					
					HttpGet getReq = new HttpGet("https://www.supremenewyork.com/checkout/" + slug + "/status.json");
					getReq.setHeaders(request.getAllHeaders());
					response = client.execute(getReq);
					document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
					
					status = new JSONObject(document.text()).getString("status");
					
					while(status.equalsIgnoreCase("queued")) {
						sleep(300L);
						response = client.execute(getReq);
						document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
						
						status = new JSONObject(document.text()).getString("status");
						
					}			
				}
				
				if(status.equalsIgnoreCase("paid")) {
					updateMessage("@gre@Successfully checked out");			
				} else if(status.equalsIgnoreCase("dup")) {
					updateMessage("@red@Billing used");			
				} else if(status.equalsIgnoreCase("outOfStock")) {
					updateMessage("@red@OOS, waiting for restock");			
					article.setCartLink("");
					article.setProductId("");
					article.setVariant("SOLD");
					return false;
				} else if(status.equalsIgnoreCase("failed")) {
					if(checkoutJson.has("cvv"))
						updateMessage("@red@Invalid CVV");
					else
						updateMessage("@red@Invalid billing");		
					
					return false;
				}
				
				
				/*if(status.equalsIgnoreCase("queued")) {				
					String temp = checkoutJson.getString("slug");
					updateMessage("@org@Processing payment");				
					return grabStatus(temp, csrfToken, true);				
				} else if(status.equalsIgnoreCase("paid")) {
					updateMessage("@gre@Successfully checked out");			
				} else if(status.equalsIgnoreCase("dup")) {
					updateMessage("@red@Billing used");			
				} else if(status.equalsIgnoreCase("outOfStock")) {
					updateMessage("@red@OOS, waiting for restock");			
					article.setCartLink("");
					article.setProductId("");
					article.setVariant("SOLD");
					return false;
				} else if(status.equalsIgnoreCase("failed")) {
					if(checkoutJson.has("cvv"))
						updateMessage("@red@Invalid CVV");
					else
						updateMessage("@red@Invalid billing");		
					
					return false;
				} */
				
				return true;			
			} else if(response.getStatusLine().getStatusCode() == 302) {
				String location = response.getFirstHeader("Location").getValue();
				print("Status Code: " + response.getStatusLine().getStatusCode() + " RedirectUrl: " + location + " Body: " + document.text());
			} else 
				print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + document.text());
				
			sleep(delay); // sleep random time 1.5-3 secs
		} catch (Exception e ) {
			if(!e.getClass().getName().contains("SocketTimeoutException")) {
				if(debug) 
					e.printStackTrace();
				else 		
					print("[Exception - checkout(variant)] -> " + e.getClass().getName());
			} else
				updateMessage("@red@Connection timeout... retrying");
		} finally {
			if(request != null)
				request.releaseConnection();
			try {
				if(response != null && response.getEntity() != null)
					EntityUtils.consume(response.getEntity());
			} catch (Exception e) { e.printStackTrace(); }
		}
		return false;
	}
	
	public boolean checkoutMobile(String csrfToken) {
		HttpPost request = new HttpPost("https://www.supremenewyork.com/checkout.json");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
		request.setHeader("Accept", "application/json");
		request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Origin", "https://www.supremenewyork.com");		
		
		String payload = "";
		try {
			payload = "utf8=%26%23x2713%3B"
					+ "&authenticity_token=" + URLEncoder.encode(csrfToken, "UTF-8")
					+ "&order%5Bbilling_name%5D=" + URLEncoder.encode(profile.getShipping().getFirstName() + " " + profile.getShipping().getLastName(), "UTF-8")
					+ "&order%5Bemail%5D=" + URLEncoder.encode(profile.getEmail(), "UTF-8")
					+ "&order%5Btel%5D=" + URLEncoder.encode(profile.getShipping().getPhone(), "UTF-8")
					+ "&order%5Bbilling_address%5D=" + URLEncoder.encode(profile.getShipping().getAddress(), "UTF-8")
					+ "&order%5Bbilling_address_2%5D=" + URLEncoder.encode(profile.getShipping().getAddress2(), "UTF-8")
					+ "&order%5Bbilling_city%5D=" + URLEncoder.encode(profile.getShipping().getCity(), "UTF-8")
					+ "&order%5Bbilling_state%5D=" + URLEncoder.encode(profile.getShipping().getState().name(), "UTF-8")
					+ "&order%5Bbilling_zip%5D=" + URLEncoder.encode(profile.getShipping().getZip(), "UTF-8")
					+ "&order%5Bbilling_country%5D=USA"
					+ "&same_as_billing_address=1"
					+ "&asec=Rmasn"
					+ "&store_credit_id="
					+ "&store_address=1"
					+ "&credit_card%5Bnlb%5D=" + URLEncoder.encode(profile.getCard().getNumber(), "UTF-8")
					+ "&credit_card%5Bmonth%5D=" + URLEncoder.encode(profile.getCard().getMonth(), "UTF-8")
					+ "&credit_card%5Byear%5D=" + URLEncoder.encode("20" + profile.getCard().getYear(), "UTF-8")
					+ "&credit_card%5Brvv%5D=" + URLEncoder.encode(profile.getCard().getCode(), "UTF-8")
					+ "&order%5Bterms%5D=1"
					+ "&commit=process+payment";
			
			if(token != null)
				payload = payload + "&g-recaptcha-response=" + URLEncoder.encode(token.getToken(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		print(payload);
		
		try {
				
			request.setEntity(new StringEntity(payload, "UTF-8"));
			response = client.execute(request);
			       
			Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
						
			long delay = new Random().nextInt((int) (3500L - 1500L) + 1) + 1500L;
				
			if(response.getStatusLine().getStatusCode() == 200) {
				JSONObject checkoutJson = new JSONObject(document.text());
				String status = checkoutJson.getString("status");
				
				if(debug)
					print("checkout delay: " + delay + " checkout reponse: " + document.text());
				
				if(status.equalsIgnoreCase("queued")) {
					updateMessage("@org@Processing payment");	
					String slug = checkoutJson.getString("slug");
					
					HttpGet getReq = new HttpGet("https://www.supremenewyork.com/checkout/" + slug + "/status.json");
					getReq.setHeaders(request.getAllHeaders());
					response = client.execute(getReq);
					document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
					
					status = new JSONObject(document.text()).getString("status");
					
					while(status.equalsIgnoreCase("queued")) {
						sleep(300L);
						response = client.execute(getReq);
						document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
						
						status = new JSONObject(document.text()).getString("status");
						
					}			
				}
				
				if(status.equalsIgnoreCase("paid")) {
					updateMessage("@gre@Successfully checked out");			
				} else if(status.equalsIgnoreCase("dup")) {
					updateMessage("@red@Billing used");			
				} else if(status.equalsIgnoreCase("outOfStock")) {
					updateMessage("@red@OOS, waiting for restock");			
					article.setCartLink("");
					article.setProductId("");
					article.setVariant("SOLD");
					return false;
				} else if(status.equalsIgnoreCase("failed")) {
					if(checkoutJson.has("cvv"))
						updateMessage("@red@Invalid CVV");
					else
						updateMessage("@red@Invalid billing");		
					
					return false;
				}
				
				
				/*if(status.equalsIgnoreCase("queued")) {				
					String temp = checkoutJson.getString("slug");
					updateMessage("@org@Processing payment");				
					return grabStatus(temp, csrfToken, true);				
				} else if(status.equalsIgnoreCase("paid")) {
					updateMessage("@gre@Successfully checked out");			
				} else if(status.equalsIgnoreCase("dup")) {
					updateMessage("@red@Billing used");			
				} else if(status.equalsIgnoreCase("outOfStock")) {
					updateMessage("@red@OOS, waiting for restock");			
					article.setCartLink("");
					article.setProductId("");
					article.setVariant("SOLD");
					return false;
				} else if(status.equalsIgnoreCase("failed")) {
					if(checkoutJson.has("cvv"))
						updateMessage("@red@Invalid CVV");
					else
						updateMessage("@red@Invalid billing");		
					
					return false;
				} */
				
				return true;
			} else if(response.getStatusLine().getStatusCode() == 302) {
				String location = response.getFirstHeader("Location").getValue();
				print("Status Code: " + response.getStatusLine().getStatusCode() + " RedirectUrl: " + location + " Body: " + document.text());
			} else 
				print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + document.text());
				
			sleep(delay); // sleep random time 1.5-3 secs
		} catch (Exception e ) {
			if(!e.getClass().getName().contains("SocketTimeoutException")) {
				if(debug) 
					e.printStackTrace();
				else 		
					print("[Exception - checkoutMobile(variant)] -> " + e.getClass().getName());
			} else
				updateMessage("@red@Connection timeout... retrying");
		} finally {
			if(request != null)
				request.releaseConnection();
			try {
				if(response != null && response.getEntity() != null)
					EntityUtils.consume(response.getEntity());
			} catch (Exception e) { e.printStackTrace(); }
		}
		return false;
	}
	
	private boolean grabStatus(String slug, String csrfToken, boolean mobile) {
		HttpGet request = new HttpGet("https://www.supremenewyork.com/checkout/" + slug + "/status.json");
		HttpResponse response = null;
		
		if(mobile) {
			request.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
			request.setHeader("Accept", "*");
			request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");
			request.setHeader("Connection", "keep-alive");
			request.setHeader("Origin", "https://www.supremenewyork.com");
			request.setHeader("Referer", "https://www.supremenewyork.com/mobile");
		} else {
			request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
			request.setHeader("Accept", "*/*");
			request.setHeader("Accept-Encoding", "gzip, deflate, br");
			request.setHeader("Accept-Language", "en-US,en;q=0.9");;
			request.setHeader("Referer", "https://www.supremenewyork.com/checkout");
			request.setHeader("x-csrf-token", csrfToken);	
		}	
		
		request.setHeader("X-Requested-With", "XMLHttpRequest");
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);
				Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");

				if(response.getStatusLine().getStatusCode() == 200) {
					JSONObject checkoutJson = new JSONObject(document.text());
					String status = checkoutJson.getString("status");
					if(status.equalsIgnoreCase("queued")) {		
						
						if(checkoutJson.has("slug")) {
							String temp = checkoutJson.getString("slug");
							updateMessage("@org@Processing payment");				
							return grabStatus(temp, csrfToken, mobile);	
						} else 
							response = null;
					} else if(status.equalsIgnoreCase("paid")) {
						updateMessage("@gre@Successfully checked out");		
						return true;
					} else if(status.equalsIgnoreCase("dup")) {
						updateMessage("@red@Billing used");			
						return true;
					} else if(status.equalsIgnoreCase("outOfStock")) {
						updateMessage("@red@OOS, waiting for restock");			
						article.setCartLink("");
						article.setProductId("");
						article.setVariant("SOLD");
						return false;
					} else if(status.equalsIgnoreCase("failed")) {
						if(checkoutJson.has("cvv"))
							updateMessage("@red@Invalid CVV");
						else
							updateMessage("@red@Invalid billing");		
						
						return true;
					} 
					
					//return true;	
				} else if(response.getStatusLine().getStatusCode() == 302) {
					String location = response.getFirstHeader("Location").getValue();
					print("Status Code: " + response.getStatusLine().getStatusCode() + " RedirectUrl: " + location + " Body: " + document.text());
				} else 
					print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + document.text());;
			} catch (Exception e ) {			
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - grabStatus(slug, token)] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry++ + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		
		return false;
	}
	
	public Article findProductMobile() {
		HttpGet request = new HttpGet("https://www.supremenewyork.com/shop.json");
		HttpResponse response = null;
        List<Article> matches = new ArrayList<Article>();
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
		//request.setHeader("Accept", "text/html, application/xhtml+xml, application/xml");
		request.setHeader("Accept-Encoding", "gzip, deflate");
		request.setHeader("Accept-Language", "en-US,en;q=0.9");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Upgrade-Insecure-Requests", "1");;
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);		
				
				if(!releaseTime.isEmpty()) {
					long currTime = ZonedDateTime.parse(response.getFirstHeader("Date").getValue(), DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond();
					long release_time = ZonedDateTime.parse(releaseTime, DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond();
					
					sleep = (release_time - currTime) - 120; // Is this a thing?
				} else 
					sleep = 0L;
				
				Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");;							
				JSONArray result = new JSONObject(document.text()).getJSONObject("products_and_categories").getJSONArray(category);				
				
				result.forEach(p -> {
					JSONObject product = new JSONObject(p.toString());
					String name = product.getString("name").replaceAll("[^a-zA-Z0-9]", "");
					String productId = Integer.toString(product.getInt("id"));
					
	            	int kwMatches = (int) keywordList.stream().filter(k -> name.toLowerCase().indexOf(k.toLowerCase()) != -1).count();
					
	            	if(kwMatches > 0) {
	            		if(debug)
	            			print("Name: " + name + ", ProductId: " + productId + ", matches: " + kwMatches);
	            		
	            		Article article = new Article(name, "", "", kwMatches, 0);
		            	article.setProductId(productId);
	            		
	            		matches.add(article);
	            	}
	            	
				});
				
		    	if(matches.size() == 0) 
		    		return null;
     
		    	matches.sort((a, b) -> Integer.compare(b.getKwMatch(), a.getKwMatch()));
		    	
		    	return matches.get(0);
				
			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - getProductId(keyword)] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		
		return null;
	}
	
	public Article grabProductDataMobile() {
		HttpGet request = new HttpGet("http://www.supremenewyork.com/shop/" + article.getProductId() + ".json");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36");
		//request.setHeader("Accept", "text/html, application/xhtml+xml, application/xml");
		request.setHeader("Accept-Encoding", "gzip, deflate");
		request.setHeader("Accept-Language", "en-US,en;q=0.9");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Upgrade-Insecure-Requests", "1");;
		
		int retry = 0;
		while(response == null) { // Just incase client cant execute the request :)
			try {
				response = client.execute(request);			 
				
				Document document = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://www.supremenewyork.com");
				JSONArray result = new JSONObject(document.text()).getJSONArray("styles");	
				//print(document.text());
				
				
				for(Object p : result) {
					JSONObject product = new JSONObject(p.toString());
					String color = product.getString("name");
					JSONArray sizes = product.getJSONArray("sizes");
					
	            	int styleMatches = (int) styleList.stream().filter(s -> color.toLowerCase().indexOf(s.toLowerCase()) != -1).count();
	            	
	            	
	            	print("StyleMatch: " + styleMatches + " list size: " + styleList.size());
	            	
	            	if(styleMatches > 0 || styleList.size() == 0) {
	            		for(Object s : sizes) {
	    					JSONObject sizeData = new JSONObject(s.toString()); 	
	    					String tmpSize = sizeData.getString("name");
	    					int stock = sizeData.getInt("stock_level");
	    					String variant = Integer.toString(sizeData.getInt("id"));
	    					
	    					
	    					print("tmpSize: " + tmpSize + ", Size: " + size);
	    					
	    					if(size.equalsIgnoreCase("N/A")) {
	    						if(stock > 0) 
		    						article.setVariant(variant);	
	    						else
		    						article.setVariant("SOLD");	
	    						
	    						return article;
	    					}
	    					
	    					if(tmpSize.equalsIgnoreCase(size)) {
	    						if(stock > 0) 
		    						article.setVariant(variant);	
	    						else
		    						article.setVariant("SOLD");	
	    						
	    						if(article.getVariant().equals("SOLD"))
	    							print("Variant: " + variant + ", SOLD");
	    						
	    						return article;
	    					}
	            		}
	            	}
	            	
					print(p.toString());
				}
				
				
			} catch (Exception e ) {
				if(!e.getClass().getName().contains("SocketTimeoutException")) {
					if(debug) 
						e.printStackTrace();
					else 		
						print("[Exception - grabProductData(article, category)] -> " + e.getClass().getName());
				} else
					updateMessage("@red@Connection timeout... retrying (" + retry++ + ")");
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		return article;
	}
	
	
	
	public void saveCookies(String file) {
		try (FileWriter writer = new FileWriter(file)) {
			new GsonBuilder().enableComplexMapKeySerialization()
				.setPrettyPrinting().create().toJson(cookies.getCookies(), writer);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public List<NameValuePair> generateCheckout(String csrfToken) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		
		if(token != null && token.isExpired()) { // grab new recaptcha token
			token = null;
		    synchronized(MainController.captchas) {
		    	while(token == null && USE_CAPTCHA) {
			    	if(MainController.captchas.size() >= 1) 
			    		token = MainController.captchas.remove(new Random().nextInt(MainController.captchas.size()));
			    	 else {
			    		 updateMessage("@red@Captcha expired, please fill captcha bank.");
			    		sleep(5000L);
			    	}
		    	}  
		    }
		}
		
		data.add(new BasicNameValuePair("utf8", "\u2713"));
		data.add(new BasicNameValuePair("authenticity_token", csrfToken));		
		data.add(new BasicNameValuePair("order[billing_name]", profile.getShipping().getFirstName() + " " + profile.getShipping().getLastName()));
		data.add(new BasicNameValuePair("order[email]", profile.getEmail()));
		data.add(new BasicNameValuePair("order[tel]", profile.getShipping().getPhone()));
		data.add(new BasicNameValuePair("order[billing_address]", profile.getShipping().getAddress()));
		data.add(new BasicNameValuePair("order[billing_address_2]", profile.getShipping().getAddress2()));
		data.add(new BasicNameValuePair("order[billing_zip]", profile.getShipping().getZip()));
		data.add(new BasicNameValuePair("order[billing_city]", profile.getShipping().getCity()));
		data.add(new BasicNameValuePair("order[billing_state]", profile.getShipping().getState().name()));
		data.add(new BasicNameValuePair("order[billing_country]", "USA"));
		data.add(new BasicNameValuePair("asec", "Rmasn"));
		data.add(new BasicNameValuePair("same_as_billing_address", "1"));
		data.add(new BasicNameValuePair("store_credit_id", ""));
		data.add(new BasicNameValuePair("store_address", "1"));
		data.add(new BasicNameValuePair("credit_card[nlb]", profile.getCard().getNumber()));
		data.add(new BasicNameValuePair("credit_card[month]", profile.getCard().getMonth()));
		data.add(new BasicNameValuePair("credit_card[year]", "20" + profile.getCard().getYear()));
		data.add(new BasicNameValuePair("credit_card[rvv]", profile.getCard().getCode()));
		data.add(new BasicNameValuePair("order[terms]", "0"));
		data.add(new BasicNameValuePair("order[terms]", "1"));
		
		if(token != null)
			data.add(new BasicNameValuePair("g-recaptcha-response", token.getToken()));  
		
		data.add(new BasicNameValuePair("credit_card[vval]", ""));		
		
		if(debug)
			print("checkout post: " + data.toString());
		
		return data;
	}
	
    @Override protected void succeeded() {

        super.succeeded();
        //updateMessage("Done!");
    }

    @Override protected void cancelled() {

        updateMessage("Ready!");
        super.cancelled();
    }

    @Override protected void failed() {

        super.failed();
        updateMessage("Failed!");
    }

	public void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {		
            if (isCancelled()) 
                //updateMessage("Thread " +  Thread.currentThread().getId() + " Stopped!");
            	updateMessage("Ready!");
            
		}
	}
	
	public void print(Object text) {
		System.out.println("[" + new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime()) + "][Supreme][" + Thread.currentThread().getId() + "] " + text.toString());
	}

	private String size;
	private String releaseTime;
	private String category;
	private List<String> keywordList;
	private List<String> styleList;
	
	private Article article;
	private ProfileObject profile;
	private CookieStore cookies;
	private HttpClient client;
	private CaptchaResponse token;
	boolean USE_CAPTCHA;
	boolean debug;
	long sleep;
	
    
    private class Article {
    	
		public Article(String name, String style, String link, int kwMatch, int styleMatch) {
			super();
			//this.name = name;
			//this.style = style;
			this.link = link;
			this.kwMatch = kwMatch;
			this.styleMatch = styleMatch;
			this.cartLink = "";
			this.productId = "";
			this.variant = "";
		}
		
		public String getLink() {
			return link;
		}
		
		public int getKwMatch() {
			return kwMatch;
		}
		
		public int getStyleMatch() {
			return styleMatch;
		}
		
		public String getCartLink() {
			return cartLink;
		}
		
    	public String getProductId() {
			return productId;
		}

		public String getVariant() {
			return variant;
		}
		
    	public void setCartLink(String cartLink) {
			this.cartLink = cartLink;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}

		public void setVariant(String variant) {
			this.variant = variant;
		}

    	@Override
		public String toString() {
			return "Article [link=" + link + ", kwMatch=" + kwMatch + ", styleMatch=" + styleMatch + ", cartLink="
					+ cartLink + ", productId=" + productId + ", variant=" + variant + "]";
		}

		private String link;
    	private int kwMatch;
    	private int styleMatch;
		private String cartLink;
		private String productId;
    	private String variant;
    }
}
