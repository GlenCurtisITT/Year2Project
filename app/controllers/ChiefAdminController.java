package controllers;

import controllers.*;
import play.mvc.*;

import views.html.loginPage.*;
import views.html.mainTemplate.*;
import views.html.chiefAdminPages.*;
import play.data.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.users.*;
import models.*;
import org.mindrot.jbcrypt.BCrypt;

public class ChiefAdminController extends Controller{

    private FormFactory formFactory;

    @Inject
    public ChiefAdminController(FormFactory f){
        this.formFactory = f;
    }

    public Result chiefAdminHomePage(){
        User u = HomeController.getUserFromSession();
        return ok(chiefAdminHomePage.render(u));
    }

    public Result viewUsers(){
        User u = HomeController.getUserFromSession();
        List<User> allUsers = User.findAll();

        return ok(viewUsers.render(u, allUsers));
    }

    public Result viewWards(){
        User u = HomeController.getUserFromSession();
        List<Ward> allWards = Ward.findAll();
        Collections.sort(allWards, new WardComparator());

        return ok(viewWards.render(u, allWards));
    }

    public Result createWard(){
        User u = HomeController.getUserFromSession();
        Ward w = new Ward("", 0);
        return ok(createWard.render(u, w));
    }

    public Result createWardSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String name = df.get("name");
        int capacity = 0;
        Ward w = new Ward(name, capacity);
        try{
            capacity = Integer.parseInt(df.get("capacity"));
        }catch(NumberFormatException e){
            flash("error", "Invalid number entered.");
            return badRequest(createWard.render(HomeController.getUserFromSession(), w));
        }
        if(name.equals("")){
            flash("error", "Name must be entered.");
            return badRequest(createWard.render(HomeController.getUserFromSession(), w));
        }
        if(capacity <= 0){
            flash("error", "Max capacity must be greater than zero.");
            return badRequest(createWard.render(HomeController.getUserFromSession(), w));
        }

