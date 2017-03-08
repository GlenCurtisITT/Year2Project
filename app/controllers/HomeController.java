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
        return ok(index.render(loginForm));
    }

    public Result homepage(){
        User u = getUserFromSession();
        return ok(homepage.render(u));
    }

    public Result addPatient(){
        Form<Patient> addPatientForm = formFactory.form(Patient.class);
        User u = getUserFromSession();
        return ok(addPatient.render(addPatientForm, null, u));
    }

    public Result createUser(){
        Form<User> adduserForm = formFactory.form(User.class);
        return ok(createUser.render(adduserForm, null));
    }

    public Result searchPatient(){
        List<Patient> patientList = Patient.findAll();
        return ok(searchPatient.render(patientList, getUserFromSession()));
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
        User.create(newUserForm.get("email"), newUserForm.get("role"), newUserForm.get("fname"),newUserForm.get("lname"),
                newUserForm.get("address"),newUserForm.get("phoneNumber"), newUserForm.get("ppsNumber"), date, newUserForm.get("password"));
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
            return badRequest(addPatient.render(errorForm, "Error with date", getUserFromSession()));
        }
        //Checking that Email and Name are not blank.
        if(newPatientForm.get("email").equals("") || newPatientForm.get("fname").equals("") || newPatientForm.get("lname").equals("")){
            return badRequest(addPatient.render(errorForm, "Error with date", getUserFromSession()));
        }
        if(newPatientForm.get("medicalCard").equals("select")){
            return badRequest(addPatient.render(errorForm, "Error with date", getUserFromSession()));
        }

        //Checking if ppsNumber exists already in database (additional functionality)
        List<Patient> allpatients = Patient.findAll();
        for(Patient a : allpatients) {
            if (a.getPpsNumber().equals(newPatientForm.get("ppsNumber"))) {
                return badRequest(addPatient.render(errorForm, "Error with date", getUserFromSession()));
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
        if(u.getRole().equals("Admin")){
            return redirect(routes.AdminController.adminHomePage());
        }else if(u.getRole().equals("Consultant")){
            return redirect(routes.ConsultantController.consultantHomePage());
        }else{
            return badRequest();
        }

    }

    public static User getUserFromSession(){
        return User.getUserById(session().get("email"));
    }
}
