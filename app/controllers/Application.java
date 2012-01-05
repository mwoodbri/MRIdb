package controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Patient;
import models.Series;
import models.Study;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dcm4che.data.Dataset;

import play.libs.IO;
import util.Dicom;
import util.Properties;

public class Application extends SecureController {

	public static void index() throws Exception {
		//		Query query = JPA.em().createNativeQuery("select * from study");
		//		List results = query.getResultList();
		//		System.out.println(Arrays.toString((Object[]) results.get(0)));

		//		query = JPA.em().createNamedQuery("nativeSQL");
		//		results = query.getResultList();
		//		System.out.println(results);

		//		System.out.println(Patient.findAll());
		//		System.out.println(Study.findAll());
		//System.out.println(Series.findAll());
		//		
		//		for (Study study : Study.<Study>findAll()) {
		//			System.out.printf("%s\t%s\t%s\t%s\t%s%n", study.patient.pat_name, study.patient.pat_id, study.patient.pat_birthdate, study.study_desc, study.study_datetime);
		//		}

		//		String name = "%" + "EXAM NAME" + "%";
		//		String patientID = "%" + "Comments" + "%";
		//		System.out.println(Study.find("study_desc like ? and (patient.pat_id like ? or study_custom1 like ?)", name, patientID, patientID).fetch());

		//System.out.println(Tags.toString(Tags.valueOf("(2005,140F)")));

		render();
	}

	public static void help() throws Exception {
		render();
	}

	private static Map<String, String> comparators = new HashMap<String, String>() {{
		put("before", "<");
		put("on", "=");
		put("after", ">");
		put("since", ">");
	}};
	//TODO need patient data for date of birth, sex and weight
	public static void studies(String name, String id, Integer age, Character sex, String protocol, String acquisition, String study) throws Exception {
		List<String> from = new ArrayList<String>();
		from.add("Study study");

		List<String> where = new ArrayList<String>();
		List<Object> args = new ArrayList<Object>();

		if (!name.isEmpty()) {
			where.add("patient.pat_name like ?");
			args.add("%" + name + "%");
		}
		if (!id.isEmpty()) {
			where.add("(patient.pat_id like ? or study_custom1 like ?)");
			args.add("%" + id + "%");
			args.add("%" + id + "%");
		}
		if (sex != null) {
			where.add("patient.pat_sex = ?");
			args.add(sex);
		}
		if (!protocol.isEmpty()) {
			from.add("in(study.series) series");
			where.add("series.series_custom1 like ?");
			args.add("%" + protocol + "%");
		}
		if (!study.isEmpty()) {
			where.add("study_desc like ?");
			args.add("%" + study + "%");
		}
		if (!acquisition.isEmpty()) {
			where.add(String.format("cast(study_datetime as date) %s ?", comparators.get(acquisition)));
			args.add(params.get(acquisition, Date.class));
		}

		String query = "select study from " + StringUtils.join(from, ", ");
		if (!where.isEmpty()) {
			query += " where " + StringUtils.join(where, " and ");
		}
		//System.out.println(query + " " + args);
		List<Study> studies = Study.find(query, args.toArray()).fetch();
		//System.out.println(studies);
		render(studies);
		//		System.out.printf("name %s%n", name);
		//		System.out.printf("id %s%n", id);
		//		System.out.printf("age %s%n", age);
		//		System.out.printf("sex %s%n", sex);
		//		System.out.printf("protocol %s%n", protocol);
		//		System.out.printf("acquisition %s%n", acquisition);
		//		System.out.printf("date %s%n", date);
		//		System.out.printf("study %s%n", study);
	}

	public static void patients(String name) throws Exception {
		//		List<DicomObject> patients = DicomQuery.patient(name);
		//		render(patients);
	}

	public static void patient(long pk) throws Exception {
		//		List<DicomObject> studies = DicomQuery.studies(id);
		//		render(studies);
		Patient patient = Patient.findById(pk);
		render(patient);
	}

	public static void study(String id) throws Exception {
		//		List<DicomObject> seriess = DicomQuery.seriess(id);
		//		render(seriess);
	}

	public static void series(long pk) throws Exception {
		//		List<DicomObject> images = DicomQuery.images(id);
		//		render(images);
		Series series = Series.findById(pk);
		Dataset dataset = Dicom.dataset(series);
		render(series, dataset);
	}

	public static void image(String objectUID, Integer columns) throws MalformedURLException, IOException {
		notFoundIfNull(objectUID);
		String url = String.format("http://%s:8080/wado?requestType=WADO&studyUID=&seriesUID=&objectUID=%s", Properties.getString("dicom.host"), objectUID);
		if (columns != null) {
			url += String.format("&columns=%s", columns);
		}
		IO.copy(new URL(url).openConnection().getInputStream(), response.out);
	}

	//TODO anonymise all downloads
	public static void download(long pk, String format) throws InterruptedException, IOException {
		File dicom = Dicom.file(Series.<Series>findById(pk));
		if ("nii".equals(format)) {
			//TODO do as a background job
			File tmp = new File(System.getProperty("java.io.tmpdir"));
			File nii = new File(tmp, String.format("%s.nii", dicom.getName()));
			nii.delete();
			new ProcessBuilder(Properties.getString("dcm2nii"), "-a", "n", "-g", "n", "-v", "n", "-f", "y", "-e", "n", "-d", "n", "-p", "n", "-o", tmp.getPath(), dicom.getPath()).start().waitFor();
			renderBinary(nii);
		} else {
			renderBinary(dicom);
		}
	}
}