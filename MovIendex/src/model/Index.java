package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A basic table of words, movies and their number of entries.
 * If you mutate the Movie objects after indexing them the indexer will fuck up in interesting ways.
 * You have been warned.
 * 
 * @author Haakon Løtveit (haakon.lotveit@student.uib.no)
 *
 */
public class Index {
	Map<String, Map<Movie, Integer>> table;
	Map<Movie, String> abstracts;
	final Set<String> stopWords = evil.Globals.safeWords;
	private void init(){
		table = new HashMap<>();
	}
	
	public Index(){
		init();
	}
	
	public Index addMovie(Movie mov){
		/*
		 * Start by going over the data and abstracts.
		 */
		Set<String> words = new HashSet<>();
		for(String datapoint : mov.getDataNames()){
			/* Hopelessly naïve way to get words out of a string. */
			for(String hopefullyAWord : mov.getData(datapoint).replaceAll("\\W", " ").replaceAll("\n", " ").toLowerCase().split("\\s")){
				if(!stopWords.contains(hopefullyAWord) && hopefullyAWord.length() > 0){
					words.add(hopefullyAWord);
				}
			}			
		}
		
		for(String searchTerm : words){
			if(!table.containsKey(searchTerm)){
				HashMap<Movie, Integer> hash = new HashMap<>();
				hash.put(mov, Integer.valueOf(1));
				table.put(searchTerm, hash);
			}
			else{
				Map<Movie, Integer> submap = table.get(searchTerm);
				if(!submap.containsKey(mov)){
					submap.put(mov, 1);
				}
				else {
					submap.put(mov, submap.get(mov) + 1);
				}
			}
		}
		
		/*
		 * Then consume their word-frequencies
		 */
		// TODO: ↑
		
		return this;
	}
	
	public static void main(String[] args) throws Exception {
		Index ind = new Index()
			.addMovie(new Movie().setData("title"   , "Frozen")
			 	  				 .setData("studio"  , "Walt Disney Pictures")
			 	  				 .setData("year"    , "2013")
				  				 .setData("producer", "Peter Del Vecho")
				  				 .setAbstract("Fearless optimist Anna teams up with Kristoff in an epic journey, encountering Everest-like conditions, and a hilarious snowman named Olaf in a race to find Anna's sister Elsa, whose icy powers have trapped the kingdom in eternal winter.")
				  				 .download())
				  				 
			.addMovie(new Movie().setData("title"   , "Wreck-it Ralph")
								 .setData("year"    , "2012")
								 .setData("studio"  , "Walt Disney Pictures")
								 .setData("producer", "Clark Spencer")
								 .setAbstract("A video game villain wants to be a hero and sets out to fulfill his dream, but his quest brings havoc to the whole arcade where he lives.")
								 .download())
								 
			.addMovie(new Movie().setData("title"   , "Despicable Me 2")
								 .setData("studio"  , "Illumination Entertainment")
								 .setData("year"    , "2013")
								 .setData("producer", "Chris Meledandri, Janet Healy")
								 .setAbstract("Gru is recruited by the Anti-Villain League to help deal with a powerful new super criminal.")
								 .download());
		System.out.println(ind);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("Index (summary): ");
		
		for(String term : table.keySet()){
			sb.append('"').append(term).append("\":").append(sumTerm(term)).append(" ");
		}
		
		
		return sb.toString();
	}
	
	private int sumTerm(String term){
		Integer sum = 0;
		for(Integer num : table.get(term).values()){
			sum += num;
		}
		return sum;
	}
}
