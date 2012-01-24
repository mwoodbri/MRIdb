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
import java.util.Set;
import java.util.UUID;

import jobs.Exporter;
import models.Instance;
import models.Patient;
import models.Series;
import models.Study;

import org.apache.commons.lang.StringUtils;
import org.dcm4che.data.Dataset;

import play.Play;
import play.libs.IO;
import play.mvc.Before;
import util.Clipboard;
import util.Dicom;
import util.PersistentLogger;
import util.Properties;

public class Application extends SecureController {
	private static final String CLIPBOARD = "clipboard";

	@Before
	static void before() {
		renderArgs.put(CLIPBOARD, new Clipboard(session.get(CLIPBOARD)));
	}

	public static void index(String reset) throws Exception {
		//Query query = JPA.em().createNativeQuery("select * from study");
		//List results = query.getResultList();
		//System.out.println(Arrays.toString((Object[]) results.get(0)));

		//query = JPA.em().createNamedQuery("nativeSQL");
		//results = query.getResultList();
		//System.out.println(results);

		//for (Study study : Study.<Study>findAll()) {
		//	System.out.printf("%s\t%s\t%s\t%s\t%s%n", study.patient.pat_name, study.patient.pat_id, study.patient.pat_birthdate, study.study_desc, study.study_datetime);
		//}

		//System.out.println(Tags.toString(Tags.valueOf("(2005,140F)")));

		//		if (reset != null) {
		//			session.put(CLIPBOARD, "S51,s55");
		//		}

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
	public static void studies(String name, String id, Integer age, Character sex, String protocol, String acquisition, String study) throws Exception {
		List<String> from = new ArrayList<String>();
		from.add("Study study");

		List<String> where = new ArrayList<String>();
		List<Object> args = new ArrayList<Object>();

		if (!name.isEmpty()) {
			where.add("lower(patient.pat_name) like ?");
			args.add("%" + name.toLowerCase() + "%");
		}
		if (!id.isEmpty()) {
			where.add("(lower(patient.pat_id) like ? or lower(study_custom1) like ?)");
			args.add("%" + id.toLowerCase() + "%");
			args.add("%" + id.toLowerCase() + "%");
		}
		if (age != null) {
			where.add("cast(study_datetime as date) - cast(patient.pat_birthdate as date) >= ? and cast(study_datetime as date) - cast(patient.pat_birthdate as date) < ?");
			args.add(365D * age);
			args.add(365D * (age + 1));
		}
		if (sex != null) {
			where.add("patient.pat_sex = ?");
			args.add(sex);
		}
		if (!protocol.isEmpty()) {
			from.add("in(study.series) series");
			where.add("lower(series.series_custom1) like ?");
			args.add("%" + protocol.toLowerCase() + "%");
		}
		if (!study.isEmpty()) {
			where.add("lower(study_desc) like ?");
			args.add("%" + study.toLowerCase() + "%");
		}
		if (!acquisition.isEmpty()) {
			where.add(String.format("cast(study_datetime as date) %s ?", comparators.get(acquisition)));
			args.add(params.get(acquisition, Date.class));
		}

		String query = "select study from " + StringUtils.join(from, ", ");
		if (!where.isEmpty()) {
			query += " where " + StringUtils.join(where, " and ");
		}
		List<Study> studies = Study.find(query, args.toArray()).fetch();
		render(studies);
	}

	public static void patient(long pk) throws Exception {
		Patient patient = Patient.findById(pk);
		render(patient);
	}

	public static void series(long pk) throws Exception {
		Series series = Series.findById(pk);
		Dataset dataset = Dicom.dataset(Dicom.file(series.instances.iterator().next()));
		//Dataset privateDataset = Dicom.privateDataset(dataset);
		Set<String> echoes = Dicom.echoes(dataset);
		render(series, dataset, echoes);
	}

	public static void image(long pk, Integer columns, Integer frameNumber) throws MalformedURLException, IOException {
		Series series = Series.findById(pk);
		Instance instance = series.instances.iterator().next();
		if (!instance.sop_cuid.equals("1.2.840.10008.5.1.4.1.1.4") && !instance.sop_cuid.equals("1.2.840.10008.5.1.4.1.1.7")) {
			renderBinary(new File(Play.applicationPath, "public/images/128x128.gif"));
		}
		String url = String.format("http://%s:8080/wado?requestType=WADO&studyUID=&seriesUID=&objectUID=%s", Properties.getString("dicom.host"), instance.sop_iuid);
		if (columns != null) {
			url += String.format("&columns=%s", columns);
		}
		if (frameNumber != null) {
			url += String.format("&frameNumber=%s", frameNumber);
		}
		IO.copy(new URL(url).openConnection().getInputStream(), response.out);
	}

	public static void download(long pk, String format, String echo) throws InterruptedException, IOException {
		PersistentLogger.log("Downloaded series %s", pk);
		File tmpDir = new File(new File(Play.tmpDir, "downloads"), UUID.randomUUID().toString());
		tmpDir.mkdir();
		renderBinary(await(new Exporter(pk, format, echo, tmpDir).now()));
	}

	public static void clipboard(String type, long pk, boolean remove) throws ClassNotFoundException {
		if (remove) {
			if (type == null) {
				session.put(CLIPBOARD, ((Clipboard) renderArgs.get(CLIPBOARD)).clear());
			} else {
				session.put(CLIPBOARD, ((Clipboard) renderArgs.get(CLIPBOARD)).remove(type, pk));
			}
		} else {
			session.put(CLIPBOARD, ((Clipboard) renderArgs.get(CLIPBOARD)).add(type, pk));
		}
		if (!request.isAjax()) {
			redirect(request.headers.get("referer").value());
		}
		render();
	}
}