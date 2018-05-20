package jobs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import controllers.Application;
import jobs.Downloader.Format;
import models.Series;
import models.Study;
import play.db.jpa.GenericModel;
import play.jobs.Job;
import play.libs.Files;
import play.mvc.Scope.Session;
import util.Clipboard;
import util.Download;
import util.Properties;

public class ClipboardExporter extends Job {

	private Clipboard clipboard;
	private File tmpDir;
	private Session session;
	private String username;
	private boolean preferMultiframe;
	private String niftiMultiframeScript;

	public ClipboardExporter(Clipboard clipboard, File tmpDir, Session session, String username, boolean preferMultiframe, String niftiMultiframeScript) {
		this.clipboard = clipboard;
		this.tmpDir = tmpDir;
		this.session = session;
		this.username = username;
		this.preferMultiframe = preferMultiframe;
		this.niftiMultiframeScript = niftiMultiframeScript;
		addExport();
	}

	@Override
	public void doJob() throws Exception {
		for (GenericModel object : clipboard.getObjects()) {
			if (object instanceof Study) {
				Study study = (Study) object;
				Download.study(study, tmpDir, Format.dcm, preferMultiframe, niftiMultiframeScript);
			} else if (object instanceof Series) {
				Series series = (Series) object;
				Download.series(series, tmpDir, Format.dcm, preferMultiframe, niftiMultiframeScript);
			}
		}
		File dest = getDest();
		getDest().getParentFile().mkdirs();
		Files.copyDir(tmpDir, dest);
	}

	private void addExport() {
		String exports = session.get(Application.EXPORTS);
		if (exports == null) {
			exports = "";
		}
		String name;
		search:
			for (int i = 1; ; i++) {
				name = String.format("%s-%s-%s", username, new SimpleDateFormat("yyyyMMdd").format(new Date()), i);
				if (!exports.isEmpty()) {
					for (String export : exports.split(",")) {
						if (name.equals(export.split(":")[1])) {
							continue search;
						}
					}
				}
				break;
			}
		String fullName = String.format("%s:%s", tmpDir.getName(), name);
		session.put(Application.EXPORTS, exports.isEmpty() ? fullName : StringUtils.join(ArrayUtils.add(exports.split(","), fullName), ','));
	}

	private File getDest() {
		String exports = session.get(Application.EXPORTS);
		for (String export : Arrays.asList(exports.split(","))) {
			if (tmpDir.getName().equals(export.split(":")[0])) {
				return new File(new File(Properties.getExports(), export.split(":")[1].split("-")[0]), export.split(":")[1]);
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
					archives.add(new File(new File(Properties.getExports(), export.split(":")[1].split("-")[0]), export.split(":")[1]));
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

}