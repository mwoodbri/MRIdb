package jobs;

import java.io.File;
import java.io.IOException;

import models.Series;
import play.jobs.Job;
import util.Download;

public class SeriesDownloader extends Job<Void> {

	public static enum Format {
		dcm, //DICOM
		nii, //NIfTI
		img  //Analyze
	}

	private long pk;
	private Format format;
	private File tmpDir;

	public SeriesDownloader(long pk, Format format, File tmpDir) {
		this.pk = pk;
		this.format = format;
		this.tmpDir = tmpDir;
	}

	@Override
	public void doJob() {
		try {
			Download.series(Series.<Series>findById(pk), tmpDir, format);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}