        w = new Ward(name, capacity);
        StandbyList sl = new StandbyList(w);
        w.save();
        sl.save();
        flash("success", String.format("%s (ward) created.", w.getName()));
        return redirect(routes.ChiefAdminController.viewWards());
    }

    public Result updateWardSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        Ward w = Ward.find.byId(df.get("id"));
        int capacity = 0;

        String name = df.get("name");
        try {
            capacity = Integer.parseInt(df.get("capacity"));
        }catch(NumberFormatException e){
            flash("error", "Invalid number entered.");
            return badRequest(updateWard.render(HomeController.getUserFromSession(), w));
        }
        if(name.equals("")){
            flash("error", "Name cannot be blank.");
            return badRequest(updateWard.render(HomeController.getUserFromSession(), w));
        }
        if(w.getCurrentOccupancy() > capacity){
            flash("error", "Max Capacity cannot be lower than current occupancy.");
            return badRequest(updateWard.render(HomeController.getUserFromSession(), w));
        }

        w.setMaxCapacity(capacity);
        w.setName(name);
        w.update();
        flash("success", "Ward Updated.");
        return redirect(routes.ChiefAdminController.viewWards());
    }

    public Result updateWard(String wardId){
        Ward w = Ward.find.byId(wardId);
        User u = HomeController.getUserFromSession();

        return ok(updateWard.render(u, w));
    }

    public Result viewMedication(){
        User u = HomeController.getUserFromSession();
        List<Medicine> allMedicine = Medicine.findAll();
        //Sorting medicines alphabetically by name.
        Collections.sort(allMedicine, new MedicineComparator());

        return ok(viewMedication.render(u, allMedicine));
    }

    public Result createMedicationSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String name = df.get("name");
        String sideEffect = df.get("sideEffects");
        String ingredients = df.get("ingredients");
        String unitOfMeasure = df.get("measurement");
        double price = 0;
        Medicine m = new Medicine(name, sideEffect, ingredients, price, unitOfMeasure);
        try {
            price = Double.parseDouble(df.get("price"));
        }catch(NumberFormatException e) {
            flash("error", "Invalid Price Entered.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }
        if(name.equals("") || sideEffect.equals("") || ingredients.equals("") || unitOfMeasure.equals("")){
            flash("error", "No fields can be blank.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }
        if(price <= 0){
            flash("error", "Price cannot be negative or zero.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }
        List<Medicine> allMedicine = Medicine.findAll();
        for(Medicine med : allMedicine){
            if(med.getName().equals(name)){
                flash("error", "Medicine already exists in system.");
                return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
            }
        }

        m = new Medicine(name, sideEffect, ingredients, price, unitOfMeasure);
        m.save();
        flash("success", "Medication has been added.");
        return redirect(routes.ChiefAdminController.viewMedication());
    }

    public Result updateMedication(String id){
        Medicine m = Medicine.find.byId(id);
        User u = HomeController.getUserFromSession();

        return ok(updateMedication.render(u, m));
    }

    public Result updateMedicationSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String name = df.get("name");
        String sideEffect = df.get("sideEffects");
        String ingredients = df.get("ingredients");
        String unitOfMeasure = df.get("measurement");
        String id = df.get("id");
        double price = 0;
        Medicine m = Medicine.find.byId(id);
        try {
            price = Double.parseDouble(df.get("price"));
        }catch(NumberFormatException e) {
            flash("error", "Invalid Price Entered.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }
        if(name.equals("") || sideEffect.equals("") || ingredients.equals("") || unitOfMeasure.equals("")){
            flash("error", "No fields can be blank.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }
        if(price <= 0){
            flash("error", "Price cannot be negative or zero.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }
        List<Medicine> allMedicine = Medicine.findAll();
        allMedicine.remove(m);
        for(Medicine med : allMedicine){
            if(med.getName().equals(name)){
                flash("error", "Medicine already exists in system.");
                return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
            }
        }

        m.setName(name);
        m.setSideAffects(sideEffect);
        m.setIngredients(ingredients);
        m.setUnitOfMeasurement(unitOfMeasure);
        m.setPricePerUnit(price);
        m.update();
        flash("success", "Medication has been updated.");
        return redirect(routes.ChiefAdminController.viewMedication());
    }

    public Result createMedication(){
        User u = HomeController.getUserFromSession();
        Medicine m = new Medicine("", "", "", 0, "");
        return ok(createMedication.render(u, m));
    }

    public Result deleteMedication(String id){
        Medicine m = Medicine.find.byId(id);
        m.delete();
        flash("success", "Medicine Deleted.");
        return redirect(routes.ChiefAdminController.viewMedication());
    }

    public Result updateUser(String idNum){
        User u = HomeController.getUserFromSession();
        User updatedUser = User.find.byId(idNum);

        return ok(updateUser.render(u, updatedUser));
    }

    public Result updateUserSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String idNum = df.get("idNum");
        String fName = df.get("fName");
        String lName = df.get("lName");
        String pps = df.get("ppsNum");
        String address = df.get("address");
        String phoneNum = df.get("phoneNumber");
        String dob = df.get("dob");
        String email = df.get("email");
        DateFormat format = new SimpleDateFormat("yyyy-dd-MM");
        Date date = new Date();
        try{
            date = format.parse(dob);
        } catch (ParseException e) {
            User u = User.find.byId(idNum);
            flash("error", "Invalid Date Entered. Please try again.");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        User u = User.find.byId(idNum);
        //Checking if email already exist in the system.
        List<User> allUsers = User.findAll();
        allUsers.remove(u);
        for(User user : allUsers){
            if(user.getEmail().equals(email)){
                flash("error", "Email already exists in system.");
                return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
            }
        }

        u.setEmail(email);
        u.setFname(fName);
        u.setLname(lName);
        u.setPpsNumber(pps);
        u.setAddress(address);
        u.setPhoneNumber(phoneNum);
        u.setDateOfBirth(date);
        u.update();

        flash("success", String.format("%s %s %s has been updated.", u.checkRole(), u.getFname(), u.getLname()));
        return redirect(routes.ChiefAdminController.viewUsers());
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
        return redirect(controllers.routes.ChiefAdminController.viewUsers());
    }

    public Result updatePasswordSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String id = df.get("idNum");
        String password = df.get("password");
        String confPassword = df.get("confPassword");
        User u = User.find.byId(id);

        if(!password.equals(confPassword)){
            flash("error", "Passwords do not match.");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        if(password.length() < 6){
            flash("error", "Password must be longer then 6 characters.");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        u.update();

        flash("success", String.format("%s %s %s's password has been changed.", u.checkRole(), u.getFname(), u.getLname()));
        return redirect(routes.ChiefAdminController.viewUsers());
    }

    public Result createUser(){
        Form<User> addUserForm = formFactory.form(User.class);
        return ok(createUser.render(addUserForm, null));
    }
}
