package tags;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.util.Map;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import play.templates.GroovyTemplate.ExecutableTemplate;

public class FastTags extends play.templates.FastTags {

	public static void _PixelSpacing(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		String axis = (String) args.get("axis");
		float[] pixelSpacing = dataset.getFloats(Tags.PixelSpacing);
		if (pixelSpacing == null) {
			pixelSpacing = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).getItem(Tags.PixelMeasuresSeq).getFloats(Tags.PixelSpacing);
		}
		out.println(pixelSpacing["X".equals(axis) ? 0 : 1]);
	}

	public static void _SliceThickness(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		if (!dataset.contains(Tags.SliceThickness)) {
			dataset = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).getItem(Tags.valueOf("(2005,140F)"));
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
		}
		out.println(dataset.getInteger(Tags.MRAcquisitionPhaseEncodingStepsInPlane));
	}

	public static void _ReceiveCoilName(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		if (!dataset.contains(Tags.ReceiveCoilName)) {
			if (dataset.getItem(Tags.SharedFunctionalGroupsSeq).contains(Tags.MRReceiveCoilSeq)) {
				dataset = dataset.getItem(Tags.SharedFunctionalGroupsSeq).getItem(Tags.MRReceiveCoilSeq);
			} else {
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

	public static void _attr(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Dataset dataset = (Dataset) args.get("arg");
		int tag = Tags.forName((String) args.get("tag"));
		if (!dataset.contains(tag)) {
			dataset = dataset.getItem(Tags.PerFrameFunctionalGroupsSeq).getItem(Tags.valueOf("(2005,140F)"));
		}
		out.println(dataset.getString(tag));
	}

}
