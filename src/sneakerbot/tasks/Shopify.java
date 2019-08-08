package sneakerbot.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import sneakerbot.loaders.Profile.ProfileObject;
import sneakerbot.loaders.Proxy.ProxyObject;

public class Shopify extends javafx.concurrent.Task<String>  {
	
	public Shopify(String base, String keywords, String size, ProfileObject profile, ProxyObject proxy) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		client = HttpClientBuilder.create()
				.setDefaultCookieStore(cookies)
				.setRoutePlanner(proxy != null ? new DefaultProxyRoutePlanner(new HttpHost(proxy.getAddress(), proxy.getPort())) : null)
				.setConnectionReuseStrategy( (response, context) -> false )
				.setRedirectStrategy(new LaxRedirectStrategy())
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
		
		this.base = base;
		this.keywords = keywords;
		this.size = size;
		this.profile = profile;
		debug = true;
	}

	@Override
	protected String call() throws Exception {
		String productLink = "https://kith.com/collections/kith/products/kith-classics-laguardia-tee-black-2";
		String variant = getVariant(productLink);

		if(variant == null) {
			updateMessage("Variant is null. Stopping task..");
			return null;
		}
		String body = addToCart(variant, productLink);;	
			
		if(body == null) {
			updateMessage("Unable to grab checkout url, and auth token.");
			return null;
		}
		
		Document doc = Jsoup.parse(body, "https://" + base);
		long startTimer = System.currentTimeMillis();
		checkout(doc.select("form[class=edit_checkout]").attr("action"), doc.select("input[name=authenticity_token]").get(1).attr("value"));
		
		//updateMessage("Success, " + (System.currentTimeMillis() - startTimer)  + " ms ->  https://" + base + doc.select("form[class=edit_checkout]").attr("action"));
		return null;
	}
	
	public boolean search() {
		try {
			
			
		} catch (Exception e ) { }
		return false;
	}
	
	
	public String getVariant(String url) {
		HttpGet request = new HttpGet(url + ".json");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");
		request.setHeader("Accept-Language", "en-US,en;q=0.8");		
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Upgrade-Insecure-Requests", "1");
	       
		try {
			response = client.execute(request);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) 
				result.append(line);
			
			rd.close();
			
			String json = result.toString();		
			
			if(response.getStatusLine().getStatusCode() == 200) {
				
			if(json != null) {
					JSONObject products = new JSONObject(json).getJSONObject("product");
					
					for(Object variant : products.getJSONArray("variants")) {
						JSONObject product = new JSONObject(variant.toString());	
						
						boolean match1 = !product.get("option1").equals(null) && size.toLowerCase().equals(product.getString("option1").toLowerCase());
						boolean match2 = !product.get("option2").equals(null) && size.toLowerCase().equals((product.getString("option2")).toLowerCase());
						boolean match3 = !product.get("option3").equals(null) && size.toLowerCase().equals(product.getString("option3").toLowerCase());
						
						if(match1 || match2 || match3) 
							return Long.toString(product.getLong("id"));							
					}
				}				
			} else 
				print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + result.toString());
			
		} catch (Exception e) {
			if(debug) 
				e.printStackTrace();
			else {
				String name = e.getClass().getName();
				
				if(!name.contains("SocketTimeoutException"))
					print("[Exception - splashRequest()] -> " + name);
			}
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
	
	public String addToCart(String variant, String referer) {
		HttpGet request = new HttpGet("https://" + base + "/cart/" + variant + ":1");
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36");
		request.setHeader("Accept", "application/json, text/plain, */*");
		request.setHeader("Accept-Encoding", "gzip, deflate, br");
		request.setHeader("Accept-Language", "en-US,en;q=0.8");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Content-Type", "application/json;charset=UTF-8");
		request.setHeader("Host", base);
		request.setHeader("Origin", "https://" + base);
		request.setHeader("Referer", referer);
		
		while(response == null) {
			try {
				response = client.execute(request);
				       
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = in.readLine()) != null) 
					result.append(line);
					
				in.close();

				String body = result.toString();
				
				if(response.getStatusLine().getStatusCode() == 200) { 
					updateMessage("Product added to cart!");
					return body;
				} else 
					print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + body);
					
				//sleep(new Random().nextInt((int) (3500L - 1500L) + 1) + 1500L); // sleep random time 1.5-3 secs
			} catch (Exception e ) {
				if(debug) 
					e.printStackTrace();
				else {
					String name = e.getClass().getName();
						
					if(!name.contains("SocketTimeoutException"))
						print("[Exception - addToCart(baseUrl, variant, referer)] -> " + name);
				}
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
	
	public void checkout(String checkout, String token) {	
		HttpPost request = new HttpPost("https://" + base + checkout);
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		HttpResponse response = null;
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, br");
		request.setHeader("Accept-Language", "en-US,en;q=0.8");
		request.setHeader("Cache-Control", "max-age=0");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setHeader("Host", base);
		request.setHeader("Referer", "https://" + base + checkout + "?step=contact_information");
		request.setHeader("Upgrade-Insecure-Requests", "1");
		
		data.add(new BasicNameValuePair("utf8", "✓"));		
		data.add(new BasicNameValuePair("_method", "patch"));
		data.add(new BasicNameValuePair("button", ""));
		data.add(new BasicNameValuePair("authenticity_token", token));
		data.add(new BasicNameValuePair("previous_step", "contact_information"));
		data.add(new BasicNameValuePair("checkout[email]", profile.getEmail()));
		data.add(new BasicNameValuePair("checkout[shipping_address][first_name]", profile.getShipping().getFirstName()));
		data.add(new BasicNameValuePair("checkout[shipping_address][last_name]", profile.getShipping().getLastName()));
		data.add(new BasicNameValuePair("checkout[shipping_address][address1]", profile.getShipping().getAddress()));
		data.add(new BasicNameValuePair("checkout[shipping_address][address2]", profile.getShipping().getAddress2()));
		data.add(new BasicNameValuePair("checkout[shipping_address][city]", profile.getShipping().getCity()));
		data.add(new BasicNameValuePair("checkout[shipping_address][country]", "United States"));		
		data.add(new BasicNameValuePair("checkout[shipping_address][province]", profile.getShipping().getState().name()));
		data.add(new BasicNameValuePair("checkout[shipping_address][zip]", profile.getShipping().getZip()));		
		data.add(new BasicNameValuePair("checkout[shipping_address][phone]", profile.getShipping().getPhone()));
		data.add(new BasicNameValuePair("checkout[client_details][browser_height]", "728"));
		data.add(new BasicNameValuePair("checkout[client_details][browser_width]", "1280"));
		data.add(new BasicNameValuePair("checkout[client_details][javascript_enabled]", "0"));
		data.add(new BasicNameValuePair("step", "shipping_method"));
		
		token = null;
		updateMessage("Submiting shipping information.");
		while(token == null) {
			try {
				request.setEntity(new UrlEncodedFormEntity(data));
				response = client.execute(request);
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = in.readLine()) != null) 
					result.append(line);
					
				in.close();

				String body = result.toString();
				
				if(response.getStatusLine().getStatusCode() == 200) {
					int start = body.indexOf("authenticity_token\" value=\"") + 27;
					int end = body.indexOf('"', start);
					token = body.substring(start, end); 

				}  else if(response.getStatusLine().getStatusCode() == 302) {
					String location = response.getFirstHeader("Location").getValue();
					print("Status Code: " + response.getStatusLine().getStatusCode() + " RedirectUrl: " + location + " Body: " + result.toString());
				} else 
					print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + result.toString());;
					
			} catch (Exception e ) {
				if(debug) 
					e.printStackTrace();
				else {
					String name = e.getClass().getName();
						
					if(!name.contains("SocketTimeoutException"))
						print("[Exception - checkout(baseUrl)] -> " + name);
				}
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}		
		}		
		
		//print(token);
		
		request = new HttpPost("https://" + base + checkout);
		String paymentGateway = null, price = null;
		response = null;
		data.clear();
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, br");
		request.setHeader("Accept-Language", "en-US,en;q=0.8");
		request.setHeader("Cache-Control", "max-age=0");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setHeader("Host", base);
		request.setHeader("Origin", "https://" + base);
		request.setHeader("Referer", "https://" + base + checkout);
		request.setHeader("Upgrade-Insecure-Requests", "1");
		
		data.add(new BasicNameValuePair("utf8", "✓"));		
		data.add(new BasicNameValuePair("_method", "patch"));
		data.add(new BasicNameValuePair("authenticity_token", token));
		data.add(new BasicNameValuePair("previous_step", "shipping_method"));
		data.add(new BasicNameValuePair("step", "payment_method"));
		data.add(new BasicNameValuePair("checkout[shipping_rate][id]", "shopify-UPS%20GROUND%20(5-7%20business%20days)-10.00"));
		data.add(new BasicNameValuePair("button", ""));
		data.add(new BasicNameValuePair("checkout[client_details][browser_height]", "728"));
		data.add(new BasicNameValuePair("checkout[client_details][browser_width]", "1280"));
		data.add(new BasicNameValuePair("checkout[client_details][javascript_enabled]", "0"));
		
		updateMessage("Submiting shipping rate.");
		while(response == null && paymentGateway == null) {
			try {
				request.setEntity(new UrlEncodedFormEntity(data));
				response = client.execute(request);
				       
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = in.readLine()) != null) 
					result.append(line);
					
				in.close();
				
				String body = result.toString();
				if(response.getStatusLine().getStatusCode() == 200) {			
					int start = body.indexOf("authenticity_token\" value=\"") + 27;
					int end = body.indexOf('"', start);
					token = body.substring(start, end); 	
					
					start = body.indexOf("data-select-gateway=\"") + 21;
					end = body.indexOf('"', start);
					paymentGateway = body.substring(start, end); 
					
					start = body.indexOf("data-checkout-payment-due-target=\"") + 34;
					end = body.indexOf('"', start);
					price = body.substring(start, end); 
				}  else 
					print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + body);
					
			} catch (Exception e ) {
				if(debug) 
					e.printStackTrace();
				else {
					String name = e.getClass().getName();
						
					if(!name.contains("SocketTimeoutException"))
						print("[Exception - checkout(baseUrl)] -> " + name);
				}
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}		
		}	

		String storeId = checkout.split("/")[1];
		String checkoutId = checkout.split("/")[3];
		
		updateMessage("Submiting payment information.");
		request = new HttpPost("https://elb.deposit.shopifycs.com/sessions");
		String ccVerify = null;
		response = null;
		data.clear();
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36");
		request.setHeader("Accept", "application/json");
		request.setHeader("Accept-Encoding", "gzip, deflate, br");
		request.setHeader("Accept-Language", "en-US,en;q=0.8");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Content-Type", "application/json");
		request.setHeader("Host", "elb.deposit.shopifycs.com");
		request.setHeader("Origin", "https://checkout.shopifycs.com");
		request.setHeader("Referer", "https://checkout.shopifycs.com/number?identifier=" + checkoutId + "&location=https%3A%2F%2F" + base + "%2F" + storeId + "%2Fcheckouts%2F" + checkoutId + "%3Fprevious_step%3Dshipping_method%26step%3Dpayment_method&fonts[]=Lato");
		
		int month = Integer.parseInt(profile.getCard().getMonth());
		String payload = "{\"credit_card\":{\"number\":\"" + profile.getCard().getNumber().replaceAll("-", " ") + "\",\"name\":\"" + profile.getShipping().getFirstName() + " " + profile.getShipping().getLastName() + "\",\"month\":" + (month < 10 ? profile.getCard().getMonth().replaceAll("0", "") : profile.getCard().getMonth()) + ",\"year\":" + profile.getCard().getYear() + ",\"verification_value\":\"" + profile.getCard().getCode() + "\"}}";
		updateMessage("Submiting payment information.");
		while(response == null && ccVerify == null) {
			try {
				request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
				response = client.execute(request);
				       
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = in.readLine()) != null) 
					result.append(line);
					
				in.close();
				
				String body = result.toString();
				if(response.getStatusLine().getStatusCode() == 200) {
					ccVerify = new JSONObject(body).getString("id");
				} else 
					print("Status Code: " + response.getStatusLine().getStatusCode() + " Body: " + body);
					
			} catch (Exception e ) {
				if(debug) 
					e.printStackTrace();
				else {
					String name = e.getClass().getName();
						
					if(!name.contains("SocketTimeoutException"))
						print("[Exception - checkout(baseUrl)] -> " + name);
				}
			} finally {
				if(request != null)
					request.releaseConnection();
				try {
					if(response != null && response.getEntity() != null)
						EntityUtils.consume(response.getEntity());
				} catch (Exception e) { e.printStackTrace(); }
			}		
		}
		
		//print(ccVerify + ", " + paymentGateway + ", " + token);
		response = null;
		request = new HttpPost("https://" + base + checkout);
		
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.98 Safari/537.36");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, br");
		request.setHeader("Accept-Language", "en-US,en;q=0.8");
		request.setHeader("Cache-Control", "max-age=0");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setHeader("Host", base);
		request.setHeader("Origin", "https://" + base);
		request.setHeader("Referer", "https://" + base + checkout + "?previous_step=shipping_method&step=payment_method");
		request.setHeader("Upgrade-Insecure-Requests", "1");
		
		data.add(new BasicNameValuePair("utf8", "✓"));		
		data.add(new BasicNameValuePair("_method", "patch"));
		data.add(new BasicNameValuePair("button", ""));
		data.add(new BasicNameValuePair("authenticity_token", token));
		data.add(new BasicNameValuePair("previous_step", "payment_method"));
		data.add(new BasicNameValuePair("step", ""));
		data.add(new BasicNameValuePair("s", ccVerify));
		data.add(new BasicNameValuePair("checkout[payment_gateway]", paymentGateway));
		data.add(new BasicNameValuePair("checkout[credit_card][vault]", "false"));
		data.add(new BasicNameValuePair("checkout[different_billing_address]", "false"));
		data.add(new BasicNameValuePair("checkout[shipping_address][first_name]", profile.getShipping().getFirstName()));
		data.add(new BasicNameValuePair("checkout[shipping_address][last_name]", profile.getShipping().getLastName()));
		data.add(new BasicNameValuePair("checkout[shipping_address][address1]", profile.getShipping().getAddress()));
		data.add(new BasicNameValuePair("checkout[shipping_address][address2]", ""));
		data.add(new BasicNameValuePair("checkout[shipping_address][city]", profile.getShipping().getCity()));
		data.add(new BasicNameValuePair("checkout[shipping_address][country]", "United States"));		
		data.add(new BasicNameValuePair("checkout[shipping_address][province]", profile.getShipping().getState().name()));
		data.add(new BasicNameValuePair("checkout[shipping_address][zip]", profile.getShipping().getZip()));		
		data.add(new BasicNameValuePair("checkout[shipping_address][phone]", profile.getShipping().getPhone()));
		data.add(new BasicNameValuePair("checkout[remember_me]", "false"));
		data.add(new BasicNameValuePair("checkout[remember_me]", "0"));
		data.add(new BasicNameValuePair("checkout[vault_phone]", ""));
		data.add(new BasicNameValuePair("checkout[total_price]", price));
		data.add(new BasicNameValuePair("complete", "1"));
		data.add(new BasicNameValuePair("checkout[client_details][browser_height]", "728"));
		data.add(new BasicNameValuePair("checkout[client_details][browser_width]", "1280"));
		data.add(new BasicNameValuePair("checkout[client_details][javascript_enabled]", "0"));

		while(response == null) {
			try {
				request.setEntity(new UrlEncodedFormEntity(data));
				response = client.execute(request);
				
				if(response.getStatusLine().getStatusCode() == 200) {
					Document doc = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "http://" + base);
					
					if(doc.select("input[name=step]").val().equalsIgnoreCase("processing") || doc.select("title").text().indexOf("Processing") > -1) {
						updateMessage("Payment Submited.");
					} else 
						print("Cond1: " + doc.select("input[name=step]").val() + ", Title: " + doc.select("title").text() + ", Html: " + doc.text());
					
					//print(body);
					
				} else 
					print("Status Code: " + response.getStatusLine().getStatusCode());
					
			} catch (Exception e ) {
				if(debug) 
					e.printStackTrace();
				else {
					String name = e.getClass().getName();
						
					if(!name.contains("SocketTimeoutException"))
						print("[Exception - checkout(baseUrl)] -> " + name);
				}
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
	
	public void print(Object text) {
		System.out.println("[" + new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime()) + "][Shopify] " + text.toString());
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

	private String base;
	private String keywords;
	private String size;
	private ProfileObject profile;
	private CookieStore cookies;
	private HttpClient client;
	boolean debug;
}
