package controllers;

import models.Person;
import play.Logger;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import util.Properties;
import controllers.Secure.Security;

@With(Secure.class)
public class SecureController extends Controller {

	@Before
	static void log() {
		if (Security.isConnected() && !request.isAjax() && !"false".equals(Properties.getString("log.actions"))) {
			Logger.info(Security.connected() + " " + request.action);
		}
	}

	public static Person getUser() {
		String username = Security.connected().toLowerCase();
		Person person = Cache.get(username, Person.class);
		if (person == null) {
			person = Person.findById(username);
			Cache.set(username, person);
		}
		return person;
	}

}
