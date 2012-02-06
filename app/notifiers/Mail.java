package notifiers;

import play.mvc.Http.Request;
import play.mvc.Mailer;
import play.mvc.Scope.Session;
import util.Properties;

public class Mail extends Mailer {

	public static void exception(Request request, Session session, Throwable exception) {
		setSubject("Exception");
		addRecipient(Properties.getString("author.email"));
		setFrom(String.format("MRIdb <%s>", Properties.getString("author.email")));
		send(request, session, exception);
	}
}
