package jobs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jobs.Downloader.Format;
import models.Series;
import models.Study;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import play.Play;
import play.db.jpa.GenericModel;
import play.jobs.Job;
import play.mvc.Scope.Session;
import util.Clipboard;
import util.Properties;
import controllers.Application;

public class Exporter extends Job {

	private Clipboard clipboard;
	private File tmpDir;
	private String password;
	private Session session;
	private String user;

	public Exporter(Clipboard clipboard, File tmpDir, String password, Session session, String user) {
		this.clipboard = clipboard;
		this.tmpDir = tmpDir;
		this.password = password;
		this.session = session;
		this.user = user;
		addExport();
	}

	@Override
	public void doJob() throws Exception {
		for (GenericModel model : clipboard.getObjects()) {
			if (model instanceof Series) {
				Downloader.export((Series) model, Format.dcm, tmpDir, null);
			} else {
				Study study = (Study) model;
				File dir = new File(tmpDir, study.toClipboardString());
				dir.mkdir();
				for (Series series : ((Study) model).series) {
					Downloader.export(series, Format.dcm, dir, null);
				}
			}
		}
		File zipFile = new File(String.format("%s.7z", tmpDir.getPath()));
		new ProcessBuilder("7za", "a", "-mhe=on", String.format("-p%s", password), zipFile.getPath(), tmpDir.getPath()).start().waitFor();
		zipFile.renameTo(getDest());
	}

	private void addExport() {
		String exports = session.get(Application.EXPORTS);
		if (exports == null) {
			exports = "";
		}
		String name;
		for (int i = 1; ; i++) {
			name = String.format("%s-%s-%s", user, new SimpleDateFormat("yyyyMMdd").format(new Date()), i);
			if (!new File(tmpDir.getParentFile(), String.format("%s.7z", name)).exists()) {
				break;
			}
		}
		String fullName = String.format("%s:%s", tmpDir.getName(), name);
		session.put(Application.EXPORTS, exports.isEmpty() ? fullName : StringUtils.join(ArrayUtils.add(exports.split(","), fullName), ','));
	}

	private File getDest() {
		String exports = session.get(Application.EXPORTS);
		for (String export : Arrays.asList(exports.split(","))) {
			if (tmpDir.getName().equals(export.split(":")[0])) {
				return new File(tmpDir.getParentFile(), String.format("%s.7z", export.split(":")[1]));
			}
		}
		return null;
	}

	public static List<File> getExports(Session session) {
		String exports = session.get(Application.EXPORTS);
		List<File> archives = new ArrayList<File>();
		if (exports != null) {
			List<String> exportList = new ArrayList(Arrays.asList(exports.split(",")));
			for (Iterator<String> i = exportList.iterator(); i.hasNext();) {
				String export = i.next();
				File tmpDir = new File(Properties.getDownloads(), export.split(":")[0]);
				if (tmpDir.exists()) {
					archives.add(new File(tmpDir.getParentFile(), String.format("%s.7z", export.split(":")[1])));
				} else {
					i.remove();
				}
			}
			if (exportList.isEmpty()) {
				session.remove(Application.EXPORTS);
			} else {
				session.put(Application.EXPORTS, StringUtils.join(exportList, ","));
			}
		}
		return archives;
	}

	public static File getExport(String name, Session session) {
		String exports = session.get(Application.EXPORTS);
		if (exports != null) {
			for (String export : Arrays.asList(exports.split(","))) {
				if (name.equals(export.split(":")[1])) {
					return new File(Properties.getDownloads(), String.format("%s.7z", name));
				}
			}
		}
		return null;
	}

}