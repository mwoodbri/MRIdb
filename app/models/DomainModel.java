package models;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.db.jpa.GenericModel;

@MappedSuperclass
public abstract class DomainModel extends GenericModel {

	@Id
	public long pk;

}
