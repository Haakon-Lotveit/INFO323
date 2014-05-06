package experimentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class IntToArr {
	public static void main(String[] args) {
		
		/*
		 * This is the perverted way of doing it.
		 * Note that this is encoding specific, so it's not a good general idea.
		 * The ENTARPRISE edition would be a switch/case statement for '0' to '9'.
		 * If you do this, it will make your TA cry. Therefore, you should do it this way.
		 */
		try(Scanner kb = new Scanner(System.in)){
			int num = Integer.parseInt(kb.nextLine());
			String stringNum = String.valueOf(num);
			char[] charArray = stringNum.toCharArray();
			int[] intArray = new int[charArray.length];
			for(int i = 0; i < charArray.length; intArray[i] = charArray[i] - '0', ++i);

			int sum = 0;
			for(int i : intArray) sum += i;
			
			System.out.println(sum);
			
		/*
		 * This is the slightly more sane way to do it.
		 * Note the «slightly».
		 */
			num = Integer.parseInt(kb.nextLine());
			sum = 0;
			while(num > 0){
				sum += num % 10; /* This should really be a radix parameter though. I mean, I need my base 1337 arithmetic. */
				num /= 10;
			}
			System.out.println(sum);
		}
	}
}
