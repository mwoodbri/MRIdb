package jobs;

import java.io.File;
import java.io.IOException;

import models.Series;
import play.jobs.Job;
import util.Download;

public class SeriesDownloader extends Job<File> {

	public static enum Format {
		dcm, //DICOM
		nii, //NIfTI
		img  //Analyze
	}

	private long pk;
	private Format format;
	private File tmpDir;
	private String username;

	public SeriesDownloader(long pk, Format format, File tmpDir, String username) {
		this.pk = pk;
		this.format = format;
		this.tmpDir = tmpDir;
		this.username = username;
	}

	@Override
	public void doJob() {
		try {
			Download.series(Series.<Series>findById(pk), tmpDir, username, format);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}