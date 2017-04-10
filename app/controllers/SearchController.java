package controllers;

import play.mvc.*;

import views.html.loginPage.*;
import views.html.mainTemplate.*;
import play.data.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.inject.Inject;
import models.users.*;
import models.*;

import static controllers.HomeController.endPatientSession;
import static controllers.HomeController.getUserFromSession;
import static play.mvc.Results.ok;


/**
 * Created by conno on 10/04/2017.
 */
public class SearchController {

    private FormFactory formFactory;

    @Inject
    public SearchController(FormFactory f){
        this.formFactory = f;
    }


    public Result searchPatient(){
        endPatientSession();
        List<Patient> patientList = Patient.findAll();
        return ok(searchPatient.render(patientList, getUserFromSession()));
    }

    public Result searchByMRN(){
        DynamicForm searchForm = formFactory.form().bindFromRequest();
        String MRN = searchForm.get("mrn");
        List<Patient> searchedPatients = Patient.find.where().like("mrn", MRN).findList();
        return ok(searchPatient.render(searchedPatients, getUserFromSession()));
    }

    public Result searchArchiveByMRN(){
        DynamicForm searchForm = formFactory.form().bindFromRequest();
        String mrn = searchForm.get("archiveMrn");
        List<Patient> searchedPatients = new ArrayList<>();
        Patient p = Patient.readArchive(mrn);
        List<Chart> c = Chart.readArchive(mrn);
        List<Prescription> pres = Prescription.readArchive(mrn);
        if(p != null) {
            searchedPatients.add(p);
            if(c != null) {
                p.setChartList(c);
                for(Chart chart: p.getCharts()) {
                    chart.setP(p);
                }
            }
            if(pres != null){
                p.setPrescriptionList(pres);
                for(Prescription x : pres){
                    x.setPatient(p);
                }
            }
        }
        return ok(searchPatient.render(searchedPatients, getUserFromSession()));
    }

    public Result searchByLastName(){
        DynamicForm searchForm = formFactory.form().bindFromRequest();
        String lName = searchForm.get("lName");
        List<Patient> searchedPatients = Patient.find.where().like("lName", lName + "%").findList();
        return ok(searchPatient.render(searchedPatients, getUserFromSession()));
    }

}
