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
			if ("MR".equals(series.modality)) {
				series(series, tmpDir, username);
			}
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
				Medcon.convert(Dicom.collate(series), format, dir);
			} else {
				Instance instance = Dicom.multiFrame(series);
				File dcm = Dicom.file(instance);
				File nii = new File(dir, String.format("%s.nii", dcm.getName()));
				Util.exec(new File(Play.applicationPath, "bin/dicom_2_nifti.py").getPath(), dcm.getPath(), nii.getPath());
			}
		} else if (format == Format.img) {
			File dir = new File(studyDir, series.toDownloadString());
			dir.mkdir();
			Medcon.convert(Dicom.collate(series), format, dir);
		} else {
			File dir = new File(studyDir, series.toDownloadString());
			dir.mkdir();
			for (Files files : Dicom.getFiles(series)) {
				Dicom.anonymise(new File(Properties.getArchive(), files.filepath), new File(dir, String.format("%s.dcm", files.pk)));
			}
		}
	}

}
