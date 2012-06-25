package models;

import javax.persistence.Entity;
import javax.persistence.Lob;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class Help extends Model {

	@Lob
	@MaxSize(10000)
	public String faq;

}
