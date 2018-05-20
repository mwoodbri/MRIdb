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
		File archive = new File(Filesystem.<Filesystem>findAll().get(0).dirpath);
		return archive.isAbsolute() ? archive : new File(getString("dcm4chee"), String.format("server/default/%s", archive.getPath()));
	}

	public static int pageSize() {
		return Integer.parseInt(Play.configuration.getProperty("page.size", String.valueOf(20)));
	}

	public static File getDownloads() {
		File downloads = new File(Play.tmpDir, "downloads");
		downloads.mkdir();
		return downloads;
	}

	public static File getExports() {
		return new File(getString("exports"));
	}

	public static File getCollations() {
		File downloads = new File(Play.tmpDir, "collations");
		downloads.mkdir();
		return downloads;
	}
}
