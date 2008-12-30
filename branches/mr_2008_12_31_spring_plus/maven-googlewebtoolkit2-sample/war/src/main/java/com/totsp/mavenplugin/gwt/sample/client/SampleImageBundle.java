package com.totsp.mavenplugin.gwt.sample.client;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * This is a sample how to use image bundles in your gwt-maven project. 
 * @author Andrew
 */
public interface SampleImageBundle extends ImageBundle {

	/**
	 * @gwt.resource com/totsp/mavenplugin/gwt/sample/public/images/gwt-logo.png
	 */
	public AbstractImagePrototype getGWTLogo();

}
