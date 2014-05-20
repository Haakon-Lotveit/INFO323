package experimentation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import download.Tools;

public class JSuppeTesting {
	public static void main(String[] args) throws Exception {
		final Map<String, Integer> wordFreq = new JSuppeTesting("Shrek", "2001").download();
		System.out.println(wordFreq);
		List<String> wordList = new LinkedList<>(wordFreq.keySet());
		
		Collections.sort(wordList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return - (wordFreq.get(o1) - wordFreq.get(o2));
			}
		});
		
		for(String s : wordList){
			System.out.printf("%s\t\t%d%n", s, wordFreq.get(s));
		}
	}
	
	private final String FILM, YEAR, HOST;
	public JSuppeTesting(final String film, final String year){
		this.FILM = film;
		this.YEAR = year;
		this.HOST = "www.imsdb.com";
	}
	
	public Map<String, Integer> download() throws Exception {
		
		/*
		 * Step 1:
		 * Search the website for the gosh-darned script.
		 * This is an interesting exercise, because you have to post data to the webserver.
		 * The problem with this from a UX perspective is that you can't search for something,
		 * and bookmark the result, or share it with anybody else. And there's no reason not to make this possible.
		 */
		Document search = Jsoup.connect("http://www.imsdb.com/search.php")
							   .data("search_query", FILM)
							   .userAgent("SomePoorStudent")
							   .post();
		
		
		/*
		 * Step 2: So now we've got a results page.
		 * Of course, the results page is shit, (doesn't validate either)
		 * so we have to carefully sift through the crap to find what we want.
		 * Notice how everything is in a table. It's like it's 2001, and some drooling kid just pirated MS Frontpage.
		 * Anyway, what we do is that we find every anchor (<a> tag) that might be a search result, and stash it in a list for processing.
		 */
		List<Element> possibleLinks = new LinkedList<>();
		for(Element e : search.select("td")){
			/* Which is why we're pretty much just grepping for a specific string. A little markup goes a long way fellas. */
			if(e.toString().contains(String.format("<h1>Search results for '%s'</h1><p>", FILM))){
				for(Element someAnchor : e.select("a")){
					if(someAnchor.toString().length() != 0){
						possibleLinks.add(someAnchor);
					}
				}
			}
		}

		/*
		 * Step 3: The searching and sorting
		 * So now that we've got our anchors, we want to sort them after the probability that they are the link we want to get.
		 * Then we check if the first link actually refers to the film we're after. If it *does*, we're going to go hunting for the script-page.
		 * If it *doesn't* we're screwed, there's no script.
		 */
		findBest(possibleLinks, FILM, YEAR);
		String link = String.format("http://%s%s",
									HOST,
									possibleLinks.get(0).attr("href"));
		if(possibleLinks.get(0).toString().contains(FILM)){
			/*
			 * Step 4: The frustrationing.
			 * Of course, the link to the script is not a link to the script. It's a link to some bullshit page
			 * where people can tell us what they think of the movie, and has links to desktop wallpapers and stuff like that.
			 * So we need to find the link to the actual script. Remember kids, your link might say it's the script, but it's probably not.
			 * Why? My guess is to show more commercials. It's not a dumb idea, but it's frustrating.
			 * We find the link by grepping again.
			 * Because marking up the special link with some special attribute would be too hard for the webmaster I guess.
			 */
			Document movieBaitPage = Jsoup.connect(link).get();
			String pretext = "Read ";   /* So basically, the way this bs works is that the text in the <a> tag will read “Read "<movie title>" Script”. */
			String context = " Script"; /* I made a funny! :D */
			
			for(Element a : movieBaitPage.select("a")){
				if(a.text().contains(pretext) && a.text().contains(context)){
					/* 
					 * This is a descriptive name for a webpage that has two <html> tags 
					 * and consists entirely of tables nested within tables.
					 * It also closes tags more than once at times.
					 * I have no idea how JSoup manages to parse this crap.
					 * I'm sorry for putting it through this.
					 */
					Document unholyGibberish = Jsoup.connect(String.format("http://%s%s", HOST, a.attr("href"))).get();
					for(Element td : unholyGibberish.select("td")){
						/* This is sort of a lie. This webpage has no class. *Badumpsh.wav* */
						if(td.hasAttr("class") && td.attr("class").equalsIgnoreCase("scrtext")){
							Map<String, Integer> script = Tools.wordsToFrequencyMap(td.text());
							return script;
						}
					}
				}
			}
		}
		/* Or we didn't find any links. In that case, we have no script to return
		 * so we just return an empty hashmap. */
		return new HashMap<>(); 
	}
	
	/* This looks a lot like the same sort of thing in Wikindexer, except Wikipedia is a good site. */
	private static void findBest(List<Element> links, final String title, final String year){
		Collections.sort(links, new Comparator<Element>() {

			@Override
			public int compare(Element e1, Element e2) {
				int e1Score = 0,
					e2Score = 0;
				
				int maxBonus = e1.text().length() + e2.text().length();
				/* Shortest text is more likely to be correct, unless the search engine is truly retarded.
				 * tbh, there is a real possibility that this is indeed the case, but we still need a tiebreaker. ;_;  */
				e1Score += maxBonus - e1.text().length();
				e2Score += maxBonus - e2.text().length();
				
				if(e1.toString().contains(year)){
					e1Score += maxBonus;
				}
				if(e2.toString().contains(year)){
					e2Score += maxBonus;
				}
				if(e1.toString().contains(title)){
					e1Score += maxBonus;
				}
				if(e2.toString().contains(title)){
					e2Score += maxBonus;
				}
				return e2Score - e1Score; // The smallest item goes first, and the ones with the highest score should go first. Therefore this inversion
			}
		});
	}
	
}
