package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.PrePersist;

import play.db.jpa.Model;
import play.mvc.Scope.Session;

@Entity
public class Log extends Model {

	public Date timestamp;
	public String username;
	public String message;

	public Log(String message) {
		this.message = message;
	}

	@SuppressWarnings("unused")
	@PrePersist
	private void prePersist() {
		timestamp = new Date();
		username = Session.current().get("username");
	}

}
