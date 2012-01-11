package ext;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;

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

	public static String formatAsRelativeAge(String string, Date date) {
		return String.valueOf(Years.yearsBetween(DateTimeFormat.forPattern("yyyyMMdd").parseDateTime(string), new DateTime(date)).getYears());
	}

	public static String formatAsDate(String string) throws ParseException {
		return play.templates.JavaExtensions.format(new SimpleDateFormat("yyyyMMdd").parse(string));
	}
}
