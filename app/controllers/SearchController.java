package controllers;

import play.mvc.*;

import services.Serializer;
import views.html.chiefAdminPages.viewUsers;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import views.html.chiefAdminPages.*;
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

import static java.util.stream.Collectors.toList;


public class SearchController extends Controller{

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
        if(mrn.equals("")){
            return ok(searchPatient.render(new ArrayList<Patient>(), getUserFromSession()));
        }
        List<Patient> searchedPatients = new ArrayList<>();
        Patient p = Serializer.readPatientArchive(mrn);
        if(p == null){
            return ok(searchPatient.render(new ArrayList<Patient>(), getUserFromSession()));
        }
        if(p.getPatientRecord() != null) {
            PatientRecord pr = Serializer.readPatientRecordArchive(p.getPatientRecord().getRecordId());
            pr.setP(p);
            List<Chart> charts = Serializer.readChartArchive(pr.getRecordId());
            charts.stream().filter(c -> c.getPatientRecord().getRecordId().equals(pr.getRecordId())).forEach(c -> { c.insert(); pr.addChart(c); c.setPatientRecord(pr); c.update();});
            List<Appointment> appointments = Serializer.readAppointmentArchive(pr.getRecordId());
            appointments.stream().filter(a -> a.getPatientRecord().getRecordId().equals(pr.getRecordId())).forEach(a -> { a.insert(); pr.addAppointment(a); a.setPatientRecord(pr); a.update();});

            pr.update();
        }
        List<Prescription> pres = Serializer.readPrescriptionArchive(mrn);
        if(p != null) {
            searchedPatients.add(p);
            if(pres != null){
                p.setPrescriptionList(pres);
                p.update();
                for(Prescription x : pres){
                    x.setPatientOnly(p);
                    x.update();
                }
            }
        }
        return ok(searchPatient.render(searchedPatients, getUserFromSession()));
    }

    public Result searchByLastName(){
        DynamicForm searchForm = formFactory.form().bindFromRequest();
        String lName = searchForm.get("lName");
        if(session("role").equals("ChiefAdmin")){
            List<User> searchedUsers = User.find.where().like("lName", lName + "%").findList();
            return ok(viewUsers.render(HomeController.getUserFromSession(), searchedUsers));

        }
        List<Patient> searchedPatients = Patient.find.where().like("lName", lName + "%").findList();
        return ok(searchPatient.render(searchedPatients, getUserFromSession()));
    }

    public Result searchByEmail(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String email = df.get("email");
        List<User> searchedUsers = User.find.where().like("email", email + "%").findList();
        return ok(viewUsers.render(HomeController.getUserFromSession(), searchedUsers));
    }

    public Result searchByMedicationName(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String name = df.get("name");
        List<Medicine> searchedMedicine = Medicine.find.where().like("name", name + "%").findList();
        return ok(viewMedication.render(HomeController.getUserFromSession(), searchedMedicine));
    }

}
