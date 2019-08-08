package sneakerbot.util;

import java.util.Arrays;
import java.util.stream.Stream;

public class JigAddress {
	
	static String[][] TRANSLATIONS = new String[][] { 
		  {"street", "st.", "st"},
		  {"drive", "dr.", "dr"},
		  {"lane", "ln.", "ln"},
		  {"avenue", "ave.", "ave"},
		  {"west", "w.", "w"},
		  {"east", "e.", "e"},
		  {"north", "n.", "n"},
		  {"south", "s.", "s"},
		  {"east", "e.", "e"},
		  {"boulevard", "blvd", "blvd."},
		  {"mountain", "mtn.", "mtn"}
		};
	
	public static String jigAddress(String address) {
		Stream<Object> stream = Arrays.stream(TRANSLATIONS).flatMap(Arrays::stream);
		
		stream.forEach(z -> {
			System.out.println(z);
		});
		
		return null;
	}

}
