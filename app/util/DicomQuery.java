package util;

import java.io.IOException;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.tool.dcmqr.DcmQR;

public class DicomQuery {
	private static DcmQR dcmqr;
	static {
		dcmqr = new DcmQR(Properties.getString("dicom.callingAET"));
		dcmqr.setCalledAET(Properties.getString("dicom.calledAET"), true);
		dcmqr.setRemoteHost(Properties.getString("dicom.host"));
		dcmqr.setRemotePort(Properties.getInt("dicom.port"));
		dcmqr.configureTransferCapability(false);
	}

	public static List<DicomObject> query(String patientName) throws IOException, ConfigurationException, InterruptedException {
		dcmqr.setQueryLevel(DcmQR.QueryRetrieveLevel.PATIENT);
		dcmqr.addMatchingKey(new int[] {Tag.PatientName}, patientName);
		//	dcmqr.addReturnKey(new int[]{Tag.StudyID});

		dcmqr.start();
		dcmqr.open();
		List<DicomObject> objects = dcmqr.query();
		dcmqr.close();
		dcmqr.stop();
		
		return objects;
	}
}
