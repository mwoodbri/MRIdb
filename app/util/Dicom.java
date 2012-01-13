package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	public static List<File> files(Series series) {
		List<File> files = new ArrayList<File>();
		for (Instance instance : series.instances) {
			files.add(new File(Properties.getString("archive"), instance.files.iterator().next().filepath));
		}
		return files;
	}

	//retrieve attributes that aren't in the table or blob by looking in the file
	public static Dataset dataset(Series series) throws IOException {
		Dataset dataset = DcmObjectFactory.getInstance().newDataset();
		//TODO this (incorrectly?) only looks at the first file of a series
		dataset.readFile(Dicom.files(series).get(0), null, -1);
		return dataset;
	}

	public static Set<Double> echoes(Dataset dataset) {
		Set<Double> echoes = new HashSet<Double>();
		if (dataset.contains(Tags.EchoTime)) {
			echoes.add(Double.parseDouble(dataset.getString(Tags.EchoTime)));
		} else {
			for (int i = 0; i < dataset.get(Tags.PerFrameFunctionalGroupsSeq).countItems(); i++) {
				echoes.add(dataset.getItem(Tags.PerFrameFunctionalGroupsSeq, i).getItem(Tags.MREchoSeq).getDouble(Tags.EffectiveEchoTime));
			}
		}
		return echoes;
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
