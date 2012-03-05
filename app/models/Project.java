package models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Project extends Model {

	public String name;
	@OneToMany(mappedBy = "project")
	public Set<ProjectAssociation> projectAssociations;

	public Project(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
