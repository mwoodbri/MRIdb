package jobs;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Files;
import util.Properties;

@Every("1h")
public class TmpWatch extends Job {

	private static final List<String> watchedFolders = Arrays.asList(Properties.getDownloads().getName(), Properties.getCollations().getName());

	@Override
	public void doJob() {
		for (String folderName : watchedFolders) {
			File folder = new File(Play.tmpDir, folderName);
			if (folder.exists()) {
				for (File file : folder.listFiles()) {
					if (TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - file.lastModified()) > 1) {
						Files.delete(file);
					}
				}
			}
		}
	}
}
