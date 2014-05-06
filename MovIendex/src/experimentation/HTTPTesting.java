package experimentation;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

/**
 * Elemental example for executing multiple GET requests sequentially.
 */
public class HTTPTesting{

	public static void main(String[] args) throws Exception {
		String wikipediaDomain = "en.wikipedia.org";
		String film = "Frozen";
		String year = "2013";
		
//		String wikipedia = get("en.wikipedia.org", 80, String.format("/wiki/%s", film));
		
//		String[] promisingLines = grepForFilms(wikipedia);
			
//		String[] urls = urlsFromHref(promisingLines);
		
//		String bestUrl = bestUrl(urls, year);
			
//		System.out.printf("Best URL: %s%n", bestUrl);
//		System.out.println(Arrays.toString(urls));
		
//		String bestWikipediaPage = get(wikipediaDomain, 80, bestUrl);
		
//		System.out.println(bestWikipediaPage);
//		try(FileWriter fw = new FileWriter(new File("frozen.wikipedia.html"))){
//			fw.write(bestWikipediaPage);
//		}
		
		String bestWikipediaPage = "";
		
		try(Scanner wikislurp = new Scanner(new File("frozen.wikipedia.html"))){
			bestWikipediaPage = wikislurp.useDelimiter("\\Z").next();
		}
		
		System.out.println(bestWikipediaPage.length());
		System.out.println("BEFORE STRIPPING COMMENTS");
		bestWikipediaPage = stripComments(bestWikipediaPage);
		System.out.println(bestWikipediaPage.length());
		System.out.println("AFTER STRIPPING COMMENTS");
		
		bestWikipediaPage = cutHeaders(bestWikipediaPage);
		
		System.out.println(bestWikipediaPage.length());
		System.out.println("AFTER CUTTING HEADERS");
		
		
		bestWikipediaPage = cutExtraneousWikipedia(bestWikipediaPage);
		System.out.println(bestWikipediaPage.length());
		System.out.println("AFTER CUTTING EXTRANEOUS");
		
		System.out.println(bestWikipediaPage);
	}
	
	
	public static String cutExtraneousWikipedia(String page){
		int open   = page.indexOf("<");
		int closed = page.indexOf(">");
		while(open >= 0 && closed >= 0 && closed > open){
			page = page.substring(open, closed);
			open   = page.indexOf("<");
			closed = page.indexOf(">");
		}
		return page;
	}
	
	public static String stripComments(String page){
		int commentLevel = 0;
		StringBuilder noComments = new StringBuilder();
		char[] string = page.toCharArray();
		
		for(int i = 0; i < string.length; ++i){
			/* Beginning of an html comment */
			if(string[i]   == '<' &&
			   string[i+1] == '!' &&
			   string[i+2] == '-' &&
			   string[i+3] == '-'){			
				++commentLevel;
				i += 3;
			}
			/* We have an html comment ending. */
			else if(string[i]   == '-' &&
					string[i+1] == '-' &&
					string[i+2] == '>'){
				--commentLevel;
				i += 2;
			}
			/* We're not incommented out html, so we append the char. */
			else if(0 == commentLevel){
				noComments.append(string[i]);
			}
		}
		
		return noComments.toString();
	}
	
	public static String cutHeaders(String page){
		return page.substring(page.indexOf("</head>"));
	}
	
	public static String bestUrl(String[] urls, final String year){
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
		Arrays.sort(urls, cmp);
		return urls[0];
	}
	
	public static String[] urlsFromHref(String[] hrefs){
		String[] urls = new String[hrefs.length];
		
		String preCut = "<a href=\"";
		String postCut = "\" title";
		for(int i = 0; i < hrefs.length; ++i){
			urls[i] = hrefs[i].substring(hrefs[i].indexOf(preCut) + preCut.length(),
										 hrefs[i].indexOf(postCut));
		}
		
		return urls;
	}
	
	public static String[] grepForFilms(String html){
		// cut from and up til this string. Quick, dirty and Wikipedia specific.
		String fromThis= "<p><b>Frozen</b> may refer to:</p>";
		String toThis = "<table id=\"disambigbox\" class=\"metadata plainlinks dmbox dmbox-disambig\" style=\"\" role=\"presentation\">";
		html = html.substring(html.indexOf(fromThis) + fromThis.length(), html.indexOf(toThis));
		
		ArrayList<String> containsFilms = new ArrayList<>();
		/* We assume that the interesting bits of the film is in li tags.
		   Furthermore we assume that there is a link there somewhere. */
		String hrefStart = "<a href";
		String hrefEnd = "</a>";
		for(String line : html.split("\\<\\/li\\>")){
			if(line.contains("film") && line.contains("href")){
				String possible = line.substring(line.indexOf(hrefStart), 
												 line.indexOf(hrefEnd));
				if(possible.contains("film")){
					containsFilms.add(possible);
				}
			}
		}
		
		return containsFilms.toArray(new String[0]);
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