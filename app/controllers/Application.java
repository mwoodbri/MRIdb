package controllers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import jobs.ClipboardExporter;
import jobs.Downloader;
import jobs.Downloader.Format;
import models.DomainModel;
import models.Instance;
import models.Person;
import models.Person.Role;
import models.Project;
import models.ProjectAssociation;
import models.Series;
import models.Study;
import notifiers.Mail;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dcm4che.data.Dataset;

import play.Invoker;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.binding.As;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.libs.Files;
import play.libs.IO;
import play.mvc.Before;
import play.mvc.Catch;
import util.Clipboard;
import util.Clipboard.Item;
import util.Dicom;
import util.DomainModelBinder;
import util.Medcon;
import util.PersistentLogger;
import util.Properties;
import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import controllers.Secure.Security;

public class Application extends SecureController {
	//session variables
	public static final String CLIPBOARD = "clipboard";
	public static final String EXPORTS = "exports";

	@Before(unless="guest")
	static void before() {
		if (Security.isConnected()) {
			if (getUser().role == Role.Guest) {
				guest();
			}
			renderArgs.put(CLIPBOARD, new Clipboard(getUser().clipboard));
			renderArgs.put(EXPORTS, ClipboardExporter.getExports(session));
		}
	}

	public static void guest() {
		render();
	}

	public static void index(Integer page, String order, String sort) {
		if (page == null) {
			index(0, order, sort);
		}
		List<Study> studies = Study.find(String.format("from Study where study_datetime is not null order by %s %s", order == null || order.isEmpty() ? "study_datetime" : order, "asc".equals(sort) ? "asc" : "desc")).fetch(page + 1, Properties.pageSize());
		int studyCount = (int) Study.count();
		render(studies, studyCount, page);
	}

	public static void recent() {
		render();
	}

	public static void help() {
		render();
	}

	public static void preferences(Person person, String frame) {
		if ("POST".equals(request.method)) {
			person.preferMultiframe = "multi".equals(frame);
			person.save();
		}
		render(person);
	}

	public static void advanced() {
		render();
	}

	public static void batch(File spreadsheet) throws IOException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		if (spreadsheet == null) {
			if ("POST".equals(request.method)) {
				Validation.addError("spreadsheet", "Please select a file");
			}
			render();
		}

		List<List<DomainModel>> objects = new ArrayList<List<DomainModel>>();
		List<String> found = new ArrayList<String>();
		List<String> missing = new ArrayList<String>();

		CSVReader reader = new CSVReader(new FileReader(spreadsheet), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
		for (String[] line : reader.readAll()) {
			List<String> series_descs = new ArrayList<String>();
			for (int i = 2; line.length > i && !line[i].trim().isEmpty(); i++) {
				series_descs.add(line[i].trim().toUpperCase());
			}
			String pat_id = line[0].trim().toUpperCase();
			if (!pat_id.isEmpty()) {
				if (!series_descs.isEmpty()) {
					List<DomainModel> serieses = new ArrayList<DomainModel>();
					for (String series_desc : series_descs) {
						List<Series> candidates = (List<Series>) CollectionUtils.select(Series.find("study.patient.pat_id = ? and series_desc = ?", pat_id, series_desc).<Series>fetch(), new Predicate() {
							@Override
							public boolean evaluate(Object candidate) {
								return Dicom.renderable((Series) candidate);
							}
						});
						if (!candidates.isEmpty()) {
							for (Series series : candidates) {
								found.add(String.format("%s - %s", pat_id, series_desc));
								serieses.add(series);
							}
						} else {
							missing.add(String.format("%s - %s", pat_id, series_desc));
						}
					}
					if (!serieses.isEmpty()) {
						objects.add(serieses);
					}
				} else {
					List<DomainModel> studies = Study.find("patient.pat_id", pat_id).<DomainModel>fetch();
					if (!studies.isEmpty()) {
						for (DomainModel study : studies) {
							found.add(pat_id);
							objects.add(Collections.singletonList(study));
						}
					} else {
						missing.add(pat_id);
					}
				}
			} else {
				String participationID = line[1].trim().toUpperCase();
				if (!series_descs.isEmpty()) {
					List<DomainModel> serieses = new ArrayList<DomainModel>();
					for (String series_desc : series_descs) {
						List<Series> candidates = (List<Series>) CollectionUtils.select(Series.find("select series from Series series, in(series.study.projectAssociations) projectAssociation where projectAssociation.participationID = ? and series_desc = ?", participationID, series_desc).<Series>fetch(), new Predicate() {
							@Override
							public boolean evaluate(Object candidate) {
								return Dicom.renderable((Series) candidate);
							}
						});
						if (candidates.isEmpty()) {
							missing.add(String.format("%s - %s", participationID, series_desc));
						} else {
							for (Series series : candidates) {
								found.add(String.format("%s - %s", participationID, series_desc));
								serieses.add(series);
							}
						}
					}
					if (!serieses.isEmpty()) {
						objects.add(serieses);
					}
				} else {
					List<DomainModel> studies = Study.find("select study from Study study, in(study.projectAssociations) projectAssociation where projectAssociation.participationID = ?", participationID).<DomainModel>fetch();
					if (!studies.isEmpty()) {
						for (DomainModel study : studies) {
							found.add(participationID);
							objects.add(Collections.singletonList(study));
						}
					} else {
						missing.add(participationID);
					}
				}
			}
		}
		reader.close();

		if (objects.isEmpty()) {
			Validation.addError(null, "No studies or series found");
		}

		if (Validation.hasErrors()) {
			render();
		}

		Map<String, String> pks = new LinkedHashMap<String, String>();
		for (List<DomainModel> object : objects) {
			Study study = object.get(0) instanceof Study ? ((Study) object.get(0)) : ((Series) object.get(0)).study;
			pks.put("pk=" + StringUtils.join(Item.serialize(object), "&pk="), study.patient.pat_id);
		}

		renderTemplate("@batch2", pks, found, missing);
	}

