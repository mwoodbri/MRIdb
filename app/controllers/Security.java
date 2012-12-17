package controllers;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import models.Person;
import models.Person.Role;
import play.Logger;
import play.Play;
import util.PersistentLogger;
import util.Properties;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		boolean authenticated = false;
		if (Properties.getString("ldap.server") == null) {
			authenticated = password.equals(username);
		} else {
			if (!password.isEmpty()) {
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.PROVIDER_URL, Properties.getString("ldap.server"));
				env.put(Context.SECURITY_PRINCIPAL, String.format("%s@%s", username, Properties.getString("ldap.domain")));
				env.put(Context.SECURITY_CREDENTIALS, password);
				try {
					new InitialDirContext(env);
					authenticated = true;
				} catch (NamingException e) {
					Logger.info("LDAP authentication failed for %s", username);
				}
			}
		}
		return authenticated;
	}

	static void onAuthenticated() {
		String username = Security.connected().toLowerCase();
		Person person = Person.findById(username);
		if (person == null) {
			person = new Person(username);
			person.validateAndCreate();
		}
		PersistentLogger.log("logged in");
	}

	static boolean check(String profile) {
		String username = Security.connected().toLowerCase();
		if ("admin".equals(profile)) {
			return Person.<Person>findById(username).role == Role.Administrator;
		}
		return false;
	}
}
