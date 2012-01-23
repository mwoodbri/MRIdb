package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import ext.JavaExtensions;

import play.db.jpa.GenericModel;

@Entity
public class Series extends GenericModel {

	@Id
	public long pk;
	public String series_no;
	public String series_iuid;
	public String series_custom1;
	@ManyToOne
	@JoinColumn(name="study_fk")
	public Study study;
	@OneToMany(mappedBy = "series")
	public Set<Instance> instances;

	public String toDownloadString() {
		return String.format("%s_%s-%s_%s", study.patient.pat_id, series_custom1, series_no, JavaExtensions.format(study.study_datetime));
	}
	
	public String toClipboardString() {
		return String.format("%s of %s", series_custom1, study.toClipboardString());
	}
}
