package util;

import java.io.File;

import org.w3c.dom.Document;

import play.Play;
import play.libs.XML;
import play.libs.XPath;

public class Properties {

	public static int getInt(String key) {
		return Integer.parseInt(Play.configuration.getProperty(key));
	}

	public static String getString(String key) {
		return Play.configuration.getProperty(key);
	}

	public static File getArchive() {
		Document document = XML.getDocument(new File(getString("dcm4chee"), "server/default/data/xmbean-attrs/dcm4chee.archive@3Aservice@3DFileSystemMgt@2Cgroup@3DONLINE_STORAGE.xml"));
		File archive = new File(XPath.selectText("/attribute-list/attribute[@name='DefaultStorageDirectory']", document));
		return archive.isAbsolute() ? archive : new File(getString("dcm4chee"), String.format("server/default/%s", archive));
	}
}
