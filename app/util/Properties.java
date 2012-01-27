package util;

import java.io.File;

import models.Filesystem;
import play.Play;

public class Properties {

	public static int getInt(String key) {
		return Integer.parseInt(Play.configuration.getProperty(key));
	}

	public static String getString(String key) {
		return Play.configuration.getProperty(key);
	}

	public static File getArchive() {
		String dirpath = Filesystem.<Filesystem>findAll().get(0).dirpath;
		File archive = new File(dirpath);
		return archive.isAbsolute() ? archive : new File(getString("dcm4chee"), String.format("server/default/%s", dirpath));
	}
}
