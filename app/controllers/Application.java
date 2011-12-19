package controllers;

import java.io.IOException;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.ConfigurationException;

import util.DicomQuery;

public class Application extends SecureController {

    public static void index() throws Exception {
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
}