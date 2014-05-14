package download;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

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

import static evil.Globals.safeWords;
public class Tools {

	public static Map<String, Integer> wordsToFrequencyMap(String words){
		Map<String, Integer> freq = new HashMap<>();
		String onlyNumbers = "^\\d+$"; // Matches a string that consists of solely numerals.
		
		for(String hopeFullyWord : words.replaceAll("[^\\w]", " ").split("\\s+")){
			String cand = hopeFullyWord.toLowerCase();
			if(!safeWords.contains(cand) && !cand.matches(onlyNumbers)){
				if(!freq.containsKey(cand)){
					freq.put(cand, Integer.valueOf(1));
				}
				else{
					freq.put(cand, freq.get(cand) + 1);
				}
			}
		}

		return freq;
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
