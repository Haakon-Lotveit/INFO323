package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Movie {
	private Map<String, String> data;
	private String summary;
	
	private void init(){
		this.data = new HashMap<String, String>();
	}
	public Movie(){
		init();
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
	@Override
	public String toString(){
		return new StringBuilder()
			.append("[Movie: <Data: ")
			.append(stringifyMap(this.data))
			.append(">]")
			.toString();
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
		
	}
	
	public static Movie makeSampleMovie(){
		return new Movie().setData("title"   , "Frozen")
						  .setData("studio"  , "Walt Disney Pictures")
						  .setData("year"    , "2013")
						  .setData("producer", "Peter Del Vecho");
	}
}
