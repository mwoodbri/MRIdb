package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Util {
	public static List<Integer> pages(int count, int pageSize, int currentPage) {
		int pageCount = 1 + (count -1) / pageSize;
		List<Integer> pages = new ArrayList<Integer>();
		pages.add(1);
		if (pageCount < 2) {
			return pages;
		} else if (pageCount <= 6 || currentPage < 6 - 1) {
			pages.add(2);
		} else {
			pages.add(0);
		}
		if (pageCount < 3) {
			return pages;
		} else if (pageCount <= 6 || currentPage < 6 - 1) {
			pages.add(3);
		} else if (currentPage > pageCount - 3) {
			pages.add(pageCount - 3);
		} else {
			pages.add(currentPage - 1);
		}
		if (pageCount < 4) {
			return pages;
		} else if (pageCount <= 6 || currentPage < 6 - 1) {
			pages.add(4);
		} else if (currentPage > pageCount - 3) {
			pages.add(pageCount - 2);
		} else {
			pages.add(currentPage);
		}
		if (pageCount < 5) {
			return pages;
		} else if (pageCount <= 6 || currentPage < 6 - 1) {
			pages.add(5);
		} else if (currentPage > pageCount - 3) {
			pages.add(pageCount - 1);
		} else {
			pages.add(currentPage + 1);
		}
		if (pageCount < 6) {
			return pages;
		} else if (pageCount <= 6) {
			pages.add(6);
		} else if (currentPage > pageCount - 3) {
			pages.add(pageCount);
		} else {
			pages.add(0);
		}
		return pages;
	}

	public static void exec(File directory, Map<String, String> environment, String... command) throws InterruptedException, IOException {
		System.out.println(StringUtils.join(command, ' '));
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(directory);
		processBuilder.environment().putAll(environment);
		int exit = processBuilder.start().waitFor();
		if (exit != 0) {
			throw new IOException(String.format("command failed: %s", StringUtils.join(command, ' ')));
		}
	}

	public static void exec(String... command) throws InterruptedException, IOException {
		exec(null, Collections.EMPTY_MAP, command);
	}
}
