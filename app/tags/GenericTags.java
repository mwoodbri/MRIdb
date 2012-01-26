package tags;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.util.Map;

import models.Files;
import models.Instance;
import models.Series;
import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;

public class GenericTags extends FastTags {

	public static void _stats(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Series series = (Series) args.get("arg");
		int files = 0;
		Long bytes = 0L;
		for (Instance instance : series.instances) {
			for (Files file : instance.files) {
				files++;
				bytes += file.file_size;
			}
		}
		out.printf(JavaExtensions.toString(body), files, JavaExtensions.formatSize(bytes));
	}

}
