package ext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.Play;
import util.Dicom;

public class JavaExtensions extends play.templates.JavaExtensions {

	public static String time(Date date) {
		return new SimpleDateFormat(Play.configuration.getProperty("time.format")).format(date);
	}

	public static String ellipsis(String string, Integer length) {
		return string.length() <= length ? string : string.substring(0, length - 1) + '…';
	}

	public static String get(byte[] dataset, String tag) throws IOException {
		return Dicom.attribute(dataset, tag);
	}
}
