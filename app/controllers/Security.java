package controllers;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import play.Logger;
import play.Play;
import util.Properties;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		boolean authenticated = false;
		if (Play.mode.isDev() && Properties.getString("ldap.server") == null && password.equals(username)) {
			authenticated = true;
		} else {
			if (!"".equals(password)) {
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.PROVIDER_URL, Properties.getString("ldap.server"));
				env.put(Context.SECURITY_PRINCIPAL, username + "@" + Properties.getString("ldap.domain"));
				env.put(Context.SECURITY_CREDENTIALS, password);
				try {
					new InitialDirContext(env);
					authenticated = true;
				} catch (NamingException e) {
				}
			}
		}
		Logger.info(username + " authenticated " + authenticated);
		return authenticated;
	}
}
