package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

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

	public static File file(Series series) {
		return new File(Properties.getString("archive"), series.instances.iterator().next().files.iterator().next().filepath);
	}

	public static Dataset dataset(Series series) throws IOException {
		Dataset dataset = DcmObjectFactory.getInstance().newDataset();
		dataset.readFile(Dicom.file(series), null, -1);
		return dataset;
	}
	
	public static Dataset privateDataset(Dataset dataset) throws IOException {
		Dataset privateDataset = DcmObjectFactory.getInstance().newDataset();
		byte[] buf = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).get(Tags.valueOf("(2005,140F)")).getDataFragment(0).array();
		privateDataset.readFile(new ByteArrayInputStream(buf), null, -1);
		return privateDataset;
	}

	//	public static String attribute(Instance instance, String tag) throws IOException {
	//		Dataset d = DcmObjectFactory.getInstance().newDataset();
	//		d.readFile(new File(Properties.getString("archive"), instance.files.iterator().next().filepath), null, -1);
	//		return d.getItem(Tags.SharedFunctionalGroupsSeq).getItem(Tags.MRFOVGeometrySeq).getString(Tags.forName(tag));
	//	}
}
