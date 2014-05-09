package experimentation;

public class WrapArround {
	public static void main(String[] args) {
		long l = ((long) Integer.MAX_VALUE) + 1L;
		System.out.println(l);
		
		int lel = (int) l;
		System.out.println(lel);
		
	}
}
