package controllers;

import models.*;
import play.mvc.*;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.mainTemplate.*;
import views.html.adminPages.*;
import play.data.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import javax.inject.Inject;
import models.users.*;

@Security.Authenticated(Secured.class)
@With(AuthAdmin.class)
public class AdminController extends Controller{

    private FormFactory formFactory;

    @Inject
    public AdminController(FormFactory f){
        this.formFactory = f;
    }

    public Result adminHomePage(){
        User u = HomeController.getUserFromSession();
        HomeController.endPatientSession();
        List<String> logEntries = LogFile.readTodaysLogEntries();
        return ok(adminHomePage.render(u, logEntries));
    }

    public Result deletePatient(String mrn){
        Patient p = Patient.getPatientById(mrn); //serialize before delete
        Chart c = p.getCurrentChart();
        if(p.getAppointments().size() != 0){
            flash("error", "Cannot archive Patient while there are still appointments due");
            return redirect(routes.SearchController.searchPatient());
        }
        if(c.getDischargeDate() == null){
            flash("error", "Cannot archive Patient while they are staying in the hospital");
            return redirect(routes.SearchController.searchPatient());
        }
        try {
            p.serialize();
            for(Chart chart: p.getCharts()) {
                chart.serialize();
                chart.delete();
            }
            if (p.getPrescriptionList().size() != 0) {
                for(Prescription prescription : p.getPrescriptionList()){
                    prescription.serialize();
                    prescription.delete();
                }
            }
            p.delete();
        } catch(FileNotFoundException e) {
            flash("error", "Could not find file");
            return redirect(routes.SearchController.searchPatient());
        } catch(IOException e){
            flash("error", "Could not archive patient");
            return redirect(routes.SearchController.searchPatient());
        }
        flash("success", "Patient has been archived.");
        return redirect(routes.SearchController.searchPatient());
    }

    public Result viewFullLog(){
        User u = HomeController.getUserFromSession();
        List<String> logEntries = LogFile.readAllLogEntries();
        return ok(viewFullLog.render(u, logEntries));
    }

    public Result deleteLogFile(){
        LogFile.deleteLogFile();
        User u = HomeController.getUserFromSession();
        List<String> logEntries = LogFile.readAllLogEntries();
        return ok(viewFullLog.render(u, logEntries));
    }

    @Transactional
    public Result updatePatient(String mrn){
        Patient p;
        Form<Patient> patientForm;
        try{
            p = Patient.find.byId(mrn);
            patientForm = formFactory.form(Patient.class).fill(p);
        }catch(Exception e){
            return badRequest();
        }

        return ok(addPatient.render(patientForm,null, HomeController.getUserFromSession()));
    }

    public Result listConsultants(){
        List<Consultant> consultants = Consultant.findAllConsultants();
        Patient p = HomeController.getPatientFromSession();
        User u = HomeController.getUserFromSession();

        return ok(listConsultants.render(consultants, u, p));
    }

    public Result addConsultant(String idNum){
        Consultant c = Consultant.find.byId(idNum);
        Patient p = HomeController.getPatientFromSession();
        User u = HomeController.getUserFromSession();
        p.assignConsultant(c);
        String logFileString = "Dr. " + c.getLname() + "(" + c.getIdNum() + ") assigned to patient(" + p.getMrn() + ")";
        LogFile.writeToLog(logFileString);
        flash("success", "Patient assigned to Dr. " + c.getLname());
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }

    public Result genBill(){
        Patient p = HomeController.getPatientFromSession();
        User u = HomeController.getUserFromSession();
        Chart c = p.getCurrentChart();
        Bill b;
        if(c.getB() == null) {
            b = new Bill(p.getCurrentChart());
            c.setB(b);
            c.save();
            b.calcBill();
            b.save();
        }else{
            b = c.getB();
            b.calcBill();
            b.update();
        }

        String logFileString = "Bill generated for patient(" + p.getMrn() + ") by User '" + u.getFname() + " " + u.getLname() + "'(" + u.getIdNum() + ")";
        LogFile.writeToLog(logFileString);
        flash("success", "Bill generated ");
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }

}
