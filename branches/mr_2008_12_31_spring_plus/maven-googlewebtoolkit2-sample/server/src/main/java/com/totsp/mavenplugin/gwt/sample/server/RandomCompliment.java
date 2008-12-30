package com.totsp.mavenplugin.gwt.sample.server;

/*
 * Java5+ Class, showing how to run Java5 server side code.. and Java1.4 GWT Client code simultaneously in GWT-Maven
 */
public class RandomCompliment {

	/*
	 * Java5+ enum
	 */
	private static enum compliment {
		GREAT, EXCELLENT, FANTASTIC, AWESOME, L337
	}

	/**
	 * Just use the static method to get a random value
	 */
	private RandomCompliment() {
	};

	/**
	 * Randomly select a compliment from the Java5+ enum
	 * 
	 * @return a random compliment
	 */
	public static String get() {
		return "" + compliment.values()[(int) getRandomIndex()];
	}

	/**
	 * Generates a random index number, to select a random compliment
	 * 
	 * @return random index number to the compliment.values array
	 */
	private static int getRandomIndex() {
		// (int) float : will round DOWN, so [ 0 <= RETURN_VAL <
		// compliment.length ]
		return (int) (Math.random() * ((double) compliment.values().length));
	}

}
