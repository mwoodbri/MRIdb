package util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import jobs.SeriesDownloader.Format;
import models.Files;
import models.Instance;
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
		series(series, tmpDir, username, Format.dcm);
	}

	public static void series(Series series, File tmpDir, String username, Format format) throws IOException, InterruptedException {
		File studyDir = new File(tmpDir, series.study.toDownloadString(username));
		studyDir.mkdir();

		if (format == Format.nii) {
			File dir = new File(studyDir, series.toDownloadString());
			dir.mkdir();
			Collection singleFrames = Dicom.singleFrames(series);
			if (singleFrames.size() > 0) {
				File collated = Dicom.collate(series);
				new ProcessBuilder(
						Properties.getString("dcm2nii"),
						"-d", "n",//don't put date in filename
						"-p", "n",//don't put protocol in filename
						"-g", "n",//don't gzip
						"-o", dir.getPath(),//don't put destination file in same directory as source
						collated.getPath()
						).start().waitFor();
			} else {
				Instance instance = Dicom.multiFrame(series);
				File dcm = Dicom.file(instance);
				File nii = new File(dir, String.format("%s.nii", dcm.getName()));
				new ProcessBuilder(new File(Play.applicationPath, "bin/dicom_2_nifti.py").getPath(), dcm.getPath(), nii.getPath()).start().waitFor();
			}
		} else if (format == Format.img) {
			File dir = new File(studyDir, series.toDownloadString());
			dir.mkdir();
			File collated = Dicom.collate(series);
			new ProcessBuilder(
					Properties.getString("dcm2nii"),
					"-d", "n",//don't put date in filename
					"-p", "n",//don't put protocol in filename
					"-n", "n",//.hdr/.img pair
					"-s", "y",//analyze
					"-o", dir.getPath(),//don't put destination file in same directory as source
					collated.getPath()
					).start().waitFor();
		} else {
			File dir = new File(studyDir, series.toDownloadString());
			dir.mkdir();
			for (Files files : Dicom.getFiles(series)) {
				Dicom.anonymise(new File(Properties.getArchive(), files.filepath), new File(dir, String.format("%s.dcm", files.pk)));
			}
		}
	}

}
