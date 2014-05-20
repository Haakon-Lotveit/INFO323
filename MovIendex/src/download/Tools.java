package download;

import static evil.Globals.safeWords;

import java.util.HashMap;
import java.util.Map;
public class Tools {

	public static Map<String, Integer> wordsToFrequencyMap(String words){
		Map<String, Integer> freq = new HashMap<>();
		String onlyNumbers = "^\\d+$"; // Matches a string that consists of solely numerals.
		/* Some basic translation of apostrophic words into something that makes sense, and is then caught by the stop-word
		 * filter instead is happening here. It's not real stemming, and should be put in a config-file,
		 * and run as its own method, but I don't have the time right now */
		for(String hopeFullyWord : words.replaceAll("n't", " not ")
										.replaceAll("'s", " ") // is is a stop-word and the s-genitive is but a suffix to a morphological root anyway.
										.replaceAll("I'm", "I am")
										.replaceAll("'ll", " will")
										.replaceAll("'re", " are ")
										.replaceAll("'d", " had ")
										.replaceAll("'ve", " have ")
				                        .replaceAll("[^\\w]", " ")
				                        .split("\\s+")){
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
