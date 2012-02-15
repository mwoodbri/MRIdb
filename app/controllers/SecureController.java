package controllers;

import models.Person;
import play.Logger;
import play.cache.Cache;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import controllers.Secure.Security;

@With(Secure.class)
public class SecureController extends Controller {

	@Before
	static void log() {
		if (Security.isConnected() && !request.isAjax()) {
			Logger.info(Security.connected() + " " + request.action);
		}
	}

	public static Person getUser() {
		Person person = Cache.get(Security.connected(), Person.class);
		if (person == null) {
			person = Person.findById(Security.connected());
			Cache.set(Security.connected(), person);
		}
		return person;
	}

}
