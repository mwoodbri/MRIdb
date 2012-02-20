package jobs;

import models.Person;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {
	@Override
	public void doJob() {
		if (Person.count() == 0) {
			Fixtures.loadModels("initial-data.yml");
		}
	}
}
