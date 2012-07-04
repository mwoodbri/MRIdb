package util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import jobs.Downloader.Format;
import models.Files;
import models.ProjectAssociation;
import models.Series;
import models.Study;
import play.Play;

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
			Collection singleFrames = Dicom.singleFrames(series);
			if (singleFrames.size() > 0) {
				//Medcon.convert(Dicom.collate(series), format, dir);
				Util.exec(Properties.getString("dcm2nii"), "-g", "n", "-p", "n", "-d", "n", "-o", dir.getPath(), Dicom.collate(series).getPath());
			} else {
				File dcm = Dicom.file(Dicom.multiFrame(series));
				Util.exec(new File(Play.applicationPath, "bin/dicom_2_nifti.py").getPath(), dcm.getPath(), new File(dir, String.format("%s.nii", dcm.getName())).getPath());
			}
		} else if (format == Format.img) {
			Medcon.convert(Dicom.collate(series), format, dir);
		} else {
			String identifier = null;
			ProjectAssociation projectAssociation = series.study.getProjectAssociation();
			if (projectAssociation != null && projectAssociation.participationID != null && !projectAssociation.participationID.isEmpty()) {
				identifier = projectAssociation.participationID;
			}
			for (Files files : Dicom.getFiles(series)) {
				Dicom.anonymise(new File(Properties.getArchive(), files.filepath), new File(dir, String.format("%s.dcm", files.toDownloadString())), identifier);
			}
		}
	}

}
