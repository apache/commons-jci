package org.apache.commons.jci.examples.configuration;

import java.util.Properties;

public interface Configurable {
	
	void configure( Properties properties ) throws ConfigurationException;

}
