package evil;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Globals {
	public static Set<String> safeWords;
	
	static {
		/* Set up safeWords */
		safeWords = new HashSet<>();
		File f = new File("stop-words.txt");
		try(Scanner rdr = new Scanner(f)){
			while(rdr.hasNext()){
				safeWords.add(rdr.next().trim().toLowerCase());
			}
		} catch (Exception e) {
			System.err.printf("Error downloading file \"%s\"%n", f.getAbsolutePath());
			e.printStackTrace();
		}
		
		
	}
}
