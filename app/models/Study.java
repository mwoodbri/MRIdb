package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import play.db.jpa.GenericModel;
import ext.JavaExtensions;

@Entity
//@NamedNativeQuery(name = "nativeSQL", query = "select * from study", resultClass = Study.class)
public class Study extends GenericModel {

	@Id
	public long pk;
	public String study_id;
	public String study_desc;
	public Date study_datetime;
	public String study_custom1;
	public Date created_time;
	@ManyToOne
	@JoinColumn(name="patient_fk")
	public Patient patient;
	@OneToMany(mappedBy = "study")
	public Set<Series> series;
	@OneToMany(mappedBy = "study")
	public Set<ProjectAssociation> projectAssociations;

	public ProjectAssociation getProjectAssociation() {
		return ProjectAssociation.find("select projectAssociation from Project project, in(project.projectAssociations) projectAssociation where projectAssociation.study = ?", this).first();
	}

	public String toDownloadString() {
		List<String> parts = new ArrayList<String>();
		ProjectAssociation projectAssociation = getProjectAssociation();
		if (projectAssociation != null) {
			if (projectAssociation.participationID != null && !projectAssociation.participationID.isEmpty()) {
				parts.add(projectAssociation.participationID);
			}
			parts.add(projectAssociation.project.name);
		}
		parts.add(new SimpleDateFormat("yyyyMMddHHmm").format(study_datetime));
		String station_name = getStation_name();
		if (station_name != null) {
			parts.add(station_name);
		}
		return StringUtils.join(parts, "_").replaceAll("\\W+", "");
	}

	public String getStation_name() {
		Series scannedSeries = (Series) CollectionUtils.find(series, new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				return ((Series) arg0).station_name != null;
			}
		});
		return scannedSeries == null ? null : scannedSeries.station_name;
	}

	public String toClipboardString() {
		return String.format("%s on %s", patient.pat_name == null ? "UNKNOWN" : JavaExtensions.formatAsName(patient.pat_name), study_datetime == null ? "" : JavaExtensions.format(study_datetime));
	}

}
