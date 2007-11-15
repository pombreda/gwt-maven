package com.totsp.mavenplugin.gwt.sample.ejb;

import javax.ejb.*;

@Stateless
public class RandomComplimentBean implements RandomCompliment {
	
	/**
	 * This is proof the GWT Server is Java5+
	 * @author andrew
	 */
	private enum JavaFiveEnum {
		BRILLIANT, EXCELLENT, L33T, GOOD, WICKED, COOL, AWESOME, SWEET
	}

	/**
	 * This is what will execute with GWT RPC call.
	 */
	public String getCompliment() {
		double randomIndex = Math.random() * JavaFiveEnum.values().length;
		return "" + JavaFiveEnum.values()[(int) randomIndex];

	}	
}
