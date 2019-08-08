package sneakerbot.captcha;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

import sneakerbot.controllers.MainController;

public class Harvester extends Thread {
	
	Thread captchaThread;
	String siteKey;
	boolean debug;
	boolean running = false;
	HttpServer server;
	
	public Harvester(String siteKey) {
		captchaThread = null;
		this.siteKey = siteKey;
        running = true;
	}
	
	@Override
	public void run() {
		if(siteKey != null) {
	        SocketConfig socketConfig = SocketConfig.custom()
	                .setSoTimeout(15000)
	                .setTcpNoDelay(true)
	                .build();
	
	        server = ServerBootstrap.bootstrap()
	                .setListenerPort(8080)
	                .setServerInfo("Test/1.1")
	                .setSocketConfig(socketConfig)
	                .registerHandler("*", new HttpHandler(siteKey))
	                .create();
	
	        try {
				server.start();
				//server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
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
				    	HttpHandler.print("Removed " + count + " expired captchas from bank.");   
				    
				    try {
						Thread.sleep(30000L);
					} catch (InterruptedException e) { e.printStackTrace(); }
		    	}
	        };
	        
	        HttpHandler.print("Captcha engine started using sitekey -> " + siteKey);
	        
	        captchaThread = new Thread(task);
	        captchaThread.start();
		}	
	}
	
	public void stopHarvester() {
		running = false;
		server.stop();
        HttpHandler.print("Harvester stopped.");
	}
	
    static class HttpHandler implements HttpRequestHandler  {

    	String sitekey;
    	
        public HttpHandler(String sitekey) {
            super();
        	this.sitekey = sitekey;
        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            
            if (request instanceof HttpEntityEnclosingRequest) {			
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                String token = new String(entityContent);
                MainController.captchas.add(new CaptchaResponse(token.substring(6), System.currentTimeMillis()));
                
                print("Captcha Tokens Harvested: " + MainController.captchas.size());
            }
            
            response.setStatusCode(HttpStatus.SC_OK);
            //String html = "<html>\r\n<head>\r\n  <meta charset=\"utf-8\">\r\n  <title>NanoAIO Harvester</title>\r\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n</head>\r\n\r\n<body>\r\n  <style>\r\n    body {\r\n\t\tvertical-align: middle;\r\n\t\tmargin: 0;\r\n\t\tbackground-color: #0B132B;\r\n\t\toverflow: hidden;\r\n    }  \r\n    #div-captcha {\r\n\t\tright: 50%;\r\n\t\tbottom: 50%;\r\n\t\ttransform: translate(50%,50%);\r\n\t\tposition: absolute;\r\n    }\r\n  </style>\r\n  \r\n<script type='text/javascript' src='https://www.google.com/recaptcha/api.js'></script>\r\n<center>\r\n<div id=\"div-captcha\">\r\n<div style=\"opacity: 0.9\" class=\"g-recaptcha\" data-sitekey=" + sitekey + ">\r\n</div>\r\n</div>\r\n</center>\r\n\r\n<script>\r\n\r\nwindow.setInterval(function(){\r\n    var token = document.getElementById('g-recaptcha-response').value;\r\n    document.getElementById('g-recaptcha-response').value = '';\r\n    if(token != '') {\r\n    var http = new XMLHttpRequest();\r\n    var params = \"token=\" +token;\r\n    http.open(\"POST\", \"*\", true);\r\n    http.setRequestHeader(\"Content-type\", \"application/x-www-form-urlencoded\");\r\n    http.setRequestHeader(\"Content-length\", params.length)\r\n    http.send(params);\r\n    location.reload();\r\n}\r\n}, 500);\r\n</script>\r\n</body></html>\r\n";
            String html = "<html>\r\n<head>\r\n  <meta charset=\"utf-8\">\r\n  <title>NanoAIO Harvester</title>\r\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n</head>\r\n\r\n<body>\r\n  <style>\r\n    body {\r\n\t\tvertical-align: middle;\r\n\t\tmargin: 0;\r\n\t\tbackground-color: #0B132B;\r\n\t\toverflow: hidden;\r\n    }  \r\n    #submit {\r\n\t\tright: 50%;\r\n\t\tbottom: 50%;\r\n\t\ttransform: translate(50%,50%);\r\n\t\tposition: absolute;\r\n    }\r\n  </style>\r\n  \r\n  <script>\r\n\tfunction onSubmit(token) {\r\n\t\twindow.setInterval(function() {\r\n\t\t\tif(token != '') {\r\n\t\t\t\tvar http = new XMLHttpRequest();\r\n\t\t\t\tvar params = \"token=\" +token;\r\n\t\t\t\thttp.open(\"POST\", \"*\", true);\r\n\t\t\t\thttp.setRequestHeader(\"Content-type\", \"application/x-www-form-urlencoded\");\r\n\t\t\t\thttp.setRequestHeader(\"Content-length\", params.length)\r\n\t\t\t\thttp.send(params);\r\n\t\t\t\tlocation.reload();\r\n\t\t\t}\r\n\t\t}, 500);\r\n\t}\r\n\r\n\tfunction validate(event) {\r\n\t\tevent.preventDefault();\r\n\t\tgrecaptcha.execute();\r\n\t}\r\n\r\n\tfunction onload() {\r\n\t\tvar element = document.getElementById('submit');\r\n\t\telement.onclick = validate;\r\n\t}\r\n  </script>\r\n<script type='text/javascript' src='https://www.google.com/recaptcha/api.js'></script>\r\n<center>\r\n   <form>\r\n     <div id='recaptcha' class=\"g-recaptcha\"\r\n          data-sitekey=" + sitekey  + "\r\n          data-callback=\"onSubmit\"\r\n          data-size=\"invisible\"></div>\r\n     <button id='submit'>Harvest Captcha</button>\r\n   </form>\r\n</center>\r\n<script>onload();</script>\r\n</body></html>";
            
            StringEntity entity = new StringEntity(html,
                    ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);

        }
        
    	public static void print(String text) {
    		System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "][Harvester] " + text);
    	}
    }
    
    public static class CaptchaResponse {
    	
    	public CaptchaResponse(String token, long time) {
    		this.token = token;
    		this.time = time;
    	}
    	
    	public String getToken() {
    		return token;
    	}
    	
    	public long getTime() {
    		return time;
    	}
    	
    	public boolean isExpired() {
    		return (time + 115000L) < System.currentTimeMillis();
    	}
    	
    	private String token;
    	private long time;
    }
}
