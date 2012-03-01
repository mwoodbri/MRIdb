package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import models.Files;
import models.Instance;
import models.Series;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

public class Dicom {

	public static String attribute(byte[] dataset, String tag) throws IOException {
		Dataset d = DcmObjectFactory.getInstance().newDataset();
		d.readFile(new ByteArrayInputStream(dataset), null, -1);
		return d.getString(Tags.forName(tag));
	}

	public static File file(Instance instance) {
		return new File(Properties.getArchive(), instance.files.iterator().next().filepath);
	}

	public static List<File> files(Series series, String echo) throws IOException {
		List<File> files = new ArrayList<File>();
		for (Instance instance : series.instances) {
			File dicom = file(instance);
			if (echo == null || dataset(dicom).getString(Tags.EchoTime).equals(echo)) {
				files.add(dicom);
			}
		}
		return files;
	}

	public static File folder(Series series) {
		File folder = new File(Properties.getArchive(), series.instances.iterator().next().files.iterator().next().filepath).getParentFile();
		if (folder.list().length != series.instances.size()) {
			throw new RuntimeException(String.format("Folder %s contains %s files but series has length %s!", folder, folder.list().length, series.instances.size()));
		}
		return folder;
	}

	public static File collate(Series series) {
		File collated = new File(Properties.getCollations(), UUID.randomUUID().toString());
		collated.mkdir();
		for (Instance instance : series.instances) {
			for (Files files : instance.files) {
				play.libs.Files.copy(new File(Properties.getArchive(), files.filepath), new File(collated, String.valueOf(files.pk)));
			}
		}
		return collated;
	}

	//retrieve attributes that aren't in the table or blob by looking in the file
	public static Dataset dataset(File dicom) throws IOException {
		Dataset dataset = DcmObjectFactory.getInstance().newDataset();
		dataset.readFile(dicom, null, -1);
		return dataset;
	}

	public static Set<String> echoes(Dataset dataset) {
		Set<String> echoes = new LinkedHashSet<String>();
		if (dataset.contains(Tags.EchoTime)) {
			echoes.add(dataset.getString(Tags.EchoTime));
		} else {
			for (int i = 0; i < dataset.get(Tags.PerFrameFunctionalGroupsSeq).countItems(); i++) {
				boolean unseen = echoes.add(dataset.getItem(Tags.PerFrameFunctionalGroupsSeq, i).getItem(Tags.MREchoSeq).getString(Tags.EffectiveEchoTime));
				if (!unseen) {
					break;
				}
			}
		}
		return echoes;
	}

	public static void anonymise(File from, File to) throws IOException {
		Dataset dataset = DcmObjectFactory.getInstance().newDataset();
		dataset.readFile(from, null, -1);
		dataset.remove(Tags.PatientID);
		dataset.remove(Tags.PatientName);
		dataset.remove(Tags.PatientSex);
		dataset.remove(Tags.PatientBirthDate);
		dataset.remove(Tags.PatientAddress);
		dataset.remove(Tags.ReferringPhysicianName);
		dataset.remove(Tags.InstitutionName);
		dataset.remove(Tags.StationName);
		dataset.remove(Tags.ManufacturerModelName);
		dataset.writeFile(to, null);
	}

	private static final List<String> validCUIDs = Arrays.asList("1.2.840.10008.5.1.4.1.1.4", "1.2.840.10008.5.1.4.1.1.4.1", "1.2.840.10008.5.1.4.1.1.7");
	public static boolean renderable(Series series) {
		return validCUIDs.contains(series.instances.iterator().next().sop_cuid);
	}

	public static int numberOfFrames(Series series) throws IOException {
		Dataset d = DcmObjectFactory.getInstance().newDataset();
		d.readFile(new ByteArrayInputStream(series.instances.iterator().next().inst_attrs), null, -1);
		Integer numberOfFrames = d.getInteger(Tags.forName("NumberOfFrames"));
		return numberOfFrames != null ? numberOfFrames : series.instances.size();
	}

	//	public static Dataset privateDataset(Dataset dataset) throws IOException {
	//		Dataset privateDataset;
	//		Dataset perFrameFunctionalGroupsSeq = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq);
	//		if (perFrameFunctionalGroupsSeq.get(Tags.valueOf("(2005,140F)")).hasItems()) {
	//			privateDataset = perFrameFunctionalGroupsSeq.getItem(Tags.valueOf("(2005,140F)"));
	//		} else {
	//			byte[] buf = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).get(Tags.valueOf("(2005,140F)")).getDataFragment(0).array();
	//			privateDataset = DcmObjectFactory.getInstance().newDataset();
	//			privateDataset.readFile(new ByteArrayInputStream(buf), null, -1);
	//		}
	//		return privateDataset;
	//	}

	//	public static String attribute(Instance instance, String tag) throws IOException {
	//		Dataset d = DcmObjectFactory.getInstance().newDataset();
	//		d.readFile(new File(Properties.getString("archive"), instance.files.iterator().next().filepath), null, -1);
	//		return d.getItem(Tags.SharedFunctionalGroupsSeq).getItem(Tags.MRFOVGeometrySeq).getString(Tags.forName(tag));
	//	}
}
