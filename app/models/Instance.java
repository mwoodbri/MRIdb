package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Instance extends DomainModel {

	public String sop_iuid;
	public String sop_cuid;
	public byte[] inst_attrs;
	public String inst_no;
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
