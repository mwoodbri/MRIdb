package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import models.Files;
import models.Instance;
import models.Series;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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
		Collection singleFrames = Dicom.singleFrames(series);
		if (singleFrames.size() > 0) {
			for (Object instance : Dicom.singleFrames(series)) {
				filesList.add(((Instance) instance).files.iterator().next());
			}
		} else {
			Instance instance = Dicom.multiFrame(series);
			if (instance == null) {
				instance = spectrogram(series);
			}
			filesList.add(instance.files.iterator().next());
		}
		return filesList;
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

	public static void anonymise(File from, File to, String identifier) throws IOException {
		Dataset dataset = anonymise(from, to);
		if (identifier != null) {
			dataset.putPN(Tags.PatientName, identifier);
			dataset.putLO(Tags.PatientID, identifier);
		}
		dataset.writeFile(to, null);
	}

	public static Dataset anonymise(File from, File to) throws IOException {
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
		return dataset;
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
