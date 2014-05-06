package model;

import java.util.LinkedList;
import java.util.List;

public class Indexer {
	List<Movie> movies;
	List<Index> indexen;
	
	private void init(){
		movies = new LinkedList<>();
		indexen = new LinkedList<>();
	}
	public Indexer(){
		init();
	}
	
}
