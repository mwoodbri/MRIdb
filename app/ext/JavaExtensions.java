package ext;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
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
		int years = Years.yearsBetween(DateTimeFormat.forPattern("yyyyMMdd").parseDateTime(string), new DateTime(date)).getYears();
		if (years > 0) {
			return String.format("%s", years);
		} else {
			int days = Days.daysBetween(DateTimeFormat.forPattern("yyyyMMdd").parseDateTime(string), new DateTime(date)).getDays();
			if (days <= 29) {
				return String.format("%s days", days);
			} else {
				return String.format("%s weeks, %s days", days / 7, days % 7);
			}
		}
	}

	public static String formatAsDate(String string) throws ParseException {
		return play.templates.JavaExtensions.format(new SimpleDateFormat("yyyyMMdd").parse(string));
	}

	public static String formatAsName(String string) {
		String[] parts = string.split("\\^", -1);
		return String.format("%s %s %s %s %s", parts[3], parts[0], parts[1], parts[2], parts[4]).replaceAll("[ ]+", " ");
	}
}
