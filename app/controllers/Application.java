package controllers;

import java.io.IOException;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.ConfigurationException;

import util.DicomQuery;

public class Application extends SecureController {

    public static void index(String name) throws IOException, ConfigurationException, InterruptedException {
    	params.flash();
    	List<DicomObject> patients = DicomQuery.query(name);
        render(patients);
    }

}