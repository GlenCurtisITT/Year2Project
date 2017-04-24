package controllers;

import play.mvc.*;

import services.InvalidPPSNumberException;
import views.html.chiefAdminPages.*;
import play.data.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.inject.Inject;
import models.users.*;
import models.*;
import org.mindrot.jbcrypt.BCrypt;

@Security.Authenticated(Secured.class)
@With(AuthChiefAdmin.class)
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

    public Result viewEquipment(){
        User u = HomeController.getUserFromSession();
        List<Equipment> allEquipment = Equipment.findAll();
        Collections.sort(allEquipment, new EquipmentComparator());

        return ok(viewEquipment.render(u, allEquipment));
    }

    public Result createEquipment(){
        User u = HomeController.getUserFromSession();
        Equipment e = new Equipment("", true);

        return ok(createEquipment.render(u, e));
    }

    public Result createEquipmentSubmit(){
        User u = HomeController.getUserFromSession();
        DynamicForm df = formFactory.form().bindFromRequest();
        String name = df.get("name");
        Equipment e = new Equipment(name, true);
        if(name.equals("")){
            flash("error", "Name cannot be blank.");
            return badRequest(createEquipment.render(u, e));
        }
        if(name.matches(".*\\d+.*")){
            flash("error", "Name cannot contain numbers.");
            return badRequest(updateEquipment.render(u, e));
        }

        flash("success", "Equipment Created.");
        e.save();
        return redirect(routes.ChiefAdminController.viewEquipment());
    }

    public Result updateEquipment(String id){
        User u = HomeController.getUserFromSession();
        Equipment e = Equipment.find.byId(id);

        return ok(updateEquipment.render(u, e));
    }

    public Result updateEquipmentSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        User u = HomeController.getUserFromSession();
        Equipment e = Equipment.find.byId(df.get("id"));
        String name = df.get("name");
        String operational = df.get("operational");
        Boolean isOperational;
        if(operational.equals("true")){
            isOperational = true;
        }else{
            isOperational = false;
        }
        if(name.equals("")){
            flash("error", "Name cannot be blank");
            return badRequest(updateEquipment.render(u, e));
        }
        if(name.matches(".*\\d+.*")){
            flash("error", "Name cannot contain numbers.");
            return badRequest(updateEquipment.render(u, e));
        }
        e.setType(name);
        e.setFunctional(isOperational);
        e.update();
        flash("success", "Equipment Updated");
        return redirect(routes.ChiefAdminController.viewEquipment());
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
        if(name.matches(".*\\d+.*")){
            flash("error", "Name cannot contain numbers.");
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
        if(name.matches(".*\\d+.*")){
            flash("error", "Name cannot contain numbers.");
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

        if(name.matches(".*\\d+.*")){
            flash("error", "Name cannot contain numbers.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }

        if(ingredients.matches(".*\\d+.*")){
            flash("error", "Ingredients cannot contain numbers.");
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

        if(name.matches(".*\\d+.*")){
            flash("error", "Name cannot contain numbers.");
            return badRequest(createMedication.render(HomeController.getUserFromSession(), m));
        }

        if(ingredients.matches(".*\\d+.*")){
            flash("error", "Ingredients cannot contain numbers.");
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
        User u = User.find.byId(idNum);

        if(phoneNum.equals("") || fName.equals("") || lName.equals("") || address.equals("") || email.equals("")){
            flash("error", "Please fill all fields in the form");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try{
            date = format.parse(dob);
        } catch (ParseException e) {
            flash("error", "Invalid Date Entered. Please try again.");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        if(date.after(new Date())){
            flash("error", "Invalid Date of Birth entered");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        //Checking if email already exist in the system.
        List<User> allUsers = User.findAll();
        allUsers.remove(u);
        for(User user : allUsers){
            if(user.getEmail().equals(email)){
                flash("error", "Email already exists in system.");
                return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
            }
        }

        try{
            Integer.parseInt(df.get("phoneNumber"));
        }catch(NumberFormatException e){
            flash("error", "Phone number must contain numbers only");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        if(!email.contains("@")){
            flash("error", "Invalid email entered");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        if(fName.matches(".*\\d+.*") || lName.matches(".*\\d+.*")){
            flash("error", "Name must not contain numbers");
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
        }

        //Setting to null to check for duplicates in Database.
        u.setPpsNumber("");
        u.update();
        try {
            HomeController.ppsChecker(pps);
        } catch (InvalidPPSNumberException e) {
            //Resetting PPS back to initial value if there is an error.
            u.setPpsNumber(pps);
            u.update();
            flash("error", e.getMessage());
            return badRequest(updateUser.render(HomeController.getUserFromSession(), u));
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
        User user = new User("", "", "", "", "", new Date(), "", "");
        user.setEmail(newUserForm.get("email"));
        user.setAddress(newUserForm.get("address"));
        user.setFname(newUserForm.get("fname"));
        user.setLname(newUserForm.get("lname"));
        user.setPhoneNumber(newUserForm.get("phoneNumber"));
        user.setPpsNumber(newUserForm.get("ppsNumber"));

        //Checking if Form has errors.
        if(newUserForm.hasErrors()){
            return badRequest(createUser.render(user, "Error in form."));
        }
        //Checking that Email and Name are not blank.
        if(newUserForm.get("email").equals("") || newUserForm.get("fname").equals("") || newUserForm.get("lname").equals("") || newUserForm.get("phoneNumber").equals("") || newUserForm.get("address").equals("")){
            return badRequest(createUser.render(user, "Please fill all forms in the field"));
        }

        if(newUserForm.get("fname").matches(".*\\d+.*") || newUserForm.get("lname").matches(".*\\d+.*")){
            return badRequest(createUser.render(user, "Name must not contain numbers"));
        }

        if(!newUserForm.get("email").contains("@")){
            return badRequest(createUser.render(user, "Invalid email entered"));
        }

        if(newUserForm.get("role").equals("select")){
            return badRequest(createUser.render(user, "Please enter a role."));
        }

        int test = 0;
        try{
            test = Integer.parseInt(newUserForm.get("phoneNumber"));
        } catch(NumberFormatException e){
            return badRequest(createUser.render(user, "PhoneNumber must contain numbers only"));
        }

        try {
            HomeController.ppsChecker(newUserForm.get("ppsNumber"));
        } catch (InvalidPPSNumberException e) {
            return badRequest(createUser.render(user, e.getMessage()));
        }

        //Checking if password == confirmed password
        if(!newUserForm.get("password").equals(newUserForm.get("passwordConfirm"))){
            return badRequest(createUser.render(user, "Passwords do not match."));
        }
        //Checking if password is longer than 6 characters
        if(newUserForm.get("password").length() < 6){
            return badRequest(createUser.render(user, "Password must be at least six characters."));
        }
        //Checking if email exists already in database (Duplicate primary key)
        List<User> allusers = User.findAll();
        for(User a : allusers) {
            if (a.getEmail().equals(newUserForm.get("email"))) {
                return badRequest(createUser.render(user, "Email already exists in system."));
            }
        }

        String dateString = newUserForm.get("dateOfBirth");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try{
            date = format.parse(dateString);
            user.setDateOfBirth(date);
        } catch (ParseException e) {
            return badRequest(createUser.render(user, dateString));
        }
        if(date.after(new Date())){
            return badRequest(createUser.render(user, "Invalid Date of Birth entered"));
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
            return badRequest(createUser.render(user, "Invalid role chosen."));
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
        User user = new User("", "", "", "", "", new Date(), "", "");
        return ok(createUser.render(user, null));
    }
}
