package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.GenericModel;

@Entity
public class Instance extends GenericModel {

	@Id
	public long pk;
	public String sop_iuid;
	public byte[] inst_attrs;
	@ManyToOne
	@JoinColumn(name="series_fk")
	public Series series;
	@OneToMany(mappedBy = "instance")
	public Set<Files> files;

	@Override
	public String toString() {
		return String.format("%s", pk);
	}
}
