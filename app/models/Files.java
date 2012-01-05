package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import play.db.jpa.GenericModel;

@Entity
public class Files extends GenericModel {

	@Id
	public long pk;
	public String filepath;
	@ManyToOne
	@JoinColumn(name="instance_fk")
	public Instance instance;

	@Override
	public String toString() {
		return String.format("%s", pk);
	}
}
