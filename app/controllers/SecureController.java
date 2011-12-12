package controllers;

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
		if (Security.isConnected()) {
			Logger.info(Security.connected() + " " + request.action);
		}
	}

//	public static User getUser() {
//		User user = Cache.get(Security.connected(), User.class);
//		if (user == null) {
//			user = User.find("byIdentifier", Security.connected()).first();
//			Cache.set(Security.connected(), user);
//		}
//		return user;
//	}

}
