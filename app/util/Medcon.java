package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jobs.SeriesDownloader.Format;

public class Medcon {

	private static final Map<Format, String> formats = new HashMap<Format, String>() {{
		put(Format.dcm, "dicom"); //DICOM
		put(Format.nii, "nifti"); //NIfTI
		put(Format.img, "anlz");  //Analyze
	}};
	public static void convert(File from, Format format, File to) throws InterruptedException, IOException {
		File[] files = from.isDirectory() ? from.listFiles() : new File[] { from };
		File directory = to.isDirectory() ? to : to.getParentFile();
		List<String> prefix = new ArrayList(Arrays.asList(
				new File(Properties.getString("xmedcon"), "bin/medcon").getPath(),
				"-noprefix",
				"-c", formats.get(format)
				));
		if (from.isDirectory() && !to.isDirectory()) {
			prefix.addAll(Arrays.asList(
					"-stack3d",
					"-o", to.getName()
					));
		}
		if (format == Format.nii || format == Format.img) {
			prefix.add("-fv");
		}
		prefix.add("-f");
		String[] command = Arrays.copyOf(prefix.toArray(new String[0]), prefix.size() + files.length);
		for (int i = 0; i < files.length; i++) {
			command[prefix.size() + i] = files[i].getPath();
		}
		Util.exec(directory,
				new HashMap<String, String>() {{
					put("LD_LIBRARY_PATH", new File(Properties.getString("xmedcon"), "lib").getPath());
				}},
				command
				);
	}

}
