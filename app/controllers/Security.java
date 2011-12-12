package controllers;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import play.Logger;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		boolean authenticated = false;
		if (!"".equals(password)) {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			//TODO put these in settings file
			//TODO don't allow blank username or password
			env.put(Context.PROVIDER_URL, "ldap://fuji.isd.csc.mrc.ac.uk");
			env.put(Context.SECURITY_PRINCIPAL, username + "@ISD");
			env.put(Context.SECURITY_CREDENTIALS, password);
			try {
				new InitialDirContext(env);
				authenticated = true;
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		Logger.info(username + " authenticated " + authenticated);
		return authenticated;
	}
}
