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
	//public Viewer viewer;

	public Person(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", username, role, clipboard);
	}

	public static enum Role {
		RESEARCHER, PI, ADMIN
	}

	//	public static enum Viewer {
	//		WEASIS, IMAGEJ
	//	}

}
