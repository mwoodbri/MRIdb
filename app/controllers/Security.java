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
		if (Play.mode.isDev() && Properties.getString("ldap.server") == null) {
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
		Person person = Person.findById(Security.connected());
		if (person == null) {
			person = new Person(Security.connected());
			person.validateAndCreate();
		}
		PersistentLogger.log("Logged in");
	}
	
	static boolean check(String profile) {
		if ("admin".equals(profile)) {
			return Person.<Person>findById(Security.connected()).role == Role.Administrator;
		}
		return false;
	}
}
