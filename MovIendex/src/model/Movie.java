package model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import download.Wikindexer;
import evil.Globals;

public class Movie {
	private Map<String, String> data;
	private Map<String, Map<String, Integer>> wordFrequencies;
	private Map<String, Integer> sumWordFrequencies;
	
	private String summary;
	
	private void init(){
		this.data = new HashMap<>();
		this.wordFrequencies = new HashMap<>();
		this.sumWordFrequencies = new HashMap<>();
		
		/* This code is safe to delete, it's just some manual testing.
		 * I chose to let it stick around in case I ever have to rerun these sorts of things. /*
		Map<String, Integer> testMap = new HashMap<>();
		testMap.put("schadenfreude", 50);
		testMap.put("zeitgeist", 25);
		testMap.put("kindergarten", 10);
		this.wordFrequencies.put("testing", testMap);
		this.addToSumWordFrequencies(testMap);
		/**/
	}
	
	public Movie(){
		init();
	}
	
	public Map<String, Integer> getWordFrequencies(){
		return sumWordFrequencies;
	}
	
	public Map<String, Integer> getWordFrequencyFrom(String source){
		if(wordFrequencies.containsKey(source)){
			return wordFrequencies.get(source);
		}
		return new HashMap<>();
	}
	public Movie setData(String name, String datum){
		this.data.put(name, datum);
		return this;
	}
	
	public Movie setAbstract(String summary){
		this.data.put("abstract", summary);
		this.summary = summary;
		return this;
	}
	
	public String getAbstract(){
		return summary;
	}
	
	public String getData(String name){
		return this.data.get(name);
	}
	
	public Collection<String> getDataNames(){
		return this.data.keySet();	
	}
	
	public Movie downloadWikipedia(){
		try {
			Map<String, Integer> wikiFreq = new Wikindexer().indexMovie(this.getData("title"), this.getData("year"), Globals.safeWords);
			this.wordFrequencies.put("en.wikipedia.org", wikiFreq);
			this.addToSumWordFrequencies(new Wikindexer().indexMovie(this.getData("title"), this.getData("year"), Globals.safeWords));
		} catch (IOException e) {
			// Not much we can do about IOException at this stage. :(
			e.printStackTrace();
		}
		return this;
	}
	
	@Override
	public String toString(){
		return new StringBuilder()
			.append("[Movie: <Data: ")
			.append(stringifyMap(this.data))
			.append(addFrequenciesString(this.wordFrequencies))
			.append(">]")
			.toString();
	}
	
	private String addFrequenciesString(Map<String, Map<String, Integer>> freqs){
		StringBuilder sb = new StringBuilder().append("<Frequencies:");
		
		for(String sourceName : freqs.keySet()){
			int sumWords = 0;
			for(Integer i : freqs.get(sourceName).values()){
				sumWords += i;
			}
			sb.append(String.format(" %s (%d words)",
					                sourceName,
					                sumWords));
		}
		
		/* The sum of all frequencies */
		int sum = 0;
		for(Integer frequency : sumWordFrequencies.values()){
			sum += frequency;
		}
		sb.append(". Altogether ").append(sum).append(" words>");
		
		
		return sb.toString();
	}
	
	private void addToSumWordFrequencies(Map<String, Integer> freq){
		for(String word : freq.keySet()){
			int frequency = freq.get(word);
			
			if(sumWordFrequencies.containsKey(word)){
				frequency += sumWordFrequencies.get(word);
			}
			
			sumWordFrequencies.put(word, frequency);
		}
	}
	
	private String stringifyMap(Map<String, String> map){
		StringBuilder sb = new StringBuilder();
		for(String key : map.keySet()){
			sb.append(String.format("(%s->%s) ", key, map.get(key)));
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Movie frozen = Movie.makeSampleMovie();
		
		System.out.println(frozen);
		System.out.println(frozen.getWordFrequencyFrom("en.wikipedia.org"));
		System.out.println(frozen.getWordFrequencies());
		
	}
	
	
	public static Movie makeSampleMovie(){
		return new Movie().setData("title"   , "Frozen")
						  .setData("studio"  , "Walt Disney Pictures")
						  .setData("year"    , "2013")
						  .setData("producer", "Peter Del Vecho")
						  .downloadWikipedia();
	}
}
