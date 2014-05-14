package model;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import download.Tools;
import download.Wikindexer;
import evil.Globals;
import experimentation.HTTPTesting.OmdbRequestBuilder;

public class Movie {
	private Map<String, String> data;
	private Map<String, Map<String, Integer>> wordFrequencies;
	private Map<String, Integer> sumWordFrequencies;
	
	private String summary;

	/**
	 * Sets up the data-structures etc. that are needed
	 */
	private void init(){
		this.data = new HashMap<>();
		this.wordFrequencies = new HashMap<>();
		this.sumWordFrequencies = new HashMap<>();
		
	}
	
	/**
	 * Creates a new movie with no data.
	 * You must add data with calls to {@link Movie#setData(String, String) setData(String, String)}
	 * The only pieces of data you need to add are "title" and "year". These allows the movie object to download the rest itself.
	 */
	public Movie(){
		init();
	}
	
	/**
	 * Lets you get the word-frequencies of the entire movie.
	 * This is the map of all frequencies of all words, from all sources.
	 * 
	 * @return the word-frequencies from every source.
	 */
	public Map<String, Integer> getWordFrequencies(){
		return sumWordFrequencies;
	}
	
	/**
	 * Let's you get a wordFrequency from a specific source.
	 * Currently supported are "en.wikipedia.org" for wikipedia data </br>
	 * Planned are "plot" for the OMDB plot synopsis and "tomato-consensus" for the RottenTomato description of the movie. </br>
	 * Planned in the future are "metadata" for grabbing all the metadata and search through that in addition to the other things.
	 * @param source the name of the source you want word-frequencies for
	 * @return either the corresponding map, or an empty one if no such map was found.
	 */
	public Map<String, Integer> getWordFrequencyFrom(String source){
		if(wordFrequencies.containsKey(source)){
			return wordFrequencies.get(source);
		}
		return new HashMap<>();
	}
	
	/**
	 * Sets any piece of metadata of this movie.
	 * @param name the name of the metadata. Some names are required by the Movie object in order to download information from the internet,
	 * namely the "title" and "year" pieces of metadata.
	 * @param datum the data that you want to set.
	 * @return this Movie object, so that you may chain calls
	 */
	public Movie setData(String name, String datum){
		this.data.put(name, datum);
		return this;
	}
	
	/**
	 * Sets the magical metadata known as the abstract.
	 * If you use the normal {@link Movie#download() download()} method, then
	 * this will be set to the plot-synopsis offered by OMDB. (by standard a long one.) 
	 * @param summary the text that will replace the current summary
	 * @return this movie object, so that you may chain calls
	 */
	public Movie setAbstract(String summary){
		this.data.put("abstract", summary);
		this.summary = summary;
		return this;
	}
	
	/**
	 * Returns the magical metadata known as the abstract.
	 * Will be removed at some point.
	 * @return the summary of this movie.
	 */
	public String getAbstract(){
		return summary;
	}
	
	/**
	 * Gets a particular piece of metadata.
	 * @param name the name of the metadata. Use {@link Movie#getDataNames()} if you need to programmatically get the list of them.
	 * @return either a string, or a null if no data is found. Remember that nulls are evil,
	 * 	       either make sure that you're not getting one, or make sure that you cannot get one.
	 */
	public String getData(String name){
		return this.data.get(name);
	}
	
	/**
	 * This is useful if you want to know WHAT metadata is available, although not necessarily what it is.
	 * If you want to iterate over all the metadata, you could do something like 
	 * for(String key : movie.getDataNames(){ doSomething(movie.getData(key)); }
	 * 
	 * @return the set of metadata keys that can be used in tandem with getData to get metadata about the movie.
	 */
	public Collection<String> getDataNames(){
		return this.data.keySet();	
	}
	
	/**
	 * This method will be renamed and made private in the future. Use at own risk.
	 * This downloads information from wikipedia, and assumes that the year and title variables in the data-map have been set.
	 * If they have not been set, things will crash. No errors for you.
	 * @return this movie object, so that calls may be chained.
	 */
	private Movie downloadWikipedia(){
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
			.append("> ")
			.append(addFrequenciesString(this.wordFrequencies))
			.append("]")
			.toString();
	}
	
	/**
	 * Creates a prettyprinted version of the word-frequency maps.
	 * @param freqs wordFreqiencies, pretty much. It's parametrized just to keep things flexible,
     * 		  so that if I ever need to massage other maps into this form, I could.
     * 
	 * @return a prettyprinted string of the word-frequencies.
	 */
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
	
	
	/**
	 * This creates a better looking string of all the metadata in the movie-object.
	 * @param map The map to stringify (the only real candidate here is the data map. (Map:String→String)
	 * @return a prettyprinted version of the map, for use by {@link Movie#toString()}
	 */
	private String stringifyMap(Map<String, ? extends Object> map){
		StringBuilder sb = new StringBuilder();
		for(String key : map.keySet()){
			sb.append(String.format("(%s->%s) ", key, map.get(key)));
		}
		return sb.toString();
	}
	
	/**
	 * Takes a frequency-map and adds its contents to the sum of frequencies map (sumWordFrequencies : Map<String, Integer>)
	 * @param freq the frequency map to add, cannot be null. If it's null it will crash. Nulls are evil. Do not be evil, that's Google's job.
	 */
	private void addToSumWordFrequencies(Map<String, Integer> freq){
		for(String word : freq.keySet()){
			int frequency = freq.get(word);
			
			if(sumWordFrequencies.containsKey(word)){
				frequency += sumWordFrequencies.get(word);
			}
			
			sumWordFrequencies.put(word, frequency);
		}
	}

