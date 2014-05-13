package experimentation;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import javax.naming.InsufficientResourcesException;

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

/**
 * Elemental example for executing multiple GET requests sequentially.
 */
public class HTTPTesting{

	public static void main(String[] args) throws Exception {
		String wikipediaDomain = "www.omdbapi.com";
		String film = "Frozen";
		String year = "2013";

		String req = new OmdbRequestBuilder().title(film)
				                             .year(year)
				                             .getLongPlot()
				                             .returnJSON()
				                             .request();
		System.out.println(req);
		//		http://www.omdbapi.com/?t=Frozen&y=2013&plot=full&tomatoes=false&response=JSON
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
			return String.format("http://%s/%s",
							     host,
							     target);
					             
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