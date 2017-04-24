package controllers;

import play.mvc.*;

import services.InvalidPPSNumberException;
import views.html.loginPage.*;
import views.html.mainTemplate.*;
import play.data.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
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
            Equipment a = new Equipment("Consultation Room", true);
            Equipment b = new Equipment("X-Ray", true);
            Equipment c = new Equipment("CT Scanner", true);

            a.save();
            b.save();
            c.save();
        }

        if(Ward.findAll().size() == 0) {
            Ward a = new Ward("Maternity Ward", 20);
            Ward b = new Ward("Intensive Care Unit", 15);
            Ward c = new Ward("Psychiatric Ward", 20);
            Ward d = new Ward("Burns Unit", 5);
            Ward e = new Ward("Private Ward", 1);

            StandbyList f = new StandbyList(a);
            StandbyList g = new StandbyList(b);
            StandbyList h = new StandbyList(c);
            StandbyList i = new StandbyList(d);
            StandbyList j = new StandbyList(e);

            a.setSl(f);
            b.setSl(g);
            c.setSl(h);
            d.setSl(i);
            e.setSl(j);


            a.save();
            b.save();
            c.save();
            d.save();
            e.save();
            f.save();
            g.save();
            h.save();
            i.save();
            j.save();

        }

        if(Medicine.findAll().size() == 0) {
            Medicine a = new Medicine("Zotepine","Anxiety, Flushing, dry skin, Arthralgia, Myalgia, Acne, Conjunctivitis, Thrombocythaemia", "metabolite, norzotepine", .4, "mg");
            Medicine b = new Medicine("Corzide", "Dizziness, lightheadedness, slow heartbeat, tiredness, nausea, vomiting", "nadolol and bendroflumethiazide", .3, "mg");
            Medicine c = new Medicine("Periactin", "Drowsiness, dizziness, blurred vision, constipation", "Cyproheptadine", .2, "mg");

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

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result addPatient(){
        endPatientSession();
        Form<Patient> addPatientForm = formFactory.form(Patient.class);
        User u = getUserFromSession();
        return ok(addPatient.render(addPatientForm, null, u));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result viewPatient(){
        Patient p = getPatientFromSession();
        return ok(viewPatient.render(getUserFromSession(), p));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result viewPatientByID(String mrn){
        endPatientSession();
        Patient p = Patient.find.byId(mrn);
        if(!session().containsKey("mrn")) {
            session("mrn", mrn);
        }
        if(p.getCharts().size() == 0){
            Chart c = new Chart(p);
            c.save();
            p.setChart(c);
            p.update();

        }
        return ok(viewPatient.render(getUserFromSession(), getPatientFromSession()));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result appointmentMain(String id){
        Appointment a = Appointment.find.byId(id);
        User u = getUserFromSession();
        return ok(appointmentMain.render(u, a));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result cancelAppointment(String id){
        Appointment a = Appointment.find.byId(id);
        if(getUserFromSession() instanceof Consultant){
            Consultant c = (Consultant) getUserFromSession();
            Patient p = Patient.find.byId(a.getP().getMrn());
            String logFileString = getUserFromSession().checkRole() + " "
                    + getUserFromSession().getFname() + " "
                    + getUserFromSession().getLname() + " cancelled an appointment with "
                    + p.getfName() + " "
                    + p.getlName() + " the appointment that was cancelled was on the "
                    + a.getFormattedAppDate(a.getAppDate()) + " at "
                    + a.getFormattedAppTime(a.getAppDate());
            LogFile.writeToLog(logFileString);
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

    public Result addChiefAdmin(){
        return ok(addChiefAdmin.render());
    }

    public Result addChiefAdminSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        String email = df.get("email");
        String password = df.get("password");
        String confPassword = df.get("confPassword");

        if(email.equals("")){
            flash("error", "Email must be entered.");
            return badRequest(addChiefAdmin.render());
        }

        if(!email.contains("@")){
            flash("error", "Invalid email entered.");
            return badRequest(addChiefAdmin.render());
        }

        if(password.equals("") || confPassword.equals("")){
            flash("error", "Password or Confirm Password was blank.");
            return badRequest(addChiefAdmin.render());
        }

        if(password.length() < 6){
            flash("error", "Passwords must be at least 6 characters");
            return badRequest(addChiefAdmin.render());
        }

        if(!password.equals(confPassword)){
            flash("error", "Passwords did not match.");
            return badRequest(addChiefAdmin.render());
        }

        //Checking for duplicate emails.
        List<User> users = User.findAll();
        for(User u : users){
            if(u.getEmail().equals(email)){
                flash("error", "User already exists with that email address.");
                return badRequest(addChiefAdmin.render());
            }
        }

        ChiefAdmin ca = new ChiefAdmin("Chief", "Admin", null, null, null, null, email, password);
        ca.create(ca);
        flash("success", "Chief admin created. Please log in");
        return redirect(routes.HomeController.index());
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result reportBrokenEquipment(String id){
        Patient p = getPatientFromSession();
        Appointment a = Appointment.find.byId(id);
        a.getE().setFunctional(false);
        String s = "Equipment recorded as broken";
        flash("success", s);
        endPatientSession();
        String logFileString = getUserFromSession().checkRole() + " "
                + getUserFromSession().getFname() + " "
                + getUserFromSession().getLname() + " recorded "
                + a.getE().getType() + " as broken.";
        LogFile.writeToLog(logFileString);
        return ok(appointmentMain.render(getUserFromSession(), a));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
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
        if(newAppointmentForm.get("appDate").equals("")){
            flash("error", "Date was not chosen. Appointment has not been rescheduled");
            return badRequest(appointmentMain.render(getUserFromSession(), a));
        }
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
        String logFileString = getUserFromSession().checkRole() + " "
                + getUserFromSession().getFname() + " "
                + getUserFromSession().getLname() + " rescheduled appointment for "
                + p.getfName() + " "
                + p.getlName() + " for the "
                + a.getFormattedAppDate(a.getAppDate()) + " at "
                + a.getFormattedAppTime(a.getAppDate());
        LogFile.writeToLog(logFileString);
        return ok(appointmentMain.render(getUserFromSession(), a));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result makeAppointment(){
        Form<Appointment> addAppointmentForm = formFactory.form(Appointment.class);
        List<Consultant> consultants = Consultant.findAllConsultants();
        List<Equipment> equipments = Equipment.findAll().stream().filter(e -> e.getFunctional()).collect(Collectors.toList());

        return ok(makeAppointment.render(addAppointmentForm, consultants, getUserFromSession(), getPatientFromSession(), equipments, null));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result addAppointmentSubmit(){
        DynamicForm newAppointmentForm = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        List<Equipment> equipments = Equipment.findAll();
        List<Consultant> consultants = Consultant.findAllConsultants();
        Equipment e = Equipment.find.byId(newAppointmentForm.get("equipment"));
        Patient p = getPatientFromSession();
        if(Equipment.find.byId(newAppointmentForm.get("equipment")) == null) {
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Please select an equipment type"));
        }
        if(Consultant.find.byId(newAppointmentForm.get("consultant")) == null){
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Please enter a consultant."));
        }
        Consultant c = Consultant.getConsultantById(newAppointmentForm.get("consultant"));
        //Checking if Form has errors.
        if(newAppointmentForm.hasErrors()){
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Error in form."));
        }
        //handles processing of date
        if(newAppointmentForm.get("appDate").equals("")){
            return badRequest(makeAppointment.render(errorForm, consultants, getUserFromSession(), p, equipments, "Please enter an appointment date"));
        }
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
        //Writing to log file.
        String logFileString = getUserFromSession().checkRole() + " "
                + getUserFromSession().getFname() + " "
                + getUserFromSession().getLname() + " made an appointment for "
                + p.getfName() + " "
                + p.getlName() + " for the "
                + a.getFormattedAppDate(a.getAppDate()) + " at "
                + a.getFormattedAppTime(a.getAppDate());
        LogFile.writeToLog(logFileString);
        return redirect(routes.HomeController.viewPatient());
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result viewSchedule(){
        User u = getUserFromSession();
        List<Appointment> appointments = Appointment.findAll().stream().filter(a ->!a.isComplete()).collect(Collectors.toList());
        List<DateForCalendar> formattedDates = new ArrayList<>();
        if(session("role").equals("Admin")){
            formattedDates = Appointment.formatedDateList(appointments);
        }else if(session("role").equals("Consultant")){
            Consultant c = (Consultant) u;
            formattedDates = Appointment.formatedDateList(c.getAppointments());
        }
        return ok(viewSchedule.render(u, appointments, formattedDates));
    }

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result addPatientSubmit(){
        DynamicForm df = formFactory.form().bindFromRequest();
        Form errorForm = formFactory.form().bindFromRequest();
        //Checking if Form has errors.
        if(df.hasErrors()){
            return badRequest(addPatient.render(errorForm, "Error in form", getUserFromSession()));
        }

        String[] strings = {df.get("fname"), df.get("lname"), df.get("nokFName"), df.get("nokLName"), df.get("prevIllness"), df.get("nokAddress"), df.get("address"), df.get("email")};
        String[] nums = {df.get("homePhone"), df.get("mobilePhone"), df.get("nokNumber")};

        for(int i = 0; i < strings.length; i++) {
            if (strings[i].equals("")) {
                return badRequest(addPatient.render(errorForm, "Email, Address, Illness, First Name and Last Name cannot be blank.", getUserFromSession()));
            }
        }
        for(int i = 0; i < strings.length - 3; i++){
            if(strings[i].matches(".*\\d+.*")){ //checks to see if contains number
                return badRequest(addPatient.render(errorForm, "Illness, First Name and Last Name cannot contain numbers.", getUserFromSession()));
            }
        }
        for(int i = 0; i < nums.length; i++){
            if(nums[i].equals("")){
                return badRequest(addPatient.render(errorForm, "Must enter Home Phone, Mobile Phone, and Contact number", getUserFromSession()));
            }
            if(nums[i].length() < 5){
                return badRequest(addPatient.render(errorForm, "Phone number entered is too short", getUserFromSession()));
            }
            try {
                Long.parseLong(nums[i]);
            }catch (NumberFormatException e){
                return badRequest(addPatient.render(errorForm, "Phone numbers cannot contain letters.", getUserFromSession()));
            }
        }
        if(!df.get("email").contains("@")){
            return badRequest(addPatient.render(errorForm, "Invalid email address entered", getUserFromSession()));
        }

        if(df.get("medicalCard").equals("select")){
            return badRequest(addPatient.render(errorForm, "Please select medical card status", getUserFromSession()));
        }

        //PPS Number validation
        try{
            ppsChecker(df.get("ppsNumber"));
        }catch(InvalidPPSNumberException e){
            return badRequest(addPatient.render(errorForm, e.getMessage(), getUserFromSession()));
        }

        //formatting date to work
        String dateString = df.get("dob");
        DateFormat format = new SimpleDateFormat("yyyy-dd-MM");
        Date date = new Date();
        try{
            date = format.parse(dateString);
        } catch (ParseException e) {
            return badRequest(addPatient.render(errorForm, "Error with date", getUserFromSession()));
        }
        if(date.after(new Date())){
            return badRequest(addPatient.render(errorForm, "Invalid date of birth entered", getUserFromSession()));
        }
        //converting medicalCard from form from string to boolean
        String medicalCard = df.get("medicalCard");
        boolean medCard;
        String genderInput = df.get("gender");
        boolean gender;
        if(medicalCard.equals("true")){
            medCard = true;
        } else {
            medCard = false;
        }

        if(genderInput.equals("true")){
            gender = true;
        } else {
            gender = false;
        }

        //Adding user to database
        Patient p = Patient.create(df.get("fname"), df.get("lname"), gender, df.get("ppsNumber"), date,
                df.get("address"),df.get("email"), df.get("homePhone"),
                df.get("mobilePhone"), df.get("nokFName"), df.get("nokLName")
                , df.get("nokAddress"), df.get("nokNumber"), medCard, df.get("prevIllness"));
        String s = "Patient: " + df.get("fname") + " " + df.get("lname") + " was added successfully.\nMRN: " + p.getMrn();
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

    @Security.Authenticated(Secured.class)
    @With(AuthAdminOrConsultant.class)
    public Result viewRecord(){
        Patient p = getPatientFromSession();
        PatientRecord pr = p.getPatientRecord();
        User u = getUserFromSession();
        return ok(viewPatientRecord.render(u, p, pr));
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

    public Result unauthorised(){
        return ok(unauthorised.render(null));
    }

    public static void ppsChecker(String pps) throws InvalidPPSNumberException{
        if(pps.equals("")){
            throw new InvalidPPSNumberException("Please enter a PPS Number");
        }
        if(pps.length() < 8 || pps.length() > 9){
            throw new InvalidPPSNumberException("PPS number must be 8 or 9 digits long");
        }
        String testPPS = String.valueOf(pps.charAt(pps.length() - 1));
        if(testPPS.matches(".*\\d+.*")){   //make sure last digit is letter
            throw new InvalidPPSNumberException("Last digit in PPS Number must be a letter");
        }
        testPPS = pps.substring(0, 6);
        try{
            Long.parseLong(testPPS);
        }catch(NumberFormatException e){
            throw new InvalidPPSNumberException("Invalid PPS number entered");
        }
        for(User u : User.findAll()){
            if(!(u instanceof ChiefAdmin)){
                if (u.getPpsNumber().equals(pps)) {
                    throw new InvalidPPSNumberException("There is a User already on the system with that PPS Number");
                }
            }
        }
        for(Patient p : Patient.findAll()){
            if(p.getPpsNumber().equals(pps)){
                throw new InvalidPPSNumberException(("There is a Patient already on the system with that PPS Number"));
            }
        }

    }


}
