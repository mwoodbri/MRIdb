package util;

import models.Log;

public class PersistentLogger {

	public static void log(String message, Object... args) {
		new Log(String.format(message, args)).save();
	}
}
