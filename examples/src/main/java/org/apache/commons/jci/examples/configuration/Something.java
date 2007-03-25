package org.apache.commons.jci.examples.configuration;

import java.util.Properties;

public class Something implements Configurable {

	public void configure( Properties properties ) throws ConfigurationException {
		System.out.println("Configuration changed");
	}

}
