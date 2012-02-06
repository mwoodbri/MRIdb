package controllers.crud;

import models.Person;
import play.mvc.With;
import controllers.CRUD;
import controllers.Check;
import controllers.Secure;

@With(Secure.class)
@Check("admin")
public class Persons extends CRUD {
}
