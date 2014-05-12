package download;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


public interface Indexer {

	/**
	 * This downloads data for a movie, and returns a basic index
	 * @param movie The title of the movie, for instance Brother Bear, Tangled, or Frozen.
	 * @param year The year of theatrical release in the US of A.
	 * @param safeWords words that should be ignored. stopWords sounded so negative, so we call them safeWords instead. ^_^
	 * @return A map {@link String} → {@link Integer}, representing the word-frequency of different terms in the source.
	 * @throws IOException if something goes wrong downloading or what have you.
	 */
	public Map<String, Integer> indexMovie(String movie, String year, Set<String> safeWords) throws IOException;
}
