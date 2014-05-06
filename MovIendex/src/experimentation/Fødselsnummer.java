package experimentation;

public class Fødselsnummer {
	public static void main(String[] args) {
		long fnr = 12345612344L;
		System.out.println(isKvinne(fnr));
		System.out.println(erDame(fnr));
	}
	public static boolean isKvinne(long fnr){
		return fnr % 2 == 1;
	}
	
	public static boolean erDame(long fnr){
		return (fnr << 63) >> 63 != 0;
	}
}
