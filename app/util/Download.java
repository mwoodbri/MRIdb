package util;

import java.io.File;
import java.io.IOException;

import jobs.SeriesDownloader.Format;
import models.Files;
import models.Series;
import models.Study;

public class Download {

	public static void study(Study study, File tmpDir) throws IOException, InterruptedException {
		for (Series series : study.series) {
			if ("MR".equals(series.modality)) {
				series(series, tmpDir);
			}
		}
	}

	public static void series(Series series, File tmpDir) throws IOException, InterruptedException {
		series(series, tmpDir, Format.dcm);
	}

	public static void series(Series series, File tmpDir, Format format) throws IOException, InterruptedException {
		File dir = new File(new File(tmpDir, series.study.toDownloadString()), series.toDownloadString());
		dir.mkdirs();

		if (format == Format.nii) {
			//			Collection singleFrames = Dicom.singleFrames(series);
			//			if (singleFrames.size() > 0) {
			//				Medcon.convert(Dicom.collate(series), format, dir);
			//			} else {
			//				Instance instance = Dicom.multiFrame(series);
			//				File dcm = Dicom.file(instance);
			//				File nii = new File(dir, String.format("%s.nii", dcm.getName()));
			//				Util.exec(new File(Play.applicationPath, "bin/dicom_2_nifti.py").getPath(), dcm.getPath(), nii.getPath());
			//			}
			Medcon.convert(Dicom.collate(series), format, dir);
		} else if (format == Format.img) {
			Medcon.convert(Dicom.collate(series), format, dir);
		} else {
			for (Files files : Dicom.getFiles(series)) {
				Dicom.anonymise(new File(Properties.getArchive(), files.filepath), new File(dir, String.format("%s.dcm", files.toDownloadString())));
			}
		}
	}

}
