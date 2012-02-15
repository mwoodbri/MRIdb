package jobs;

import java.io.File;
import java.io.IOException;

import models.Series;
import play.Play;
import play.jobs.Job;
import play.libs.Files;
import util.Dicom;
import util.Properties;

public class Downloader extends Job<File> {

	public static enum Format { dcm, nii, img };

	private long pk;
	private Format format;
	private String echo;
	private File tmpDir;

	public Downloader(long pk, Format format, String echo, File tmpDir) {
		this.pk = pk;
		this.format = format;
		this.echo = echo;
		this.tmpDir = tmpDir;
	}

	@Override
	public File doJobWithResult() {
		File result;
		try {
			Series series = Series.<Series>findById(pk);
			File export = export(series, format, tmpDir, echo);
			if (export.isDirectory()) {
				result = new File(tmpDir, String.format("%s.zip", export.getName()));
				Files.zip(export, result);
			} else {
				result = export;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public static File export(Series series, Format format, File tmpDir, String echo) throws InterruptedException, IOException {
		if (format == Format.nii) {
			if (series.instances.size() == 1) {
				File dir = new File(tmpDir, series.toDownloadString());
				dir.mkdir();
				File dcm = Dicom.file(series.instances.iterator().next());
				File nii = new File(dir, String.format("%s.nii", series.toDownloadString()));
				new ProcessBuilder(new File(Play.applicationPath, "bin/dicom_2_nifti.py").getPath(), dcm.getPath(), nii.getPath()).start().waitFor();
				return nii.exists() ? nii : dir;
			} else {
				new ProcessBuilder(Properties.getString("dcm2nii"),
						"-e", "n",//don't put series/acq in filename
						"-g", "n",//don't gzip
						"-o", tmpDir.getPath(),//don't put destination file in same directory as source
						"-p", "n",//don't put protocol in filename
						Dicom.folder(series).getPath()).start().waitFor();
				File nii = new File(tmpDir, String.format("%s.nii", series.toDownloadString()));
				tmpDir.listFiles()[0].renameTo(nii);
				return nii;
			}
		} else if (format == Format.img) {
			File dir = new File(tmpDir, series.toDownloadString());
			dir.mkdir();
			new ProcessBuilder(Properties.getString("dcm2nii"),
					"-e", "n",//don't put series/acq in filename
					"-n", "n",//.hdr/.img pair
					"-s", "y",//analyze
					"-o", dir.getPath(),//don't put destination file in same directory as source
					"-p", "n",//don't put protocol in filename
					Dicom.folder(series).getPath()).start().waitFor();
			return dir;
		} else {
			if (series.instances.size() == 1) {
				File anon = new File(tmpDir, String.format("%s.dcm", series.toDownloadString()));
				Dicom.anonymise(Dicom.file(series.instances.iterator().next()), anon);
				return anon;
			} else {
				File dir = new File(tmpDir, series.toDownloadString());
				dir.mkdir();
				for (File dcm : Dicom.files(series, echo)) {
					Dicom.anonymise(dcm, new File(dir, String.format("%s.dcm", dcm.getName())));
				}
				return dir;
			}
		}
	}

}