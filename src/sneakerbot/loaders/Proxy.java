package sneakerbot.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Proxy {
	
	public static ArrayList<ProxyObject> load(String name) {
        File file = new File(name); 
        ArrayList<ProxyObject> proxies = new ArrayList<ProxyObject>();
        
        if(!file.exists()) {
        	System.out.println(name + " does not exist!");
        	return proxies;
        }
        
        BufferedReader in = null;
        String input = null;
        
        try {
            in = new BufferedReader(new FileReader(file));

            try {
                while ((input = in.readLine()) != null) {
                	
                	if(input.isEmpty())
                		break;
                	
                	try {
	                    String[] split = input.split(":");
	                    String address = split[0];
	                    int port = Integer.parseInt(split[1]);
	                    String username = split.length >= 3 ? split[2] : null;
	                    String password = split.length == 4 ? split[3] : null;
	                    
	                    proxies.add(new ProxyObject(address, port, username, password));
                	} catch (Exception e) {  e.printStackTrace(); }
                }
            } catch (IOException e) {  e.printStackTrace(); } finally { in.close(); }
        } catch (Exception e) {
        	e.printStackTrace();
        	
        }
       // System.out.println(proxies.size() + " proxies loaded!");
        return proxies;
	}
	
	public static class ProxyObject {
		
		public ProxyObject(String address, int port, String username, String password) {
			this.address = address;
			this.port = port;
			this.username = username;
			this.password = password;
		}
		
		public String getAddress() {
			return address;
		}
		
		public void setAddress(String address) {
			this.address = address;
		}
		
		public int getPort() {
			return port;
		}
		
		public void setPort(int port) {
			this.port = port;
		}
		
		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public String toString() {
			return address + ":" + port + (password == null ? "" : ":" + username + ":"+ password);
		}

		private String address;
		private int port;
		private String username;
		private String password;	
	}
}