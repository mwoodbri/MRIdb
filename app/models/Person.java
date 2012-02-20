package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.jpa.GenericModel;

@Entity
public class Person extends GenericModel {

	@Id
	public String username;
	public Role role;
	public String clipboard;

	public Person(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return username;
	}

	public static enum Role {
		Researcher, Administrator
	}

}
