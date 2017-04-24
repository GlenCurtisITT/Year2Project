package controllers;

import models.*;
import play.mvc.*;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Char;
import services.InvalidPPSNumberException;
import services.Serializer;
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

import static controllers.HomeController.getPatientFromSession;
import static java.util.stream.Collectors.toList;

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
        Bill b = p.getB();
        if(!b.isPaid()) {
            flash("error", "Cannot archive Patient while bill is overdue");
            return redirect(routes.SearchController.searchPatient());
        }
        if(p.getAppointmentsDue().size() != 0){
            flash("error", "Cannot archive Patient while there are still appointments due");
            return redirect(routes.SearchController.searchPatient());
        }
        if(c.getDateOfAdmittance() != null){
            flash("error", "Cannot archive Patient while they are staying in the hospital");
            return redirect(routes.SearchController.searchPatient());
        }
        if(p.getSl() != null){
            flash("error", "Cannot archive Patient while they are on a standby list");
            return redirect(routes.SearchController.searchPatient());
        }
        try {
            Serializer.serialize(p);
            Serializer.serialize(p.getB());
            PatientRecord pr;
            if(p.getPatientRecord() != null){
                pr = p.getPatientRecord();
                Serializer.serialize(pr);
                for(Chart chart: pr.getCharts()) {
                    Serializer.serialize(chart);
                    chart.delete();
                }
                for(Appointment a: pr.getAppointments()) {
                    Serializer.serialize(a);
                    a.delete();
                }
                pr.delete();
            }
            if (p.getPrescriptionList().size() != 0) {
                for(Prescription prescription : p.getPrescriptionList()){
                    Serializer.serialize(prescription);
                    prescription.delete();
                }
            }
            p.getCurrentChart().delete();
            p.delete();
            b.delete();
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

    public Result removeConsultant(String id){
        Patient p = Patient.find.byId(id);
        p.getC().releasePatient(p);
        flash("success", "Consultant removed.");
        LogFile.writeToLog("Consultant removed from patient " + p.getfName() + " " + p.getlName());
        return ok(viewPatient.render(HomeController.getUserFromSession(), p));
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
        String[] strings = {df.get("fname"), df.get("lname"), df.get("nokFname"), df.get("nokLname"), df.get("illness"), df.get("nokAdd"), df.get("address"), df.get("email")};
        String[] nums = {df.get("homePhone"), df.get("mobileNum"), df.get("nokPhone")};
        for(int i = 0; i < strings.length; i++) {
            if (strings[i].equals("")) {
                flash("error", "Email, Address, Illness, First Name and Last Name cannot be blank.");
                return badRequest(updatePatient.render(u, p));
            }
        }
        for(int i = 0; i < strings.length - 3; i++){
            if(strings[i].matches(".*\\d+.*")){ //checks to see if contains number
                flash("error", "Illness, First Name and Last Name cannot contain numbers.");
                return badRequest(updatePatient.render(u, p));
            }
        }
        for(int i = 0; i < nums.length; i++){
            if(nums[i].equals("")){
                flash("error", "Must enter Home Phone, Mobile Phone, and Contact number");
                return badRequest(updatePatient.render(u, p));
            }
            if(nums[i].length() < 5){
                flash("error", "Phone number entered is too short");
                return badRequest(updatePatient.render(u, p));
            }
            try {
                Integer.parseInt(nums[i]);
            }catch (NumberFormatException e){
                flash("error", "Phone numbers cannot contain letters.");
                return badRequest(updatePatient.render(u, p));
            }
        }
        if(!df.get("email").contains("@")){
            flash("error", "Invalid email address entered");
            return badRequest(updatePatient.render(u, p));
        }
        p.setfName(df.get("fname"));
        p.setlName(df.get("lname"));
        if(df.get("gender").equals("true")){
            p.setGender(true);
        }else{
            p.setGender(false);
        }

        String tempPPS = p.getPpsNumber();
        p.setPpsNumber("");
        try{
            HomeController.ppsChecker(df.get("ppsNumber"));
        } catch (InvalidPPSNumberException e) {
            p.setPpsNumber(tempPPS);
            flash("error", e.getMessage());
            return badRequest(updatePatient.render(u, p));
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
        if(date.after(new Date())){
            flash("error", "Invalid Date of Birth entered");
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
        Patient p = getPatientFromSession();
        User u = HomeController.getUserFromSession();

        return ok(listConsultants.render(consultants, u, p));
    }

    public Result addConsultant(String idNum){
        Consultant c = Consultant.find.byId(idNum);
        Patient p = getPatientFromSession();
        User u = HomeController.getUserFromSession();
        p.assignConsultant(c);
        String logFileString = "Dr. " + c.getLname() + "(" + c.getIdNum() + ") assigned to patient(" + p.getMrn() + ")";
        LogFile.writeToLog(logFileString);
        flash("success", "Patient assigned to Dr. " + c.getLname());
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }

    public Result genBill(){
        Patient p = getPatientFromSession();
        User u = HomeController.getUserFromSession();
        Bill b = p.getB();
        b.calcBill();
        b.update();

        String logFileString = "Bill generated for patient(" + p.getMrn() + ") by User '" + u.getFname() + " " + u.getLname() + "'(" + u.getIdNum() + ")";
        LogFile.writeToLog(logFileString);
        flash("success", "Bill generated ");
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdmin.class)
    public Result payBill(){
        Patient p = getPatientFromSession();
        if(p.getAllAppointments().size() != 0 || p.getAllBillingCharts().size() != 0){
            if(p.getPatientRecord() == null) {
                PatientRecord pr = PatientRecord.record(p);
            } else{
                p.getPatientRecord().addToRecord();
            }
        }
        p.getB().payBill();
        p.getPrescriptionList().stream().filter(pres -> !pres.isPaid()).forEach(pres -> pres.setPaid(true));
        String s = "Bill has been paid for " + p.getfName() + " " + p.getlName() + "(" + p.getMrn() + ")";
        LogFile.writeToLog(s);
        flash("success", s);
        return redirect(routes.HomeController.viewPatientByID(p.getMrn()));
    }

}
