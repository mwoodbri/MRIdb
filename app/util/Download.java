package util;

import java.io.File;

import jobs.Downloader.Format;
import models.Files;
import models.ProjectAssociation;
import models.Series;
import models.Study;

public class Download {

	public static void study(Study study, File tmpDir, Format format, Boolean preferMultiframe, String niftiMultiframeScript) throws Exception {
		for (Series series : study.series) {
			//if ("MR".equals(series.modality)) {
			if (Dicom.downloadable(series)) {
				series(series, tmpDir, format, preferMultiframe, niftiMultiframeScript);
			}
		}
	}

	public static void series(Series series, File tmpDir, Format format, boolean preferMultiframe, String niftiMultiframeScript) throws Exception {
		File dir = new File(new File(tmpDir, series.study.toDownloadString()), series.toDownloadString());
		dir.mkdirs();

		if (format == Format.nii) {
			if (Dicom.singleFrame(series) || "dcm2nii".equals(niftiMultiframeScript)) {
				Util.exec("timeout", Properties.getString("exec.timeout"), Properties.getString("dcm2nii"), "-g", "n", "-p", "n", "-d", "n", "-o", dir.getPath(), Dicom.collate(series, preferMultiframe).getPath());
			} else {
				Util.exec("timeout", Properties.getString("exec.timeout"), "python", Properties.getString("dicom_2_nifti"), Dicom.file(Dicom.multiFrame(series)).getPath(), new File(dir, String.format("%s.nii.gz", series.toDownloadString())).getPath());
			}
		} else if (format == Format.img) {
			Medcon.convert(Dicom.collate(series, preferMultiframe), format, dir);
		} else {
			String identifier = null;
			if (series.study.getProjectAssociations().size() > 0) {
				ProjectAssociation projectAssociation = series.study.getProjectAssociations().iterator().next();
				if (projectAssociation.participationID != null && !projectAssociation.participationID.isEmpty()) {
					identifier = projectAssociation.participationID;
				}
			}
			for (Files files : Dicom.getFiles(series, preferMultiframe)) {
				Dicom.anonymise(new File(Properties.getArchive(), files.filepath), new File(dir, String.format("%s.dcm", files.toDownloadString())), identifier);
			}
		}
	}

}
