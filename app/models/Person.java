package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PostUpdate;

import org.springframework.security.crypto.bcrypt.BCrypt;

import play.cache.Cache;
import play.db.jpa.GenericModel;

@Entity
public class Person extends GenericModel {

	@Id
	public String username;
	public String password;
	public Role role = Role.Guest;
	@Lob
	public String clipboard;
	public Boolean preferMultiframe;
	public String niftiMultiframeScript;

	public Person(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		if (!password.startsWith("$2a$")) {
			this.password = BCrypt.hashpw(password, BCrypt.gensalt());
		}
	}

	@PostUpdate
	public void postUpdate() {
		Cache.delete(username);
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", username, role);
	}

	public static enum Role {
		Researcher, Administrator, Visitor, Guest;
	}

}
