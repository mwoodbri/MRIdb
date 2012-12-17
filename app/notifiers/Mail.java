package notifiers;

import play.mvc.Http.Request;
import play.mvc.Mailer;
import play.mvc.Scope.Session;
import util.Properties;

public class Mail extends Mailer {

	public static void exception(Request request, Session session, Throwable exception) {
		setSubject("Exception");
		addRecipient(Properties.getString("mail.to"));
		setFrom(String.format("%s <%s>", Properties.getString("application.name"), Properties.getString("mail.from")));
		send(request, session, exception);
	}

}
