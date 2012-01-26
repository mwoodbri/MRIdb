package jobs;

import java.io.File;
import java.io.IOException;

import models.Series;
import play.Play;
import play.jobs.Job;
import play.libs.Files;
import util.Dicom;
import util.Properties;

public class Exporter extends Job<File> {
	public static enum Format { dcm, nii };

	private long pk;
	private Format format;
	private String echo;
	private File tmpDir;

	public Exporter(long pk, Format format, String echo, File tmpDir) {
		this.pk = pk;
		this.format = format;
		this.echo = echo;
		this.tmpDir = tmpDir;
	}

	public File doJobWithResult() {
		File result;
		try {
			Series series = Series.<Series>findById(pk);
			File dir = new File(tmpDir, series.toDownloadString());
			dir.mkdir();
			if (format == Format.nii) {
				if (series.instances.size() == 1) {
					File nii = new File(dir, String.format("%s.nii", series.toDownloadString()));
					new ProcessBuilder(new File(Play.applicationPath, "bin/dicom_2_nifti.py").getPath(), Dicom.file(series.instances.iterator().next()).getPath(), nii.getPath()).start().waitFor();
				} else {
					File tmp = new File(tmpDir, series.series_iuid);
					tmp.mkdir();
					new ProcessBuilder(Properties.getString("dcm2nii"),
							"-d", "n",//don't put date in filename
							"-e", "n",//don't put series/acq in filename
							"-g", "n",//don't gzip
							"-i", "y",//use id in filename
							"-o", tmp.getPath(),//don't put destination file in same directory as source
							"-p", "n",//don't put protocol in filename
							Dicom.folder(series).getPath()).start().waitFor();
					File nii = new File(dir, String.format("%s.nii", series.toDownloadString()));
					tmp.listFiles()[0].renameTo(nii);
				}
			} else {
				if (series.instances.size() == 1) {
					File anon = new File(dir, String.format("%s.dcm", series.toDownloadString()));
					Dicom.anonymise(Dicom.file(series.instances.iterator().next()), anon);
				} else {
					for (File dcm : Dicom.files(series, echo)) {
						Dicom.anonymise(dcm, new File(dir, String.format("%s.dcm", dcm.getName())));
					}
				}
			}
			if (dir.list().length > 1) {
				File zip = new File(tmpDir, String.format("%s.zip", series.toDownloadString()));
				Files.zip(dir, zip);
				result = zip;
			} else {
				result = dir.listFiles()[0];
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

}