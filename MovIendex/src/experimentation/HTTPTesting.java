package experimentation;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import model.Movie;

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

/**
 * Elemental example for executing multiple GET requests sequentially.
 */
public class HTTPTesting{

	public static void main(String[] args) throws Exception {
		Movie m = new Movie().setData("title", "Frozen")
				.setData("year", "2013");
		download(m);
	}
	
	public static void download(Movie m) throws Exception {
		String req = new OmdbRequestBuilder().title(m.getData("title"))
				.year(m.getData("year"))
				.getLongPlot()
				.returnJSON()
				.getTomatoData()
				.request();
		System.out.println(req);
		//		http://www.omdbapi.com/?t=Frozen&y=2013&plot=full&tomatoes=false&response=JSON

		Map<String,String> json = new HashMap<String,String>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			//convert JSON string to Map
			json = mapper.readValue(req, 
					new TypeReference<HashMap<String,String>>(){});

			System.out.println(json);

		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Integer> rottenTomatoes = new HashMap<>();
		Map<String, Integer> omdbPlot = new HashMap<>();
		m.setAbstract(json.get("tomatoConsensus"))
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
		 .download();
		for(String key : json.keySet()){
			System.out.printf("%s → %s%n", key, json.get(key));
		}
		Map<String, Integer> plotFreq = Tools.wordsToFrequencyMap(json.get("Plot"));
		Map<String, Integer> consensusFreq = Tools.wordsToFrequencyMap(json.get("tomatoConsensus"));
		
		System.out.println(plotFreq);
		System.out.println(consensusFreq);
		
		System.out.println(m);

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
			String req = get(host, 80, target);
			System.out.println(req);
			return req;

		}
	}
	/**
	 * Copypasted from the HttpCore tutorial.
	 * @param hostname
	 * @param port 
	 * @param target
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws HttpException
	 */
	public static String get(String hostname, int port, String target) throws UnknownHostException, IOException, HttpException {
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new RequestContent())
				.add(new RequestTargetHost())
				.add(new RequestConnControl())
				.add(new RequestUserAgent("Test/1.1"))
				.add(new RequestExpectContinue(true)).build();

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpCoreContext coreContext = HttpCoreContext.create();
		HttpHost host = new HttpHost(hostname, port);
		coreContext.setTargetHost(host);

		String responseEntity = "ERROR";
		try(DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(8 * 1024)){

			if (!conn.isOpen()) {
				Socket socket = new Socket(host.getHostName(), host.getPort());
				conn.bind(socket);
			}
			BasicHttpRequest request = new BasicHttpRequest("GET", target);

			httpexecutor.preProcess(request, httpproc, coreContext);
			HttpResponse response = httpexecutor.execute(request, conn, coreContext);
			httpexecutor.postProcess(response, httpproc, coreContext);

			responseEntity = EntityUtils.toString(response.getEntity());
		}

		return responseEntity;
	}

}
