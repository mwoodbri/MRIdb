package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Years;

import play.db.jpa.GenericModel;

@Entity
public class Patient extends GenericModel {

	@Id
	public long pk;
	public String pat_id;
	public String pat_name;
	public String pat_birthdate;
	public Character pat_sex;
	public byte[] pat_attrs;
	@OneToMany(mappedBy = "patient")
	public Set<Study> studies;
	
	public Date birthdate() throws ParseException {
		return new SimpleDateFormat("yyyyMMdd").parse(pat_birthdate);
	}
	
	public int age() throws ParseException {
		DateMidnight birthdate = new DateMidnight(birthdate());
		return Years.yearsBetween(birthdate, new DateTime()).getYears();
	}

	@Override
	public String toString() {
		return String.format("%s %s %s %s", pat_id, pat_name, pat_birthdate, pat_sex);
	}
}
