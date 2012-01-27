package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.jpa.GenericModel;

@Entity
public class Filesystem extends GenericModel {

	@Id
	public long pk;
	public String dirpath;

	@Override
	public String toString() {
		return String.format("%s", pk);
	}
}
