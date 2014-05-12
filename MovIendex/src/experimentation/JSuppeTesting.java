package experimentation;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JSuppeTesting {
	public static void main(String[] args) throws Exception {
		String wikipediaDomain = "en.wikipedia.org";
		String film = "Brother Bear";
		String year = "2003";

		/**
		 * safeWords makes us stop immediately, like good people.
		 */
		Set<String> safeWords = new HashSet<>();
		safeWords.add("wikimedia");
		safeWords.add("wikipedia");
		safeWords.add("is");
		safeWords.add("a");
		safeWords.add("registered");
		safeWords.add("trademark");
				
		Map<String, Integer> wordFrequency = new HashMap<>();
		
		List<String> candidates = new LinkedList<>();
		Document doc = null;
		boolean noPage = false;
		try{
			doc = Jsoup.connect(String.format("http://%s/wiki/%s", wikipediaDomain, wikiEscapeChars(film))).get();
		}
		catch(IOException ioe){
			if(ioe.toString().contains("Status=404")){
				noPage = true;
			}
		}
		// If we 404
		if(noPage){
			System.out.println("NO DATA");
		}
		else{
			//<p><b>Frozen</b> may refer to:</p>
			boolean disambigPage = doc.toString().contains(String.format("<p><b>%s</b> may refer to:</p>", film));


			if(disambigPage){
				for(Element e : doc.body().select("li")){
					for(Element anchor : e.select("a")){
						if(anchor.toString().matches(".*[F|f]ilm.*")){
							candidates.add(anchor.attr("href"));
						}
					}
				}

				String best = String.format("http://%s%s", wikipediaDomain, bestUrl(candidates, year));
				doc = Jsoup.connect(best).get();
			}
			
			/*
			 * So now we've got a page. How do we get the text out of it?
			 * We grab all the paragraphs, and remove all the references, they're of no import.
			 * Then we replace all non-word characters with space, and split on whitespace.
			 * I WISH I HAD JAVA 8 FOR THIS.
			 * PLEASE BE CAN IT BE STREAMS TIEM SOON PLOX?
			 */
			
			for(String hopeFullyWord : doc.select("p").text().replaceAll("\\[\\d+\\]", " ").replaceAll("[^\\w]", " ").split("\\s+")){
				String cand = hopeFullyWord.toLowerCase();
				if(!safeWords.contains(cand) && !cand.matches("^\\d+$")){
					if(!wordFrequency.containsKey(cand)){
						wordFrequency.put(cand, Integer.valueOf(1));
					}
					else{
						wordFrequency.put(cand,
								          wordFrequency.get(cand) + 1);
					}
				}
			}
			
			System.out.println(wordFrequency);
		}
	}
	
	
	
	
	
	/**
	 * Escapes some of the characters for wikipedia. This is not necessary, but might lighten the load on their servers somewhat.
	 * @param escapeMe The string that is to be escaped, may never be null.
	 * @return the string with offending characters escaped.
	 */
	public static String wikiEscapeChars(String escapeMe){
		return escapeMe.replace(' ', '_');
	}
	/**
	 * Finner den beste strengen som representerer en URL fra en liste URLer.
	 * Den gjør dette vha sortering som følger:
	 * Dersom du er fra samme år som filmen får du en haug med poeng.
	 * Dersom du er kortere en en annen film, så får du også poeng.
	 * Den med mest poeng er den beste filmen.
	 * 
	 * Det er ordnet slik at du ALLTID får fler poeng for å være fra korrekt år, enn for å være kort.
	 * 
	 * @param urls urlene som skal velges mellom
	 * @param year året filmen kom ut
	 * @return urlen med høyest poengsum
	 */
	public static String bestUrl(List<String> urls, final String year){
		Comparator<String> cmp = new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {				
				int s1Score = 0, s2Score = 0;
				// Make the score for hitting the correct year high enough that it cannot be dwarfed by string lengths.
				// Note that if strings get long enough, this won't help us, but since valid URLs are defined with a maximum length, we should be good here.
				int yearScore = s1.length() + s2.length();
				s1Score -= s1.length();
				s2Score -= s2.length();
				if(s1.contains(year)){
					s1Score += yearScore;
				}
				if(s2.contains(year)){
					s2Score += yearScore;
				}				
				return s2Score - s1Score;
			}

		};
		Collections.sort(urls, cmp);
		return urls.get(0);
	}
}
