package util;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.tool.dcmqr.DcmQR;

public class DicomQuery {

	public static List<DicomObject> patient(final String patientName) throws Exception {
		return new Query() {
			public void append(DcmQR dcmqr) {
				dcmqr.setQueryLevel(DcmQR.QueryRetrieveLevel.PATIENT);
				dcmqr.addMatchingKey(new int[] {Tag.PatientName}, patientName);
			}
		}.call();
	}

	public static List<DicomObject> studies(final String patientID) throws Exception {
		return new Query() {
			public void append(DcmQR dcmqr) {
				dcmqr.setQueryLevel(DcmQR.QueryRetrieveLevel.STUDY);
				dcmqr.addMatchingKey(new int[] {Tag.PatientID}, patientID);
				dcmqr.addReturnKey(new int[] {Tag.StudyDescription});
			}
		}.call();
	}
	
	public static List<DicomObject> seriess(final String studyID) throws Exception {
		List<DicomObject> results = new Query() {
			public void append(DcmQR dcmqr) {
				dcmqr.setQueryLevel(DcmQR.QueryRetrieveLevel.SERIES);
				dcmqr.addMatchingKey(new int[] {Tag.StudyID}, studyID);
				dcmqr.addReturnKey(new int[] {Tag.SeriesDescription});
				dcmqr.addReturnKey(new int[] {Tag.SeriesDate});
			}
		}.call();
		Collections.sort(results, new Comparator<DicomObject>() {
			public int compare(DicomObject o1, DicomObject o2) {
				return o1.getString(Tag.SeriesNumber).compareTo(o2.getString(Tag.SeriesNumber));
			}
		});
		return results;
	}
	
	public static List<DicomObject> images(final String seriesNumber) throws Exception {
		return new Query() {
			public void append(DcmQR dcmqr) {
				dcmqr.setQueryLevel(DcmQR.QueryRetrieveLevel.IMAGE);
				dcmqr.addMatchingKey(new int[] {Tag.SeriesNumber}, seriesNumber);
			}
		}.call();
	}
	
	static abstract class Query implements Callable<List<DicomObject>> {
		protected abstract void append(DcmQR dcmqr);
		@Override
		public List<DicomObject> call() throws Exception {
			DcmQR dcmqr = new DcmQR(Properties.getString("dicom.callingAET"));
			dcmqr.setCalledAET(Properties.getString("dicom.calledAET"), true);
			dcmqr.setRemoteHost(Properties.getString("dicom.host"));
			dcmqr.setRemotePort(Properties.getInt("dicom.port"));
			dcmqr.configureTransferCapability(false);
			append(dcmqr);
			dcmqr.start();
			dcmqr.open();
			List<DicomObject> objects = dcmqr.query();
			dcmqr.close();
			dcmqr.stop();
			return objects;
		}
	}
}
