package jobs;

import java.io.File;

import jobs.Downloader.Format;
import models.Series;
import models.Study;
import play.db.jpa.GenericModel;
import play.jobs.Job;
import util.Clipboard;

public class Exporter extends Job {

	private Clipboard clipboard;
	private File tmpDir;

	public Exporter(Clipboard clipboard, File tmpDir) {
		this.clipboard = clipboard;
		this.tmpDir = tmpDir;
	}

	@Override
	public void doJob() throws Exception {
		for (GenericModel model : clipboard.getObjects()) {
			if (model instanceof Series) {
				Downloader.export((Series) model, Format.dcm, tmpDir, null);
			} else {
				Study study = (Study) model;
				File dir = new File(tmpDir, study.toClipboardString());
				dir.mkdir();
				for (Series series : ((Study) model).series) {
					Downloader.export(series, Format.dcm, dir, null);
				}
			}
		}
	}

}