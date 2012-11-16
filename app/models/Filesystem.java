package models;

import javax.persistence.Entity;

@Entity
public class Filesystem extends DomainModel {

	public String dirpath;

	@Override
	public String toString() {
		return String.format("%s", pk);
	}
}
