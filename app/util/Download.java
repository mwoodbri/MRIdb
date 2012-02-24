package util;

import java.io.File;
import java.io.IOException;

import jobs.SeriesDownloader.Format;
import models.Series;
import models.Study;
import play.Play;

public class Download {

	public static void study(Study study, File tmpDir, String username) throws IOException, InterruptedException {
		for (Series series : study.series) {
			series(series, tmpDir, username);
		}
	}

	public static void series(Series series, File tmpDir, String username) throws IOException, InterruptedException {
		series(series, tmpDir, username, Format.dcm, null);
	}

	public static void series(Series series, File tmpDir, String username, Format format, String echo) throws IOException, InterruptedException {
		tmpDir = new File(tmpDir, series.study.toDownloadString(username));
		tmpDir.mkdir();

		if (format == Format.nii) {
			File dir = new File(tmpDir, series.toDownloadString());
			dir.mkdir();
			if (series.instances.size() == 1) {
				File dcm = Dicom.file(series.instances.iterator().next());
				File nii = new File(dir, String.format("%s.nii", dcm.getName()));
				new ProcessBuilder(new File(Play.applicationPath, "bin/dicom_2_nifti.py").getPath(), dcm.getPath(), nii.getPath()).start().waitFor();
			} else {
				new ProcessBuilder(
						Properties.getString("dcm2nii"),
						"-d", "n",//don't put date in filename
						"-p", "n",//don't put protocol in filename
						"-g", "n",//don't gzip
						"-o", dir.getPath(),//don't put destination file in same directory as source
						Dicom.folder(series).getPath()
						).start().waitFor();
			}
		} else if (format == Format.img) {
			File dir = new File(tmpDir, series.toDownloadString());
			dir.mkdir();
			new ProcessBuilder(
					Properties.getString("dcm2nii"),
					"-d", "n",//don't put date in filename
					"-p", "n",//don't put protocol in filename
					"-n", "n",//.hdr/.img pair
					"-s", "y",//analyze
					"-o", dir.getPath(),//don't put destination file in same directory as source
					Dicom.folder(series).getPath()
					).start().waitFor();
		} else {
			File dir = new File(tmpDir, series.toDownloadString());
			dir.mkdir();
			for (File dcm : Dicom.files(series, echo)) {
				Dicom.anonymise(dcm, new File(dir, String.format("%s.dcm", dcm.getName())));
			}
		}
	}

}
