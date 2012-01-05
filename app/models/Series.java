package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.GenericModel;

@Entity
public class Series extends GenericModel {

	@Id
	public long pk;
	public String series_no;
	public String series_custom1;
	@ManyToOne
	@JoinColumn(name="study_fk")
	public Study study;
	@OneToMany(mappedBy = "series")
	public Set<Instance> instances;

	@Override
	public String toString() {
		return String.format("%s %s %s", pk, series_custom1, study);
	}
}
