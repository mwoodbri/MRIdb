package util;

import play.Play;

public class Properties {
	public static int getInt(String key) {
		return Integer.parseInt(Play.configuration.getProperty(key));
	}
	
	public static String getString(String key) {
		return Play.configuration.getProperty(key);
	}
}