	public Movie download() throws Exception {
		String req = new OmdbRequestBuilder().title(this.getData("title"))
				.year(this.getData("year"))
				.getLongPlot()
				.returnJSON()
				.getTomatoData()
				.request();
		//		http://www.omdbapi.com/?t=Frozen&y=2013&plot=full&tomatoes=false&response=JSON

		Map<String,String> json = new HashMap<String,String>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			//convert JSON string to Map
			json = mapper.readValue(req, 
					new TypeReference<HashMap<String,String>>(){});

		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Integer> rottenTomatoes = new HashMap<>();
		Map<String, Integer> omdbPlot = new HashMap<>();
		this.setAbstract(json.get("tomatoConsensus"))
		 .setData("website", json.get("Website"))
		 .setData("poster_url", json.get("Poster"))
		 .setData("age-rating", json.get("Rated"))
		 .setData("imdbRating", json.get("imdbRating"))
		 .setData("actors", json.get("Actors"))
		 .setData("metascore", json.get("Metascore"))
		 .setData("tomato-meter", json.get("tomatoMeter"))
		 .setData("tomato-user-rating", json.get("tomatoUserRating"))
		 .setData("genre", json.get("Genre"))
		 .setData("awards", json.get("Awards"))
		 .setData("director", json.get("Director"))
		 .setData("tomato-rating", json.get("tomatoRating"))
		 .setData("runtime", json.get("Runtime"))
		 .setData("writer", json.get("Writer"))
		 .setData("production", json.get("Production"))
		 .setData("language", json.get("Language"))
		 .setData("tomato-user-meter", json.get("tomatoUserMeter"))
		 .downloadWikipedia();
		Map<String, Integer> plotFreq = Tools.wordsToFrequencyMap(json.get("Plot"));
		Map<String, Integer> consensusFreq = Tools.wordsToFrequencyMap(json.get("tomatoConsensus"));
		
		return this;
	}



	/* TODO: Move these into javadocs for the class.
	s (NEW!) 	string (optional) 	title of a movie to search for
	i 	string (optional) 	a valid IMDb movie id
	t 	string (optional) 	title of a movie to return
	y 	year (optional) 	year of the movie
	r 	JSON, XML 	response data type (JSON default)
	plot 	short, full 	short or extended plot (short default)
	callback 	name (optional) 	JSONP callback name
	tomatoes 	true (optional) 	adds rotten tomatoes data 
	 */
	/**
	 * A builder that returns a valid request for the OMDB api.
	 * This does not allow you to build ALL the requests you may want,
	 * but sticks to what is deemed an acceptable amount.
	 * (You can't set the s-parameter, and it doesn't take an IMDB identifier)
	 * 
	 * It's made for an INFO323 project, 
	 * and is not supposed to be some great greatness that solves everything for everyone.
	 * (IOW, if it doesn't work for you, build your own builder. ^_^)
	 * 
	 * @author Haakon Løtveit (email: haakon.lotveit@student.uib.no)
	 *
	 */
	public static class OmdbRequestBuilder {
		String title, year;
		boolean tomatoData, callbackName, longPlot, returnXML;

		public OmdbRequestBuilder(){
			title = year = null;
			tomatoData = longPlot = returnXML = false;
		}

		public boolean validate(){
			return (null != title &&
					null != year);
		}

		public OmdbRequestBuilder getTomatoData(){
			this.tomatoData = true;
			return this;
		}

		public OmdbRequestBuilder dontGetTomatoData(){
			this.tomatoData = false;
			return this;
		}

		public OmdbRequestBuilder getShortPlot(){
			this.longPlot = false;
			return this;
		}

		public OmdbRequestBuilder getLongPlot(){
			this.longPlot = true;
			return this;
		}

		public OmdbRequestBuilder returnXML(){
			this.returnXML = true;
			return this;
		}

		public OmdbRequestBuilder returnJSON(){
			this.returnXML = false;
			return this;
		}

		public OmdbRequestBuilder title(String title){
			this.title = title;
			return this;
		}

		public OmdbRequestBuilder year(String year){
			this.year = year;
			return this;
		}

		public String request() throws Exception {
			if(!validate()){
				throw new Exception("you haven't instantiated everything!");
			}
			String host = "www.omdbapi.com";
			String target = String.format("/?t=%s&y=%s&plot=%s&tomatoes=%s&response=%s", 
					title,
					year,
					longPlot?   "full" : "short",
					tomatoData? "true" : "false",
					returnXML?  "XML"  : "JSON");
			
			String req = Tools.get(host, 80, target);
			return req;

		}
	}
	
	
	/**
	 * This creates a movie-object of the Disney-move Frozen from 2013, and prints it to the standard out.
	 * @param args are ignored.
	 */
	public static void main(String[] args) throws Exception {
		Movie frozen = Movie.makeSampleMovie();
		
		System.out.println(frozen);
		
		System.out.println("Type the word you want the frequency for. !exit ends the program");
		try(Scanner kb = new Scanner(System.in)){
			String word = "";
			while(!word.equals("!exit")){
				word = kb.nextLine().trim().toLowerCase();
				System.out.println(frozen.getWordFrequencies().get(word));
			}
		}
		
	}
	
	/**
	 * Create a sample movie object for demo-purposes.
	 * 
	 * @return a movie-object based on Disney's "Frozen" from 2013. 
	 * @throws Exception if download borks.
	 */
	public static Movie makeSampleMovie() throws Exception{
		return new Movie().setData("title"   , "Frozen")
						  .setData("studio"  , "Walt Disney Pictures")
						  .setData("year"    , "2013")
						  .setData("producer", "Peter Del Vecho")
						  .download();
	}
}
