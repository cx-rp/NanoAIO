package sneakerbot.loaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import sneakerbot.controllers.MainController;
import sneakerbot.controllers.SettingsController;

public class Profile {
	
	public static HashMap<String, ProfileObject> load() {
        File folder = new File("data/profiles"); 
        HashMap<String, ProfileObject> profiles = new HashMap<String, ProfileObject>();
        
        if(!folder.exists()) {
        	System.out.println(folder.getName() + " does not exist; One has been created for you.");
        	folder.mkdirs();
        	return profiles;
        }
        
        Type type = new TypeToken<ProfileObject>() { }.getType();
        
        for (File file : folder.listFiles()) {
    		try {
    			profiles.put(file.getName().substring(0, file.getName().lastIndexOf(".")), new GsonBuilder().create().fromJson(new FileReader("data/profiles/" + file.getName()), type));
    		} catch (JsonIOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JsonSyntaxException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}      	
        }
		
		return profiles;
	}
	
	public static void save() {
		for(Entry<String, ProfileObject> profiles : MainController.profiles.entrySet()) 
			save(profiles.getKey(), profiles.getValue());
		
	}
	
	public static void save(String name, ProfileObject profile) {
	       try (FileWriter writer = new FileWriter("data/profiles/" + name + ".json")) {
	    	   new GsonBuilder().enableComplexMapKeySerialization()
	           .setPrettyPrinting().create().toJson(profile, writer);
	        } catch (IOException e) { e.printStackTrace(); }
	       
			MainController.profiles.put(name, profile);
	}
	
	
	
	public static class ProfileObject {
		
		public ProfileObject(String email, Shipping shipping, CreditCard card) {
			super();
			this.email = email;
			this.card = card;
			this.shipping = shipping;
		}
		
		public String getEmail() {
			return email;
		}
		
		public CreditCard getCard() {
			return card;
		}
		
		public Shipping getShipping() {
			return shipping;
		}
	
		@Override
		public String toString() {
			return "CredentialObject [email=" + email + ", card=" + card + ", shipping=" + shipping + "]";
		}

		private String email;
		private CreditCard card;
		private Shipping shipping;
		
	}
	
	public static class Shipping {
		
		public Shipping(String firstName, String lastName, String address, String address2,  String city, SettingsController.State state,
				String zip, String phone) {
			super();
			this.firstName = firstName;
			this.lastName = lastName;
			this.address = address;
			this.address2 = address2;
			this.city = city;
			this.state = state;
			this.zip = zip;
			this.phone = phone;
		}

		public String getFirstName() {
			return firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public String getAddress() {
			return address;
		}
		
		public String getAddress2() {
			return address2;
		}
		
		public String getCity() {
			return city;
		}
		
		public SettingsController.State getState() {
			return state;
		}
		
		public String getZip() {
			return zip;
		}
		
		public String getPhone() {
			return phone;
		}
		
		@Override
		public String toString() {
			return "Shipping [firstName=" + firstName + ", lastName=" + lastName + ", address=" + address
					+ ", address2=" + address2 + ", city=" + city + ", state=" + state + ", zip=" + zip + ", phone="
					+ phone + "]";
		}

		private String firstName;
		private String lastName;
		private String address;
		private String address2;
		private String city;
		private SettingsController.State state;
		private String zip;
		private String phone;
		
	}
	
	public static class CreditCard {
		
		public CreditCard(String number, String month, String year, String code) {
			super();
			this.number = number;
			this.month = month;
			this.year = year;
			this.code = code;
		}
		
		public String getNumber() {
			return number;
		}
		
		public String getMonth() {
			return month;
		}
		
		public String getYear() {
			return year;
		}
		
		public String getCode() {
			return code;
		}
		
		@Override
		public String toString() {
			return "CreditCard [number=" + number + ", month=" + month + ", year=" + year + ", code="
					+ code + "]";
		}

		private String number;
		private String month;
		private String year;
		private String code;
	}

}
