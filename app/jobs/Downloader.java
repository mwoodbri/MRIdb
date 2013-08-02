package jobs;

import java.io.File;

import models.DomainModel;
import models.Series;
import models.Study;
import play.jobs.Job;
import util.Clipboard.Item;
import util.Download;

public class Downloader extends Job<File> {

	public static enum Format {
		dcm, //DICOM
		nii, //NIfTI
		img  //Analyze
	}

	private Item[] items;
	private Format format;
	private File tmpDir;

	public Downloader(Format format, File tmpDir, Item... items) {
		this.format = format;
		this.tmpDir = tmpDir;
		this.items = items;
	}

	@Override
	public File doJobWithResult() throws Exception {
		try {
			for (Item item : items) {
				DomainModel model = item.getModel();
				if (model instanceof Study) {
					Download.study((Study) model, tmpDir, format);
				} else if (model instanceof Series) {
					Download.series((Series) model, tmpDir, format);
				}
			}
			return tmpDir.listFiles()[0];
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}