	@Check("admin")
	public static void audit(File spreadsheet) throws IOException, ParseException {
		if (spreadsheet == null) {
			render();
		}
		response.contentType = "text/csv";
		response.setHeader("Content-Disposition", "attachment; filename='audit.csv'");
		CSVReader reader = new CSVReader(new FileReader(spreadsheet));
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(response.out));
		String[] headers = reader.readNext();
		writer.writeNext(headers);
		TypedQuery<Study> studyQueryByPatientID = JPA.em().createQuery("from Study where patient.pat_id = :pat_id and cast(study_datetime as date) = :study_datetime", Study.class);
		TypedQuery<Study> studyQueryByParticipationID = JPA.em().createQuery("select study from Study study left join study.projectAssociations projectAssociation where cast(study.study_datetime as date) = :study_datetime and projectAssociation.participationID = :participationID", Study.class);
		TypedQuery<Project> projectQuery = JPA.em().createQuery("from Project where lower(name) = lower(:name)", Project.class);
		String[] line = null;
		int lineNumber = 0;
		while ((line = reader.readNext()) != null) {
			if ((StringUtils.isEmpty(line[3].trim()) && StringUtils.isEmpty(line[2].trim())) || StringUtils.isEmpty(line[9].trim())) {
				continue;
			}
			List<Study> studies;
			if (!StringUtils.isEmpty(line[3].trim())) {
				//Lookup using 3 and 9, link using 1 and 2
				studies = studyQueryByPatientID.setParameter("pat_id", line[3].trim()).setParameter("study_datetime", new SimpleDateFormat("dd/MM/yyyy").parse(line[9].trim())).getResultList();
			} else {
				//Lookup using 2 and 9, no link
				studies = studyQueryByParticipationID.setParameter("participationID", line[2].trim()).setParameter("study_datetime", new SimpleDateFormat("dd/MM/yyyy").parse(line[9].trim())).getResultList();
			}
			if (!studies.isEmpty()) {
				line[10] = "Yes";
				List<String> comments = new ArrayList<String>();
				for (Study study : studies) {
					comments.add(study.study_custom1);
				}
				line[12] = StringUtils.join(comments, "\n");
				if (!StringUtils.isEmpty(line[3].trim())) {
					Project project = null;
					String projectName = line[1].trim();
					if (!projectName.isEmpty()) {
						try {
							project = projectQuery.setParameter("name", projectName).getSingleResult();
						} catch (NoResultException e) {
							project = new Project(projectName).save();
						}
					}
					for (Study study : studies) {
						associate(study, project, line[2].trim());
					}
				}
			}
			Boolean singleFrames = null;
			for (int i = 13; i < headers.length; i++) {
				final String header = headers[i];
				for (Study study : studies) {
					Series series = (Series) CollectionUtils.find(study.series, new Predicate() {
						@Override
						public boolean evaluate(Object arg0) {
							return header.equalsIgnoreCase(((Series) arg0).series_desc);
						}
					});
					if (series != null) {
						line[i] = "Yes";
						singleFrames = (singleFrames == null ? true : singleFrames) && Dicom.singleFrame(series);
					}
				}
			}
			if (Boolean.TRUE.equals(singleFrames)) {
				line[11] = "Yes";
			}
			writer.writeNext(line);
			if (++lineNumber % 100 == 0) {
				JPA.em().flush();
				JPA.em().clear();
			}
		}
		reader.close();
		writer.close();
	}

	public static void simpleSearch(String terms, int page, String order, String sort) {
		if (terms.trim().isEmpty()) {
			index(0, null, null);
		}
		//String[] termsArray = terms.toLowerCase().split(" ");
		//no view, no full text
		//String query = "from study, (select study.pk, patient.pk pat_pk, pat_name, pat_id, pat_birthdate, study_desc, study_datetime, lower(study_desc || ' ' || pat_name || ' ' || pat_id || ' ' || coalesce(projectassociation.participationid, '') || ' ' || coalesce(project.name, '')) result from study join patient on (study.patient_fk = patient.pk) left join projectassociation on (projectassociation.study_pk = study.pk) left join project on (projectassociation.project_id = project.id)) as subquery where study.pk = subquery.pk";
		//for (int i = 0; i < termsArray.length; i++) {
		//	query += " and subquery.result like ?";
		//}
		//view, no full text
		//String query = "from study, studyfulltext where study.pk = studyfulltext.pk";
		//for (int i = 0; i < termsArray.length; i++) {
		//	query += " and studyfulltext.fulltext like ?";
		//}
		//full text
		String query = String.format("from study join patient on study.patient_fk = patient.pk left join projectassociation on projectassociation.study_pk = study.pk left join project on projectassociation.project_id = project.id where to_tsvector('english_nostop', study_desc || %s' ' || pat_id || ' ' || coalesce(projectassociation.participationid, '') || ' ' || coalesce(project.name, '')) @@ plainto_tsquery('english_nostop', ?)", getUser().role == Role.Visitor ? "" : "' ' || pat_name || ");

		Query studyCountQuery = JPA.em().createNativeQuery("select count(*) " + query);
		//		for (int i = 0; i < termsArray.length; i++) {
		//			studyCountQuery.setParameter(i + 1, "%" + termsArray[i] + "%");
		//		}
		studyCountQuery.setParameter(1, StringUtils.join(terms.split(" "), " & "));
		int studyCount = ((BigInteger) studyCountQuery.getSingleResult()).intValue();

		order = order.contains(".") ? order.split("[.]")[1] : order;
		query = "select study.* " + query + String.format(" order by %s %s", order, "desc".equals(sort) ? "desc" : "asc");
		Query studiesQuery = JPA.em().createNativeQuery(query, Study.class).setFirstResult(page * Properties.pageSize()).setMaxResults(Properties.pageSize());
		//		for (int i = 0; i < termsArray.length; i++) {
		//			studiesQuery.setParameter(i + 1, "%" + termsArray[i] + "%");
		//		}
		studiesQuery.setParameter(1, StringUtils.join(terms.split(" "), " & "));
		List studies = studiesQuery.getResultList();

		renderTemplate("@index", studies, studyCount, page);
	}

	private static final Map<String, String> comparators = new HashMap<String, String>() {{
		put("before", "<=");
		put("on", "=");
		put("after", ">");
		put("since", ">");
	}};
	public static void advancedSearch(String id, String name, Integer age, Character sex, String protocol, String acquisition, String study, int page, String order, String sort, Long project, String participationID) throws Exception {
		List<String> from = new ArrayList<String>();
		from.add("Study study");

		List<String> where = new ArrayList<String>();
		List<Object> args = new ArrayList<Object>();

		if (!id.isEmpty()) {
			where.add("(lower(study.patient.pat_id) like ? or lower(study.study_custom1) like ?)");
			args.add("%" + id.toLowerCase() + "%");
			args.add("%" + id.toLowerCase() + "%");
		}
		if (getUser().role != Role.Visitor) {
			if (!name.isEmpty()) {
				where.add("lower(study.patient.pat_name) like ?");
				args.add("%" + name.toLowerCase() + "%");
			}
			if (age != null) {
				where.add("cast(study.study_datetime as date) - cast(study.patient.pat_birthdate as date) >= ? and cast(study.study_datetime as date) - cast(study.patient.pat_birthdate as date) < ?");
				args.add(365D * age);
				args.add(365D * (age + 1));
			}
		}
		if (sex != null) {
			where.add("study.patient.pat_sex = ?");
			args.add(sex);
		}
		if (!protocol.isEmpty()) {
			from.add("in(study.series) series");
			where.add("lower(series.series_custom1) like ?");
			args.add("%" + protocol.toLowerCase() + "%");
		}
		if (!study.isEmpty()) {
			where.add("lower(study.study_desc) like ?");
			args.add("%" + study.toLowerCase() + "%");
		}
		if (!acquisition.isEmpty()) {
			//where.add(String.format("(study_datetime is null or cast(study_datetime as date) %s ?)", comparators.get(acquisition)));
			where.add(String.format("cast(study.study_datetime as date) %s ?", comparators.get(acquisition)));
			args.add(params.get(acquisition, Date.class));
		}
		if (project != null || !participationID.isEmpty()) {
			from.add("in (study.projectAssociations) association");
			if (project != null) {
				where.add("association.project.id = ?");
				args.add(project);
			}
			if (!participationID.isEmpty()) {
				where.add("association.participationID = ?");
				args.add(participationID);
			}
		}

		String query = "from " + StringUtils.join(from, ", ");
		if (!where.isEmpty()) {
			query += " where " + StringUtils.join(where, " and ");
		}
		String entityQuery = String.format("select study %s order by study.%s %s", query, order.isEmpty() ? "patient.pk" : order, "desc".equals(sort) ? "desc" : "asc");
		List<Study> studies = Study.find(entityQuery, args.toArray()).fetch(page + 1, Properties.pageSize());
		String countQuery = String.format("select count(study) %s", query);
		Query count = JPA.em().createQuery(countQuery);
		for (int i = 0; i < args.size(); i++) {
			count.setParameter(i + 1, args.get(i));
		}
		int studyCount = ((Long) count.getSingleResult()).intValue();
		renderTemplate("@index", studies, studyCount, page);
	}

	public static void study(long pk) {
		Study study = Study.findById(pk);
		ProjectAssociation projectAssociation = study.getProjectAssociation();
		render(study, projectAssociation);
	}

	public static void series(long pk) throws IOException {
		Series series = Series.findById(pk);
		Instance instance = Dicom.multiFrame(series);
		if (instance == null) {
			Collection instances = Dicom.singleFrames(series);
			instance = instances.isEmpty() ? Dicom.spectrogram(series) : (Instance) instances.iterator().next();
		}
		Dataset dataset = Dicom.dataset(Dicom.file(instance));
		Set<String> echoes = Dicom.echoes(dataset);
		render(series, dataset, echoes);
	}

	public static void image(long pk, Integer columns) throws MalformedURLException, IOException {
		Series series = Series.findById(pk);
		if (!Dicom.renderable(series)) {
			renderBinary(new File(Play.applicationPath, "public/images/spectrogram.png"));
		}
		int frameNumber;
		String objectUID = null;
		Instance instance = Dicom.multiFrame(series);
		if (instance != null) {
			frameNumber = Dicom.numberOfFrames(series) / 2 + 1;
			objectUID = instance.sop_iuid;
		} else {
			Object[] instances = Dicom.singleFrames(series).toArray(new Instance[0]);
			if (instances.length == 0) {
				renderBinary(new File(Play.applicationPath, "public/images/128x128.gif"));
			}
			frameNumber = 1;
			Arrays.sort(instances, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return Integer.valueOf(((Instance) o1).inst_no).compareTo(Integer.valueOf(((Instance) o2).inst_no));
				}
			});
			objectUID = ((Instance) instances[instances.length / 2]).sop_iuid;
		}
		//columns=256 matches prefetch configuration
		String url = String.format("http://%s:8080/wado?requestType=WADO&studyUID=&seriesUID=&objectUID=%s&frameNumber=%s&columns=%s", request.domain, objectUID, frameNumber, columns == null ? 256 : columns);
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			Logger.warn("Image not found for %s", series);
			renderBinary(new File(Play.applicationPath, "public/images/missing.png"));
		}
		IO.copy(new URL(url).openConnection().getInputStream(), response.out);
	}

	//pk is EITHER a Study OR a list of Series from a common Study
	//@Transactional(readOnly=true)
	public static void download(@As(binder=DomainModelBinder.class) List<DomainModel> pk, Format format) throws InterruptedException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		{
			Study study = pk.get(0) instanceof Study ? (Study) pk.get(0) : ((Series) pk.get(0)).study;
			PersistentLogger.log("downloaded %s %s %s", pk.get(0) instanceof Study ? "study" : "series", pk, study.patient.pat_id);
		}
		File tmpDir = new File(Properties.getDownloads(), UUID.randomUUID().toString());
		tmpDir.mkdir();
		File outDir = await(new Downloader(format == null ? Format.dcm : format, tmpDir, Boolean.TRUE.equals(getUser().preferMultiframe), getUser().niftiMultiframeScript, Item.serialize(pk)).now());
		if (FileUtils.listFiles(outDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).isEmpty()) {
			error("Failed to retrieve files");
		}
		File zip = new File(tmpDir, String.format("%s.zip", outDir.getName()));
		Files.zip(outDir, zip);
		renderBinary(zip);
	}

	public static void export(String password) throws InterruptedException, IOException, ClassNotFoundException {
		PersistentLogger.log("exported clipboard %s", getUser().clipboard);
		Clipboard clipboard = (Clipboard) renderArgs.get(CLIPBOARD);
		File tmpDir = new File(Properties.getDownloads(), UUID.randomUUID().toString());
		tmpDir.mkdir();
		new ClipboardExporter(clipboard, tmpDir, password, session, getUser().username, Boolean.TRUE.equals(getUser().preferMultiframe), getUser().niftiMultiframeScript).now();
		clipboard(null, null, null);
	}

	public static void retrieve(String filename) {
		File download = ClipboardExporter.getExport(filename, session);
		notFoundIfNull(download);
		renderBinary(download);
	}

	public enum ClipboardOp { ADD, REMOVE, CLEAR }
	public static void clipboard(ClipboardOp op, String type, Long pk) throws ClassNotFoundException {
		Clipboard clipboard = (Clipboard) renderArgs.get(CLIPBOARD);
		if (op != null) {
			switch (op) {
			case ADD: clipboard.add(type, pk); break;
			case REMOVE: clipboard.remove(type, pk); break;
			case CLEAR: clipboard.clear(); break;
			}
			Person person = getUser();
			person.clipboard = clipboard.toString();
			person.merge()._save();
			Cache.delete(Security.connected().toLowerCase());
		}
		render();
	}

	public static void imagej(long pk) throws Exception {
		Series series = Series.findById(pk);

		File tmpDir = new File(Properties.getDownloads(), UUID.randomUUID().toString());
		tmpDir.mkdir();

		File dcm;
		Instance instance = Dicom.multiFrame(series);
		if (instance != null) {
			await(new Downloader(Format.dcm, tmpDir, Boolean.TRUE.equals(getUser().preferMultiframe), getUser().niftiMultiframeScript, new Item(series)).now());
			dcm = tmpDir.listFiles()[0].listFiles()[0].listFiles()[0];
		} else {
			File unanonymised = new File(tmpDir, String.format("%s.unanonymised.dcm", series.pk));
			//medcon has a -anon flag but it doesn't work with -stack3d, so we anonymise manually in line with other exports
			Medcon.convert(Dicom.collate(series, Boolean.TRUE.equals(getUser().preferMultiframe)), Format.dcm, unanonymised);
			dcm = new File(tmpDir, String.format("%s.dcm", series.pk));
			Dicom.anonymise(unanonymised, dcm, null);
		}
		renderBinary(dcm);
	}

	public static void studyComments(Study study) {
		study.save();
		study(study.pk);
	}

	public static void associate(Study study, Long projectID, String participationID, String projectName) {
		Project project = null;
		if (!projectName.isEmpty()) {
			project = new Project(projectName).save();
		} else if (projectID != null) {
			project = Project.findById(projectID);
		}
		associate(study, project, participationID);
		PersistentLogger.log("study %s linked to project: %s (%s)", study.pk, project, participationID);
	}

	static void associate(Study study, Project project, String participationID) {
		ProjectAssociation association = ProjectAssociation.find("byStudy", study).first();
		if (project == null) {
			if (association != null) {
				association.delete();
			}
		} else {
			if (association != null) {
				if (association.project.id == project.id) {
					if (participationID.equals(association.participationID)) {
						return;
					}
				} else {
					association.project = project;
				}
			} else {
				association = new ProjectAssociation(project, study);
			}
			association.participationID = participationID;
			association.save();
		}
	}

	@Catch(Throwable.class)
	static void log(Throwable e) {
		if (!(e instanceof Invoker.Suspend) && Properties.getString("mail.to") != null) {
			Mail.exception(request, session, e);
		}
	}

}