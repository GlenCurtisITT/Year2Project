package controllers;

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
        return ok(adminHomePage.render(u));
    }

    public Result deletePatient(String mrn){
        Patient p = Patient.getPatientById(mrn); //serialize before delete
        if(p.getAppointments().size() != 0){
            flash("error", "Cannot archive Patient while there are still appointments due");
            return redirect(routes.HomeController.searchPatient());
        }
        if(p.getChart() != null){
            flash("error", "Cannot archive Patient while they are staying in the hospital");
            return redirect(routes.HomeController.searchPatient());            
        }
        try {
            p.serialize();
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

}
