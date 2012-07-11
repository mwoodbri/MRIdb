package util;

import java.io.File;
import java.util.Collection;

import jobs.Downloader.Format;
import models.Files;
import models.ProjectAssociation;
import models.Series;
import models.Study;
import play.Play;

public class Download {

	public static void study(Study study, File tmpDir, Format format) throws Exception {
		for (Series series : study.series) {
			if ("MR".equals(series.modality)) {
				series(series, tmpDir, format);
			}
		}
	}

	public static void series(Series series, File tmpDir, Format format) throws Exception {
		File dir = new File(new File(tmpDir, series.study.toDownloadString()), series.toDownloadString());
		dir.mkdirs();

		if (format == Format.nii) {
			Collection singleFrames = Dicom.singleFrames(series);
			if (singleFrames.size() > 0) {
				//Medcon.convert(Dicom.collate(series), format, dir);
				Util.exec(Properties.getString("dcm2nii"), "-g", "n", "-p", "n", "-d", "n", "-o", dir.getPath(), Dicom.collate(series).getPath());
			} else {
				File dcm = Dicom.file(Dicom.multiFrame(series));
				Util.exec("python", Properties.getString("dicom_2_nifti"), dcm.getPath(), new File(dir, String.format("%s.nii.gz", series.toDownloadString())).getPath());
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
