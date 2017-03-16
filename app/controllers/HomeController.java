package controllers;

import controllers.*;
import play.mvc.*;

import sun.rmi.runtime.Log;
import views.html.*;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import play.data.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.avaje.ebean.*;
import javax.inject.Inject;
import models.users.*;
import models.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private FormFactory formFactory;

    @Inject
    public HomeController(FormFactory f){
        this.formFactory = f;
    }

    public Result index() {
        Form<Login> loginForm = formFactory.form(Login.class);
        if(Equipment.findAll().size() == 0) {
            Equipment a = new Equipment("1", "Consultation Room", true);
            Equipment b = new Equipment("2", "X-Ray", true);
            Equipment c = new Equipment("3", "CT Scanner", true);

            a.save();
            b.save();
            c.save();
        }
        return ok(index.render(loginForm));
    }

    public Result homepage(){
        endPatientSession();
        User u = getUserFromSession();
        return ok(homepage.render(u));
    }

    public Result addPatient(){
        endPatientSession();
        Form<Patient> addPatientForm = formFactory.form(Patient.class);
        User u = getUserFromSession();
        return ok(addPatient.render(addPatientForm, null, u));
    }

    public Result createUser(){
        Form<User> adduserForm = formFactory.form(User.class);
        return ok(createUser.render(adduserForm, null));
    }

    public Result searchPatient(){
        endPatientSession();
        List<Patient> patientList = Patient.findAll();
        return ok(searchPatient.render(patientList, getUserFromSession()));
    }

    public Result viewPatient(){
        Patient p = getPatientFromSession();
        return ok(viewPatient.render(getUserFromSession(), p));
    }

    public Result viewPatientByID(java.lang.String mrn){
        endPatientSession();
        Patient p = Patient.find.byId(mrn);
        if(!session().containsKey("mrn")) {
            session("mrn", mrn);
        }
        return ok(viewPatient.render(getUserFromSession(), getPatientFromSession()));
    }

    public Result appointmentMain(String id){
        Appointment a = Appointment.find.byId(id);
        User u = getUserFromSession();
        return ok(appointmentMain.render(u, a));
    }

    public Result cancelAppointment(String id){
        Appointment a = Appointment.find.byId(id);
        if(getUserFromSession() instanceof Consultant){
            Consultant c = (Consultant) getUserFromSession();
            a.delete();
            String s = "Appointment Cancelled ";
            flash("success", s);
            c.popAppointments();
            List<Appointment> appointments = c.getAppointments();
            return ok(viewAppointments.render(c, appointments));
        }
        else{
            String s = "Only Consultants may cancel appointments";
            flash("error", s);
            return ok(appointmentMain.render(getUserFromSession(), a));
        }

    }

    public Result rescheduleAppointment(String id){
        DynamicForm newAppointmentForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        Appointment a = Appointment.find.byId(id);
        Consultant c = a.getC();
        if(!session().containsKey("mrn")) {
            session("mrn", a.getP().getMrn());
        }
        Patient p = getPatientFromSession();
        //handles processing of date
        String dateString = newAppointmentForm.get("appDate") + "T" + newAppointmentForm.get("hours") + ":" + newAppointmentForm.get("minutes") + ":00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        Date todayDate = new Date();
        try{
            date = sdf.parse(dateString);
            dateString = output.format(date);

        } catch (ParseException e) {
            flash("error", "Could not create appointment for: " + dateString);
            return badRequest(appointmentMain.render(getUserFromSession(), a));
        }

        if(date.before(todayDate)){     //check to see if appointment was made for the past
            flash("error", "Cannot create an appointment in the past: " + dateString);
            return badRequest(appointmentMain.render(getUserFromSession(), a));
        }
        //Checking if Consultant already has an appointment at that time
        if(c.checkAppointments().size() != 0){ //if consultant has appointments
            List<Date> appointments = c.checkAppointments();
            for (Date d : appointments) {
                if (d.compareTo(date) == 0) {
                    flash("error", "Consultant already has an appointment at that time.");
                    return badRequest(appointmentMain.render(getUserFromSession(), a));
                }
            }
        }
        a.setAppDate(date);
        a.save();
        c.popAppointments();
        p.popAppointments();
        String s = "Appointment rescheduled for " + p.getfName() + " " + p.getlName();
        flash("success", s);
        endPatientSession();
        return ok(appointmentMain.render(getUserFromSession(), a));
    }

    public Result makeAppointment(){
        Form<Appointment> addAppointmentForm = formFactory.form(Appointment.class);
        List<Consultant> consultants = Consultant.findAllConsultants();
        List<Equipment> equipments = Equipment.findAll();
        return ok(makeAppointment.render(addAppointmentForm, consultants, getUserFromSession(), getPatientFromSession(), equipments, null));
    }

    public Result addAppointmentSubmit(){
        DynamicForm newAppointmentForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        List<Equipment> equipments = Equipment.findAll();
        List<Consultant> consultants = Consultant.findAllConsultants();
        Equipment e = Equipment.find.byId(newAppointmentForm.get("equipment"));
        Patient p = getPatientFromSession();
        Consultant c = Consultant.getConsultantById(newAppointmentForm.get("consultant"));
        //Checking if Form has errors.
        if(newAppointmentForm.hasErrors()){
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Error in form."));
        }
        //Checking that Consultant and Date are not blank.
        if(newAppointmentForm.get("consultant") == null || newAppointmentForm.get("appDate") == null){
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Please enter a date and a consultant."));
        }
        //handles processing of date
        String dateString = newAppointmentForm.get("appDate") + "T" + newAppointmentForm.get("hours") + ":" + newAppointmentForm.get("minutes") + ":00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        Date todayDate = new Date();
        try{
            date = sdf.parse(dateString);
            dateString = output.format(date);

        } catch (ParseException ex) {
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Could not create appointment for: " + dateString));
        }

        if(date.before(todayDate)){     //check to see if appointment was made for the past
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Cannot create an appointment in the past: " + dateString));
        }

        //Checking if Consultant already has an appointment at that time
        if(c.checkAppointments().size() != 0){ //if consultant has appointments
            List<Date> appointments = c.checkAppointments();
            for (Date a : appointments) {
                if (a.compareTo(date) == 0) {
                    return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Consultant already has an appointment at that time."));
                }
            }
        }
        for(Appointment a: e.getAppointments()){
            if(a.getAppDate().compareTo(date) == 0){
                return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Equipment is already booked for that time."));
            }
        }
        //Adding Appointment to database
        Appointment a = new Appointment(date, c, p);
        a.setEquipment(e);
        Appointment.create(a);
        c.popAppointments();
        p.popAppointments();
        //Flashing String s to memory to be used in view patient screen.
        String s = "Appointment booked for " + getPatientFromSession().getfName() + " " + getPatientFromSession().getlName() + " with Dr." + c.getLname() + " at " + dateString;
        flash("success", s);
        return redirect(controllers.routes.HomeController.viewPatient());
    }

    public Result addUserSubmit(){
        DynamicForm newUserForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        //Checking if Form has errors.
        if(newUserForm.hasErrors()){
            return badRequest(createUser.render(errorForm, "Error in form."));
        }
        //Checking that Email and Name are not blank.
        if(newUserForm.get("email").equals("") || newUserForm.get("fname").equals("") || newUserForm.get("lname").equals("")){
            return badRequest(createUser.render(errorForm, "Please enter an email and name."));
        }
        if(newUserForm.get("role").equals("select")){
            return badRequest(createUser.render(errorForm, "Please enter a role."));
        }
        //Checking if password == confirmed password
        if(!newUserForm.get("password").equals(newUserForm.get("passwordConfirm"))){
            return badRequest(createUser.render(errorForm, "Passwords do not match."));
        }
        //Checking if password is longer than 6 characters
        if(newUserForm.get("password").length() < 6){
            return badRequest(createUser.render(errorForm, "Password must be at least six characters."));
        }
        //Checking if email exists already in database (Duplicate primary key)
        List<User> allusers = User.findAll();
        for(User a : allusers) {
            if (a.getEmail().equals(newUserForm.get("email"))) {
                return badRequest(createUser.render(errorForm, "Email already exists in system."));
            }
        }

        String dateString = newUserForm.get("dateOfBirth");
        DateFormat format = new SimpleDateFormat("yyyy-dd-MM");
        Date date = new Date();
        try{
            date = format.parse(dateString);
        } catch (ParseException e) {
            return badRequest(createUser.render(errorForm, dateString));
        }
        //Adding user to database
        if(newUserForm.get("role").equals("Admin")){
            User u = new User(newUserForm.get("fname"), newUserForm.get("lname"), newUserForm.get("phoneNumber"), newUserForm.get("address")
                    , newUserForm.get("ppsNumber"), date, newUserForm.get("email"), newUserForm.get("password"));
            User.create(u);
        }else if (newUserForm.get("role").equals("Consultant")){
            Consultant c = new Consultant(newUserForm.get("fname"), newUserForm.get("lname"), newUserForm.get("phoneNumber"), newUserForm.get("address")
                    , newUserForm.get("ppsNumber"), date, newUserForm.get("email"), newUserForm.get("password"));
            Consultant.create(c);
        } else {
            return badRequest(createUser.render(errorForm, "Invalid role chosen."));
        }
        String s = newUserForm.get("role") + ": " + newUserForm.get("fname") + " " + newUserForm.get("lname") + " added successfully.";
        //Flashing String s to memory to be used in index screen.
        flash("success", s);
        return redirect(controllers.routes.HomeController.index());
    }

    public Result addPatientSubmit(){
        DynamicForm newPatientForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        //Checking if Form has errors.
        if(newPatientForm.hasErrors()){
            return badRequest(addPatient.render(errorForm, "Error in form", getUserFromSession()));
        }
        //Checking that Email and Name are not blank.
        if(newPatientForm.get("email").equals("") || newPatientForm.get("fname").equals("") || newPatientForm.get("lname").equals("")){
            return badRequest(addPatient.render(errorForm, "Error name and email must be valid", getUserFromSession()));
        }
        if(newPatientForm.get("medicalCard").equals("select")){
            return badRequest(addPatient.render(errorForm, "Please select medical card status", getUserFromSession()));
        }

        //Checking if ppsNumber exists already in database (additional functionality)
        List<Patient> allpatients = Patient.findAll();
        for(Patient a : allpatients) {
            if (a.getPpsNumber().equals(newPatientForm.get("ppsNumber"))) {
                return badRequest(addPatient.render(errorForm, "A patient with that PPS number already exits on the system", getUserFromSession()));
            }
        }
        //formatting date to work
        String dateString = newPatientForm.get("dob");
        DateFormat format = new SimpleDateFormat("yyyy-dd-MM");
        Date date = new Date();
        try{
            date = format.parse(dateString);
        } catch (ParseException e) {
            return badRequest(addPatient.render(errorForm, "Error with date", getUserFromSession()));
        }

        //converting medicalCard from form from string to boolean
        String medicalCard = newPatientForm.get("medicalCard");
        boolean medCard;
        if(medicalCard.equals("true")){
            medCard = true;
        } else {
            medCard = false;
        }

        //Adding user to database
        Patient p = Patient.create(newPatientForm.get("fname"), newPatientForm.get("lname"), newPatientForm.get("ppsNumber"), date,
                newPatientForm.get("address"),newPatientForm.get("email"), newPatientForm.get("homePhone"),
                newPatientForm.get("mobilePhone"), newPatientForm.get("nokFName"), newPatientForm.get("nokLName")
                , newPatientForm.get("nokAddress"), newPatientForm.get("nokNumber"), medCard, newPatientForm.get("prevIllness"));
        String s = "Patient: " + newPatientForm.get("fname") + " " + newPatientForm.get("lname") + "was added successfully.\nMRN: " + p.getMrn();
        //Flashing String s to memory to be used in index screen.
        flash("success", s);
        User u = getUserFromSession();
        if(u instanceof Consultant){
            return redirect(routes.ConsultantController.consultantHomePage());
        }else if(u == null){
            return badRequest();
        }else{
            return redirect(routes.AdminController.adminHomePage());
        }

    }

    public Result searchByMRN(){
        DynamicForm searchForm = formFactory.form().bindFromRequest();
        String MRN = searchForm.get("mrn");
        List<Patient> searchedPatients = Patient.find.where().like("mrn", MRN + "%").findList();
        return ok(searchPatient.render(searchedPatients, getUserFromSession()));
    }

    public Result searchByLastName(){
        DynamicForm searchForm = formFactory.form().bindFromRequest();
        String lName = searchForm.get("lName");
        List<Patient> searchedPatients = Patient.find.where().like("lName", lName + "%").findList();
        return ok(searchPatient.render(searchedPatients, getUserFromSession()));
    }


    public static User getUserFromSession(){
        if(User.getUserById(session().get("numId")) instanceof Consultant){
            return Consultant.getConsultantById(session().get("numId"));
        }
        else{
            return User.getUserById(session().get("numId"));
        }
    }

    public static Patient getPatientFromSession(){
        return Patient.getPatientById(session().get("mrn"));
    }

    public static void endPatientSession(){
        if(session().containsKey("mrn")){
            session().remove("mrn");
        }
    }
}
