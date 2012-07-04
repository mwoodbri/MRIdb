package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.PrePersist;

import play.db.jpa.Model;
import play.mvc.Scope.Session;
import ext.JavaExtensions;

@Entity
public class Log extends Model {

	public Date timestamp;
	public String username;
	public String message;

	public Log(String message) {
		this.message = message;
	}

	@PrePersist
	private void prePersist() {
		timestamp = new Date();
		username = Session.current().get("username");
	}

	@Override
	public String toString() {
		return String.format("%s %s %s %s", JavaExtensions.format(timestamp), JavaExtensions.time(timestamp), username, message);
	}
}
