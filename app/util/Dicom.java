package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import models.Files;
import models.Instance;
import models.Series;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
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
		return new File(Properties.getArchive(), files(instance).filepath);
	}

	static Files files(Instance instance) {
		for (Files files : instance.files) {
			if (new File(Properties.getArchive(), files.filepath).exists()) {
				return files;
			}
		}
		return null;
	}

	public static File collate(Series series) {
		File collated = new File(Properties.getCollations(), UUID.randomUUID().toString());
		collated.mkdir();
		for (Files files : Dicom.getFiles(series)) {
			play.libs.Files.copy(new File(Properties.getArchive(), files.filepath), new File(collated, files.toDownloadString()));
		}
		return collated;
	}

	public static Collection<Files> getFiles(Series series) {
		List<Files> filesList = new ArrayList<Files>();
		if (Dicom.singleFrame(series)) {
			for (Object instance : Dicom.singleFrames(series)) {
				filesList.add(files((Instance) instance));
			}
		} else {
			Instance instance = Dicom.multiFrame(series);
			if (instance == null) {
				instance = spectrogram(series);
			}
			filesList.add(files(instance));
		}
		return filesList;
	}

	//retrieve attributes that aren't in the table or blob by looking in the file
	public static Dataset dataset(File dicom) throws IOException {
		Dataset dataset = DcmObjectFactory.getInstance().newDataset();
		dataset.readFile(dicom, null, Tags.PixelData);
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

	private static final String dcmodify = new File(Properties.getString("dcmtk"), "bin/dcmodify").getPath();
	private static final Map<String, String> environment = new HashMap<String, String>() {{
		put("DCMDICTPATH", new File(Properties.getString("dcmtk"), "share/dcmtk/dicom.dic").getPath());
	}};
	private static final int[] tags = new int[] {
		Tags.PatientID,
		Tags.PatientName,
		Tags.PatientSex,
		Tags.PatientBirthDate,
		Tags.PatientAddress,
		Tags.ReferringPhysicianName,
		Tags.InstitutionName,
		Tags.StationName,
		Tags.ManufacturerModelName
	};
	public static void anonymise(File inFile, File outFile, String identifier) throws Exception {
		FileUtils.copyFile(inFile, outFile);
		String[] command = new String[3 + 2 * tags.length + 1];
		int i = 0;
		command[i++] = dcmodify;
		command[i++] = "-nb";
		command[i++] = "-imt";
		for (int tag : tags) {
			if (identifier != null && (tag == Tags.PatientID || tag == Tags.PatientName)) {
				command[i++] = "-m";
				command[i++] = String.format("%s=%s", Tags.toString(tag), identifier);
			} else {
				command[i++] = "-e";
				command[i++] = Tags.toString(tag);
			}
		}
		command[i++] = outFile.getPath();
		Util.exec(null, environment, command);
	}

	public static int numberOfFrames(Series series) throws IOException {
		Instance instance = multiFrame(series);
		if (instance != null) {
			Dataset d = DcmObjectFactory.getInstance().newDataset();
			d.readFile(new ByteArrayInputStream(instance.inst_attrs), null, -1);
			return d.getInteger(Tags.forName("NumberOfFrames"));
		}
		return singleFrames(series).size();
	}

	private enum CUID {
		MRImageStorage("1.2.840.10008.5.1.4.1.1.4"),
		EnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.1"),
		MRSpectroscopyStorage("1.2.840.10008.5.1.4.1.1.4.2");

		final String value;
		CUID(String value) {
			this.value = value;
		}
	}

	public static Instance multiFrame(Series series) {
		return (Instance) CollectionUtils.find(series.instances, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return CUID.EnhancedMRImageStorage.value.equals(((Instance) arg0).sop_cuid);
			}
		});
	}

	public static Collection singleFrames(Series series) {
		return CollectionUtils.select(series.instances, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return CUID.MRImageStorage.value.equals(((Instance) arg0).sop_cuid);
			}
		});
	}

	public static boolean singleFrame(Series series) {
		return CollectionUtils.exists(series.instances, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return CUID.MRImageStorage.value.equals(((Instance) arg0).sop_cuid);
			}
		});
	}

	public static Instance spectrogram(Series series) {
		return (Instance) CollectionUtils.find(series.instances, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return CUID.MRSpectroscopyStorage.value.equals(((Instance) arg0).sop_cuid);
			}
		});
	}

	private static final List<String> renderable = Arrays.asList(CUID.MRImageStorage.value, CUID.EnhancedMRImageStorage.value);
	public static boolean renderable(Series series) {
		return CollectionUtils.exists(series.instances, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return renderable.contains(((Instance) arg0).sop_cuid);
			}
		});
	}

	private static final List<String> downloadable = Arrays.asList(CUID.MRImageStorage.value, CUID.EnhancedMRImageStorage.value, CUID.MRSpectroscopyStorage.value);
	public static boolean downloadable(Series series) {
		return CollectionUtils.exists(series.instances, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return downloadable.contains(((Instance) arg0).sop_cuid);
			}
		});
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
