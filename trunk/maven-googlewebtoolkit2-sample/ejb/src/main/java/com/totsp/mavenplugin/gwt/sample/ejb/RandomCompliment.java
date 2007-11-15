package com.totsp.mavenplugin.gwt.sample.ejb;

import javax.ejb.Remote;

@Remote
public interface RandomCompliment {

	public String getCompliment();

}
