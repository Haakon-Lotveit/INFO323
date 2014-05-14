package download;

import java.util.HashMap;
import java.util.Map;

import static evil.Globals.safeWords;
public class FrequenceTools {

	public static Map<String, Integer> wordsToFrequencyMap(String words){
		Map<String, Integer> freq = new HashMap<>();
		String onlyNumbers = "^\\d+$"; // Matches a string that consists of solely numerals.
		
		for(String hopeFullyWord : words.replaceAll("[^\\w]", " ").split("\\s+")){
			String cand = hopeFullyWord.toLowerCase();
			if(!safeWords.contains(cand) && !cand.matches(onlyNumbers)){
				if(!freq.containsKey(cand)){
					freq.put(cand, Integer.valueOf(1));
				}
				else{
					freq.put(cand, freq.get(cand) + 1);
				}
			}
		}

		return freq;
	}
}
