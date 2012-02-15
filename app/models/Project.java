package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Project extends Model {

	public String name;
	@ManyToOne
	public Person person;
	@OneToMany(mappedBy = "project")
	public Set<ProjectAssociation> projectAssociations;

	public Project(String name, Person person) {
		this.name = name;
		this.person = person;
	}

	@Override
	public String toString() {
		return name;
	}
}
