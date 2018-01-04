package com.neopetsconnect.utils;

import org.filemanager.core.Properties;

class ConfigPropertiesReader {

	private static final String CONFIG_PATH = "config.properties";
	
	static Properties props = new Properties(CONFIG_PATH);
	
	private ConfigPropertiesReader() {
	}
}
