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
	public Role role = Role.Visitor;
	@Lob
	public String clipboard;

	public Person(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = BCrypt.hashpw(password, BCrypt.gensalt());
	}

	@PostUpdate
	public void postUpdate() {
		Cache.delete(username);
	}

	@Override
	public String toString() {
		return username;
	}

	public static enum Role {
		Researcher, Administrator, Visitor
	}

}
