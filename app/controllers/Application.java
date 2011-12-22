package controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.dcm4che2.data.DicomObject;

import play.libs.IO;
import util.DicomQuery;
import util.Properties;

public class Application extends SecureController {

    public static void index() throws Exception {
        render();
    }

    public static void help() throws Exception {
        render();
    }
    
    public static void patients(String name) throws Exception {
    	List<DicomObject> patients = DicomQuery.patient(name);
        render(patients);
    }

    public static void patient(String id) throws Exception {
    	List<DicomObject> studies = DicomQuery.studies(id);
    	render(studies);
    }
    
    public static void study(String id) throws Exception {
    	List<DicomObject> seriess = DicomQuery.seriess(id);
    	render(seriess);
    }
    
    public static void series(String id) throws Exception {
    	List<DicomObject> images = DicomQuery.images(id);
    	render(images);
    }
    
    public static void image(String objectUID, Integer columns) throws MalformedURLException, IOException {
    	notFoundIfNull(objectUID);
    	String url = String.format("http://%s:8080/wado?requestType=WADO&studyUID=&seriesUID=&objectUID=%s", Properties.getString("dicom.host"), objectUID);
    	if (columns != null) {
    		url += String.format("&columns=%s", columns);
    	}
    	IO.copy(new URL(url).openConnection().getInputStream(), response.out);
    }
}