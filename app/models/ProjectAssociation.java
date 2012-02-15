package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class ProjectAssociation extends Model {

	public String participationID;
	@ManyToOne
	public Project project;
	@ManyToOne
	public Study study;

	public ProjectAssociation(Project project, Study study) {
		this.project = project;
		this.study = study;
	}

}
