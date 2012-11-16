package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class Patient extends DomainModel {

	public String pat_id;
	public String pat_name;
	public String pat_birthdate;
	public Character pat_sex;
	public byte[] pat_attrs;
	@OneToMany(mappedBy = "patient")
	public Set<Study> studies;

	@Override
	public String toString() {
		return String.format("%s %s %s %s", pat_id, pat_name, pat_birthdate, pat_sex);
	}
}
