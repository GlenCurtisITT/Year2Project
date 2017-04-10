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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        Bill b = new Bill();
        if(c.getB() != null){
            b = c.getB();
            if(!b.isPaid()) {
                flash("error", "Cannot archive Patient while bill is overdue");
                return redirect(routes.SearchController.searchPatient());
            }
        }
        if(p.getAppointments().size() != 0){
            flash("error", "Cannot archive Patient while there are still appointments due");
            return redirect(routes.SearchController.searchPatient());
        }
        if(c.getDateOfAdmittance() != null){
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

    public Result updatePatient(String mrn){
        User u = HomeController.getUserFromSession();
        Patient p = Patient.find.byId(mrn);
        return ok(updatePatient.render(u, p));
    }

    public Result updatePatientSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        Patient p = Patient.find.byId(df.get("mrn"));
        User u = HomeController.getUserFromSession();
        if(df.get("email").equals("") || df.get("fname").equals("") || df.get("lname").equals("")){
            flash("error", "Email, First Name and Last Name cannot be blank.");
            return badRequest(updatePatient.render(u, p));
        }
        p.setfName(df.get("fname"));
        p.setlName(df.get("lname"));
        if(df.get("gender").equals("true")){
            p.setGender(true);
        }else{
            p.setGender(false);
        }

        //Find all patients in system. Check PPS number against all but self.
        List<Patient> allPatients = Patient.findAll();
        allPatients.remove(p);
        for(Patient patient : allPatients){
            if(patient.getPpsNumber().equals(df.get("ppsNumber"))){
                //Return bad request if PPS number is the same as another patient.
                flash("error", "Another patient with same PPS.");
                return badRequest(updatePatient.render(u, p));
            }
        }

        p.setPpsNumber(df.get("ppsNumber"));
        p.setEmail(df.get("email"));
        p.setAddress(df.get("address"));
        p.setHomePhone(df.get("homePhone"));
        p.setMobilePhone(df.get("mobileNum"));
        p.setNokFName(df.get("nokFname"));
        p.setNokLName(df.get("nokLname"));
        p.setNokAddress(df.get("nokAdd"));
        p.setNokNumber(df.get("nokPhone"));
        String dateString = df.get("date");
        if(df.get("medicalCard").equals("true")){
            p.setMedicalCard(true);
        }else{
            p.setMedicalCard(false);
        }
        p.setIllness(df.get("illness"));
        DateFormat format = new SimpleDateFormat("yyyy-dd-MM");
        Date date = new Date();
        try{
            date = format.parse(dateString);
        } catch (ParseException e) {
            flash("error", "Error with date. Must be yyyy-dd-MM");
            return ok(updatePatient.render(u, p));
        }
        p.setDob(date);

        p.update();
        String logFileString = u.checkRole() + " " + u.getFname() + " " + u.getLname() + " updated patient "
                + p.getfName() + " " + p.getlName() + "'s information.";
        LogFile.writeToLog(logFileString);
        flash("success", "Patient Updated");
        return redirect(routes.SearchController.searchPatient());
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
