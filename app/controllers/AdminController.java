package controllers;

import models.Chart;
import models.LogFile;
import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import models.Patient;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import sun.rmi.runtime.Log;
import views.html.*;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import views.html.adminPages.*;
import views.html.consultantPages.*;
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
        Chart c = p.getChart();
        if(p.getAppointments().size() != 0){
            flash("error", "Cannot archive Patient while there are still appointments due");
            return redirect(routes.HomeController.searchPatient());
        }
        if(c.getDischargeDate() == null){
            flash("error", "Cannot archive Patient while they are staying in the hospital");
            return redirect(routes.HomeController.searchPatient());            
        }
        try {
            p.serialize();
            c.serialize();
            c.delete();
            p.delete();
        } catch(FileNotFoundException e) {
            flash("error", "Could not find file");
            return redirect(routes.HomeController.searchPatient());
        } catch(IOException e){
            flash("error", "Could not archive patient");
            return redirect(routes.HomeController.searchPatient());
        }
        flash("success", "Patient has been archived.");
        return redirect(routes.HomeController.searchPatient());
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
        p.setPrevIllnesses(df.get("illness"));
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
        return redirect(routes.HomeController.searchPatient());
    }

}
