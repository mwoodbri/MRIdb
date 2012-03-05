package tags;

import groovy.lang.Closure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import models.Series;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

import play.templates.FastTags;
import play.templates.GroovyTemplate.ExecutableTemplate;
import util.Dicom;

public class DicomTags extends FastTags {

	public static void _PixelSpacing(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		String axis = (String) args.get("axis");
		float[] pixelSpacing = dataset.getFloats(Tags.PixelSpacing);
		if (pixelSpacing == null) {
			pixelSpacing = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).getItem(Tags.PixelMeasuresSeq).getFloats(Tags.PixelSpacing);
		}
		out.println(pixelSpacing["X".equals(axis) ? 0 : 1]);
	}

	public static void _SliceThickness(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws IOException {
		Dataset dataset = (Dataset) args.get("arg");
		if (!dataset.contains(Tags.SliceThickness)) {
			dataset = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq);
			if (dataset.get(Tags.valueOf("(2005,140F)")).hasItems()) {
				dataset = dataset.getItem(Tags.valueOf("(2005,140F)"));
			} else {
				byte[] buf = dataset.get(Tags.valueOf("(2005,140F)")).getDataFragment(0).array();
				dataset = DcmObjectFactory.getInstance().newDataset();
				dataset.readFile(new ByteArrayInputStream(buf), null, -1);
			}
		}
		out.println(dataset.getFloat(Tags.SliceThickness));
	}

	public static void _InPlanePhaseEncodingDirection(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		if (!dataset.contains(Tags.InPlanePhaseEncodingDirection)) {
			if (dataset.getItem(Tags.SharedFunctionalGroupsSeq).contains(Tags.MRFOVGeometrySeq)) {
				dataset = dataset.getItem(Tags.SharedFunctionalGroupsSeq).getItem(Tags.MRFOVGeometrySeq);
			} else {
				dataset = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).getItem(Tags.MRFOVGeometrySeq);
			}
		}
		out.println(dataset.getString(Tags.InPlanePhaseEncodingDirection));
	}

	public static void _MRAcquisitionPhaseEncodingStepsInPlane(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		Integer numberOfPhaseEncodingSteps = dataset.getInteger(Tags.NumberOfPhaseEncodingSteps);
		if (numberOfPhaseEncodingSteps == null) {
			if (dataset.getItem(Tags.SharedFunctionalGroupsSeq).contains(Tags.MRFOVGeometrySeq)) {
				dataset = dataset.getItem(Tags.SharedFunctionalGroupsSeq).getItem(Tags.MRFOVGeometrySeq);
			} else {
				dataset = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).getItem(Tags.MRFOVGeometrySeq);
			}
			numberOfPhaseEncodingSteps = dataset.getInteger(Tags.MRAcquisitionPhaseEncodingStepsInPlane);
		}
		out.println(numberOfPhaseEncodingSteps);
	}

	public static void _ReceiveCoilName(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		if (!dataset.contains(Tags.ReceiveCoilName)) {
			if (dataset.contains(Tags.SharedFunctionalGroupsSeq) && dataset.getItem(Tags.SharedFunctionalGroupsSeq).contains(Tags.MRReceiveCoilSeq)) {
				dataset = dataset.getItem(Tags.SharedFunctionalGroupsSeq).getItem(Tags.MRReceiveCoilSeq);
			} else if (dataset.contains(Tags.SharedFunctionalGroupsSeq)) {
				dataset = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).getItem(Tags.MRReceiveCoilSeq);
			}
		}
		out.println(dataset.getString(Tags.ReceiveCoilName));
	}

	public static void _RepetitionTime(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		if (!dataset.contains(Tags.RepetitionTime)) {
			dataset = dataset.getItem(Tags.SharedFunctionalGroupsSeq).getItem(Tags.MRTimingAndRelatedParametersSeq);
		}
		out.println(dataset.getFloat(Tags.RepetitionTime));
	}

	public static void _attr(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws IOException {
		Dataset dataset = (Dataset) args.get("arg");
		int tag = Tags.forName((String) args.get("tag"));
		if (!dataset.contains(tag) && dataset.contains(Tags.PerFrameFunctionalGroupsSeq)) {
			dataset = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq);
			if (dataset.get(Tags.valueOf("(2005,140F)")).hasItems()) {
				dataset = dataset.getItem(Tags.valueOf("(2005,140F)"));
			} else {
				byte[] buf = dataset.get(Tags.valueOf("(2005,140F)")).getDataFragment(0).array();
				dataset = DcmObjectFactory.getInstance().newDataset();
				dataset.readFile(new ByteArrayInputStream(buf), null, -1);
			}
		}
		out.println(Boolean.TRUE.equals(args.get("float")) ? dataset.getFloat(tag) : dataset.getString(tag));
	}

	public static void _NumberOfFrames(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) throws IOException {
		Series series = (Series) args.get("arg");
		out.println(String.valueOf(Dicom.numberOfFrames(series)));
	}

}
