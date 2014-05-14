package test.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import download.Tools;

public class TestFrequencyTools {

	@Test
	public void test() {
		String testString = 
		    "Come over the hills my handsome Irish lad \n" +
		    "Come over the hills to your darling \n" +
		    "You choose the road love, and I'll make a vow \n" +
		    "That I'll be your true love forever \n" +
		    "\n" +
		    "Red is the rose by yonder garden grows \n" +
		    "And fair is the lily of the valley \n" +
		    "Clear is the water that flows from the Boyne \n" +
		    "But my love is fairer than any\n" +
		    "\n" +
		    "Down by Killarneys green woods we did stray\n" +
		    "The moon and the stars they were shining\n" +
		    "The moon shone it's rays on his locks of golden hair\n" +
		    "And he swore he'd be my love forever\n" +
		    "\n" +
		    "Red is the rose by yonder garden grows\n" +
		    "And fair is the lily of the valley\n" +
		    "Clear is the water that flows from the Boyne\n" +
		    "But my love is fairer than any\n" +
		    "\n" +
		    "It's not for the parting that my sister pains\n" +
		    "It's not for the grief of my mother\n" +
		    "It's all for the loss of my handsome Irish lad\n" +
		    "Now my heart is broken forever\n" +
		    "\n" +
		    "Red is the rose by yonder garden grows\n" +
		    "And fair is the lily of the valley\n" +
		    "Clear is the water that flows from the Boyne\n" +
		    "But my love is fairer than any\n";
		
		Map<String, Integer> frequencies = Tools.wordsToFrequencyMap(testString);
		frequencies.get("hills");
		assertEquals("There are three \"boyne\"s, and we didn't get three.", Integer.valueOf(3), frequencies.get("boyne"));
	}

}
