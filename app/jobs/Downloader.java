package jobs;

import java.io.File;
import java.io.IOException;

import models.Series;
import models.Study;
import play.jobs.Job;
import util.Download;

public class Downloader extends Job<Void> {

	public static enum Format {
		dcm, //DICOM
		nii, //NIfTI
		img  //Analyze
	}

	private long[] pks;
	private Format format;
	private File tmpDir;

	public Downloader(long[] pks, Format format, File tmpDir) {
		this.pks = pks;
		this.format = format;
		this.tmpDir = tmpDir;
	}

	@Override
	public void doJob() {
		try {
			for (long pk : pks) {
				Study study = Study.findById(pk);
				if (study != null) {
					Download.study(study, tmpDir);
				} else {
					Download.series(Series.<Series>findById(pk), tmpDir, format);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}